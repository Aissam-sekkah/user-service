# Getting Started with Spring boot - Gradle -JPA

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.5/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.3.5/gradle-plugin/packaging-oci-image.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.3.5/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Web](https://docs.spring.io/spring-boot/3.3.5/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

# HEXAGONALE ARCHITECTURE

```text
user-service/
|_______domain/
|       |____model/
|       |       |____user.java
|       |       |____group.java
|       |       |____role.java
|       |____Port/
|       |       |____in/
|       |       |     |_____UserUseCase.java
|       |       |     |_____GroupUseCase.java
|       |       |     |_____RoleUseCase.java
|       |       |____out/
|       |       |     |_____UserRepositoryPort.java
|       |       |     |_____GroupRepositoryPort.java
|       |       |     |_____RoleRepositoryPort.java
|       |       |     |_____TokenServicePort.java
|       |____service/
|               |____UserDomainService.java
|               |____GroupDomainService.java
|               |____RoleDomainService.java
|
|______application/
|        |______UserServiceApplication.java
|
|______adapter/
|       |_______in/
|       |       |____web/
|       |       |       |_______ UserController.java
|       |       |       |_______ GroupController.java
|       |       |       |_______ RoleController.java
|       |       |       |_______ dto/
|       |       |       |         |_____UserRequest.java
|       |       |       |         |_____UserResponse.java
|       |       |       |         |_____GroupRequest.java
|       |       |       |         |_____GroupResponse.java
|       |       |       |         |_____RoleRequest.java
|       |       |       |         |_____RoleResponse.java
|       |       |       |_______ mapper/
|       |       |       |         |UserWebMapper.java
|       |       |       |         |GroupWebMapper.java
|       |       |       |         |RoleWebMapper.java
|       |_______out/
|       |       |____persistence/
|       |       |       |_______ UserPersistenceAdapter.java
|       |       |       |_______ GroupPersistenceAdapter.java
|       |       |       |_______ RolePersistenceAdapter.java
|       |       |       |_______ entity/
|       |       |       |         |_____UserEntity.java
|       |       |       |         |_____GroupEntity.java
|       |       |       |         |_____RoleEntity.java
|       |       |       |_______ repository/
|       |       |       |         |_____UserJpaRepository.java
|       |       |       |         |_____GroupJpaRepository.java
|       |       |       |         |_____RoleJpaRepository.java
|       |       |       |_______ mapper/
|       |       |       |         |_____UserPersistenceMapper.java
|       |       |       |         |_____GroupPersistenceMapper.java
|       |       |       |         |_____RolePersistenceMapper.java
|
|______config/
|       |_______BeanConfig.java
|       |_______SwaggerConfig.java

```

## 1. Domaine - le coeur métier

Aucune dépendance vers Spring, JPA, ou quoi que ce soit externe.
C'est du JAVA PUR.

## Run the application locally

Prerequisites:

- Java 21
- Docker and Docker Compose

Steps:

1. Start PostgreSQL from the project root:

```bash
docker compose up -d
```

2. Check that the database container is running on port `5432`.

The application is configured to use:

- database: `userdb`
- username: `postgres`
- password: `secret`

3. Start the application from the project root:

```bash
./gradlew bootRun
```

4. Wait until Spring Boot finishes starting.

The application will be available at:

```text
http://localhost:8080
```

Main API base path:

```text
http://localhost:8080/api/v1/users
```

5. Stop the application with `Ctrl+C` when finished.

6. Stop the database when you no longer need it:

```bash
docker compose down
```

## Run tests

Unit and integration tests:

```bash
./gradlew test
```

Run only the controller integration tests:

```bash
./gradlew test --tests com.aissek.userservice.adapter.in.web.UserControllerTest
```

Run only the domain service tests:

```bash
./gradlew test --tests com.aissek.userservice.domain.service.UserDomainServiceTest
```

Notes:

- Integration tests now use an **H2 In-Memory Database** via the `test` profile.
- Docker is NOT required to run tests.
- Test reports are generated in `build/reports/tests/test/index.html`.

## API Testing with Swagger

The application includes an interactive API documentation page.

1. Start the application.
2. Visit: `http://localhost:8080/swagger-ui/index.html`

You can explore all endpoints, test requests, and see the required JSON formats directly from the browser.

## Exposed endpoints

### Users API (Base URL: http://localhost:8080/api/v1/users)

- `POST /api/v1/users` creates a new user
- `GET /api/v1/users/{id}` returns one user by id
- `GET /api/v1/users` returns all users
- `PUT /api/v1/users/{id}` updates an existing user
- `DELETE /api/v1/users/{id}` deletes a user

### Groups API (Base URL: http://localhost:8080/api/v1/groups)

- `POST /api/v1/groups` creates a new group
- `GET /api/v1/groups` returns all groups
- `GET /api/v1/groups/{id}` returns one group by id
- `DELETE /api/v1/groups/{id}` deletes a group

### Roles API (Base URL: http://localhost:8080/api/v1/roles)

- `POST /api/v1/roles` creates a new role
- `GET /api/v1/roles` returns all roles
- `PUT /api/v1/roles/user/{userId}/{roleId}` assigns a role to a specific user
- `PUT /api/v1/roles/group/{groupId}/{roleId}` assigns a role to a group (all group members inherit this role)
- `DELETE /api/v1/roles/user/{userId}/{roleId}` removes a direct role assignment
- `DELETE /api/v1/roles/group/{groupId}/{roleId}` removes a role from a group

### Examples for Users

1. Create a user:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Ali","email":"ali@example.com","password":"password123"}'
```

2. Get all users:
```bash
curl http://localhost:8080/api/v1/users
```

## Role Management & Authorization (RBAC)

The system implements a professional Role-Based Access Control (RBAC) model:

### The Hierarchy
`User` $\to$ `Group` $\to$ `Role`

1. **Direct Assignment**: A Role can be assigned directly to a User.
2. **Group Assignment**: A Role can be assigned to a Group.
3. **Inheritance**: Any User who is a member of a Group automatically inherits all Roles assigned to that Group.

### Effective Roles
The system calculates **Effective Roles** as the union of:
$$\text{Effective Roles} = (\text{Directly Assigned Roles}) \cup (\text{Roles of all Groups the User belongs to})$$

### Token-Based Security
The `Effective Roles` are calculated at login and embedded into the JWT token under the `roles` claim. This allows other microservices to verify permissions statelessly without querying the user-service.

## Request flow in port and adapter architecture

This codebase follows a classic hexagonal architecture:

- incoming adapters receive requests from the outside world
- the domain owns business rules and use cases
- outgoing adapters implement technical access to external systems

The critical rule is simple:

- the domain does not depend on Spring MVC
- the domain does not depend on JPA
- the domain does not depend on PostgreSQL
- infrastructure depends on the domain, never the opposite

### Main components and responsibilities

`UserController`, `GroupController` & `RoleController`

- entry point for HTTP traffic
- reads request data from JSON and path variables
- calls the inbound ports `UserUseCase` / `GroupUseCase` / `RoleUseCase`
- maps the domain model to the HTTP response DTO
- contains no business rules

`UserUseCase`, `GroupUseCase` & `RoleUseCase`

- inbound ports
- define what the application exposes to external clients
- are the contracts that the web adapter is allowed to call

`UserDomainService`, `GroupDomainService` & `RoleDomainService`

- implementation of the inbound ports
- contain business logic and orchestration rules
- decide when to create, read, update or delete entities
- raise business exceptions

`UserRepositoryPort`, `GroupRepositoryPort` & `RoleRepositoryPort`

- outbound ports
- define what the domain needs from persistence
- hide JPA and PostgreSQL details from the domain

`UserPersistenceAdapter`, `GroupPersistenceAdapter` & `RolePersistenceAdapter`

- implementation of the outbound ports
- translate domain operations into repository calls
- delegate to Spring Data JPA

`UserJpaRepository`, `GroupJpaRepository` & `RoleJpaRepository`

- technical persistence interfaces
- execute SQL through JPA/Hibernate

`UserWebMapper`, `GroupWebMapper`, `RoleWebMapper`, `UserPersistenceMapper`, `GroupPersistenceMapper`, `RolePersistenceMapper`

- isolate transformations between layers
- prevent HTTP DTOs or JPA entities from leaking into the domain model

`GlobalExceptionHandler`

- translates domain and validation exceptions into HTTP responses
- keeps controller methods simple
- centralizes API error semantics such as `400`, `404`, and `409` using `ProblemDetail`

### Detailed request flow for POST /api/v1/users

1. The client sends an HTTP request to `POST /api/v1/users` with a JSON body.
2. Spring MVC routes the request to `UserController.create(...)`.
3. Spring deserializes the JSON body into `UserRequest`.
4. `UserController` calls: `userUseCase.createUser(request.name(), request.email(), request.password(), null)`.
5. `UserDomainService` applies business rules (e.g., email uniqueness).
6. The domain service persists the aggregate through the outbound port: `userRepository.save(user)`.
7. `UserPersistenceAdapter` converts the domain object into `UserEntity` using `UserPersistenceMapper`.
8. `UserJpaRepository` persists the entity through JPA/Hibernate into PostgreSQL.
9. The saved entity is mapped back to the domain model and returned.
10. `UserController` maps the domain `User` into `UserResponse` using `UserWebMapper`.
11. Spring serializes `UserResponse` into JSON and returns `201 Created`.

### Why this architecture matters

For juniors, the most important operational rule is:

- never push web concerns into the domain
- never push persistence concerns into the domain

Concrete consequences:

- `UserController` should not contain business rules
- `UserDomainService` should not manipulate `ResponseEntity`
- the domain should not know `UserEntity`
- JPA repositories should not be called directly from controllers

This separation gives real engineering benefits:

- business logic stays testable without Spring
- infrastructure can change with limited impact
- controllers remain thin and predictable
- persistence code remains isolated
- exception handling becomes consistent and centralized

### How to read the codebase efficiently

When onboarding, follow this order:

1. Start from `UserController`, `GroupController` and `RoleController` to understand the public API.
2. Open `UserUseCase`, `GroupUseCase` and `RoleUseCase` to see the application contract.
3. Read `UserDomainService`, `GroupDomainService` and `RoleDomainService` to understand business rules.
4. Open `UserRepositoryPort`, `GroupRepositoryPort` and `RoleRepositoryPort` to see what the domain expects from persistence.
5. Read `UserPersistenceAdapter`, `GroupPersistenceAdapter` and `RolePersistenceAdapter` to understand how data is stored.
6. Read the mappers last to understand object translation between layers.

If you follow this order, the separation of concerns becomes much easier to reason about than reading the project package by package.
