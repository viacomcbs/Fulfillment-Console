"""Shared setup for BSD-29582 Table Refresh PoC."""

from __future__ import annotations

import pytest
from playwright.sync_api import Page

from helpers.session_state import refresh_state
from pages.left_filter_panel import LeftFilterPanel
from pages.table_refresh_page import TableRefreshPage


@pytest.fixture(scope="session")
def refresh_orders_page(logged_in_page: Page) -> Page:
    panel = LeftFilterPanel(logged_in_page)
    panel.ensure_open()
    if not refresh_state.calendar_yesterday:
        try:
            panel.set_calendar_yesterday()
            refresh_state.calendar_yesterday = True
        except NotImplementedError:
            # PoC: run without calendar setup until CalendarSetupUtil is ported
            print("WARN: calendar yesterday not ported — continuing with default date range")
            refresh_state.calendar_yesterday = True
    panel.navigate_orders()
    return logged_in_page


@pytest.fixture
def table_refresh(refresh_orders_page: Page) -> TableRefreshPage:
    return TableRefreshPage(refresh_orders_page)
