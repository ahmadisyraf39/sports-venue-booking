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

## Testing approach

Routing correctness is verified with real, isolated HTTP stub servers (using
Java's built-in `com.sun.net.httpserver.HttpServer`, no external mocking
library needed) — one per downstream service, dynamically wired into
Gateway's routes via `@DynamicPropertySource`. Tests send genuine HTTP
requests through a real, running Gateway instance and verify the response
actually originated from the correct stub, via a distinguishing header —
this is true end-to-end routing behavior verification, not just
configuration inspection.

JWT-protected routes are tested by generating a token directly within the
test (signed with the same secret Gateway validates against), rather than
depending on a live User Service instance — keeping the test fast and
self-contained while still exercising Gateway's real JwtValidator logic.
A dedicated test also confirms unauthenticated requests to protected routes
are correctly rejected with 401.

## Real bugs found and fixed

1. Initial route for user-service used an incorrect path predicate
   (/api/users/** instead of /api/auth/**) — caught by cross-checking
   against user-service's actual controller mapping, since the test itself
   shared the same incorrect assumption and didn't catch it independently.
   Fixed in both the route config and the test's own route spec.
2. JWT dependencies were initially placed in <dependencyManagement> instead
   of <dependencies> in pom.xml — meaning they were never actually included
   in the build, despite appearing correctly declared. Caught via
   "cannot resolve symbol" errors on Claims/Keys/Jwts.

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