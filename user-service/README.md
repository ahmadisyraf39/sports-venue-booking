# User Service

Handles user registration, authentication (JWT), and profile management for
the Sports Venue Booking Platform.

## Endpoints

- `POST /api/auth/register` — register a new user
- `POST /api/auth/login` — authenticate and receive a JWT

## Architecture notes

- **Database**: PostgreSQL, via Spring Data JPA/Hibernate.
- **Authentication**: stateless JWT, issued on login/register. Password
  hashing via BCrypt. A custom `JwtAuthFilter` validates tokens on protected
  routes; `/api/auth/**` and `/actuator/**` are public.
- Other services (venue-service, booking-service) currently trust identity
  passed directly in requests rather than validating JWTs themselves — this
  is deferred to the API Gateway (not yet built), which will forward a
  trusted user identity rather than each service duplicating JWT validation.

## Running locally

Requires PostgreSQL running:
```bash
docker run --name postgres-dev -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userdb -p 5432:5432 -d postgres:16
./mvnw spring-boot:run
```

## Notable debugging findings

- Spring Boot 4.x split test starters per-module (e.g.,
  `spring-boot-starter-security-test`, `spring-boot-starter-data-jpa-test`) —
  a genuine change from 3.x's single `spring-boot-starter-test`.
- Lombok's annotation processor requires explicit `maven-compiler-plugin`
  configuration in this Spring Boot 4.1/Maven combination — not automatic
  from the Lombok dependency alone. This needed to be added manually to
  every freshly-generated service's `pom.xml` in this project.
- `@WebMvcTest` in this version auto-configures Spring Security, requiring
  either `@Import` of the real security config or `@MockitoBean` mocks for
  security-related dependencies (`JwtUtil`, `CustomUserDetailsService`) to
  avoid `403`s or missing-bean errors in controller slice tests.

## AI-Assisted Development

This service was built entirely manually — entities, JWT authentication,
Docker, and CI/CD — establishing the architectural and testing patterns
that later services (venue-service, booking-service) were scaffolded from,
either manually following the same pattern or via Claude Code with review.