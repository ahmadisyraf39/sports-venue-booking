# Sports Venue Booking Platform

Microservices-based sports venue booking platform (Java/Spring Boot) with
event-driven Saga pattern, Docker, and CI/CD.

**Status:** In Progress (Aug 2026–Present)

## Architecture

6 independently deployable Spring Boot microservices, plus an API Gateway,
communicating via REST (synchronous) and RabbitMQ (asynchronous events for
the booking saga).

| Service | Responsibility | Database | Status |
|---|---|---|---|
| User Service | Auth (JWT), user profiles, RBAC | PostgreSQL | ✅ Complete |
| Venue Service | Venue/court management, browse/search | PostgreSQL | ✅ Complete |
| Booking Service | Booking lifecycle, Saga orchestration, Redis slot-locking | MongoDB | 🔲 Not started |
| Payment Service | Payment processing (event-driven) | PostgreSQL | 🔲 Not started |
| Notification Service | Booking/payment notifications (event consumer) | — | 🔲 Not started |
| API Gateway | Routing, JWT validation | — | 🔲 Not started |

## Tech Stack

- Java 25, Spring Boot 4.1.0, Maven
- PostgreSQL, MongoDB, Redis, RabbitMQ
- Docker (multi-stage builds), GitHub Actions CI/CD
- Testcontainers (repository integration tests), JUnit 5, Mockito, AssertJ
- GitHub Container Registry (image publishing)

## Getting Started

Each service currently runs independently. Docker Compose orchestration
(all services together) is planned once all 6 services are built.

**Prerequisites:** Docker, Java 25, Maven

Per-service setup (example: user-service):
\`\`\`bash
cd user-service
docker run --name postgres-dev -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userdb -p 5432:5432 -d postgres:16
./mvnw spring-boot:run
\`\`\`
See each service's own README for its specific endpoints and setup notes.

## Key Design Decisions

- **Polyglot persistence**: PostgreSQL for relational data (users, venues,
  payments), MongoDB for flexible/nested booking documents
- **JWT authentication**: issued by User Service; validation to be handled
  at the Gateway layer (not yet built) rather than duplicated in every service
- **Event-driven Saga**: Booking Service orchestrates the booking → payment →
  confirmation flow via RabbitMQ events (planned)
- **Redis-based slot locking**: prevents double-booking race conditions
  (planned, in Booking Service)

## Demo

- Postman collection: [`/postman/collection.json`](./postman)

## AI-Assisted Development

Later services in this repo are being scaffolded using Claude Code, after
establishing the architecture pattern manually in User Service (entities,
JWT auth, Docker, CI/CD — all built and debugged by hand first). Each
AI-generated service is thoroughly reviewed before merging — see individual
service READMEs (e.g. [`venue-service/README.md`](./venue-service)) for the
specific prompts used and documented review findings.

## Roadmap

- [x] User Service — entities, JWT auth, Docker, CI/CD
- [x] Venue Service — CRUD, Docker, CI/CD (Claude Code-assisted, reviewed)
- [ ] Booking Service — Saga pattern, Redis locking, MongoDB
- [ ] Payment Service — event-driven payment processing
- [ ] Notification Service — event consumer
- [ ] API Gateway — routing, JWT validation
- [ ] Docker Compose — full system orchestration
- [ ] Observability stack (Prometheus, Grafana, Zipkin)
- [ ] Scaling demonstration (horizontal scaling + load test)
- [ ] Recommendation Service (stretch goal)
- [ ] Kubernetes deployment (stretch goal)

## License