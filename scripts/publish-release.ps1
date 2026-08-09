param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    if (-not $SkipBuild) {
        & .\scripts\build-release.ps1
        if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    }
    $dirty = git status --porcelain
    if ($dirty) { throw 'Git working tree must be clean before publishing.' }
    $versionName = ((Get-Content .\gradle.properties) | Where-Object { $_ -like 'HABITAMA_VERSION_NAME=*' }).Split('=')[1]
    $tag = "v$versionName"
    $apk = ".\dist\Habitama-$versionName.apk"
    $manifest = '.\dist\version.json'
    gh release view $tag --repo toshiwd/Habitama *> $null
    if ($LASTEXITCODE -eq 0) { throw "Release already exists: $tag" }
    gh release create $tag $apk $manifest `
        --repo toshiwd/Habitama `
        --title "ハビタマ $versionName" `
        --notes "Phase 0: 目標1件、当日記録、部分達成・超過評価、累積エネルギー、7日履歴。"
    if ($LASTEXITCODE -ne 0) { throw 'GitHub Release publication failed.' }
    & .\scripts\verify-release.ps1
} finally {
    Pop-Location
}
