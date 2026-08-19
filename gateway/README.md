# API Gateway

Single entry point for the Sports Venue Booking Platform — routes external
requests to the appropriate backend service.

## Architecture notes

- **Spring Cloud Gateway (reactive/WebFlux)** — architecturally different
  from every other service in this repo, which use Spring MVC. No entities,
  no database, no `@RestController` classes; routing is configured
  declaratively in `application.yaml`.
- **Routing** — direct URL-based routing to each service (no service
  discovery/Eureka yet — a known simplification, matching the pattern
  already used for cross-service REST calls elsewhere in this project):

| Path | Routes to |
|---|---|
| `/api/auth/**` | user-service (8081) |
| `/api/venues/**` | venue-service (8082) |
| `/api/bookings/**` | booking-service (8083) |
| `/api/payments/**` | payment-service (8084) |
| `/api/notifications/**` | notification-service (8085) |

- **JWT validation**: not yet implemented at the Gateway — deferred to a
  dedicated follow-up branch. Right now, routing is purely path-based;
  individual services either have no auth enforcement or (in user-service's
  case) their own JWT filter.

## Testing approach — a deliberate trade-off

Routing correctness is tested by verifying **route configuration** (that
each route's predicate and target URI load correctly from `application.yaml`
via `RouteLocator`), rather than full end-to-end request forwarding through
a live or mocked downstream service. This is a lighter-weight test than
using WireMock or running real services in CI — it doesn't verify the full
request/response cycle, but it does catch real misconfiguration, which is
exactly what it caught during development (see below).

## Real bug found and fixed during review

The initial route for user-service was configured as `Path=/api/users/**`,
matching an incorrect assumption about the endpoint structure. User Service's
actual auth endpoints are mapped to `/api/auth/**`, not `/api/users/**` —
this meant login/register requests through the Gateway would have returned
404, despite the route "existing." Caught by cross-checking the generated
config against user-service's actual `AuthController` mapping, not by the
automated test (which was written against the same incorrect assumption).
Fixed, then verified live: a real login request through
`http://localhost:8080/api/auth/login` correctly returned a JWT, and
`/actuator/gateway/routes` confirmed the corrected predicate.

## Running locally

Requires all 5 backend services running (see each service's own README):

```bash
./mvnw spring-boot:run
```

Gateway runs on port 8080. Verify routes are loaded correctly:
GET http://localhost:8080/actuator/gateway/routes


## AI-Assisted Development

Scaffolded using Claude Code. Notably, Claude Code caught and corrected two
real issues beyond the literal prompt: an incorrect route predicate (see
above — though the exact fix required a second, manual cross-check against
the actual controller mapping, since the automated test shared the same
wrong assumption), and a version-specific technical detail (declarative
YAML routes vs. an imperative `RouteLocatorBuilder` approach that has known
issues with this Spring Boot 4.1/Spring Cloud Gateway 5.0.2 combination).