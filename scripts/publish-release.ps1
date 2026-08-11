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
    $existingTags = @(gh release list --repo toshiwd/Habitama --limit 100 --json tagName --jq '.[].tagName')
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect existing GitHub Releases.' }
    if ($existingTags -contains $tag) { throw "Release already exists: $tag" }
    gh release create $tag $apk $manifest `
        --repo toshiwd/Habitama `
        --title "Habitama $versionName" `
        --notes "Calendar navigation now supports past and future months, up to 10 action goals can be registered, and the Habitama egg and launcher icon use a new original spotted design."
    if ($LASTEXITCODE -ne 0) { throw 'GitHub Release publication failed.' }
    & .\scripts\verify-release.ps1
} finally {
    Pop-Location
}
