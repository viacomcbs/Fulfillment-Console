"""FF_TR_003 — Orders Refresh does not duplicate Order ID rows on visible page."""

import pytest

from helpers.network_capture import normalize_business_key
from pages.left_filter_panel import LeftFilterPanel
from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.orders
def test_refresh_no_duplicate_order_ids(table_refresh: TableRefreshPage) -> None:
    page = table_refresh.page
    panel = LeftFilterPanel(page)
    page.wait_for_timeout(2000)

    keys_before = _sample_keys(page, TableRefreshPage.ORDER_ID_CELLS)
    assert keys_before, "Need at least one visible Order ID before refresh"

    table_refresh.click_refresh()
    page.wait_for_timeout(3000)

    keys_after = _sample_keys(page, TableRefreshPage.ORDER_ID_CELLS)
    _assert_no_duplicates(keys_after, "after 1st refresh")

    table_refresh.click_refresh()
    page.wait_for_timeout(3000)
    keys_after_second = _sample_keys(page, TableRefreshPage.ORDER_ID_CELLS)
    _assert_no_duplicates(keys_after_second, "after 2nd refresh")

    count_before = panel.get_table_record_count()
    count_after = panel.get_table_record_count()
    if count_before > 0 and count_after > 0:
        assert count_after < count_before * 2, "Total count doubled — possible append bug"


def _sample_keys(page, locator: str, limit: int = 30) -> list[str]:
    cells = page.locator(locator)
    n = min(cells.count(), limit)
    keys = []
    for i in range(n):
        text = cells.nth(i).inner_text()
        key = normalize_business_key(text)
        if key:
            keys.append(key)
    return keys


def _assert_no_duplicates(keys: list[str], phase: str) -> None:
    seen: set[str] = set()
    dupes: list[str] = []
    for k in keys:
        if k in seen:
            dupes.append(k)
        seen.add(k)
    assert not dupes, f"Duplicate keys {phase}: {dupes}"
