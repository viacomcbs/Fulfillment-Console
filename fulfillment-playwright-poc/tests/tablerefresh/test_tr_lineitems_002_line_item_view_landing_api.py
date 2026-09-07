"""FF_TR_005 — Line Items Refresh triggers LineItemViewLanding API."""

import pytest

from helpers.network_capture import NetworkCapture
from pages.left_filter_panel import LeftFilterPanel
from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.lineitems
def test_line_items_refresh_triggers_api(logged_in_page) -> None:
    panel = LeftFilterPanel(logged_in_page)
    panel.navigate_line_items()
    refresh = TableRefreshPage(logged_in_page)
    net = NetworkCapture(logged_in_page)
    net.install()
    net.clear()
    refresh.click_refresh()
    assert net.wait_for_line_item_view_landing(timeout_sec=20), (
        f"LineItemViewLanding not called (count={net.capture.line_item_view_landing_count})"
    )
    assert not net.capture.failures
