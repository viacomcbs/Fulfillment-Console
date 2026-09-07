"""FF_TR_001 — Orders Refresh table button visible near Export."""

import pytest

from pages.table_refresh_page import TableRefreshPage


@pytest.mark.tablerefresh
@pytest.mark.orders
def test_refresh_button_visible_near_export(table_refresh: TableRefreshPage) -> None:
    table_refresh.assert_refresh_visible()
