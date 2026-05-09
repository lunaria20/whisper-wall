param(
    [string]$DbUrl = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0",
    [string]$DbUsername = "postgres.mqssfwzsisxwpdiujiwl",
    [string]$SupabaseUrl = "https://mqssfwzsisxwpdiujiwl.supabase.co",
    [string]$SupabaseAnonKey,
    [string]$DbPassword,
    [string]$AppJwtSecret,
    [switch]$UseMavenWrapper
)

function ConvertTo-PlainText {
    param([Security.SecureString]$SecureValue)
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

function Import-DotEnv {
    param([string]$FilePath)

    if (-not (Test-Path $FilePath)) {
        return @{}
    }

    $result = @{}
    foreach ($line in Get-Content $FilePath) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line.TrimStart().StartsWith("#")) { continue }

        $idx = $line.IndexOf("=")
        if ($idx -lt 1) { continue }

        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $result[$key] = $value
    }

    return $result
}

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

$envFile = Join-Path $projectRoot ".env"
$envVars = Import-DotEnv -FilePath $envFile

if (-not $PSBoundParameters.ContainsKey("DbUrl") -and $envVars.ContainsKey("DB_URL")) { $DbUrl = $envVars["DB_URL"] }
if (-not $PSBoundParameters.ContainsKey("DbUsername") -and $envVars.ContainsKey("DB_USERNAME")) { $DbUsername = $envVars["DB_USERNAME"] }
if (-not $PSBoundParameters.ContainsKey("DbPassword") -and $envVars.ContainsKey("DB_PASSWORD")) { $DbPassword = $envVars["DB_PASSWORD"] }
if (-not $PSBoundParameters.ContainsKey("SupabaseUrl") -and $envVars.ContainsKey("SUPABASE_URL")) { $SupabaseUrl = $envVars["SUPABASE_URL"] }
if (-not $PSBoundParameters.ContainsKey("SupabaseAnonKey") -and $envVars.ContainsKey("SUPABASE_ANON_KEY")) { $SupabaseAnonKey = $envVars["SUPABASE_ANON_KEY"] }
if (-not $PSBoundParameters.ContainsKey("AppJwtSecret") -and $envVars.ContainsKey("APP_JWT_SECRET")) { $AppJwtSecret = $envVars["APP_JWT_SECRET"] }

if (-not $AppJwtSecret) {
    $jwtSecure = Read-Host "Enter APP_JWT_SECRET" -AsSecureString
    $AppJwtSecret = ConvertTo-PlainText -SecureValue $jwtSecure
}

if (-not $DbPassword) {
    $dbPasswordSecure = Read-Host "Enter Supabase DB password" -AsSecureString
    $DbPassword = ConvertTo-PlainText -SecureValue $dbPasswordSecure
}

if (-not $SupabaseAnonKey) {
    $anonKeySecure = Read-Host "Enter SUPABASE_ANON_KEY" -AsSecureString
    $SupabaseAnonKey = ConvertTo-PlainText -SecureValue $anonKeySecure
}

$env:DB_URL = $DbUrl
$env:DB_USERNAME = $DbUsername
$env:DB_PASSWORD = $DbPassword
$env:SUPABASE_URL = $SupabaseUrl
$env:SUPABASE_ANON_KEY = $SupabaseAnonKey
$env:APP_JWT_SECRET = $AppJwtSecret
if ($envVars.ContainsKey("DB_DDL_AUTO")) {
    $env:DB_DDL_AUTO = $envVars["DB_DDL_AUTO"]
}

Write-Host "Environment variables set for Supabase connection." -ForegroundColor Green

if ($UseMavenWrapper) {
    & .\mvnw.cmd org.springframework.boot:spring-boot-maven-plugin:2.7.10:run
}
else {
    $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $mvnCmd) {
        Write-Error "Maven is not available in PATH. Install Maven or run with -UseMavenWrapper."
        exit 1
    }
    & mvn org.springframework.boot:spring-boot-maven-plugin:2.7.10:run
}
