import markdown
from xhtml2pdf import pisa

doc_content = """
# YatraNow Project Documentation

## 1. Project Features & Components
YatraNow is a comprehensive transportation booking platform designed to facilitate ticket reservations for trains and buses.

### Key Features
- **User & Owner Portals**: Distinct dashboards for travelers (Users) and agency operators (Owners).
- **Real-Time Booking**: Live availability tracking for seats and bogies.
- **Secure Payments**: Integrated Razorpay gateway for seamless and secure transactions.
- **E-Ticketing**: Automated generation of PDF boarding passes with QR codes, delivered via Email.
- **Role-Based Access Control**: Strict hierarchical security ensuring Admins, Owners, and Users only access permitted endpoints.
- **Social Login**: OAuth2 integration with Google and GitHub for rapid onboarding.

### Core Components
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, utilizing `fetch` API for asynchronous backend communication.
- **Backend Core**: Java 17, Spring Boot 3.
- **Database Layer**: MySQL relational database.
- **Security**: Spring Security & JWT (JSON Web Tokens).

---

## 2. Detailed Implementation Explanations

### Spring Data JPA
Spring Data JPA significantly reduces boilerplate code required to implement data access layers. In YatraNow, it is used by extending interfaces like `JpaRepository`. This provides automatic implementation of CRUD operations. We leverage custom query methods (e.g., `findByEmail`, `findAvailableSeats`) to execute optimized SQL queries.

### Hibernate
Hibernate operates as the primary JPA provider (ORM) in our project. It maps our Java Entities (like `User`, `Vehicle`, `Booking`) directly to MySQL tables. We utilize features like lazy loading (`FetchType.LAZY`) to optimize performance and prevent N+1 query problems when fetching relationships like a `Vehicle` and its `Schedules`.

### Spring Security
Spring Security is the backbone of our application's defense. It is configured to be stateless (`SessionCreationPolicy.STATELESS`), relying entirely on JWTs for authorization. The `SecurityConfig` class dictates endpoint permissions (e.g., `/api/admin/**` requires `ROLE_ADMIN`). We implemented a custom `JwtAuthenticationFilter` that intercepts every request, validates the token, and populates the `SecurityContextHolder`.

### Razorpay Integration
The Razorpay SDK is utilized to handle digital payments safely.
1. **Order Creation**: The backend creates an order using `RazorpayClient` and returns an `orderId`.
2. **Checkout**: The frontend opens the Razorpay modal.
3. **Verification**: Post-payment, the backend receives the `razorpay_signature` and verifies its authenticity using HMAC-SHA256 with the Razorpay API Secret. This guarantees that payments cannot be spoofed.

### OAuth2 Authentication
OAuth2 allows users to log in using their Google or GitHub accounts.
- We utilize `spring-boot-starter-oauth2-client`.
- When a user authenticates via a social provider, our `OAuth2AuthenticationSuccessHandler` triggers.
- It extracts the user's email, auto-registers them if they are new, generates a JWT, and redirects them to the frontend with the token appended to the URL.

### SMTP Protocol (Email Service)
JavaMailSender is configured with SMTP properties to dispatch emails. Upon a successful booking, the `EmailService` is invoked asynchronously. It constructs an HTML email template, attaches the generated PDF ticket, and sends it via Gmail's SMTP server securely over TLS.

### Additional Technologies
- **iText / OpenPDF**: Used dynamically to generate the PDF boarding pass.
- **ZXing**: Used to generate 2D QR codes embedded in the PDF for ticket validation.
- **HikariCP**: The default connection pool utilized to maintain optimal database connection availability and performance.

---

## 3. User & Owner Registration Flow

### User Registration
1. **Data Collection**: User inputs Name, Email, Phone, and Password on the frontend.
2. **API Call**: Data is sent to `/auth/register/user`.
3. **Validation & Encoding**: Backend checks for email uniqueness. The password is cryptographically hashed using `BCryptPasswordEncoder`.
4. **Persistence**: The `User` entity is saved with `ROLE_USER`.
5. **Token Generation**: A JWT is generated and returned to the client for immediate login.

### Owner Registration
1. **Data Collection**: Owner inputs Agency Name, Personal details, and uploads an Agency Logo.
2. **Multipart Request**: Data is sent to `/auth/register/owner` as `FormData`.
3. **File Handling**: `ImageService` processes and saves the logo to the filesystem/cloud.
4. **Persistence**: The `Owner` entity is created with `ROLE_OWNER`. A default dummy `Vehicle` is automatically assigned to them to jumpstart their dashboard.
5. **Token Generation**: A JWT is generated and returned.

---

## 4. System Flowcharts

### Registration Process Flowchart
<pre>
[Frontend Registration Form]
         |
         v
[Submit Data (User/Owner)] ----> [Form Validation (Frontend)]
         |
         v
[Backend REST API Controller]
         |
         v
[Check if Email Exists] ---> (Yes) ---> [Return Error: Email Taken]
         | (No)
         v
[Hash Password (BCrypt)]
         |
         v
[Save Entity to MySQL DB]
         |
         v
[Generate JWT Token]
         |
         v
[Return HTTP 200 OK + Token]
         |
         v
[Frontend Saves Token & Redirects to Dashboard]
</pre>

### Overall System Workflow
<pre>
+----------------+       +-------------------+       +-------------------+
|  Guest User    | ----> | Authentication    | ----> | Authenticated     |
| (Browsing App) |       | (Login/OAuth2)    |       | User / Owner      |
+----------------+       +-------------------+       +-------------------+
                               |
                               v
                     +-------------------+
                     | Route & Schedule  |
                     | Search Engine     |
                     +-------------------+
                               |
                               v
                     +-------------------+
                     | Seat / Bogie      |
                     | Selection         |
                     +-------------------+
                               |
                               v
                     +-------------------+
                     | Razorpay Gateway  |
                     | (Payment Pending) |
                     +-------------------+
                               | (Payment Success & Verification)
                               v
                     +-------------------+
                     | Booking Confirmed |
                     | (DB Updated)      |
                     +-------------------+
                               |
                               v
                     +-------------------+
                     | Async Notification|
                     | PDF Generation &  |
                     | Email Dispatch    |
                     +-------------------+
</pre>

---

## 5. Role-Based Authentication

Role-Based Access Control (RBAC) ensures that users can only access information and perform actions explicitly permitted by their assigned role.

### How Roles are Assigned
Roles are assigned dynamically at the point of registration. 
- A standard visitor signing up via the User form or OAuth2 is assigned `ROLE_USER`.
- A travel agency registering via the Owner form is assigned `ROLE_OWNER`.
- System administrators are predefined in the database with `ROLE_ADMIN`.

### How Access Control is Managed
Access is strictly managed via Spring Security's `SecurityFilterChain`. Endpoints are protected using `.requestMatchers()`.
- `/api/user/**` -> Requires `ROLE_USER`
- `/api/owner/**` -> Requires `ROLE_OWNER`
- `/api/admin/**` -> Requires `ROLE_ADMIN`

### The Authentication & Authorization Flow
1. **Authentication (Login)**: The user provides credentials. The `AuthService` verifies them against the database. If correct, a JWT containing the user's ID and Role is generated and signed with a secret key.
2. **Token Transmission**: The frontend stores this JWT and attaches it as a `Bearer` token in the `Authorization` header of all subsequent API requests.
3. **Authorization (Filter Interception)**: 
   - The `JwtAuthenticationFilter` intercepts the request.
   - It parses the JWT, verifying its signature and expiration.
   - It extracts the `Role` from the token payload.
   - It constructs an `Authentication` object and sets it in the `SecurityContextHolder`.
4. **Access Decision**: Spring Security checks the endpoint's required role against the role in the `SecurityContext`. If they match, the request proceeds to the Controller. If not, an HTTP 403 Forbidden error is returned.

---
**Prepared by YatraNow Team**
"""

