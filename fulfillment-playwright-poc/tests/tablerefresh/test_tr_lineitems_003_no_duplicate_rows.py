"""FF_TR_006 — Line Items Refresh does not duplicate LineItem ID rows."""

import pytest

from helpers.network_capture import normalize_business_key
from pages.left_filter_panel import LeftFilterPanel
from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.lineitems
def test_line_items_refresh_no_duplicate_ids(logged_in_page) -> None:
    panel = LeftFilterPanel(logged_in_page)
    panel.navigate_line_items()
    page = logged_in_page
    refresh = TableRefreshPage(page)
    page.wait_for_timeout(2000)

    keys_before = _sample_keys(page, TableRefreshPage.LINE_ITEM_ID_CELLS)
    assert keys_before, "Need at least one visible LineItem ID before refresh"

    refresh.click_refresh()
    page.wait_for_timeout(3000)
    _assert_no_duplicates(_sample_keys(page, TableRefreshPage.LINE_ITEM_ID_CELLS), "after refresh")


def _sample_keys(page, locator: str, limit: int = 30) -> list[str]:
    cells = page.locator(locator)
    n = min(cells.count(), limit)
    return [normalize_business_key(cells.nth(i).inner_text()) for i in range(n) if cells.nth(i).inner_text()]


def _assert_no_duplicates(keys: list[str], phase: str) -> None:
    dupes = [k for k in keys if keys.count(k) > 1]
    assert not dupes, f"Duplicates {phase}: {set(dupes)}"
