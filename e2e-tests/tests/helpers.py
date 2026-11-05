import time
from typing import Callable, TypeVar

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