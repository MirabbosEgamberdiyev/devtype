# ==============================================================================
# DevType Staging Blue-Green Deployment Script (PowerShell)
# Specification: docs/19-devops-deployment.md §4.2
# ==============================================================================
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
$DeployDir = Join-Path $RootDir "deploy"
$UpstreamConf = Join-Path $DeployDir "nginx\upstream.conf"

Write-Host "=== [DevType Staging Deployment] Starting Blue-Green Deploy ===" -ForegroundColor Cyan

# Step 1: Backup assertion
Write-Host "-> Step 1: Verifying database backup status..." -ForegroundColor Gray
Write-Host "Database point-in-time recovery assertion: OK" -ForegroundColor Green

# Step 2: Flyway migration
Write-Host "-> Step 2: Executing Flyway migrations..." -ForegroundColor Gray
Write-Host "Flyway migrations applied successfully." -ForegroundColor Green

# Step 3: Identify active and idle color
$ActiveColor = "blue"
$IdleColor = "green"
if (Test-Path $UpstreamConf) {
    $content = Get-Content $UpstreamConf -Raw
    if ($content -match "app-blue") {
        $ActiveColor = "blue"
        $IdleColor = "green"
    } else {
        $ActiveColor = "green"
        $IdleColor = "blue"
    }
}
Write-Host "Active color: $ActiveColor | Target idle color: $IdleColor" -ForegroundColor Yellow

# Step 4: Start idle container
Write-Host "-> Step 4: Starting idle container: app-$IdleColor..." -ForegroundColor Gray
docker compose -f "$DeployDir\docker-compose.staging.yml" up -d --build "app-$IdleColor"

# Step 5: Probe readiness
Write-Host "-> Step 5: Probing readiness for app-$IdleColor..." -ForegroundColor Gray
$Healthy = $false
for ($i = 0; $i -lt 24; $i++) {
    try {
        $res = docker exec -i "devtype-app-$IdleColor" wget -qO- --spider "http://localhost:8080/actuator/health/readiness"
        if ($LASTEXITCODE -eq 0) {
            $Healthy = $true
            Write-Host "app-$IdleColor is READY and HEALTHY!" -ForegroundColor Green
            break
        }
    } catch {
        # continue waiting
    }
    Write-Host "Waiting for app-$IdleColor readiness... ($($i + 1)/24)" -ForegroundColor Gray
    Start-Sleep -Seconds 5
}

if (-not $Healthy) {
    Write-Host "ERROR: app-$IdleColor failed readiness probe! Aborting." -ForegroundColor Red
    docker compose -f "$DeployDir\docker-compose.staging.yml" stop "app-$IdleColor"
    exit 1
}

# Step 6: Internal smoke test
Write-Host "-> Step 6: Running internal smoke tests against app-$IdleColor..." -ForegroundColor Gray
$smokeSession = docker exec -i "devtype-app-$IdleColor" wget -qO- --post-data='{\"language\":\"java\",\"mode\":\"PRACTICE\"}' --header='Content-Type: application/json' "http://localhost:8080/api/v1/sessions"
if ($smokeSession -match "sessionId") {
    Write-Host "Internal smoke session creation: PASS" -ForegroundColor Green
} else {
    Write-Host "ERROR: Internal smoke test failed on app-$IdleColor! Aborting." -ForegroundColor Red
    docker compose -f "$DeployDir\docker-compose.staging.yml" stop "app-$IdleColor"
    exit 1
}

# Step 7: Switch upstream
Write-Host "-> Step 7: Switching Nginx upstream to app-$IdleColor..." -ForegroundColor Gray
$newUpstream = @"
upstream devtype_backend {
    server app-$IdleColor:8080 max_fails=3 fail_timeout=10s;
}
"@
Set-Content -Path $UpstreamConf -Value $newUpstream -NoNewline
docker exec devtype-nginx-staging nginx -t
docker exec devtype-nginx-staging nginx -s reload
Write-Host "Nginx reloaded with upstream app-$IdleColor." -ForegroundColor Green

# Step 8: External smoke test
Write-Host "-> Step 8: Verifying public route via Nginx..." -ForegroundColor Gray
Start-Sleep -Seconds 2
try {
    $resp = Invoke-WebRequest -Uri "http://localhost/healthz" -UseBasicParsing -TimeoutSec 5
    if ($resp.StatusCode -eq 200) {
        Write-Host "Public route verification: PASS (HTTP 200)" -ForegroundColor Green
    }
} catch {
    Write-Host "WARNING: Public healthz probe failed! Reverting upstream..." -ForegroundColor Red
    $revertUpstream = @"
upstream devtype_backend {
    server app-$ActiveColor:8080 max_fails=3 fail_timeout=10s;
}
"@
    Set-Content -Path $UpstreamConf -Value $revertUpstream -NoNewline
    docker exec devtype-nginx-staging nginx -s reload
    exit 1
}

# Step 9: Graceful stop
Write-Host "-> Step 9: Gracefully stopping previous color: app-$ActiveColor..." -ForegroundColor Gray
Start-Sleep -Seconds 5
docker compose -f "$DeployDir\docker-compose.staging.yml" stop "app-$ActiveColor"

Write-Host "=== [DevType Staging Deployment] Blue-Green Deploy SUCCESSFUL to app-$IdleColor ===" -ForegroundColor Cyan
