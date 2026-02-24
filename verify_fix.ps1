
# 1. Register Owner
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$email = "owner_$timestamp@example.com"
$mobile = "9" + (Get-Random -Minimum 100000000 -Maximum 999999999)
$password = "password123"

echo "Registering Owner: $email / $mobile"
# Create dummy image if not exists
if (-not (Test-Path "dummy.jpg")) {
    New-Item -Path "dummy.jpg" -ItemType File -Value "dummy content" -Force
}

$registerResponse = cmd /c "curl -s -X POST -F ownerName=\"Test Owner Auto\" -F agencyName=\"Test Agency Auto\" -F email=\"$email\" -F password=\"$password\" -F mobile=\"$mobile\" -F agencyImage=@dummy.jpg http://localhost:8080/api/auth/register/owner"
echo "Register Response: $registerResponse"

if ($registerResponse -match "token") {
    # Extract token (simple regex)
    $token = $registerResponse | Select-String -Pattern '"token":"([^"]+)"' | ForEach-Object { $_.Matches.Groups[1].Value }
    
    if ($token) {
        echo "`nLogged in. Token: $token"
        
        # 2. Check Vehicles
        echo "`nChecking Vehicles..."
        $vehiclesResponse = cmd /c "curl -s -X GET -H \"Authorization: Bearer $token\" http://localhost:8080/api/owner/vehicles"
        echo "Vehicles Response: $vehiclesResponse"
        
        if ($vehiclesResponse -match "vehicleNumber") {
            echo "`nSUCCESS: Default vehicle found!"
        }
        else {
            echo "`nFAILURE: No vehicles found or error occurred."
        }
    }
    else {
        echo "`nFAILURE: Could not extract token."
    }
}
else {
    echo "`nFAILURE: Registration failed."
}
