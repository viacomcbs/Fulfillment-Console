"""Load parameters from FF_*_Suite.xml (same shape as Java TestNG suites)."""

from __future__ import annotations

import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class SuiteConfig:
    test_environment: str
    login_mode: str
    username: str
    password: str
    target_url: str
    headless: bool
    page_load_wait_sec: int
    storage_state_path: str = "auth/ff_prod_personal_state.json"

    @property
    def is_server_login(self) -> bool:
        return self.login_mode.lower() == "server"

    @property
    def is_local_login(self) -> bool:
        return self.login_mode.lower() == "local"


def load_suite_config(suite_xml: Path) -> SuiteConfig:
    tree = ET.parse(suite_xml)
    root = tree.getroot()
    params = {p.get("name"): p.get("value") for p in root.findall("parameter")}

    env = params.get("TestEnvironment", "PROD").upper()
    url_key = f"TargetUrl{env}"
    target = params.get(url_key) or params.get("TargetUrlPROD", "")

    return SuiteConfig(
        test_environment=env,
        login_mode=params.get("LoginMode", "server"),
        username=params.get("Username", ""),
        password=params.get("Password", ""),
        target_url=target,
        headless=params.get("Headless", "false").lower() == "true",
        page_load_wait_sec=int(params.get("PageLoadWaitTime", "120")),
        storage_state_path=params.get("StorageStatePath", "auth/ff_prod_personal_state.json"),
    )
