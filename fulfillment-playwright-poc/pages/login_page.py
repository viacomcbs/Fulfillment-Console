"""Okta login for Fulfillment Console — local (push) or server (service account, no push)."""

from __future__ import annotations

import time

from playwright.sync_api import Page, expect


class LoginPage:
    USERNAME = "xpath=//input[@autocomplete='username']"
    PASSWORD_LOCAL = "xpath=//input[@type='password']"
    PASSWORD_SERVER = "xpath=//input[@name='credentials.passcode' and @type='password']"
    PASSWORD_ANY = "xpath=//input[@type='password']"
    NEXT = "xpath=//input[@value='Next']"
    VERIFY = "xpath=//input[@value='Verify']"
    OKTA_VERIFY = (
        "xpath=//a[contains(@aria-label,'Okta Verify')]"
        " | //button[contains(.,'Okta Verify')]"
        " | //input[@value='Send push' or @value='Send Push']"
    )
    HOME = "xpath=//span[text()='MEDIA INGEST CONSOLE']"
    FILTER_PANEL = (
        "xpath=//*[contains(@class,'left-filter') or contains(@class,'filter-panel')]"
        " | //msc-left-filter-panel"
    )
    HEADER_TITLE = "xpath=//*[contains(@class,'header') and contains(.,'Fulfillment')]"

    def __init__(self, page: Page) -> None:
        self.page = page

    def login(self, url: str, username: str, password: str, *, server_mode: bool = False) -> None:
        self.page.goto(url, wait_until="domcontentloaded", timeout=120_000)

        username_field = self.page.locator(self.USERNAME)
        username_field.wait_for(state="visible", timeout=60_000)
        username_field.fill(username)

        next_btn = self.page.locator(self.NEXT)
        if next_btn.count() and next_btn.first.is_visible():
            next_btn.first.click()

        password_locator = self._password_locator(server_mode)
        password_locator.first.wait_for(state="visible", timeout=60_000)
        password_locator.first.fill(password)

        verify_btn = self.page.locator(self.VERIFY)
        if verify_btn.count() and verify_btn.first.is_visible():
            verify_btn.first.click()

        if server_mode:
            self._wait_for_fulfillment_console_redirect(max_seconds=90)
        else:
            self._handle_okta_verify_if_needed()
            self._wait_for_fulfillment_console_redirect(max_seconds=120)

        self._zoom_out(server_mode)

    def assert_logged_in(self) -> None:
        shell = self.page.locator(self.HOME).or_(self.page.locator(self.FILTER_PANEL)).first
        expect(shell).to_be_visible(timeout=30_000)

    def _password_locator(self, server_mode: bool):
        if server_mode:
            server = self.page.locator(self.PASSWORD_SERVER)
            if server.count():
                return server
        return self.page.locator(self.PASSWORD_ANY)

    def _handle_okta_verify_if_needed(self) -> None:
        okta = self.page.locator(self.OKTA_VERIFY)
        if okta.count() and okta.first.is_visible():
            okta.first.click()
            print("Okta Verify push sent — approve on your phone...")

    def _wait_for_fulfillment_console_redirect(self, max_seconds: int) -> None:
        """Mirrors Java Login.waitForFulfillmentConsoleRedirect — no Okta push."""
        deadline = time.time() + max_seconds
        while time.time() < deadline:
            if self.page.locator(self.HOME).first.is_visible():
                return
            if self.page.locator(self.FILTER_PANEL).first.is_visible():
                return
            panel = self.page.evaluate("() => !!document.querySelector('msc-left-filter-panel')")
            if panel:
                return
            self.page.wait_for_timeout(2_000)
        raise TimeoutError(
            f"Fulfillment Console did not load within {max_seconds}s after service-account login"
        )

    def _zoom_out(self, server_mode: bool) -> None:
        if server_mode:
            self.page.evaluate("document.body.style.zoom = '0.8'")
        else:
            for _ in range(4):
                self.page.keyboard.press("Control+-")
