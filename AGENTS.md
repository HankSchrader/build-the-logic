# Project Agent Instructions

## Coding Standards
- Target Java 21+ and Spring Boot conventions.
- Prefer constructor injection, Lombok for boilerplate reduction, and immutable DTOs where practical.
- Keep controllers thin; place business logic in services.
- Validate all inbound request DTOs with Jakarta Validation annotations.
- Write focused tests for controller and service behavior when adding features.

## Architecture Rules
- Follow the package structure: `controller`, `service`, `repository`, `model`, `dto`, `config`, and `exception`.
- Controllers may depend only on DTOs and services.
- Services coordinate repositories, mappers, and document generation.
- Repositories should expose only persistence concerns.
- JPA entities must stay in `model` and should not be returned directly from controllers.

## Naming Conventions
- Suffix REST entry points with `Controller`, business classes with `Service`, repositories with `Repository`, DTOs with `Request`/`Response`, and persistence classes with descriptive entity names.
- Use `camelCase` for fields/methods, `PascalCase` for classes/enums, and uppercase snake case for enum constants.
- Name endpoints with plural nouns such as `/api/worksheets`.
