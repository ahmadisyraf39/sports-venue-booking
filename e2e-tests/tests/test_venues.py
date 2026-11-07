import uuid

from playwright.sync_api import APIRequestContext


def test_create_venue(api: APIRequestContext, test_user: dict):
    payload = {
        "name": f"E2E Create {uuid.uuid4().hex[:8]}",
        "address": "2 Jalan Ujian, Kajang",
        "openingTime": "09:00:00",
        "closingTime": "22:00:00",
        "ownerId": test_user["id"],
    }
    response = api.post("/api/venues", data=payload)
    assert response.status == 201, f"status {response.status}: {response.text()}"

    body = response.json()
    try:
        assert body["id"], f"no id in response: {body}"
        assert body["name"] == payload["name"]
        assert body["address"] == payload["address"]
        assert body["ownerId"] == test_user["id"]
        assert body["openingTime"].startswith("09:00")
    finally:
        api.delete(f"/api/venues/{body['id']}", params={"ownerId": test_user["id"]})


def test_get_venue_by_id(api: APIRequestContext, venue: dict):
    response = api.get(f"/api/venues/{venue['id']}")
    assert response.status == 200, f"status {response.status}"
    assert response.json()["name"] == venue["payload"]["name"]


def test_search_finds_venue_by_partial_name(api: APIRequestContext, venue: dict):
    unique_part = venue["payload"]["name"].split()[-1].upper()   # e.g. "3F9A1C2B"
    response = api.get("/api/venues", params={"search": unique_part})
    assert response.status == 200, f"status {response.status}"

    ids = [v["id"] for v in response.json()]
    assert ids == [venue["id"]], f"expected only {venue['id']}, got {ids}"


def test_court_listed_under_venue(api: APIRequestContext, venue: dict, court: dict):
    response = api.get(f"/api/venues/{venue['id']}/courts")
    assert response.status == 200, f"status {response.status}"

    courts = response.json()
    assert [c["id"] for c in courts] == [court["id"]]
    assert courts[0]["sportType"] == "BADMINTON"
    assert float(courts[0]["hourlyPrice"]) == 25.0


def test_unknown_venue_returns_404(api: APIRequestContext):
    response = api.get("/api/venues/999999999")
    assert response.status == 404, f"status {response.status}"


def test_delete_by_non_owner_is_forbidden(api: APIRequestContext, venue: dict, test_user: dict):
    response = api.delete(f"/api/venues/{venue['id']}", params={"ownerId": test_user["id"] + 1000})
    assert response.status == 403, f"status {response.status}"

    still_there = api.get(f"/api/venues/{venue['id']}")
    assert still_there.status == 200, "venue was deleted by a non-owner"


def test_create_venue_without_name_returns_400(api: APIRequestContext, test_user: dict):
    response = api.post("/api/venues", data={
        "address": "3 Jalan Ujian",
        "openingTime": "09:00:00",
        "closingTime": "22:00:00",
        "ownerId": test_user["id"],
    })
    assert response.status == 400, f"status {response.status}: {response.text()}"