import uuid

import pytest
from playwright.sync_api import Playwright, APIRequestContext
from helpers import GATEWAY_URL

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

@pytest.fixture
def venue(api: APIRequestContext, test_user: dict):
    """A fresh venue owned by the test user, deleted afterwards."""
    payload = {
        "name": f"E2E Arena {uuid.uuid4().hex[:8]}",
        "address": "1 Jalan Ujian, Kajang",
        "description": "Created by the e2e test suite",
        "openingTime": "08:00:00",
        "closingTime": "23:00:00",
        "ownerId": test_user["id"],
    }
    response = api.post("/api/venues", data=payload)
    assert response.status == 201, f"create venue failed: {response.status} {response.text()}"
    created = response.json()

    yield {**created, "payload": payload}

    api.delete(f"/api/venues/{created['id']}", params={"ownerId": test_user["id"]})


@pytest.fixture
def court(api: APIRequestContext, venue: dict, test_user: dict):
    """A badminton court in the fresh venue, deleted before the venue."""
    payload = {
        "name": "Court A",
        "sportType": "BADMINTON",
        "hourlyPrice": 25.00,
        "ownerId": test_user["id"],
    }
    response = api.post(f"/api/venues/{venue['id']}/courts", data=payload)
    assert response.status == 201, f"create court failed: {response.status} {response.text()}"
    created = response.json()

    yield {**created, "payload": payload}

    api.delete(f"/api/courts/{created['id']}", params={"ownerId": test_user["id"]})