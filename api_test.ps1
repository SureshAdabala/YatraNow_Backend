$BASE = 'http://localhost:9090'
$p = 0; $f = 0

function T($label, $method, $url, $body = $null, $tok = $null, $xfail = $false) {
    $h = @{ 'Content-Type' = 'application/json' }
    if ($tok) { $h['Authorization'] = 'Bearer ' + $tok }
    try {
        $pm = @{ Uri = $url; Method = $method; Headers = $h; TimeoutSec = 10 }
        if ($body) { $pm['Body'] = $body }
        $r = Invoke-RestMethod @pm
        if ($xfail) { Write-Host ('FAIL [200] ' + $label) -ForegroundColor Red; $script:f++ }
        else { Write-Host ('PASS [2xx] ' + $label) -ForegroundColor Green; $script:p++ }
        return $r
    }
    catch {
        $c = [int]$_.Exception.Response.StatusCode
        if ($xfail -and $c -in @(400, 401, 403, 409)) { Write-Host ('PASS [' + $c + '] ' + $label) -ForegroundColor Green; $script:p++ }
        else { Write-Host ('FAIL [' + $c + '] ' + $label) -ForegroundColor Red; $script:f++ }
        return $null
    }
}

Write-Host ''
Write-Host '--- PUBLIC (no auth) ---' -ForegroundColor Cyan
T 'GET route-list' GET ($BASE + '/api/public/route-list')
T 'GET routes'     GET ($BASE + '/api/public/routes')
$d = (Get-Date).AddDays(1).ToString('yyyy-MM-dd')
$searchUrl = $BASE + '/api/public/search?from=Mumbai' + [char]38 + 'to=Pune' + [char]38 + 'date=' + $d
T 'GET search Mumbai-Pune' GET $searchUrl
T 'GET seats/1'         GET ($BASE + '/api/public/seats/1')
T 'GET images/agency/1' GET ($BASE + '/api/public/images/agency/1')

Write-Host ''
Write-Host '--- AUTH ---' -ForegroundColor Cyan
$loginAdminBody = '{"email":"suresh@gmail.com","password":"Suresh@55"}'
$ar = T 'POST login admin' POST ($BASE + '/api/auth/login') $loginAdminBody
$at = if ($ar) { $ar.token } else { $null }

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$rb = '{"fullName":"Tester","email":"tester' + $ts + '@t.com","password":"test1234","phoneNumber":"9123456789"}'
$ur = T 'POST register user' POST ($BASE + '/api/auth/register/user') $rb
$ut = if ($ur) { $ur.token } else { $null }
$uid = if ($ur) { $ur.id } else { $null }

$loginOwnerBody = '{"email":"rajesh@royal.com","password":"rajesh123"}'
$or2 = T 'POST login owner' POST ($BASE + '/api/auth/login') $loginOwnerBody
$ot = if ($or2) { $or2.token } else { $null }

$badLogin = '{"email":"admin@yatranow.com","password":"wrongpass"}'
T 'POST login bad password (expect 401)' POST ($BASE + '/api/auth/login') $badLogin -xfail $true

Write-Host ''
Write-Host '--- ADMIN (ROLE_ADMIN) ---' -ForegroundColor Cyan
if ($at) {
    T 'GET admin/users'      GET   ($BASE + '/api/admin/users')          -tok $at
    T 'GET admin/owners'     GET   ($BASE + '/api/admin/owners')         -tok $at
    
    # Test User Blocking
    if ($uid) {
        T "PATCH block user $uid"   PATCH ($BASE + "/api/admin/users/$uid/block") -tok $at
        # Try login with blocked user credentials
        $userLoginErrors = '{"email":"' + $ur.email + '","password":"test1234"}'
        T 'POST login blocked user (expect 403/401)' POST ($BASE + '/api/auth/login') $rb -xfail $true
        T "PATCH unblock user $uid" PATCH ($BASE + "/api/admin/users/$uid/block") -tok $at
        T 'POST login unblocked user' POST ($BASE + '/api/auth/login') $rb
    }

    T 'PATCH block owner 2'  PATCH ($BASE + '/api/admin/owners/2/block') -tok $at
    T 'PATCH unblock owner 2' PATCH ($BASE + '/api/admin/owners/2/block') -tok $at
    if ($ut) {
        T 'GET admin/users with USER token (expect 403)' GET ($BASE + '/api/admin/users') -tok $ut -xfail $true
    }
}
else { Write-Host 'SKIP admin (no token)' -ForegroundColor Yellow }

