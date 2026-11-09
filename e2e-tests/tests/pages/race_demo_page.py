from pathlib import Path

from playwright.sync_api import Locator, Page

DEMO_PAGE = Path(__file__).resolve().parents[3] / "demo" / "race-condition-demo.html"


class BookingPanel:
    """One of the two request panels (A or B)."""

    def __init__(self, page: Page, number: int, letter: str):
        self.root = page.locator(f"#panel{number}")
        self.court_id = self.root.get_by_label("Court ID")
        self.venue_id = self.root.get_by_label("Venue ID")
        self.user_id = self.root.get_by_label("User ID")
        self.booking_date = self.root.get_by_label("Booking Date")
        self.start_time = self.root.get_by_label("Start Time")
        self.duration = self.root.get_by_label("Duration (hours)")
        self.book_button = self.root.get_by_role("button", name=f"Book Now ({letter})")
        self.result = self.root.locator("pre")

    def fill(self, court_id: int, venue_id: int, user_id: int, booking_date: str,
             start_time: str = "16:00", hours: int = 1) -> None:
        self.court_id.fill(str(court_id))
        self.venue_id.fill(str(venue_id))
        self.user_id.fill(str(user_id))
        self.booking_date.fill(booking_date)
        self.start_time.select_option(start_time)
        self.duration.fill(str(hours))

    def book(self) -> None:
        self.book_button.click()


class RaceDemoPage:
    def __init__(self, page: Page):
        self.page = page
        self.heading = page.get_by_role("heading", name="Double-Booking Prevention Demo")
        self.token = page.get_by_label("JWT Token")
        self.panel_a = BookingPanel(page, 1, "A")
        self.panel_b = BookingPanel(page, 2, "B")
        self.fire_both = page.get_by_role("button", name="Fire Both Simultaneously")
        self.successful_panels: Locator = page.locator(".panel.success")
        self.failed_panels: Locator = page.locator(".panel.failure")

    def open(self) -> "RaceDemoPage":
        self.page.goto(DEMO_PAGE.as_uri())
        return self

    def enter_token(self, token: str) -> None:
        self.token.fill(token)