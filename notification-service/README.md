# Notification Service

Consumes payment outcome events and notifies users for the Sports Venue
Booking Platform — the final step in the booking → payment → notification
Saga.

## Endpoints

- `GET /api/notifications?userId=` — get a user's notification history

Notifications are not created via a public API — they're created reactively,
only in response to `PaymentConfirmed` or `PaymentFailed` events.

## Architecture notes

- **Database**: PostgreSQL — notification records have a fixed, simple shape
  and don't need booking-service's nested document model.
- **Event consumption**: two `@RabbitListener`s, one per queue —
  `payment.confirmed.queue` and `payment.failed.queue` (both declared
  locally, matching payment-service's exchange/queue/routing keys —
  RabbitMQ declarations are idempotent, so this doesn't conflict, it's just
  each service's own Java representation of shared infrastructure).
- **End of the Saga**: unlike user-service → venue-service → booking-service
  → payment-service, this service doesn't publish any further events; it's
  a terminal consumer.
- **Email**: sent via `spring-boot-starter-mail` (SMTP) using
  [Mailtrap](https://mailtrap.io)'s sandbox for development, configured
  entirely through environment variables (`MAILTRAP_HOST`, `MAILTRAP_PORT`,
  `MAILTRAP_USERNAME`, `MAILTRAP_PASSWORD`) so no real inbox is ever
  targeted outside of production.
- **No `GlobalExceptionHandler` / custom exceptions**: every other service
  in this repo pairs a `GlobalExceptionHandler` with a domain "not found"
  exception for its single-resource lookup endpoint. This service only
  exposes a list-by-`userId` endpoint, and an empty result is a valid
  answer, not an error — so there's no domain exception to map, and adding
  an empty `@RestControllerAdvice` just for the sake of the pattern would be
  scaffolding, not behavior. If a single-notification lookup endpoint is
  added later, this should be revisited.

## Known simplifications

- **Recipient email address**: user-service's real email addresses aren't
  wired through the event chain yet (`PaymentConfirmedEvent`/
  `PaymentFailedEvent` only carry `userId`), so the recipient address is
  derived deterministically as `user{userId}@example.com`. Swapping this
  for a real lookup (e.g. a call to user-service, or including the email in
  the event payload) is the natural next step.
- **No retry/dead-letter handling**: if email sending throws (e.g. bad
  Mailtrap credentials), the exception propagates out of the listener and
  RabbitMQ redelivers the message per its default at-least-once behavior.
  There's no dead-letter queue configured, matching the rest of this repo's
  current scope (no service here has retry/DLQ handling yet).

## Notable finding — a real, caught idempotency bug

During live end-to-end testing, a container restart (the local machine went
to sleep, stopping all running Docker containers) caused RabbitMQ to
redeliver an already-processed `PaymentConfirmed` message once the consumer
came back online. This surfaced as two `NotificationLog` entries for the
same `bookingId`, five seconds apart, with two separate emails sent for the
same booking confirmation — direct, live evidence of RabbitMQ's
at-least-once delivery guarantee, not just a theoretical concern.

This motivated adding an idempotency guard: `processPaymentConfirmed` and
`processPaymentFailed` now check for an existing `NotificationLog` matching
the same `bookingId` and `type` before creating a new record or sending an
email, returning early if one already exists. Covered by a dedicated test
(`shouldNotSendDuplicateNotification_WhenAlreadyProcessed`) simulating a
redelivered message.

## Running locally

Requires PostgreSQL and RabbitMQ running, plus Mailtrap sandbox credentials:
```bash
docker run --name postgres-notification -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=notificationdb -p 5436:5432 -d postgres:16
docker run --name rabbitmq-dev -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management

export MAILTRAP_HOST=sandbox.smtp.mailtrap.io
export MAILTRAP_PORT=2525
export MAILTRAP_USERNAME=your-mailtrap-username
export MAILTRAP_PASSWORD=your-mailtrap-password

./mvnw spring-boot:run
```

## AI-Assisted Development

Scaffolded using Claude Code following conventions established in
user-service, venue-service, booking-service, and payment-service.
