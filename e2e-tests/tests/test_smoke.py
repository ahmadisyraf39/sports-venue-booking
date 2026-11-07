import pytest
from playwright.sync_api import APIRequestContext


def test_gateway_is_up(anon_api: APIRequestContext):
    response = anon_api.get("/actuator/health")
    assert response.status == 200, f"status {response.status}"
    assert response.json()["status"] == "UP"


@pytest.mark.parametrize("path", [
    pytest.param("/api/venues", id="venue-service"),
    pytest.param("/api/bookings?userId={user_id}", id="booking-service"),
    pytest.param("/api/payments?bookingId=smoke-test", id="payment-service"),
    pytest.param("/api/notifications?userId={user_id}", id="notification-service"),
])
def test_service_reachable_through_gateway(api: APIRequestContext, test_user: dict, path: str):
    response = api.get(path.format(user_id=test_user["id"]))
    assert response.status == 200, f"{path}: status {response.status} {response.text()}"
    assert isinstance(response.json(), list)