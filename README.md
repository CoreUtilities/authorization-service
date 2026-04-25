# Authorization Service

A Spring Boot-based authentication and authorization service that provides secure user registration, login, and JWT-based token management with refresh token support.

## Overview

This service implements a modern authentication system using JWT (JSON Web Tokens) with refresh token rotation. It's designed to be integrated as a microservice in larger applications, providing centralized user authentication and authorization.

## Features

- **User Registration**: Create new user accounts with password encryption
- **User Login**: Authenticate users and issue JWT access tokens
- **JWT Authentication**: Secure API endpoints with token-based authentication
- **Refresh Tokens**: Maintain user sessions with automatic token rotation
- **HTTPOnly Cookies**: Protect refresh tokens from XSS attacks using secure HTTPOnly cookies
- **Password Security**: BCrypt password encoding for secure storage
- **OAuth2 Integration**: Google OAuth2 authentication support for seamless social login
- **Spring Security Integration**: Built on Spring Security for robust security features

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT (JJWT library)
- **Build Tool**: Gradle
- **ORM**: Spring Data JPA with Hibernate
- **Additional Libraries**: Lombok for code generation

## Project Structure

```
src/main/java/com/authorization_service/
├── AuthorizationServerApplication.java    # Main Spring Boot application
├── config/
│   ├── AppConfig.java                    # Application configuration
│   └── SecurityConfig.java               # Spring Security configuration
├── controller/
│   └── AuthController.java               # REST API endpoints
├── dto/
│   ├── LoginRequest.java                 # Login request DTO
│   └── RegisterRequest.java              # Registration request DTO
├── entity/
│   ├── User.java                         # User entity
│   └── RefreshToken.java                 # Refresh token entity
├── repository/
│   ├── UserRepository.java               # User data access
│   └── RefreshTokenRepository.java       # Refresh token data access
├── security/
│   ├── CustomUserDetailsService.java     # Custom user details loading
│   ├── JwtAuthFilter.java                # JWT validation filter
│   └── OAuth2SuccessHandler.java         # OAuth2 callback handler
└── service/
    ├── JwtService.java                   # JWT generation and validation
    └── RefreshTokenService.java          # Refresh token management
```

## API Endpoints

### Authentication Endpoints

All endpoints are prefixed with `/api/auth`

#### Register User
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "message": "User registered successfully."
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Note: Sets httpOnly refresh token cookie automatically
```

#### Refresh Token
```
POST /api/auth/refresh-token
Cookie: refreshToken=<refresh_token>

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Note: Returns new access token and refreshes the httpOnly cookie
```

### OAuth2 Endpoints

#### Google OAuth2 Login
```
GET /oauth2/authorization/google

Redirects user to Google login page. After authentication, redirects to callback handler.
```

#### OAuth2 Callback (Auto-handled by Spring Security)
```
GET /login/oauth2/code/google

This endpoint is automatically handled by Spring Security after Google authentication.
The OAuth2SuccessHandler processes the callback and redirects to frontend with access token.

Redirect URL: http://localhost:3000/oauth-callback?accessToken=<accessToken>
```

## Setup & Installation

### Prerequisites
- Java 21 or higher
- PostgreSQL 12 or higher
- Gradle 7.0+

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE authorization_db;
```

2. Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/authorization_db
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
```

### Building the Application

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build JAR
./gradlew bootJar
```

### Running the Application

```bash
# Run using Gradle
./gradlew bootRun

# Or run the built JAR
java -jar build/libs/authorization-service-0.0.1-SNAPSHOT.jar
```

The service will start on `http://localhost:8080`

## Configuration

Key configuration properties in `application.properties`:

```properties
# Application name
spring.application.name=authorization-service

# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=testpass
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update

# Token expiration (7 days in milliseconds)
app.jwt.refresh-expiration-ms=604800000

# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google

# OAuth2 Provider configuration
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

### OAuth2 Setup

For detailed Google OAuth2 configuration instructions, see [GOOGLE_OAUTH2_SETUP.md](GOOGLE_OAUTH2_SETUP.md).

## Security Features

- **JWT Access Tokens**: Short-lived tokens (24 hours) for API access
- **Refresh Tokens**: Long-lived tokens (7 days) stored in HTTPOnly cookies
- **Password Encryption**: BCrypt-based password hashing
- **Cookie Security**: 
  - HttpOnly flag prevents JavaScript access
  - Secure flag enforces HTTPS in production
  - SameSite=Strict prevents CSRF attacks
- **Token Rotation**: Refresh tokens are rotated on each refresh operation
- **Spring Security Integration**: CSRF protection, security headers, and more

## Using the Service

### Example: Traditional Login Flow

1. **Register a new user**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john@example.com","password":"password123"}'
```

2. **Login**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john@example.com","password":"password123"}' \
  -c cookies.txt
```

3. **Use the access token in API requests**:
```bash
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer <accessToken>"
```

4. **Refresh the token**:
```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -b cookies.txt
```

### Example: Google OAuth2 Login Flow

1. **Redirect user to Google login**:
```
User visits: http://localhost:8080/oauth2/authorization/google
```

2. **After Google authentication**, user is automatically redirected to your frontend with the access token:
```
http://localhost:3000/oauth-callback?accessToken=<accessToken>
```

3. **Frontend stores the token** and uses it for subsequent requests:
```bash
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer <accessToken>"
```

For detailed OAuth2 setup instructions, see [GOOGLE_OAUTH2_SETUP.md](GOOGLE_OAUTH2_SETUP.md).

## Testing

Run the test suite with:
```bash
./gradlew test
```

Test reports are available in `build/reports/tests/test/index.html`

## Future Enhancements

- Multi-factor authentication (MFA)
- Rate limiting for login attempts
- Audit logging for security events
- User roles and permissions management
- API key authentication
- Token blacklisting for logout functionality
- Additional OAuth2 providers (GitHub, Microsoft, etc.)
- Social login account linking

## License

This project is part of CoreUtilities and follows the organization's licensing policies.

## Support

For issues, questions, or contributions, please contact the development team.
