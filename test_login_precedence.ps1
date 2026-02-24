
# 1. Register User
$email = "testdual@example.com"
$password = "password123"

echo "Registering User..."
cmd /c "curl -X POST -H \"Content-Type: application/json\" -d \" { \\\"fullName\\\":\\\"Test User\\\", \\\"email\\\":\\\"$email\\\", \\\"password\\\":\\\"$password\\\", \\\"mobile\\\":\\\"9000000001\\\" }\" http://localhost:8080/api/auth/register/user"

# 2. Register Owner (with same email)
echo "`nRegistering Owner..."
# Note: Using 'phone' and 'image' alias we added
cmd /c "curl -X POST -F ownerName=\"Test Owner Dual\" -F agencyName=\"Test Agency Dual\" -F email=\"$email\" -F password=\"$password\" -F phone=\"9000000002\" -F image=@dummy.jpg http://localhost:8080/api/auth/register/owner"

# 3. Login
echo "`nLogging in..."
cmd /c "curl -X POST -H \"Content-Type: application/json\" -d \" { \\\"email\\\":\\\"$email\\\", \\\"password\\\":\\\"$password\\\" }\" http://localhost:8080/api/auth/login"
