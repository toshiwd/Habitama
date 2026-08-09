param(
    [switch]$SkipChecks
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sdkRoot = Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'Android\Sdk'
$javaRoot = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'
$propertiesPath = Join-Path $projectRoot 'keystore.properties'

if (-not (Test-Path -LiteralPath $propertiesPath)) {
    throw 'keystore.properties is missing. Run scripts\init-release-key.ps1 first.'
}

$env:JAVA_HOME = $javaRoot
$env:ANDROID_HOME = $sdkRoot
Push-Location $projectRoot
try {
    if (-not $SkipChecks) {
        & .\gradlew.bat testDebugUnitTest lintDebug
        if ($LASTEXITCODE -ne 0) { throw 'Tests or lint failed.' }
    }
    & .\gradlew.bat assembleRelease
    if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }

    $versionName = ((Get-Content .\gradle.properties) | Where-Object { $_ -like 'HABITAMA_VERSION_NAME=*' }).Split('=')[1]
    $versionCode = [int](((Get-Content .\gradle.properties) | Where-Object { $_ -like 'HABITAMA_VERSION_CODE=*' }).Split('=')[1])
    $sourceApk = Join-Path $projectRoot 'app\build\outputs\apk\release\app-release.apk'
    $dist = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Path $dist -Force | Out-Null
    $apkName = "Habitama-$versionName.apk"
    $distApk = Join-Path $dist $apkName
    Copy-Item -LiteralPath $sourceApk -Destination $distApk -Force

    $apksigner = Get-ChildItem -LiteralPath (Join-Path $sdkRoot 'build-tools') -Directory |
        Sort-Object { [version]$_.Name } -Descending |
        ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
        Where-Object { Test-Path -LiteralPath $_ } |
        Select-Object -First 1
    if (-not $apksigner) { throw 'apksigner was not found.' }

    & $apksigner verify --verbose --print-certs $distApk | Tee-Object -FilePath (Join-Path $dist "Habitama-$versionName-cert.txt")
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }

    $sha256 = (Get-FileHash -LiteralPath $distApk -Algorithm SHA256).Hash.ToUpperInvariant()
    [System.IO.File]::WriteAllText(
        (Join-Path $dist "Habitama-$versionName.sha256"),
        "$sha256  $apkName`n",
        [System.Text.UTF8Encoding]::new($false)
    )
    $manifest = [ordered]@{
        version = $versionName
        versionCode = $versionCode
        apkUrl = "https://github.com/toshiwd/Habitama/releases/download/v$versionName/$apkName"
        sha256 = $sha256
        publishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        minSdk = 26
    } | ConvertTo-Json
    [System.IO.File]::WriteAllText((Join-Path $dist 'version.json'), $manifest + "`n", [System.Text.UTF8Encoding]::new($false))

    Write-Host "Release APK: $distApk"
    Write-Host "SHA-256: $sha256"
} finally {
    Pop-Location
}
