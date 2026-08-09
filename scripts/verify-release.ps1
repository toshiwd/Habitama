$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sdkRoot = Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'Android\Sdk'
$latestManifestUrl = 'https://github.com/toshiwd/Habitama/releases/latest/download/version.json'
$temporary = Join-Path ([System.IO.Path]::GetTempPath()) ("habitama-release-" + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $temporary | Out-Null
try {
    $manifestPath = Join-Path $temporary 'version.json'
    Invoke-WebRequest -Uri $latestManifestUrl -OutFile $manifestPath
    $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($manifest.versionCode -lt 1 -or $manifest.minSdk -ne 26) { throw 'Published manifest has invalid metadata.' }

    $downloadedApk = Join-Path $temporary ("Habitama-$($manifest.version).apk")
    Invoke-WebRequest -Uri $manifest.apkUrl -OutFile $downloadedApk
    $downloadedHash = (Get-FileHash -LiteralPath $downloadedApk -Algorithm SHA256).Hash.ToUpperInvariant()
    if ($downloadedHash -ne $manifest.sha256.ToUpperInvariant()) { throw 'Published APK SHA-256 does not match version.json.' }

    $localApk = Join-Path $projectRoot ("dist\Habitama-$($manifest.version).apk")
    if (-not (Test-Path -LiteralPath $localApk)) { throw "Local release APK is missing: $localApk" }
    $localHash = (Get-FileHash -LiteralPath $localApk -Algorithm SHA256).Hash.ToUpperInvariant()
    if ($localHash -ne $downloadedHash) { throw 'Published APK does not match the local release artifact.' }

    $apksigner = Get-ChildItem -LiteralPath (Join-Path $sdkRoot 'build-tools') -Directory |
        Sort-Object { [version]$_.Name } -Descending |
        ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
        Where-Object { Test-Path -LiteralPath $_ } |
        Select-Object -First 1
    $localCert = (& $apksigner verify --print-certs $localApk | Select-String 'certificate SHA-256 digest').Line
    $publicCert = (& $apksigner verify --print-certs $downloadedApk | Select-String 'certificate SHA-256 digest').Line
    if (-not $localCert -or $localCert -ne $publicCert) { throw 'Published APK signing certificate does not match the local artifact.' }

    Write-Host "Verified public Habitama $($manifest.version)"
    Write-Host "APK URL: $($manifest.apkUrl)"
    Write-Host "SHA-256: $downloadedHash"
    Write-Host $publicCert
} finally {
    Remove-Item -LiteralPath $temporary -Recurse -Force
}
