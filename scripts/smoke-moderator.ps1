Set-Location "$PSScriptRoot\.."

$base = 'http://localhost:8080/api'
$modUser = 'moderator1004@whisperwall.com'
$modPass = 'Kpota1004**'
$adminUser = 'admin2004@whisperwall.com'
$adminPass = 'Kpota2004**'

function Log([string]$message) {
    Write-Output $message
}

function Get-Token([string]$user, [string]$pass) {
    try {
        $body = @{ username = $user; email = $user; password = $pass } | ConvertTo-Json
        $resp = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType 'application/json' -Body $body
        if ($resp.token) { return $resp.token }
        if ($resp.data -and $resp.data.token) { return $resp.data.token }
        return $null
    }
    catch {
        Log ("LOGIN_FAIL:${user}:" + $_.Exception.Message)
        return $null
    }
}

function Safe-Get([string]$uri, [hashtable]$headers, [string]$tag) {
    try {
        $resp = Invoke-RestMethod -Method Get -Uri $uri -Headers $headers
        Log ("${tag}:OK")
        return $resp
    }
    catch {
        Log ("${tag}:FAIL:" + $_.Exception.Message)
        return $null
    }
}

function Safe-Post([string]$uri, [hashtable]$headers, [string]$tag, [string]$body = '') {
    try {
        if ([string]::IsNullOrWhiteSpace($body)) {
            $resp = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers
        }
        else {
            $resp = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -ContentType 'application/json' -Body $body
        }
        Log ("${tag}:OK")
        return $resp
    }
    catch {
        Log ("${tag}:FAIL:" + $_.Exception.Message)
        return $null
    }
}

function Get-PageItems($pageResponse) {
    if (-not $pageResponse) { return @() }
    if ($null -eq $pageResponse.content) { return @() }
    return @($pageResponse.content)
}

$modToken = Get-Token $modUser $modPass
$adminToken = Get-Token $adminUser $adminPass

if ($modToken) { Log 'MOD_LOGIN_OK' }
if ($adminToken) { Log 'ADMIN_LOGIN_OK' }
if (-not $modToken) {
    Log 'ABORT_NO_MOD_TOKEN'
    exit 0
}

$modHeaders = @{ Authorization = "Bearer $modToken" }
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

$pending = Safe-Get "$base/admin/reports?status=PENDING&page=0&size=20" $modHeaders 'GET_PENDING'
if (-not $pending) { exit 0 }

$reports = Get-PageItems $pending
Log ("PENDING_INITIAL=" + $reports.Count)

if ($reports.Count -lt 2) {
    $public = Safe-Get "$base/confessions/public?page=0&size=20&sortBy=createdAt" $modHeaders 'GET_PUBLIC_CONFESSIONS'
    if ($public) {
        $confessions = Get-PageItems $public
        Log ("PUBLIC_CONFESSIONS=" + $confessions.Count)

        $needed = [Math]::Max(0, 2 - $reports.Count)
        for ($i = 0; $i -lt $needed -and $i -lt $confessions.Count; $i++) {
            $confessionId = $confessions[$i].id
            $reportBody = @{ reason = 'smoke-test-report'; description = "seed report $i" } | ConvertTo-Json
            $created = Safe-Post "$base/reports/confession/$confessionId" $modHeaders "CREATE_REPORT_CONFESSION_$confessionId" $reportBody
            if ($created) {
                Log ("REPORT_CREATED_ID=" + $created.id)
            }
        }
    }
}

$pendingAfterSeed = Safe-Get "$base/admin/reports?status=PENDING&page=0&size=20" $modHeaders 'GET_PENDING_AFTER_SEED'
if (-not $pendingAfterSeed) { exit 0 }

$reportsAfterSeed = Get-PageItems $pendingAfterSeed
Log ("PENDING_AFTER_SEED=" + $reportsAfterSeed.Count)

if ($reportsAfterSeed.Count -eq 0) {
    Log 'NO_PENDING_REPORTS_AFTER_SEED'
    exit 0
}

$first = $reportsAfterSeed[0]
$restrictionBody = @{
    confessionId = [int64]$first.confessionId
    reason = 'Smoke test restriction request'
    requestedDurationDays = 1
} | ConvertTo-Json

$restrictionResp = Safe-Post "$base/moderator/restriction-requests" $modHeaders 'POST_RESTRICTION' $restrictionBody
if ($restrictionResp) {
    Log ("RESTRICT_OK_REPORT=" + $first.id)
}

$dismissResp = Safe-Post "$base/admin/reports/$($first.id)/dismiss" $modHeaders 'POST_DISMISS'
if ($dismissResp -ne $null) {
    Log ("DISMISS_DONE_REPORT=" + $first.id)
}

$second = $reportsAfterSeed | Where-Object { $_.id -ne $first.id } | Select-Object -First 1
if ($second) {
    $removeResp = Safe-Post "$base/admin/reports/$($second.id)/remove-confession" $modHeaders 'POST_REMOVE_CONFESSION'
    if ($removeResp -ne $null) {
        Log ("REMOVE_DONE_REPORT=" + $second.id)
    }
}
else {
    Log 'REMOVE_SKIPPED_NO_SECOND'
}

$dismissed = Safe-Get "$base/admin/reports?status=DISMISSED&page=0&size=20" $modHeaders 'GET_DISMISSED'
if ($dismissed) {
    Log ("DISMISSED_COUNT=" + (Get-PageItems $dismissed).Count)
}

$reviewed = Safe-Get "$base/admin/reports?status=REVIEWED&page=0&size=20" $modHeaders 'GET_REVIEWED'
if ($reviewed) {
    Log ("REVIEWED_COUNT=" + (Get-PageItems $reviewed).Count)
}

$myRestrictions = Safe-Get "$base/moderator/restriction-requests?page=0&size=20" $modHeaders 'GET_MY_RESTRICTIONS'
if ($myRestrictions) {
    Log ("MY_RESTRICTIONS_COUNT=" + (Get-PageItems $myRestrictions).Count)
}

if ($adminToken) {
    $adminPending = Safe-Get "$base/admin/restriction-requests/pending?page=0&size=20" $adminHeaders 'GET_ADMIN_PENDING_RESTRICTIONS'
    if ($adminPending) {
        $items = Get-PageItems $adminPending
        $mine = $items | Where-Object {
            $_.requestedByModerator -and $_.requestedByModerator.email -eq $modUser
        }
        Log ("ADMIN_PENDING_RESTRICTION_TOTAL=" + $items.Count)
        Log ("ADMIN_PENDING_RESTRICTION_BY_MOD=" + @($mine).Count)
    }
}
