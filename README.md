# ReserveIt API

REST API for booking facilities (e.g. tennis courts) with payments and e-mail notifications.
Built with Java 21 and Spring Boot as a portfolio project, with focus on correctness under
concurrency, secure authentication and test coverage.


## Features

- **Authentication and roles**: registration and login, JWT (HS256, 60 min), passwords hashed with BCrypt, roles `USER` and `ADMIN`.
- **Facilities**: admins create facilities with price per hour and opening hours.
- **Reservations**: create, list own reservations (pagination, date filter), cancel.
- **No double booking**: overlapping reservations are rejected, even under concurrent requests (see [Design decisions](#design-decisions)).
- **Payments**: a new reservation is `PENDING` and has 15 minutes to be paid. A payment provider is simulated; the result arrives through a secured webhook.
- **Automatic expiry**: unpaid reservations are cancelled by a scheduled job and the time slot is released.
- **E-mail confirmation**: sent asynchronously after a successful payment.
- **Consistent errors**: all errors are returned as RFC 7807 `ProblemDetail` (400, 401, 403, 404, 409).

## Tech stack

| Area | Tools |
|---|---|
| Language and framework | Java 21, Spring Boot, Spring Web, Spring Security, Spring Data JPA (Hibernate) |
| Database | PostgreSQL 16, Flyway migrations |
| Auth | JWT (jjwt), BCrypt |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |
| Infrastructure | Docker Compose, GitHub Actions (CI), Mailpit (local SMTP) |
| Build | Maven, Lombok |

## Architecture

Layered, packages grouped by feature:

```
Controller  ->  Service  ->  Repository  ->  PostgreSQL
   (DTOs)     (business       (Spring Data
               rules,          JPA)
               transactions)
```

```
pl.kufieta.reserveit_api
├── config/          security, scheduling, async
├── common/          exceptions, global error handler
├── users/           registration, login
├── security/        JWT service and filter
├── facilities/      facilities CRUD
├── reservations/    booking, cancellation, expiry job
├── payments/        payment entity, provider abstraction, webhook
└── notifications/   e-mail sent after confirmation
```

## API overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | create an account |
| POST | `/api/auth/login` | public | get a JWT |
| GET | `/api/facilities` | authenticated | list facilities |
| POST | `/api/facilities` | `ADMIN` | create a facility |
| POST | `/api/reservations` | authenticated | book a facility; returns the reservation and payment info |
| GET | `/api/reservations/my` | authenticated | own reservations (`from`, `to`, `page`, `size`) |
| PATCH | `/api/reservations/{id}/cancel` | owner | cancel a reservation |
| POST | `/api/payments/webhook` | `X-Webhook-Secret` header | payment result from the provider |

Authenticated endpoints expect `Authorization: Bearer <token>`.

### Example flow

```bash
# 1. register and log in
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"jakub@test.pl","password":"Secret123!"}'
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"jakub@test.pl","password":"Secret123!"}'

# 2. book a court (reservation is PENDING, response contains payment info)
curl -X POST localhost:8080/api/reservations \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"facilityId":1,"startTime":"2026-11-20T10:00:00","endTime":"2026-11-20T11:00:00"}'

# 3. simulate the payment provider calling the webhook
curl -X POST localhost:8080/api/payments/webhook \
  -H "X-Webhook-Secret: dev-webhook-secret-change-me" -H "Content-Type: application/json" \
  -d '{"externalId":"fake_<id from step 2>","status":"PAID"}'
```

After step 3 the reservation becomes `CONFIRMED` and a confirmation e-mail appears in Mailpit.

## Running locally

Requirements: JDK 21, Maven, Docker.

```bash
docker compose up -d        # PostgreSQL (port 5433) and Mailpit
mvn spring-boot:run         # API on http://localhost:8080
```

- Mailpit web UI (caught e-mails): http://localhost:8025
- Flyway applies migrations automatically on startup.

### Configuration

| Property | Purpose | Default (dev only) |
|---|---|---|
| `app.jwt.secret` | key used to sign tokens | dev value, **change in production** |
| `app.jwt.expiration-minutes` | token lifetime | `60` |
| `app.payments.webhook-secret` | shared secret for the payment webhook | `dev-webhook-secret-change-me` |
| `app.mail.from` | sender address | `no-reply@reserveit.local` |
| `spring.mail.host` / `spring.mail.port` | SMTP server | `localhost` / `1025` |

Any property can be overridden with an environment variable (e.g. `APP_JWT_SECRET`).

## Tests

```bash
mvn test
```

Docker must be running: integration tests start a real PostgreSQL with Testcontainers.

- **Unit tests** (JUnit 5, Mockito): user registration and login, payment webhook (paid, failed, repeated, unknown payment, expired reservation), reservation expiry.
- **Concurrency test** (Testcontainers): 10 threads book the same slot at the same moment; exactly one succeeds and exactly one reservation is stored.
- **CI**: every push runs the whole suite in GitHub Actions.

## Design decisions

**Preventing double booking.** Checking for overlaps in code is not enough: two requests can pass the check at the same time. Three layers protect the slot:
1. an overlap query (`start < :end AND end > :start`, so back-to-back reservations are allowed),
2. a pessimistic lock on the facility row, which serializes concurrent bookings of the same facility and avoids deadlocks,
3. a PostgreSQL `EXCLUDE USING gist` constraint on the time range, as a last line of defense in the database itself.

**Payment webhook.** The provider calls the API without a user token, so the endpoint is protected by a shared secret header compared in constant time. Handling is idempotent: a repeated webhook is ignored. A `PAID` webhook for a reservation that is no longer pending returns `409`.

**Expiry job.** A `@Scheduled` task runs every minute, cancels `PENDING` reservations past `expiresAt` and marks their payments as `FAILED`. Confirmed reservations are never touched.

**E-mail after commit.** The payment service publishes an event; a listener annotated with `@Async` and `@TransactionalEventListener(AFTER_COMMIT)` sends the mail. The HTTP response is not delayed by SMTP, and no mail is sent for a transaction that rolled back. A mail failure is logged and never undoes the payment.

**Other choices.** Constructor-friendly layering with DTO records, Flyway instead of schema auto-generation, `LAZY` associations with `@EntityGraph` to avoid N+1 queries, bind parameters in all queries (no SQL injection).

## Known limitations and next steps

- The payment provider is simulated (`FakePaymentProvider`); a real one (e.g. Stripe, Przelewy24) would implement the same `PaymentProvider` interface.
- No refunds, and a payment completed after expiry is rejected rather than refunded.
- JWTs are stateless, so a token stays valid until it expires (refresh tokens or a token blacklist would solve this).
- Dockerfile for the application itself and a full `docker compose up --build`.

## Author

Jakub Kufieta, Computer Science student. [GitHub](https://github.com/Kufieta-Jakub>) · [LinkedIn](https://www.linkedin.com/in/kufieta-jakub/>)
