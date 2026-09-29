# =====================================================
# run-local.ps1 - Load .env then start Spring Boot
# Use: powershell -ExecutionPolicy Bypass -File run-local.ps1
# =====================================================

$ErrorActionPreference = 'Stop'

# Load .env
$envFile = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path $envFile)) {
    Write-Host ('[ERROR] .env not found at: ' + $envFile) -ForegroundColor Red
    exit 1
}

Write-Host '[INFO] Loading environment variables from .env...' -ForegroundColor Cyan
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }
    $eqIdx = $line.IndexOf('=')
    if ($eqIdx -lt 1) { return }
    $key = $line.Substring(0, $eqIdx).Trim()
    $val = $line.Substring($eqIdx + 1).Trim().Trim('"').Trim("'")
    [Environment]::SetEnvironmentVariable($key, $val, 'Process')
    Write-Host ('  [SET] ' + $key + ' = *** (length=' + $val.Length + ')') -ForegroundColor DarkGray
}

Write-Host ''
Write-Host '[INFO] Starting Spring Boot app...' -ForegroundColor Green
Write-Host ''

& .\gradlew.bat bootRun --no-daemon
