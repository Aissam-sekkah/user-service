# User Service

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Gradle](https://img.shields.io/badge/Gradle-8.x-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

The **User Service** is a production-ready microservice responsible for identity and access management (IAM). It handles user registration, authentication, authorization (RBAC), and group management.

## 🏗 Architecture

This project follows the **Hexagonal Architecture** (Ports and Adapters) pattern to ensure a strict separation between the core business logic and external dependencies.

### Architectural Layers:
- **Domain Layer (`domain`)**: The heart of the application. Contains business entities, domain services, and port interfaces. It is completely independent of any framework or external library.
- **Adapters Layer (`adapter`)**:
    - **Inbound (Driving) Adapters**: REST Controllers that translate HTTP requests into domain use-case calls.
    - **Outbound (Driven) Adapters**: Implementations of domain ports for persistence (JPA), security (JWT), and other external systems.
- **Configuration Layer (`config`)**: Spring-specific configurations for security, CORS, rate limiting, and Swagger.

## 🚀 Features

- **User Management**: Full CRUD operations for user profiles.
- **Authentication**: Secure login and registration using JWT (JSON Web Tokens) with refresh token support.
- **RBAC (Role-Based Access Control)**: Granular permission management via Roles.
- **Group Management**: Ability to organize users into logical groups.
- **Security Hardening**:
    - Password hashing (Bcrypt).
    - Rate limiting to prevent Brute Force/DoS attacks.
    - CORS configuration for secure cross-origin requests.
- **Observability**: Integrated Prometheus metrics for monitoring.
- **Database Migrations**: Versioned schema evolution using Flyway.

## 🛠 Tech Stack

- **Backend**: Java 17, Spring Boot 3
- **Build Tool**: Gradle
- **Database**: PostgreSQL (via JPA/Hibernate)
- **Security**: Spring Security, JWT
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **Monitoring**: Prometheus
- **Infrastructure**: Docker, Docker Compose

## 🏁 Getting Started

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Gradle (optional, as `gradlew` is provided)

### Local Development
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd user-service
   ```
2. Start the database and dependencies:
   ```bash
   docker-compose up -d
   ```
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

### Docker Deployment
Build and run the container:
```bash
docker build -t user-service .
docker run -p 8080:8080 user-service
```

## 📖 API Documentation

Once the application is running, you can access the interactive API documentation via Swagger UI:

`http://localhost:8080/swagger-ui.html`

## 📂 Project Structure

```text
src/main/java/com/aissek/userservice/
├── adapter/
│   ├── in/web/             # REST Controllers, DTOs, and Web Mappers
│   └── out/                # Persistence adapters and Security implementations
├── domain/
│   ├── model/              # Pure business entities (POJOs)
│   ├── service/            # Business logic implementation
│   └── port/
│       ├── in/             # Use Case interfaces (Input Ports)
│       └── out/            # Repository/External Service interfaces (Output Ports)
└── config/                 # Framework-specific configurations
```

## 🧪 Testing

The project maintains high code quality through a tiered testing strategy:
- **Unit Tests**: Domain logic and mappers.
- **Integration Tests**: Persistence adapters and API endpoints.
- **Security Tests**: Authentication and authorization flow verification.

Run tests using:
```bash
./gradlew test
```
