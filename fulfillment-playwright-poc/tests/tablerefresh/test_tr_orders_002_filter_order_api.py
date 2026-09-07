"""FF_TR_002 — Orders Refresh click triggers filterOrder GraphQL."""

import pytest

from helpers.network_capture import NetworkCapture
from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.orders
def test_refresh_triggers_filter_order_api(table_refresh: TableRefreshPage) -> None:
    page = table_refresh.page
    net = NetworkCapture(page)
    net.install()
    net.clear()
    table_refresh.click_refresh()
    assert net.wait_for_filter_order(timeout_sec=20), (
        f"filterOrder not called (count={net.capture.filter_order_count})"
    )
    assert not net.capture.failures, f"API failures: {net.capture.failures}"
