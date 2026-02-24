$BASE = 'http://localhost:9090'

# Login as owner rajesh
$loginBody = '{"email":"rajesh@royal.com","password":"rajesh123"}'
$or2 = Invoke-RestMethod -Uri ($BASE + '/api/auth/login') -Method POST -ContentType 'application/json' -Body $loginBody
$ot = $or2.token
Write-Host ('Owner ID from token: ' + $or2.id + ', Role: ' + $or2.role)

$h = @{ 'Content-Type' = 'application/json'; 'Authorization' = 'Bearer ' + $ot }

Write-Host ''
Write-Host '--- Testing GET /api/owner/vehicles ---'
try {
    $v = Invoke-RestMethod -Uri ($BASE + '/api/owner/vehicles') -Method GET -Headers $h -TimeoutSec 10
    Write-Host ('SUCCESS - Vehicles count: ' + $v.Count)
    $v | ForEach-Object { Write-Host ('  Vehicle: ' + $_.id + ' ' + $_.name) }
}
catch {
    $code = [int]$_.Exception.Response.StatusCode
    Write-Host ('FAILED with HTTP ' + $code)
    Write-Host ('Error body: ' + $_.ErrorDetails.Message)
}

Write-Host ''
Write-Host '--- Testing GET /api/owner/bookings ---'
try {
    $bk = Invoke-RestMethod -Uri ($BASE + '/api/owner/bookings') -Method GET -Headers $h -TimeoutSec 10
    Write-Host ('SUCCESS - Bookings count: ' + $bk.Count)
}
catch {
    $code = [int]$_.Exception.Response.StatusCode
    Write-Host ('FAILED with HTTP ' + $code)
    Write-Host ('Error body: ' + $_.ErrorDetails.Message)
}
