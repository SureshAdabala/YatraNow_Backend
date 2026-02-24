
# Create a dummy image
New-Item -Path "dummy.jpg" -ItemType File -Value "dummy content" -Force

# Curl command to test owner registration
$url = "http://localhost:8080/api/auth/register/owner"
$form = @{
    ownerName = "Test Owner"
    agencyName = "Test Agency"
    email = "testowner@example.com"
    password = "password123"
    mobile = "9876543210"
    agencyImage = Get-Item "dummy.jpg"
}

# Equivalent curl command (PowerShell syntax is tricky for multipart, using curl directly)
curl -F "ownerName=Test Owner" -F "agencyName=Test Agency" -F "email=testowner@example.com" -F "password=password123" -F "mobile=9876543210" -F "agencyImage=@dummy.jpg" http://localhost:8080/api/auth/register/owner
