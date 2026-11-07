import pytest
from playwright.sync_api import APIRequestContext

from helpers import booking_request, post_all_at_once


def bookings_on_court(api: APIRequestContext, user: dict, court: dict) -> list[dict]:
    bookings = api.get("/api/bookings", params={"userId": user["id"]}).json()
    return [b for b in bookings if b["courtId"] == court["id"]]


@pytest.mark.parametrize("requests", [2, 10])
def test_simultaneous_same_slot_only_one_wins(
    api: APIRequestContext, token: str, venue: dict, court: dict, test_user: dict, requests: int
):
    payload = booking_request(venue, court, test_user, start="18:00:00", hours=1)

    results = post_all_at_once(token, "/api/bookings", [payload] * requests)
    statuses = sorted(status for status, _ in results)

    assert statuses.count(201) == 1, f"expected exactly one success, got {statuses}: {results}"
    assert statuses.count(409) == requests - 1, f"expected the rest to be 409, got {statuses}: {results}"

    # The database must agree: exactly one booking on this court
    saved = bookings_on_court(api, test_user, court)
    assert len(saved) == 1, f"expected 1 saved booking, found {len(saved)}: {saved}"


def test_simultaneous_overlapping_slots_only_one_wins(
    api: APIRequestContext, token: str, venue: dict, court: dict, test_user: dict
):
    early = booking_request(venue, court, test_user, start="10:00:00", hours=2)   # 10:00-12:00
    late = booking_request(venue, court, test_user, start="11:00:00", hours=1)    # 11:00-12:00

    results = post_all_at_once(token, "/api/bookings", [early, late])
    statuses = sorted(status for status, _ in results)

    assert statuses == [201, 409], f"overlapping bookings: {results}"
    assert len(bookings_on_court(api, test_user, court)) == 1


def test_simultaneous_non_overlapping_slots_both_succeed(
    api: APIRequestContext, token: str, venue: dict, court: dict, test_user: dict
):
    morning = booking_request(venue, court, test_user, start="08:00:00", hours=1)     # 08:00-09:00
    afternoon = booking_request(venue, court, test_user, start="14:00:00", hours=1)   # 14:00-15:00

    results = post_all_at_once(token, "/api/bookings", [morning, afternoon])
    statuses = sorted(status for status, _ in results)

    assert statuses == [201, 201], f"non-overlapping bookings should both succeed: {results}"
    assert len(bookings_on_court(api, test_user, court)) == 2