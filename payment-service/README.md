# Payment Service

Consumes booking events and processes payments for the Sports Venue Booking
Platform — the second step in the booking → payment → confirmation Saga.

## Endpoints

- `GET /api/payments/{id}` — get a single payment
- `GET /api/payments?bookingId=` — get the payment for a specific booking

Payments are not created via a public API — they're created reactively,
only in response to a `BookingCreated` event.

## Architecture notes

- **Database**: PostgreSQL — payment records need the same consistency
  guarantees as user accounts, and have a fixed, simple shape (unlike
  booking's nested data).
- **Event consumption**: `@RabbitListener` on `booking.created.queue`
  (declared locally, matching booking-service's exchange/queue/routing key —
  RabbitMQ declarations are idempotent, so this doesn't conflict, it's just
  each service's own Java representation of shared infrastructure).
- **Idempotency**: checks for an existing payment by `bookingId` before
  creating a new one, guarding against RabbitMQ's at-least-once delivery
  occasionally redelivering a message.
- **Processing**: simulated/mocked — no real payment gateway integration.
  This was a deliberate scope decision; the focus is the event-driven Saga
  architecture, not third-party payment integration.
- **Event publishing**: on completion, publishes `PaymentConfirmed` or
  `PaymentFailed` to its own `payment.exchange`, ready for booking-service
  (or notification-service) to eventually consume.

## Running locally

Requires PostgreSQL and RabbitMQ running:
```bash
docker run --name postgres-payment -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=paymentdb -p 5435:5432 -d postgres:16
docker run --name rabbitmq-dev -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
./mvnw spring-boot:run
```

## AI-Assisted Development

Scaffolded using Claude Code following conventions established in
user-service, venue-service, and booking-service.

### Review notes

- Claude Code proactively flagged that `BookingCreatedEvent` was missing
  `venueId` despite `Booking` having a real value for it — a genuine gap
  from booking-service's original design, fixed in a dedicated PR before
  this one merged.
- Added an idempotency guard (checking for an existing payment by
  `bookingId` before creating a new one) — a legitimate, defensive addition
  beyond the literal prompt, protecting against RabbitMQ's at-least-once
  delivery semantics.
- Wrapped the create-then-update payment flow in `@Transactional` — correct,
  since the two writes should be atomic from an external observer's
  perspective.
- Verified the full Saga end-to-end: a real booking created via Postman
  triggered automatic payment processing, visible both via the RabbitMQ
  management UI and by querying the resulting Payment record.