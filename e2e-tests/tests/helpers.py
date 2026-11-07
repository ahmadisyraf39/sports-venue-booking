import asyncio
import os
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import date, timedelta
from typing import Callable, TypeVar

from playwright.async_api import async_playwright

GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")
MAILPIT_URL = os.getenv("MAILPIT_URL", "http://localhost:8025")
BOOKING_DATE = (date.today() + timedelta(days=7)).isoformat()

T = TypeVar("T")


def wait_until(check: Callable[[], T], timeout: float = 20, interval: float = 0.5, description: str = "condition") -> T:
    """Call check() repeatedly until it returns something truthy, or fail after timeout seconds."""
    deadline = time.monotonic() + timeout
    last = None
    while time.monotonic() < deadline:
        last = check()
        if last:
            return last
        time.sleep(interval)
    raise AssertionError(f"timed out after {timeout}s waiting for {description}; last value: {last!r}")


def booking_request(venue: dict, court: dict, user: dict, start: str = "10:00:00", hours: int = 2) -> dict:
    return {
        "courtId": court["id"],
        "venueId": venue["id"],
        "userId": user["id"],
        "bookingDate": BOOKING_DATE,
        "startTime": start,
        "durationHours": hours,
    }


async def _post_all_at_once(token: str, path: str, payloads: list[dict]) -> list[tuple[int, str]]:
    async with async_playwright() as p:
        context = await p.request.new_context(
            base_url=GATEWAY_URL,
            extra_http_headers={"Authorization": f"Bearer {token}"},
        )
        try:
            responses = await asyncio.gather(*(context.post(path, data=body) for body in payloads))
            return [(r.status, await r.text()) for r in responses]
        finally:
            await context.dispose()


def post_all_at_once(token: str, path: str, payloads: list[dict]) -> list[tuple[int, str]]:
    """Send one POST per payload at the same moment. Returns [(status, body), ...] in order."""
    with ThreadPoolExecutor(max_workers=1) as pool:
        return pool.submit(asyncio.run, _post_all_at_once(token, path, payloads)).result()