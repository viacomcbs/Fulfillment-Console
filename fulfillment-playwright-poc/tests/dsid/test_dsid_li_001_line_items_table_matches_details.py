"""FF_DSID_003 — Line Items DSID table matches Details panel."""

import pytest

from pages.dsid_page import DsidPage
from pages.left_filter_panel import LeftFilterPanel


@pytest.mark.dsid
@pytest.mark.lineitems
def test_line_items_dsid_matches_details(dsid_orders_page, dsid_page: DsidPage) -> None:
    panel = LeftFilterPanel(dsid_orders_page)
    panel.navigate_line_items()
    pytest.skip("TODO: port Line Items DSID flow from FF_DSID_LI_001")
