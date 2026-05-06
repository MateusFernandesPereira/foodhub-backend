# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FoodHub Backend is a Maven multi-module monorepo of Spring Boot microservices for a food ordering and delivery platform. Java 17, Spring Boot 3.5.x. Currently only `catalog-service` is implemented; future modules (orders, payments, delivery, notifications, analytics, API Gateway) are planned.

## Commands

All Maven commands should be run from the repo root using the wrapper (`./mvnw`).

```bash
# Build all modules (skip tests)
./mvnw clean package -DskipTests

# Build a specific module
./mvnw clean package -pl catalog-service -DskipTests

# Run all tests (requires Docker for Testcontainers)
./mvnw test

# Run tests for a specific module
./mvnw test -pl catalog-service

# Run a single test class
./mvnw test -pl catalog-service -Dtest=CatalogServiceApplicationTests

# Start the app (after starting infrastructure)
./mvnw spring-boot:run -pl catalog-service

# Start infrastructure (PostgreSQL on 5432, Redis on 6379)
docker compose up -d
```

## Architecture

### Module structure

Each service is a Maven module under the root `pom.xml`. The root POM sets the Spring Boot parent and shared properties; service POMs inherit from it.

### Layered package structure (`com.pereira.<service>`)

```
controller/   — @RestController + @RestControllerAdvice (GlobalExceptionHandler)
service/      — interface + impl/ subpackage for implementation
repository/   — Spring Data JPA repositories
entity/       — JPA entities
dto/
  request/    — Java records with Bean Validation annotations
  response/   — Java records with a static factory method (.from(entity))
```

### Key conventions

- **DTOs are Java records.** Request records carry `@Valid` constraints; response records have a `static ProductResponse from(Entity)` factory — no mapper library.
- **Soft delete via Hibernate annotations.** `@SQLDelete` sets `deleted_at = NOW()` and `@SQLRestriction("deleted_at IS NULL")` filters all queries automatically. Never hard-delete entities that use this pattern.
- **DB schema managed by Flyway.** Migration scripts live in `src/main/resources/db/migration/` and follow the `V<n>__description.sql` naming convention. `ddl-auto` is set to `validate`, so schema changes require a new migration file.
- **Tests use Testcontainers.** `TestcontainersConfiguration` spins up a real PostgreSQL container via `@ServiceConnection`; no mocked DB in integration tests.

### Infrastructure (docker-compose.yml)

| Service  | Image              | Port  |
|----------|--------------------|-------|
| postgres | postgres:16-alpine | 5432  |
| redis    | redis:7-alpine     | 6379  |

`catalog-service` connects to `localhost:5432/catalog_db` (user: `catalog_user`, pass: `catalog_pass`). The service runs on port **8081**.

### REST API base path

`/api/v1/products` — full CRUD (POST, GET, GET /{id}, PUT /{id}, DELETE /{id}).