Write-Host ''
Write-Host '--- OWNER (ROLE_OWNER) ---' -ForegroundColor Cyan
if ($ot) {
    T 'GET owner/vehicles'   GET ($BASE + '/api/owner/vehicles')   -tok $ot
    T 'GET owner/routes'     GET ($BASE + '/api/owner/routes')     -tok $ot
    T 'GET owner/bookings'   GET ($BASE + '/api/owner/bookings')   -tok $ot
    T 'GET owner/complaints' GET ($BASE + '/api/owner/complaints') -tok $ot
    T 'GET owner/schedules'  GET ($BASE + '/api/owner/schedules')  -tok $ot

    $newRoute = '{"fromLocation":"CityA","toLocation":"CityB","distanceKm":150}'
    $nr = T 'POST owner/routes create' POST ($BASE + '/api/owner/routes') $newRoute -tok $ot
    if ($nr) {
        $rid = $nr.id
        $upRoute = '{"fromLocation":"CityX","toLocation":"CityY","distanceKm":200}'
        T ('PUT owner/routes/' + $rid + ' update') PUT ($BASE + '/api/owner/routes/' + $rid) $upRoute -tok $ot
        
        # Test Cascade Delete: Create Schedule then Delete Route
        $today = (Get-Date).ToString("yyyy-MM-dd")
        $newSchedule = '{"vehicleId":1,"routeId":' + $rid + ',"departureTime":"10:00:00","arrivalTime":"14:00:00","price":500.0,"scheduleDate":"' + $today + '"}'
        $ns = T 'POST owner/schedules (for cascade test)' POST ($BASE + '/api/owner/schedules') $newSchedule -tok $ot
        
        if ($ns) {
            T ('DELETE owner/routes/' + $rid + ' (cascade)') DELETE ($BASE + '/api/owner/routes/' + $rid) -tok $ot
        }
        else {
            Write-Host "Skipping cascade delete test due to schedule creation failure" -ForegroundColor Yellow
            T ('DELETE owner/routes/' + $rid) DELETE ($BASE + '/api/owner/routes/' + $rid) -tok $ot
        }
    }
}
else { Write-Host 'SKIP owner (no token)' -ForegroundColor Yellow }

Write-Host ''
Write-Host '--- USER (ROLE_USER) ---' -ForegroundColor Cyan
if ($ut) {
    T 'GET user/bookings (empty)'    GET ($BASE + '/api/user/bookings') -tok $ut
    $seat = 'S' + (Get-Date -Format 'ssfff')
    $bkBody = '{"scheduleId":1,"seatNumber":"' + $seat + '","passengerName":"Test P","passengerAge":25,"passengerGender":"Male"}'
    T 'POST user/bookings'           POST ($BASE + '/api/user/bookings') $bkBody -tok $ut
    T 'POST user/bookings duplicate (expect 409)' POST ($BASE + '/api/user/bookings') $bkBody -tok $ut -xfail $true
    T 'GET user/bookings (after)'    GET ($BASE + '/api/user/bookings') -tok $ut
}
else { Write-Host 'SKIP user (no token)' -ForegroundColor Yellow }

Write-Host ''
Write-Host '--- SECURITY ---' -ForegroundColor Cyan
T 'GET admin/users no-token (expect 401)'   GET ($BASE + '/api/admin/users')   -xfail $true
T 'GET user/bookings no-token (expect 401)' GET ($BASE + '/api/user/bookings') -xfail $true

Write-Host ''
Write-Host '--- OWNER DELETION (CASCADE) ---' -ForegroundColor Cyan
# 1. Register a fresh owner using curl (multipart)
$ts2 = Get-Date -Format 'HHmmss'
$email2 = "delete$ts2@o.com"
$curlCmd = "curl.exe -s -X POST -F `"ownerName=DeleteMe`" -F `"agencyName=TempAgency`" -F `"email=$email2`" -F `"password=password123`" -F `"mobile=9999999999`" $BASE/api/auth/register/owner"
$roRaw = Invoke-Expression $curlCmd
$ro = $roRaw | ConvertFrom-Json

if ($ro) {
    Write-Host "PASS [2xx] Register fresh owner (ID: $($ro.id))" -ForegroundColor Green; $script:p++
    $newOwnerId = $ro.id
    $newOwnerToken = $ro.token
    
    # 2. Add a vehicle
    $vBody = '{"name":"Temp Bus","vehicleNumber":"T-' + $ts2 + '","vehicleType":"BUS","busType":"DELUXE","totalSeats":45}'
    $rv = T 'POST owner/vehicles' POST ($BASE + '/api/owner/vehicles') $vBody -tok $newOwnerToken
    
    if ($rv) {
        $vid = $rv.id
        # 3. Add a schedule
        $sBody = '{"vehicleId":' + $vid + ',"routeId":2,"departureTime":"10:00:00","arrivalTime":"14:00:00","price":500,"scheduleDate":"2026-12-01"}'
        $rs = T 'POST owner/schedules' POST ($BASE + '/api/owner/schedules') $sBody -tok $newOwnerToken
        
        # 4. Delete Owner as Admin
        T "DELETE admin/owners/$newOwnerId (cascade check)" DELETE ($BASE + "/api/admin/owners/$newOwnerId") -tok $at
    }
}

Write-Host ''
$color = if ($f -eq 0) { 'Green' } else { 'Yellow' }
Write-Host ('TOTAL: ' + ($p + $f) + '  |  PASSED: ' + $p + '  |  FAILED: ' + $f) -ForegroundColor $color
