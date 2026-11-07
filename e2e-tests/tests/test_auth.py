import pytest
from playwright.sync_api import APIRequestContext


def test_login_returns_jwt(token: str):
    # A JWT is three base64 parts separated by dots: header.payload.signature
    assert token.count(".") == 2, f"doesn't look like a JWT: {token[:20]}..."


def test_wrong_password_rejected(anon_api: APIRequestContext, test_user: dict):
    response = anon_api.post("/api/auth/login", data={
        "email": test_user["email"],
        "password": "wrong-password",
    })
    assert response.status == 401, f"status {response.status}"


def test_duplicate_email_rejected(anon_api: APIRequestContext, test_user: dict):
    response = anon_api.post("/api/auth/register", data={
        "email": test_user["email"],
        "password": "Passw0rd!123",
        "name": "Duplicate",
    })
    assert response.status == 409, f"status {response.status}"


def test_short_password_rejected(anon_api: APIRequestContext):
    response = anon_api.post("/api/auth/register", data={
        "email": "short-pw@example.com",
        "password": "short",
        "name": "Short",
    })
    assert response.status == 400, f"status {response.status}"


@pytest.mark.parametrize("auth_header", [
    pytest.param(None, id="no-header"),
    pytest.param("Bearer not-a-real-jwt", id="garbage-token"),
    pytest.param("Basic dXNlcjpwYXNz", id="wrong-scheme"),
])
def test_protected_path_rejects_bad_auth(anon_api: APIRequestContext, auth_header: str | None):
    headers = {"Authorization": auth_header} if auth_header else {}
    response = anon_api.get("/api/venues", headers=headers)
    assert response.status == 401, f"status {response.status}"