"""Save / load Playwright storage state (cookies) to skip repeat Okta login."""

from __future__ import annotations

from pathlib import Path

from playwright.sync_api import Browser, BrowserContext, Page

from helpers.suite_config import SuiteConfig
from pages.home_page import HomePage
from pages.login_page import LoginPage


def resolve_storage_path(cfg: SuiteConfig, project_root: Path) -> Path:
    path = Path(cfg.storage_state_path)
    if not path.is_absolute():
        path = project_root / path
    path.parent.mkdir(parents=True, exist_ok=True)
    return path


def session_is_valid(page: Page, target_url: str) -> bool:
    page.goto(target_url, wait_until="domcontentloaded", timeout=120_000)
    page.wait_for_timeout(3_000)
    return HomePage(page).is_logged_in()


def open_authenticated_page(
    browser: Browser,
    cfg: SuiteConfig,
    project_root: Path,
) -> tuple[BrowserContext, Page, bool]:
    """
    Returns (context, page, did_fresh_login).
    Loads saved session when valid; otherwise full login and saves state.
    """
    state_path = resolve_storage_path(cfg, project_root)
    context_kwargs = {"viewport": {"width": 1920, "height": 1080}}
    if state_path.exists():
        context_kwargs["storage_state"] = str(state_path)

    context = browser.new_context(**context_kwargs)
    context.set_default_timeout(cfg.page_load_wait_sec * 1000)
    page = context.new_page()

    if state_path.exists() and session_is_valid(page, cfg.target_url):
        return context, page, False

    # Fresh login — approve Okta Verify push when prompted
    login = LoginPage(page)
    login.login(cfg.target_url, cfg.username, cfg.password, server_mode=False)
    login.assert_logged_in()
    context.storage_state(path=str(state_path))
    return context, page, True
