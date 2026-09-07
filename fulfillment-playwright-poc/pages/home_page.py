"""Fulfillment Console home / app shell locators."""

from __future__ import annotations

from playwright.sync_api import Page


class HomePage:
    MEDIA_INGEST_TITLE = "xpath=//span[text()='MEDIA INGEST CONSOLE']"
    FILTER_PANEL = (
        "xpath=//msc-left-filter-panel"
        " | //*[contains(@class,'left-filter') or contains(@class,'filter-panel')]"
    )
    ORDERS_TAB = "xpath=//a[contains(.,'Orders')]"

    def __init__(self, page: Page) -> None:
        self.page = page

    def is_logged_in(self) -> bool:
        title = self.page.locator(self.MEDIA_INGEST_TITLE)
        panel = self.page.locator(self.FILTER_PANEL)
        if title.count() and title.first.is_visible():
            return True
        if panel.count() and panel.first.is_visible():
            return True
        return bool(
            self.page.evaluate("() => !!document.querySelector('msc-left-filter-panel')")
        )

    def assert_home_visible(self) -> None:
        assert self.is_logged_in(), (
            "Fulfillment Console home not visible — left filter panel or app title missing. "
            "Session may have expired; delete auth/ff_prod_personal_state.json and re-run."
        )
