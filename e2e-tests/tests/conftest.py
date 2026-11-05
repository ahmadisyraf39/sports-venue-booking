import os
import uuid

import pytest
from playwright.sync_api import Playwright, APIRequestContext

# Override in CI or for another environment: $env:GATEWAY_URL="http://..."
GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")


@pytest.fixture(scope="session")
def anon_api(playwright: Playwright):
    """Client WITHOUT a token: for public paths and negative tests."""
    context = playwright.request.new_context(base_url=GATEWAY_URL)
    yield context
    context.dispose()


@pytest.fixture(scope="session")
def test_user(anon_api: APIRequestContext) -> dict:
    """Register a brand-new user once per test run."""
    email = f"e2e-{uuid.uuid4().hex[:8]}@example.com"
    password = "Passw0rd!123"

    response = anon_api.post("/api/auth/register", data={
        "email": email,
        "password": password,
        "name": "E2E Tester",
    })
    assert response.status == 201, f"register failed: {response.status} {response.text()}"

    return {**response.json(), "password": password}


@pytest.fixture(scope="session")
def token(anon_api: APIRequestContext, test_user: dict) -> str:
    response = anon_api.post("/api/auth/login", data={
        "email": test_user["email"],
        "password": test_user["password"],
    })
    assert response.status == 200, f"login failed: {response.status} {response.text()}"

    body = response.json()
    token = body.get("token")
    assert token, f"no token in response: {body}"
    return token


@pytest.fixture(scope="session")
def api(playwright: Playwright, token: str):
    """Client WITH the JWT on every request."""
    context = playwright.request.new_context(
        base_url=GATEWAY_URL,
        extra_http_headers={"Authorization": f"Bearer {token}"},
    )
    yield context
    context.dispose()