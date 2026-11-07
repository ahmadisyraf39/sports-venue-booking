"""Wait until every service answers through the gateway, so the e2e tests don't start too early."""
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid

GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")
TIMEOUT_SECONDS = int(os.getenv("STACK_TIMEOUT", "300"))
DEADLINE = time.monotonic() + TIMEOUT_SECONDS


def call(method: str, path: str, body: dict | None = None, token: str | None = None) -> tuple[int | None, str]:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(GATEWAY_URL + path, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(request, timeout=5) as response:
            return response.status, response.read().decode()
    except urllib.error.HTTPError as error:          # got an HTTP answer, just not 2xx
        return error.code, error.read().decode()
    except (urllib.error.URLError, OSError):         # not reachable yet
        return None, ""


def wait_for(description: str, check) -> None:
    while time.monotonic() < DEADLINE:
        if check():
            print(f"ready: {description}", flush=True)
            return
        time.sleep(3)
    sys.exit(f"TIMED OUT after {TIMEOUT_SECONDS}s waiting for {description}")


email = f"ci-ready-{uuid.uuid4().hex[:8]}@example.com"
password = "Passw0rd!123"

wait_for("gateway", lambda: call("GET", "/actuator/health")[0] == 200)
wait_for("user-service (register)", lambda: call(
    "POST", "/api/auth/register", {"email": email, "password": password, "name": "CI readiness"})[0] in (201, 409))

token_holder = {}

def login_works() -> bool:
    status, body = call("POST", "/api/auth/login", {"email": email, "password": password})
    if status == 200:
        token_holder["token"] = json.loads(body)["token"]
        return True
    return False

wait_for("user-service (login)", login_works)

for path in ["/api/venues", "/api/bookings?userId=0", "/api/payments?bookingId=ci", "/api/notifications?userId=0"]:
    wait_for(path, lambda p=path: call("GET", p, token=token_holder["token"])[0] == 200)

print("All services ready.")