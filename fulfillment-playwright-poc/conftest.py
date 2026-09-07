"""Shared pytest fixtures — session browser, config, login."""

from __future__ import annotations

import os
from pathlib import Path

import pytest
import yaml
from dotenv import load_dotenv
from playwright.sync_api import Browser, BrowserContext, Page, sync_playwright

from pages.login_page import LoginPage

ROOT = Path(__file__).parent
load_dotenv(ROOT / ".env")


def _load_config() -> dict:
    with open(ROOT / "config" / "environments.yaml", encoding="utf-8") as f:
        return yaml.safe_load(f)


@pytest.fixture(scope="session")
def ff_config() -> dict:
    return _load_config()


@pytest.fixture(scope="session")
def target_url(ff_config: dict) -> str:
    env = os.getenv("FF_ENV", "PROD").upper()
    urls = ff_config["environments"]
    if env not in urls:
        raise ValueError(f"Unknown FF_ENV={env}; expected one of {list(urls)}")
    return urls[env]


@pytest.fixture(scope="session")
def playwright_instance():
    with sync_playwright() as p:
        yield p


@pytest.fixture(scope="session")
def browser(playwright_instance) -> Browser:
    headless = os.getenv("FF_HEADLESS", "false").lower() == "true"
    b = playwright_instance.chromium.launch(headless=headless, slow_mo=100 if not headless else 0)
    yield b
    b.close()


@pytest.fixture(scope="session")
def context(browser: Browser) -> BrowserContext:
    ctx = browser.new_context(viewport={"width": 1920, "height": 1080})
    ctx.set_default_timeout(60_000)
    yield ctx
    ctx.close()


@pytest.fixture(scope="session")
def logged_in_page(context: BrowserContext, target_url: str) -> Page:
    """One logged-in session reused across Refresh + DSID PoC tests."""
    page = context.new_page()
    login = LoginPage(page)
    username = os.environ["FF_USERNAME"]
    password = os.environ["FF_PASSWORD"]
    login.login(target_url, username, password)
    yield page
    page.close()
