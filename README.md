# TDRA SSIR Application

## Overview

The TDRA SSIR application supports the onboarding and management of organizations and users for the Sender ID Registry process.

The current implementation includes:

- User registration and authentication
- Role-based login and access control
- Company onboarding
- Registration submission
- Legal document upload and storage
- Sender ID management
- TDRA Admin review workflows
- JWT-based authentication
- MinIO-based document storage
- MySQL persistence
- Swagger / OpenAPI support

---

## Project Structure

The solution is divided into frontend and backend components.

### Frontend

The frontend is built using Angular.

The Angular workspace contains multiple application/project structures; however, the current end-to-end implementation is consolidated under:

```text
role-based-login
```

> Note: Multiple Angular applications/projects were initially created to support modular development and separation of features. At the current stage of development, the active functionality has been brought together under `role-based-login` so the complete application can be run and tested from a single frontend application.

### Backend

The backend is built using Spring Boot and Maven and is split into modules:

```text
ssir-core
ssir-api
```

`ssir-core` contains the reusable/core backend functionality and must be built before running the API module.

`ssir-api` contains the Spring Boot application and exposes the REST APIs used by the frontend.

---

## Technology Stack

### Frontend

- Angular
- TypeScript
- Bootstrap
- RxJS
- Angular Signals
- JWT authentication
- Role-based route access
- Custom LoggerService

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- Maven
- MySQL
- JWT authentication
- Springdoc OpenAPI / Swagger
- MinIO object storage

---

## Prerequisites

Ensure the following are installed and available locally:

- Node.js
- Angular CLI
- Java
- Maven
- MySQL
- Docker / Docker Desktop
- MinIO container

Default local ports used by the application:

| Component | URL / Port |
| --- | --- |
| Angular Frontend | `http://localhost:4200` |
| Spring Boot Backend | `http://localhost:8080` |
| MinIO API | `http://localhost:9100` |
| MinIO Console | `http://localhost:9101` |
| MySQL | `localhost:3306` |

---

# Running the Application Locally

## 1. Start Required Infrastructure

Before starting the application, ensure that:

- MySQL is running.
- Docker Desktop is running.
- The MinIO container is running.

---

## 2. Start MinIO

Open the MinIO Console:

```text
http://localhost:9101/
```

Local development credentials:

```text
Username: minioadmin
Password: minioadmin
```

The application uses the following bucket:

```text
ssir-documents
```

If the bucket does not already exist, create it before testing document uploads.

> These credentials are intended only for local development. Do not use default credentials in production.

---

## 3. Build the Backend Core Module

Open a terminal and navigate to the backend core module:

```bash
cd ssir-core
```

Run:

```bash
mvn clean
mvn install
mvn package
```

This builds the core module and installs the required artifact into the local Maven repository.

---

## 4. Start the Backend API

Navigate to the API module:

```bash
cd ssir-api
```

Run:

```bash
mvn clean
mvn install
mvn spring-boot:run
```

The backend should start at:

```text
http://localhost:8080
```

---

## 5. Start the Frontend

From the Angular workspace root, run:

```bash
ng serve role-based-login
```

The frontend should be available at:

```text
http://localhost:4200
```

---

# Application Configuration

## Spring Boot Application

The application uses the `local` Spring profile during local development.

Main application settings include:

```yaml
spring:
  application:
    name: tdra-ssir-backend

  profiles:
    active: local

server:
  port: 8080
```

---

## Database Configuration

Local MySQL configuration:

```text
Database: ssir_db
Host: localhost
Port: 3306
```

The application can create the database automatically when required because the JDBC URL contains:

```text
createDatabaseIfNotExist=true
```

Hibernate is currently configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

This allows Hibernate to update the local database schema during development.

---

## File Upload Configuration

The backend currently allows:

```text
Maximum file size: 10 MB
Maximum request size: 10 MB
```

Documents are stored in MinIO using the configured bucket:

```text
ssir-documents
```

---

# API Configuration

The local API base URL is:

```text
http://localhost:8080/api/v1
```

Current frontend API groups include:

```text
/api/v1/auth
/api/v1/registrations
/api/v1/onboarding-single
/api/v1/sender-ids
```

The frontend environment configuration should therefore use a common base URL such as:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
```

Services should append their individual endpoint paths to this base URL rather than hardcoding `localhost` throughout the application.

---

# Authentication

The application uses JWT-based authentication.

The backend currently supports:

- Access tokens
- Refresh tokens
- Role-based authorization

Current local token expiry configuration:

```text
Access token: 15 minutes
Refresh token: 7 days
```

The Angular authentication interceptor attaches the access token to protected backend API requests.

Public authentication endpoints such as login, registration initialization, refresh token, forgot password, and reset password are allowed to execute without an existing access token.

---

# Frontend Logging

The frontend uses a custom `LoggerService`.

Recommended logging levels are:

```text
Development -> DEBUG
UAT         -> WARN
Production  -> ERROR
```

For production, only error-level frontend logs should normally be enabled.

Environment-based logger configuration is recommended so logging behavior can be changed without modifying the logger implementation.

---

# CORS

During local development, the backend allows requests from:

```text
http://localhost:4200
```

This corresponds to the Angular development server.

---

# Swagger / OpenAPI

Springdoc OpenAPI is enabled in the backend.

Once the backend is running, Swagger UI can be accessed at:

```text
http://localhost:8080/swagger-ui.html
```

The generated OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger configuration currently sorts:

- Operations by HTTP method
- Tags alphabetically

Detailed API documentation and annotations can be expanded as the next documentation step.

---

# Recommended Startup Order

For local development, start the application in the following order:

```text
1. MySQL
2. Docker / MinIO
3. ssir-core build
4. ssir-api backend
5. Angular role-based-login frontend
```

This ensures all backend dependencies and storage services are available before the frontend begins making API requests.

---

# Local Development Checklist

Before testing the application, verify:

- MySQL is running.
- `ssir_db` is available or can be created.
- Docker Desktop is running.
- MinIO is running.
- `ssir-documents` bucket exists.
- `ssir-core` has been successfully built.
- `ssir-api` is running on port `8080`.
- `role-based-login` is running on port `4200`.
- Frontend API base URL points to `http://localhost:8080/api/v1`.
- Swagger UI is accessible.

---

# Security Notes

The current configuration contains local-development values for database access, MinIO access, and JWT configuration.

Before deployment to any shared, UAT, staging, or production environment:

- Move secrets and passwords to environment variables or a secure secrets-management solution.
- Do not commit production credentials to source control.
- Replace default MinIO credentials.
- Replace local database credentials.
- Use an environment-specific JWT secret.
- Configure environment-specific CORS origins.
- Disable unnecessary debug logging.
- Review Hibernate `ddl-auto` configuration.
- Use HTTPS for deployed environments.

---

# Current Development Note

The application is actively under development.

Although the Angular workspace was structured to support multiple applications/projects, the currently integrated implementation is maintained under `role-based-login`. This should be treated as the primary frontend application for local development and end-to-end testing unless the project structure is revised later.
