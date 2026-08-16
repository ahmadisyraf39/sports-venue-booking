# Venue Service

Manages static venue and court information (no booking, availability, or time-slot logic — that belongs to booking-service) for the Sports Venue Booking Platform.

## Endpoints

Public:
- `GET /api/venues` — list/search venues (optional `?search=` query param matches by name)
- `GET /api/venues/{id}` — get one venue
- `GET /api/venues/{id}/courts` — list courts for a venue

Admin (no auth yet — `ownerId` is passed in the request body, or as a query param for deletes, and is checked against the venue's owner):
- `POST /api/venues` — create venue
- `PUT /api/venues/{id}` — update venue
- `DELETE /api/venues/{id}?ownerId=` — delete venue
- `POST /api/venues/{id}/courts` — add court to venue
- `PUT /api/courts/{id}` — update court
- `DELETE /api/courts/{id}?ownerId=` — delete court

## Running locally
Requires PostgreSQL running (see docker-compose.yml at repo root, or run manually via Docker).

## AI-Assisted Development

This service's initial scaffold (entity, repository, DTOs, service, controller,
exception handling, Dockerfile, CI pipeline) was generated using Claude Code,
after the same layered architecture was built manually and understood in
`user-service`. The prompt specified exact package conventions, entity fields,
endpoint structure, and deliberately scoped out authentication (deferred to
Gateway-level JWT validation).

<details>
<summary>Prompt used</summary>

\`\`\`
Generate a new Spring Boot service in the venue-service folder, following the
exact same patterns as user-service in this repo: same package structure
conventions (com.ahmadisyraf39.sportsbooking.venue_service), same layered
architecture (entity → repository → dto → service → controller → exception),
same testing approach (Testcontainers for repository tests, Mockito for service
tests, WebMvcTest for controller tests), same Dockerfile pattern, same CI
workflow pattern (copy user-service-ci.yml, adapt paths/names to venue-service).

Entities needed:
- Venue: id, name, address, description, openingTime (LocalTime),
  closingTime (LocalTime), ownerId (Long, references a User from user-service
  by ID only — no cross-service JPA relationship)
- Court: id, venue (many-to-one relationship to Venue), name, sportType
  (String or enum — your choice, keep it simple), hourlyPrice (BigDecimal)

Scope notes:
- Venue Service only manages static venue/court info — no booking,
  availability, or time-slot logic. That belongs to booking-service.
- No authentication/JWT needed directly in this service for now — assume
  the caller's identity (ownerId) is passed in as part of the request for
  admin operations, no security filter chain needed yet.

Endpoints needed:
- Public (no auth):
    - GET /api/venues - list/search all venues
    - GET /api/venues/{id} - get one venue
    - GET /api/venues/{id}/courts - list courts for a venue
- Admin (assume ownerId passed in request body/header for now, no real auth):
    - POST /api/venues - create venue
    - PUT /api/venues/{id} - update venue (only if request's ownerId matches)
    - DELETE /api/venues/{id} - delete venue (only if request's ownerId matches)
    - POST /api/venues/{id}/courts - add court to venue
    - PUT /api/courts/{id} - update court
    - DELETE /api/courts/{id} - delete court

Use Java 25, Spring Boot 4.1.0, Maven, matching pom.xml structure to
user-service (minus the security/JWT dependencies, since none needed here).

Do not run any git commands (no add, commit, push, or PR) — stop after
generating so I can review everything first.
\`\`\`
</details>

### Review notes

- **Verified correctness**: entities, repositories, services, controllers, and
  tests all correctly matched the requested spec and followed `user-service`'s
  conventions (Lombok patterns, layered architecture, ownership-check placement,
  REST nesting for collection vs. single-resource operations).
- **Confirmed a real Jackson version discrepancy**: `user-service`'s tests
  compile only because `jjwt-jackson` accidentally pulls in classic Jackson 2
  as a side effect of the JWT dependency. `venue-service` has no JWT dependency,
  so Claude Code correctly added an explicit `spring-boot-starter-json`
  dependency and used Jackson 3's `tools.jackson.*` namespace instead. This
  surfaced a hidden, undocumented dependency in `user-service` worth addressing
  separately.
- **Investigated lazy-loading behavior**: `Court.venue` uses `FetchType.LAZY`,
  and the DTO mapper accesses `court.getVenue().getId()` outside an explicit
  `@Transactional` boundary. Verified via manual testing (create venue → create
  court → list courts) that this works correctly due to Spring's
  `open-in-view=true` default keeping the Hibernate session open for the full
  request lifecycle — not a bug, but a good example of implicit framework
  behavior worth understanding rather than assuming.
- **Verified CI/CD consistency**: confirmed `venue-service-ci.yml` correctly
  matches `user-service-ci.yml`'s fixes (executable `mvnw` permission,
  `setup-java@v5`), and confirmed both correctly rely on the repository's
  "Read and write permissions" setting for GHCR push access, rather than
  requiring per-workflow permission blocks.
- All 33 generated tests pass locally (`./mvnw clean install`), including
  Testcontainers-based repository tests. Manually verified the full
  create-venue → create-court → list-courts flow via Postman.