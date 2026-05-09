$workspaceRoot = Split-Path -Parent $PSScriptRoot
$backendPath = Join-Path $workspaceRoot "back-end\potane"
$frontendPath = Join-Path $workspaceRoot "front-end"

if (-not (Test-Path $backendPath)) {
    Write-Error "Backend path not found: $backendPath"
    exit 1
}

if (-not (Test-Path $frontendPath)) {
    Write-Error "Frontend path not found: $frontendPath"
    exit 1
}

Write-Host "Starting backend in a new PowerShell window..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$backendPath'; .\\scripts\\connect-supabase.ps1"

Write-Host "Starting frontend in a new PowerShell window..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$frontendPath'; npm start"

Write-Host "Dev environment launch triggered." -ForegroundColor Green
Write-Host "Backend window will prompt for missing secrets if .env is incomplete." -ForegroundColor Yellow
