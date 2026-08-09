param(
    [string]$KeyStorePath = (Join-Path ([Environment]::GetFolderPath('UserProfile')) '.android\release-keys\habitama-release.jks')
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$propertiesPath = Join-Path $projectRoot 'keystore.properties'
$keytool = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr\bin\keytool.exe'

if (Test-Path -LiteralPath $KeyStorePath) {
    throw "Release key already exists: $KeyStorePath"
}
if (Test-Path -LiteralPath $propertiesPath) {
    throw "keystore.properties already exists: $propertiesPath"
}
if (-not (Test-Path -LiteralPath $keytool)) {
    throw "keytool not found: $keytool"
}

$keyDirectory = Split-Path -Parent $KeyStorePath
New-Item -ItemType Directory -Path $keyDirectory -Force | Out-Null
$randomBytes = New-Object byte[] 32
$randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $randomGenerator.GetBytes($randomBytes)
} finally {
    $randomGenerator.Dispose()
}
$password = [Convert]::ToBase64String($randomBytes).Replace('+', 'A').Replace('/', 'B').TrimEnd('=')

& $keytool -genkeypair `
    -keystore $KeyStorePath `
    -storepass $password `
    -keypass $password `
    -alias habitama `
    -keyalg RSA `
    -keysize 4096 `
    -validity 10000 `
    -dname 'CN=Habitama, OU=Habitama, O=Habitama, L=Tokyo, ST=Tokyo, C=JP'
if ($LASTEXITCODE -ne 0) { throw 'keytool failed' }

$escapedStore = $KeyStorePath.Replace('\', '\\')
$properties = @(
    "storeFile=$escapedStore"
    "storePassword=$password"
    'keyAlias=habitama'
    "keyPassword=$password"
) -join [Environment]::NewLine
[System.IO.File]::WriteAllText($propertiesPath, $properties + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))

Write-Host "Created Habitama release key: $KeyStorePath"
Write-Host "Created ignored signing config: $propertiesPath"
Write-Warning 'Back up both the JKS file and its password before publishing.'
