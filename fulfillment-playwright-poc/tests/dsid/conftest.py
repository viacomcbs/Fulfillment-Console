"""Shared setup for BSD-29870 DSID PoC."""

from __future__ import annotations

import pytest
from playwright.sync_api import Page

from helpers.session_state import dsid_state
from pages.dsid_page import DsidPage
from pages.left_filter_panel import LeftFilterPanel


@pytest.fixture(scope="session")
def dsid_orders_page(logged_in_page: Page) -> Page:
    panel = LeftFilterPanel(logged_in_page)
    panel.ensure_open()
    try:
        panel.set_calendar_today()
    except NotImplementedError:
        pytest.skip("Calendar today not ported yet — implement LeftFilterPanel.set_calendar_today")
    panel.navigate_orders()
    if not dsid_state.filters_orders:
        try:
            panel.apply_dsid_filters()
            dsid_state.filters_orders = True
        except NotImplementedError:
            pytest.skip("DSID filters not ported yet — implement LeftFilterPanel.apply_dsid_filters")
    return logged_in_page


@pytest.fixture
def dsid_page(dsid_orders_page: Page) -> DsidPage:
    return DsidPage(dsid_orders_page)
