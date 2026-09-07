"""
FF Home smoke — personal login, session saved after first Okta push.

Config: suite/FF_Home_PersonalLocalSuite.xml
IntelliJ: FF Home Personal (Playwright)
"""

from __future__ import annotations

from pathlib import Path

import pytest
from playwright.sync_api import sync_playwright

from helpers.auth_session import open_authenticated_page
from helpers.suite_config import load_suite_config
from pages.home_page import HomePage

PROJECT_ROOT = Path(__file__).resolve().parent.parent
SUITE_XML = PROJECT_ROOT / "suite" / "FF_Home_PersonalLocalSuite.xml"


@pytest.mark.home
def test_fulfillment_console_home_page_visible() -> None:
    cfg = load_suite_config(SUITE_XML)
    assert cfg.is_local_login, "This suite uses personal login (LoginMode=local)"

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=cfg.headless, slow_mo=100 if not cfg.headless else 0)
        context, page, fresh_login = open_authenticated_page(browser, cfg, PROJECT_ROOT)
        try:
            home = HomePage(page)
            home.assert_home_visible()
            if fresh_login:
                print(f"Session saved — next run reuses: {cfg.storage_state_path}")
            else:
                print(f"Reused saved session: {cfg.storage_state_path}")
        finally:
            context.close()
            browser.close()