# HTML Template
html_content = f"""
<!DOCTYPE html>
<html>
<head>
    <style>
        body {{
            font-family: Arial, sans-serif;
            font-size: 14px;
            color: #333333;
            line-height: 1.6;
        }}
        h1 {{
            color: #FF7A1A;
            text-align: center;
            font-size: 28px;
            margin-bottom: 20px;
        }}
        h2 {{
            color: #2F3640;
            border-bottom: 2px solid #FF7A1A;
            padding-bottom: 5px;
            margin-top: 30px;
        }}
        h3 {{
            color: #353B48;
            margin-top: 20px;
        }}
        p, li {{
            font-size: 13px;
        }}
        ul {{
            margin-top: 5px;
            margin-bottom: 15px;
        }}
        code {{
            background-color: #f1f2f6;
            padding: 2px 4px;
            border-radius: 4px;
            font-family: Courier, monospace;
        }}
        pre {{
            background-color: #f8f9fa;
            border: 1px solid #e9ecef;
            padding: 15px;
            border-radius: 5px;
            font-family: Courier, monospace;
            font-size: 11px;
            white-space: pre-wrap;
            color: #2F3640;
        }}
    </style>
</head>
<body>
    {markdown.markdown(doc_content)}
</body>
</html>
"""

# Generate PDF
output_filename = "YatraNow_Documentation_V2.pdf"
try:
    with open(output_filename, "wb") as pdf_file:
        pisa_status = pisa.CreatePDF(html_content, dest=pdf_file)
        if pisa_status.err:
            print("Error generating PDF!")
        else:
            print(f"Successfully generated {output_filename}")
except Exception as e:
    print(f"Failed to create PDF. Error: {e}")
