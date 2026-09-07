"""FF_TR_004 — Line Items Refresh table button visible."""

import pytest

from pages.left_filter_panel import LeftFilterPanel
from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.lineitems
def test_line_items_refresh_button_visible(logged_in_page) -> None:
    panel = LeftFilterPanel(logged_in_page)
    panel.navigate_line_items()
    refresh = TableRefreshPage(logged_in_page)
    refresh.assert_refresh_visible()
