# Authentication API

A modern authentication and authorization web application built with Spring Boot 3.x, following Clean Architecture and Domain-Driven Design principles.

## Features

- **User Registration & Login**: Email/password authentication with JWT tokens
- **External Provider Login**: Google and Facebook OAuth2 integration
- **Email Verification**: Account activation via email confirmation
- **Password Recovery**: Secure password reset functionality
- **Role-Based Authorization**: USER, ADMIN, MANAGER roles with JWT claims
- **User Profile Management**: Update profile information
- **Global Error Handling**: Centralized error management with structured responses

## Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** with JWT
- **Spring Data JPA**
- **MySQL** database
- **Gradle** build tool
- **JUnit 5** + Mockito for testing

## Project Structure

Following Clean Architecture and DDD principles:

```
src/main/java/br/com/ovigia/
├── domain/
│   ├── model/          # Domain entities (User, Role, Token, etc.)
│   └── repository/     # Repository interfaces
├── service/            # Business logic services
├── usecase/            # Use cases organized by feature
│   ├── user/
│   │   ├── save/       # User registration
│   │   ├── login/      # User login
│   │   ├── verify/     # Email verification
│   │   ├── password/   # Password management
│   │   ├── profile/    # Profile management
│   │   └── oauth/      # OAuth2 integration
│   └── ...
├── config/             # Configuration classes
└── AuthenticationApiApplication.java
```

## Setup Instructions

### Prerequisites

- Java 21
- Gradle
- MySQL 8.0+
- Docker 27.5.1
- Docker Compose 2.29.2

Verify versions:
```bash
docker --version       # Docker version 27.5.1, build ...
docker compose version # Docker Compose version v2.29.2
```

Install on Ubuntu (official docs recommended):
- Docker Engine: `https://docs.docker.com/engine/install/`
- Docker Compose plugin: `https://docs.docker.com/compose/install/linux/`

Example (Ubuntu apt):
```bash
sudo apt-get update
# Docker Engine
curl -fsSL https://get.docker.com | sh
# Compose plugin
sudo apt-get install -y docker-compose-plugin
```

Add your user to docker group (no sudo):
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE auth_db;
```

2. Update `application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
```

### Environment Variables

Set the following environment variables:

```bash
export JWT_SECRET=your-secret-key-here
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export FACEBOOK_CLIENT_ID=your-facebook-client-id
export FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
export FRONTEND_URL=http://localhost:3000
```

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

## Docker

This project includes a Dockerfile and `docker-compose.yml` with three services: `authentication-api`, `mysql`, and a sample `authentication-app` frontend placeholder.

### Build and run with Docker Compose

```bash
docker compose up --build
```

- Backend: `http://localhost:8080`
- Frontend placeholder (Vite dev server): `http://localhost:3000`
- MySQL: port `3306` with database `auth_db` (user: `root` / pass: `root`)

Spring profile `docker` is activated automatically by the container via `SPRING_PROFILES_ACTIVE=docker`.

## Java Virtual Threads

Virtual threads are enabled to improve request handling throughput.

- Config in `application.yml`:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

- An executor based on `Executors.newVirtualThreadPerTaskExecutor()` is provided in `SecurityConfig` as `applicationTaskExecutor`.
- You can also set `JAVA_OPTS` to include `-Dspring.threads.virtual.enabled=true`.

## API Endpoints

### Authentication

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification
- `POST /api/auth/forgot-password` - Initiate password reset
- `POST /api/auth/reset-password` - Reset password

### OAuth2

- `GET /api/auth/oauth2/success` - OAuth2 success callback
- `GET /api/auth/oauth2/failure` - OAuth2 failure callback

### User Profile

- `GET /api/user/profile` - Get user profile (requires authentication)
- `PUT /api/user/profile` - Update user profile (requires authentication)

### OAuth2 Login URLs

- Google: `http://localhost:8080/oauth2/authorization/google`
- Facebook: `http://localhost:8080/oauth2/authorization/facebook`

## API Response Format

All API responses follow this structure:

```json
{
  "data": <response_data>,
  "error": null,
  "success": true
}
```

Error responses:
```json
{
  "data": null,
  "error": "Error message",
  "success": false
}
```

## Security Features

- **Password Hashing**: BCrypt encryption
- **JWT Tokens**: Secure token-based authentication
- **CORS Configuration**: Cross-origin resource sharing
- **Input Validation**: Comprehensive request validation
- **Role-Based Access**: Method-level security with `@PreAuthorize`

## Testing

Run tests with:
```bash
./gradlew test
```

## Password Requirements

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character (@$!%*?&)

## Email Configuration

The application uses Gmail SMTP for sending emails. Configure your email settings in `application.yml` or use environment variables.

## OAuth2 Setup

1. **Google OAuth2**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing
   - Enable Google+ API
   - Create OAuth2 credentials
   - Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

2. **Facebook OAuth2**:
   - Go to [Facebook Developers](https://developers.facebook.com/)
   - Create a new app
   - Add Facebook Login product
   - Add OAuth redirect URI: `http://localhost:8080/login/oauth2/code/facebook`

## Contributing

This project follows Clean Architecture and DDD principles. When adding new features:

1. Create domain models in `/domain/model`
2. Add repository interfaces in `/domain/repository`
3. Implement business logic in `/service`
4. Create use cases in `/usecase` following the folder structure
5. Add proper validation and error handling
6. Write comprehensive tests

## License

This project is licensed under the MIT License.
