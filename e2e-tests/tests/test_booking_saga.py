from datetime import date, timedelta

import pytest
from playwright.sync_api import APIRequestContext

from helpers import wait_until

BOOKING_DATE = (date.today() + timedelta(days=7)).isoformat()   # e.g. "2026-11-12"


def booking_request(venue: dict, court: dict, user: dict, start: str = "10:00:00", hours: int = 2) -> dict:
    return {
        "courtId": court["id"],
        "venueId": venue["id"],
        "userId": user["id"],
        "bookingDate": BOOKING_DATE,
        "startTime": start,
        "durationHours": hours,
    }


@pytest.fixture
def booking(api: APIRequestContext, venue: dict, court: dict, test_user: dict) -> dict:
    """Create one booking on a fresh court and return the response body."""
    response = api.post("/api/bookings", data=booking_request(venue, court, test_user))
    assert response.status == 201, f"create booking failed: {response.status} {response.text()}"
    return response.json()


def test_create_booking_returns_pending_with_price(booking: dict, court: dict):
    assert booking["id"], f"no id: {booking}"
    assert booking["status"] == "PENDING"
    assert booking["endTime"].startswith("12:00")            # 10:00 + 2 hours
    assert float(booking["totalPrice"]) == 2 * 25.0          # 2 hours x hourlyPrice


def test_payment_is_confirmed(api: APIRequestContext, booking: dict):
    def confirmed_payments():
        payments = api.get("/api/payments", params={"bookingId": booking["id"]}).json()
        return [p for p in payments if p["status"] == "CONFIRMED"]

    payments = wait_until(confirmed_payments, description=f"CONFIRMED payment for booking {booking['id']}")

    assert len(payments) == 1, f"expected exactly one payment, got {payments}"
    assert float(payments[0]["paymentTotal"]) == float(booking["totalPrice"])


def test_notification_is_sent(api: APIRequestContext, booking: dict, test_user: dict):
    def notifications_for_booking():
        notes = api.get("/api/notifications", params={"userId": test_user["id"]}).json()
        return [n for n in notes if n["bookingId"] == booking["id"]]

    notes = wait_until(notifications_for_booking, description=f"notification for booking {booking['id']}")

    assert len(notes) == 1, f"expected exactly one notification, got {notes}"
    assert notes[0]["type"] == "BOOKING_CONFIRMED"


def test_booking_status_becomes_confirmed(api: APIRequestContext, booking: dict):
    seen = []

    def current_status():
        status = api.get(f"/api/bookings/{booking['id']}").json()["status"]
        seen.append(status)
        return status if status != "PENDING" else None

    try:
        final_status = wait_until(current_status, description=f"booking {booking['id']} to leave PENDING")
    except AssertionError:
        pytest.fail(f"booking {booking['id']} never left PENDING; statuses seen: {sorted(set(seen))}")

    assert final_status == "CONFIRMED"


def test_same_slot_twice_is_rejected(api: APIRequestContext, booking: dict, venue: dict, court: dict, test_user: dict):
    response = api.post("/api/bookings", data=booking_request(venue, court, test_user))
    assert response.status == 409, f"status {response.status}: {response.text()}"


@pytest.mark.parametrize("change", [
    pytest.param({"durationHours": 4}, id="too-long"),
    pytest.param({"durationHours": 0}, id="zero-hours"),
    pytest.param({"bookingDate": "2020-01-01"}, id="past-date"),
])
def test_invalid_booking_returns_400(api: APIRequestContext, venue: dict, court: dict, test_user: dict, change: dict):
    payload = {**booking_request(venue, court, test_user), **change}
    response = api.post("/api/bookings", data=payload)
    assert response.status == 400, f"status {response.status}: {response.text()}"