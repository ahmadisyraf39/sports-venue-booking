# End-to-End Tests

API-level end-to-end tests for the Sports Venue Booking Platform, written
with [Playwright](https://playwright.dev/python/) (Python) and pytest.

The suite only talks to the **API Gateway** over HTTP, exactly like a real
client. It knows nothing about the Java code - it checks what a client would
actually see: status codes, response bodies, the saga's end state, and the
emails that reach the mail server.

## What it covers

| File                     | Tests                                                                                                                                                                                |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `test_smoke.py`          | Gateway health; every service reachable through the gateway with a JWT                                                                                                               |
| `test_auth.py`           | Login returns a JWT; wrong password 401; duplicate email 409; short password 400; missing / invalid / wrong-scheme tokens 401                                                        |
| `test_venues.py`         | Create, get, partial case-insensitive search, courts under a venue, unknown venue 404, non-owner delete 403, missing name 400                                                        |
| `test_booking_saga.py`   | Booking `PENDING` with correct price -> payment `CONFIRMED` -> booking `CONFIRMED` -> notification `SENT` -> exactly one email in Mailpit; same slot twice 409; invalid bookings 400 |
| `test_double_booking.py` | Simultaneous requests for the same slot (2 and 10) - exactly one wins; overlapping slots with different start times - only one wins; non-overlapping slots - both succeed            |

32 tests in total.

## Running locally

**1. Start the stack** (from the repo root):

```bash
docker compose up -d --build
```

**2. Set up the test environment** (once):

```bash
cd e2e-tests
python -m venv .venv
.venv\Scripts\Activate.ps1        # Windows PowerShell
# source .venv/bin/activate       # macOS / Linux
pip install -r requirements.txt
```

No browser download is needed - these are API tests.

**3. Wait for the services, then run:**

```bash
python wait_for_stack.py
pytest -v
```

`wait_for_stack.py` polls each service through the gateway until it answers,
so the tests don't start while Spring Boot is still booting.

Useful options:

```bash
pytest -v -k saga                      # only tests whose name contains "saga"
pytest -v tests/test_double_booking.py # one file
pytest -x                              # stop at the first failure
```

### Configuration

| Variable        | Default                 | Used for                                           |
| --------------- | ----------------------- | -------------------------------------------------- |
| `GATEWAY_URL`   | `http://localhost:8080` | Every API request                                  |
| `MAILPIT_URL`   | `http://localhost:8025` | Checking that confirmation emails were sent        |
| `STACK_TIMEOUT` | `300`                   | Seconds `wait_for_stack.py` waits before giving up |

The email test expects notification-service to send to **Mailpit**, which is
the default in `docker-compose.yml`. If your `.env` sets `MAIL_HOST` to
Mailtrap, comment it out.

## How it's organised

```
e2e-tests/
├── tests/
│   ├── conftest.py            # fixtures: API clients, user, token, venue, court
│   ├── helpers.py             # wait_until (polling), booking_request, concurrent requests
│   └── test_*.py
├── wait_for_stack.py          # readiness check used locally and in CI
├── pytest.ini
└── requirements.txt
```

**Fixtures** (`conftest.py`) build on each other:

```
anon_api ──► test_user ──► token ──► api ──► venue ──► court
```

- `anon_api` - HTTP client without a token, for public paths and negative tests
- `test_user` - registers a new user with a random email, once per run
- `token` / `api` - logs in once; `api` sends `Authorization: Bearer <jwt>` on every request
- `venue` / `court` - a fresh venue and court for each test that needs one, deleted afterwards

**Asynchronous steps** (payment, booking confirmation, notification, email)
happen after the API call returns, so the tests poll with `wait_until`
instead of sleeping a fixed time.

**Concurrency tests** use Playwright's async API with `asyncio.gather` to
send requests at the same moment, run in a separate thread so they don't
clash with the sync Playwright used by the fixtures.

## In CI

`.github/workflows/e2e-tests.yml` runs on every pull request and on pushes to
`main`: it builds and starts the full Docker Compose stack, runs
`wait_for_stack.py`, runs pytest with a JUnit XML report (uploaded as an
artifact), prints service logs if anything fails, and tears the stack down.
No secrets are needed - emails go to Mailpit.
