"""FF_DSID_002 — Orders table DSID matches last DSID in Details panel."""

import pytest

from pages.dsid_page import DsidPage


@pytest.mark.dsid
@pytest.mark.orders
def test_table_dsid_matches_details_panel_last_dsid(dsid_page: DsidPage) -> None:
    pytest.skip("TODO: port openDetailsForStoredRow + readLastDetailsPanelDsid from DsidColumnUtil")
