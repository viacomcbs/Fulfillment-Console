"""GraphQL API capture for Refresh tests — Playwright-native (replaces JS injection)."""

from __future__ import annotations

import re
from dataclasses import dataclass, field

from playwright.sync_api import Page, Request


@dataclass
class ApiCapture:
    filter_order_count: int = 0
    line_item_view_landing_count: int = 0
    failures: list[str] = field(default_factory=list)

    def reset(self) -> None:
        self.filter_order_count = 0
        self.line_item_view_landing_count = 0
        self.failures.clear()


class NetworkCapture:
    """Tracks filterOrder and LineItemViewLanding GraphQL calls."""

    def __init__(self, page: Page) -> None:
        self.page = page
        self.capture = ApiCapture()
        self._handler = None

    def install(self) -> None:
        self.capture.reset()

        def on_request(request: Request) -> None:
            body = request.post_data or ""
            if "filterOrder" in body:
                self.capture.filter_order_count += 1
            if "LineItemViewLanding" in body:
                self.capture.line_item_view_landing_count += 1

        def on_response(response) -> None:
            if response.status >= 400:
                self.capture.failures.append(f"{response.url} | HTTP {response.status}")

        self.page.on("request", on_request)
        self.page.on("response", on_response)
        self._handler = on_request

    def clear(self) -> None:
        self.capture.reset()

    def wait_for_filter_order(self, timeout_sec: int = 20) -> bool:
        return self._wait_until(lambda c: c.filter_order_count >= 1, timeout_sec)

    def wait_for_line_item_view_landing(self, timeout_sec: int = 20) -> bool:
        return self._wait_until(lambda c: c.line_item_view_landing_count >= 1, timeout_sec)

    def _wait_until(self, predicate, timeout_sec: int) -> bool:
        elapsed = 0
        while elapsed < timeout_sec * 1000:
            if predicate(self.capture):
                return True
            self.page.wait_for_timeout(500)
            elapsed += 500
        return predicate(self.capture)


def normalize_business_key(raw: str) -> str:
    if not raw:
        return ""
    match = re.search(r"\d{5,12}", raw.strip())
    return match.group(0) if match else raw.strip()
