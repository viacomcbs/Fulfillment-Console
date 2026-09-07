"""
FF Login PROD — service account (Synergy server login mode, no Okta push).

Run from IntelliJ: FF Login Service Account (Playwright)
Config: suite/FF_Login_ProdServerSuite.xml
"""

from __future__ import annotations

from pathlib import Path

import pytest
from playwright.sync_api import sync_playwright

from helpers.suite_config import load_suite_config
from pages.login_page import LoginPage

SUITE_XML = Path(__file__).resolve().parent.parent / "suite" / "FF_Login_ProdServerSuite.xml"


@pytest.mark.login
def test_service_account_login_prod_no_okta_push() -> None:
    cfg = load_suite_config(SUITE_XML)
    assert cfg.username == "mscbsdqasvc@msc.cbs.net", "Expected QA service account from suite XML"
    assert cfg.is_server_login, "LoginMode must be 'server' to skip Okta push"

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=cfg.headless, slow_mo=100 if not cfg.headless else 0)
        context = browser.new_context(viewport={"width": 1920, "height": 1080})
        context.set_default_timeout(cfg.page_load_wait_sec * 1000)
        page = context.new_page()

        login = LoginPage(page)
        login.login(
            cfg.target_url,
            cfg.username,
            cfg.password,
            server_mode=True,
        )
        login.assert_logged_in()

        context.close()
        browser.close()
