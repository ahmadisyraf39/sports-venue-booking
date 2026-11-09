import re
from datetime import date

import pytest
from playwright.sync_api import Page, expect

from helpers import BOOKING_DATE
from pages.race_demo_page import RaceDemoPage

BOOKINGS = "**/api/bookings"
SUCCESS = re.compile(r"\bsuccess\b")
FAILURE = re.compile(r"\bfailure\b")
CORS = {"Access-Control-Allow-Origin": "*"}


@pytest.fixture
def demo(page: Page) -> RaceDemoPage:
    return RaceDemoPage(page).open()


# ---------- No backend needed ----------

def test_page_loads_with_defaults(demo: RaceDemoPage):
    expect(demo.heading).to_be_visible()
    for panel in (demo.panel_a, demo.panel_b):
        expect(panel.booking_date).to_have_value(date.today().isoformat())
        expect(panel.start_time).to_have_value("14:00")
        expect(panel.result).to_have_text("Waiting...")


def test_booking_without_token_shows_hint(demo: RaceDemoPage):
    demo.panel_a.book()

    expect(demo.panel_a.result).to_contain_text("Paste a JWT token")
    expect(demo.panel_a.root).to_have_class(FAILURE)


# ---------- Real stack ----------

def test_single_booking_succeeds(demo: RaceDemoPage, token: str, venue: dict, court: dict, test_user: dict):
    demo.enter_token(token)
    demo.panel_a.fill(court["id"], venue["id"], test_user["id"], BOOKING_DATE)
    demo.panel_a.book()

    expect(demo.panel_a.result).to_contain_text("Status: 201")
    expect(demo.panel_a.result).to_contain_text('"status": "PENDING"')
    expect(demo.panel_a.root).to_have_class(SUCCESS)


def test_fire_both_only_one_wins(demo: RaceDemoPage, token: str, venue: dict, court: dict, test_user: dict):
    demo.enter_token(token)
    for panel in (demo.panel_a, demo.panel_b):
        panel.fill(court["id"], venue["id"], test_user["id"], BOOKING_DATE)

    demo.fire_both.click()

    for panel in (demo.panel_a, demo.panel_b):
        expect(panel.result).to_contain_text("Status:")          # both requests finished
    expect(demo.successful_panels).to_have_count(1)
    expect(demo.failed_panels).to_have_count(1)
    expect(demo.failed_panels.locator("pre")).to_contain_text("Status: 409")


def test_invalid_token_shows_401(demo: RaceDemoPage, venue: dict, court: dict, test_user: dict):
    demo.enter_token("not-a-real-jwt")
    demo.panel_a.fill(court["id"], venue["id"], test_user["id"], BOOKING_DATE)
    demo.panel_a.book()

    expect(demo.panel_a.result).to_contain_text("Status: 401")
    expect(demo.panel_a.root).to_have_class(FAILURE)


# ---------- Mocked backend (page.route) ----------

@pytest.mark.parametrize("status, body", [
    pytest.param(409, {"error": "This court is already booked for the requested time slot"}, id="409-conflict"),
    pytest.param(500, {"error": "Internal Server Error"}, id="500-server-error"),
])
def test_rejected_booking_is_shown_as_failure(demo: RaceDemoPage, page: Page, status: int, body: dict):
    page.route(BOOKINGS, lambda route: route.fulfill(status=status, json=body, headers=CORS))
    demo.enter_token("token-for-mocked-backend")
    demo.panel_a.book()

    expect(demo.panel_a.result).to_contain_text(f"Status: {status}")
    expect(demo.panel_a.root).to_have_class(FAILURE)


def test_network_error_is_shown(demo: RaceDemoPage, page: Page):
    page.route(BOOKINGS, lambda route: route.abort())
    demo.enter_token("token-for-mocked-backend")
    demo.panel_a.book()

    expect(demo.panel_a.result).to_contain_text("Failed after")
    expect(demo.panel_a.root).to_have_class(FAILURE)