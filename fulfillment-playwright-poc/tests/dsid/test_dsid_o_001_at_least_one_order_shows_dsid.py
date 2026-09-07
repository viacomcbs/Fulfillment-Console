"""FF_DSID_001 — At least one Orders row shows populated DSID."""

import pytest

from pages.dsid_page import DsidPage


@pytest.mark.dsid
@pytest.mark.orders
def test_at_least_one_order_shows_dsid(dsid_page: DsidPage) -> None:
    pytest.skip("TODO: port enableDsidColumnIfNeeded + row scan from DsidColumnUtil")
