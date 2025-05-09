# Car REST Service

<p>
  <a href="https://github.com/AndPolovynko/car-rest-service/actions">
    <img src="https://img.shields.io/badge/GitHub%20CI-passed-green" alt="Github Actions">
  </a>
  <img src="https://img.shields.io/badge/Coverage-99%25-green" alt="Test Coverage">
  <a href="https://github.com/AndPolovynko/car-rest-service/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License%20-%20MIT%20-green" alt="MIT License">
  </a>
</p>

**Car REST Service** is a Spring Boot–based application that exposes a RESTful API for managing car related information. It provides endpoints for creating, retrieving, updating, and deleting car records. This project follows REST architectural principles and communicates using JSON over HTTP.

## Features

Usage:
- **CRUD Operations:** Create, read, update, and delete car, category, and manufacturer records.
- **Filtering:** Retrieve cars, categories, and manufacturers using specified parameters.

Technical details:
- **Database Migration:** Automated schema management with Flyway.
- **Docker Support:** Easy setup via Docker Compose.
- **Security:** Configured OAuth2‑based authorization with Keycloak to secure endpoints.
- **Testing:** Implemented unit and integration tests to achieve 95% code coverage, and used Postman to verify correct behavior.
- **CI:** Implemented continuous integration with build, test, coverage, and Postman test steps to streamline feature development.

## Technologies Used

**Base application:**
- Java 17+
- Maven
- Spring (Core, Boot, Data, MVC)
- MapStruct
- Lombok
- PostgreSQL
- Flyway
- Swagger
- Docker

**Security:**
- Spring Security
- Keycloak

**Testing:**
- JUnit
- Mockito
- Hamcrest
- JaCoCo
- H2 Database
- REST Assured
- Testcontainers
- Postman

## Getting Started

### [[RECOMENDED]] Running using Docker Compose

```bash
git clone https://github.com/AndPolovynko/car-rest-service.git
cd car-rest-service
docker-compose up
```

API documentation will be available at [http://localhost:8080/docs](http://localhost:8080/docs)

### Running Locally with H2 database

```bash
git clone https://github.com/AndPolovynko/car-rest-service.git
cd car-rest-service
```

1. Ensure `spring.profiles.active=deploy-local` is set in `src/main/resources/application.properties`

2. Configure and run the Keycloak authorization server, and replace `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` in `src/main/resources/application.properties` with the appropriate URI.

3. Build and run the application:
```bash
mvn clean package
mvn spring-boot:run
```

API documentation will be available at [http://localhost:8080/docs](http://localhost:8080/docs)

### Running Locally with your PostgreSQL database

```bash
git clone https://github.com/AndPolovynko/car-rest-service.git
cd car-rest-service
```

1. Create a PostgreSQL database and user.

2. Ensure `spring.profiles.active=default` is set in `src/main/resources/application.properties`

3. Update the properties in `src/main/resources/application-default.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your-db-name
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

4. Configure and run the Keycloak authorization server, and replace `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` in `src/main/resources/application.properties` with the appropriate URI.

5. Build and run the application:
```bash
mvn clean package
mvn spring-boot:run
```

API documentation will be available at [http://localhost:8080/docs](http://localhost:8080/docs)

## Database & Migrations

- PostgreSQL is used as the database.
- Flyway handles schema versioning.
- Migration scripts are located in: `src/main/resources/db/migration`.

## Testing & CI

Run integration tests locally with:
```bash
mvn test jacoco:report
```
Results are available in the `target/site/jacoco` folder.

Run Postman tests locally with:
```bash
docker compose -f postman-docker-compose.yml up
```

Both steps are configured in the GitHub CI/CD workflow.
