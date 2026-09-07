"""DSID column + Details panel locators (BSD-29870)."""

from __future__ import annotations

from playwright.sync_api import Locator, Page


class DsidPage:
    COLUMN_LABEL = "DSID"
    JOB_TYPE_VALUE = "MetadataOnlyDelivery"
    PARTNER_VALUE = "PP YT FSP UK"

    GRID_COLUMN_HEADER = (
        f"xpath=//th[@title='{COLUMN_LABEL}']"
        f" | //th[.//span[normalize-space()='{COLUMN_LABEL}']]"
    )
    GRID_ROW = "xpath=//table[@id='orderTable']//tbody//tr | //tbody//tr[contains(@class,'row')]"
    DETAILS_DSID_VALUES = (
        f"xpath=//*[contains(@class,'label') and normalize-space()='{COLUMN_LABEL}']"
        "/following-sibling::*[contains(@class,'value')]//span[contains(@class,'demandSystemId-value')]//span"
    )

    def __init__(self, page: Page) -> None:
        self.page = page

    def grid_row(self, one_based_index: int) -> Locator:
        return self.page.locator(self.GRID_ROW).nth(one_based_index - 1)
