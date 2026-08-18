# Booking Service

Handles the core booking lifecycle for the Sports Venue Booking Platform —
validating availability, calculating pricing, preventing double-booking race
conditions, and initiating the payment Saga.

## Endpoints

- `POST /api/bookings` — create a new booking (validates court exists via
  Venue Service, calculates price server-side, checks for slot conflicts,
  publishes a BookingCreated event)
- `GET /api/bookings/{id}` — get a single booking
- `GET /api/bookings?userId=` — list a user's bookings

## Architecture notes

- **Database**: MongoDB (not PostgreSQL, unlike user-service/venue-service) —
  bookings have naturally nested, flexible data that doesn't benefit from a
  rigid relational schema.
- **Cross-service integration**: calls venue-service (`GET /api/courts/{id}`)
  synchronously via `RestClient` to verify the court exists and fetch its
  real hourly price — the client-supplied price is never trusted.
- **Distributed locking**: uses Redis's atomic `SETNX` (`setIfAbsent`) to
  acquire a per-slot lock before checking availability, preventing two
  concurrent requests for the same slot from both passing the conflict check.
  Lock auto-expires after 10 seconds as a safety net; always released via a
  `finally` block regardless of success or failure.
- **Event publishing**: on successful save, publishes a `BookingCreated`
  event to RabbitMQ (`booking.exchange` / `booking.created` routing key),
  the first step of the booking → payment → confirmation Saga.

## Running locally

Requires PostgreSQL (for venue-service), MongoDB, Redis, and RabbitMQ
running (see docker-compose.yml at repo root, or run each manually via
Docker — see commands in each container's setup).

```bash
docker run --name mongo-dev -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin -p 27017:27017 -d mongo:7
docker run --name redis-dev -p 6379:6379 -d redis:7
docker run --name rabbitmq-dev -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

RabbitMQ management UI: `http://localhost:15672` (guest/guest) — useful for
inspecting published events during development.

## Notable debugging findings

- **`spring.data.mongodb.*` is not a valid Spring Boot 4.x property path** —
  the correct path is `spring.mongodb.*` (no `data` segment). This caused
  silent misconfiguration (no startup error, connection just defaulted to
  unauthenticated) until diagnosed with Claude Code's direct file inspection.
- **`spring.data.redis.*` DOES require the `data` segment** — the opposite
  pattern from Mongo, confirmed via Spring's release notes rather than
  assumed from symmetry with the Mongo fix.
- **RabbitMQ connects lazily**, unlike MongoDB's eager connection — no
  connection/queue activity appears at startup or in `rabbitmqctl status`
  until a real publish, a listener, or a health check actually triggers it.
  Verified as correct (not broken) behavior by decompiling `RabbitAdmin`
  and confirming via a real end-to-end booking creation with the message
  inspected in the RabbitMQ management UI.
- **Containerizing surfaced a hardcoded `localhost:8082`** in the
  venue-service `RestClient` config — worked fine when both services ran
  directly on the host machine, broke once booking-service moved into its
  own container (where `localhost` refers to the container itself). Fixed
  by making the base URL environment-variable-driven, same pattern as the
  database host overrides.

## AI-Assisted Development

DTOs and read-only endpoints (`CreateBookingRequest`, `BookingResponse`,
`getBookingById`, `getBookingsByUserId`) were scaffolded using Claude Code,
following conventions established manually in user-service. The core
`createBooking` logic — cross-service integration, server-side pricing,
Redis locking, and RabbitMQ publishing — was designed and implemented
manually, given its architectural significance to the project's Saga pattern.

Claude Code was also used to diagnose the MongoDB configuration bug and the
RabbitMQ lazy-connection behavior described above, via direct file
inspection and a live diagnostic run — faster and more reliable than manual
log-based debugging alone.