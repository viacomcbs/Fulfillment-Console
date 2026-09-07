"""Left filter panel — PoC stubs; full port from LeftFilterPanelUtil later."""

from __future__ import annotations

from playwright.sync_api import Page


class LeftFilterPanel:
    ORDERS_TAB = "xpath=//a[contains(.,'Orders') or contains(@href,'orders')]"
    LINE_ITEMS_TAB = "xpath=//a[contains(.,'Line Items') or contains(@href,'line-items')]"
    FILTER_PANEL = "xpath=//*[contains(@class,'left-filter') or contains(@class,'filter-panel')]"

    def __init__(self, page: Page) -> None:
        self.page = page

    def ensure_open(self) -> None:
        panel = self.page.locator(self.FILTER_PANEL).first
        panel.wait_for(state="visible", timeout=60_000)

    def navigate_orders(self) -> None:
        self.ensure_open()
        self.page.locator(self.ORDERS_TAB).first.click()

    def navigate_line_items(self) -> None:
        self.ensure_open()
        self.page.locator(self.LINE_ITEMS_TAB).first.click()

    def get_table_record_count(self) -> int:
        """Parse total count label — simplified PoC."""
        label = self.page.locator(
            "xpath=//*[contains(@class,'record-count') or contains(@class,'total-count')]"
        ).first
        if not label.count():
            return -1
        text = label.inner_text()
        digits = "".join(c for c in text if c.isdigit())
        return int(digits) if digits else 0

    def apply_dsid_filters(self) -> None:
        """TODO: port Job type + Partner filter from DsidColumnUtil.applyMetadataOnlyDeliveryPartnerFilter."""
        raise NotImplementedError(
            "DSID filter setup not yet ported — implement expand/select for Job type and Partner"
        )

    def set_calendar_yesterday(self) -> None:
        """TODO: port CalendarSetupUtil.setDateRangeToYesterday for Refresh suite."""
        raise NotImplementedError("Calendar yesterday setup not yet ported")

    def set_calendar_today(self) -> None:
        """TODO: port DsidCalendarSetup.setTodayAsDefaultOnce."""
        raise NotImplementedError("Calendar today setup not yet ported")
