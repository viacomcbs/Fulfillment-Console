"""Table toolbar — Refresh table button next to Export (BSD-29582)."""

from __future__ import annotations

from playwright.sync_api import Locator, Page


class TableRefreshPage:
    TABLE_BUTTON_SECTION = "xpath=//div[contains(@class,'button-section')]"
    EXPORT_BUTTON = (
        "xpath=//div[contains(@class,'button-section')]//button[contains(@class,'export-btn')]"
        " | //button[contains(@class,'export-btn') and contains(normalize-space(),'Export')]"
    )
    REFRESH_BUTTON = (
        "xpath=//div[contains(@class,'button-section')]//button[contains(@class,'refresh-table-btn')]"
        " | //button[contains(@class,'refresh-table-btn') and contains(normalize-space(),'Refresh table')]"
    )
    REFRESH_LABEL = (
        "xpath=//button[contains(@class,'refresh-table-btn') and contains(normalize-space(),'Refresh table')]"
    )
    REFRESH_ICON = "xpath=//button[contains(@class,'refresh-table-btn')]//i[contains(@class,'bi-arrow-repeat')]"
    ORDER_ID_CELLS = (
        "xpath=//table[@id='orderTable']//tbody//tr[contains(@class,'row') and not(contains(@class,'inner'))]"
        "//td[contains(@class,'order-id-col')]"
    )
    LINE_ITEM_ID_CELLS = (
        "xpath=//app-fulfillment-main-table-container//tbody//tr[contains(@class,'row')]"
        "//td[contains(@class,'lineitem-id-col') or contains(@class,'line-item-id-col')]"
    )
    RECORD_COUNT = "xpath=//*[contains(@class,'record-count') or contains(@class,'total-count')]"

    def __init__(self, page: Page) -> None:
        self.page = page

    @property
    def refresh_button(self) -> Locator:
        return self.page.locator(self.REFRESH_BUTTON).first

    def click_refresh(self) -> None:
        self.refresh_button.click()

    def assert_refresh_visible(self) -> None:
        assert self.page.locator(self.TABLE_BUTTON_SECTION).first.is_visible()
        assert self.page.locator(self.EXPORT_BUTTON).first.is_visible()
        assert self.refresh_button.is_visible()
        assert self.page.locator(self.REFRESH_LABEL).first.is_visible()
        assert self.page.locator(self.REFRESH_ICON).first.is_visible()
