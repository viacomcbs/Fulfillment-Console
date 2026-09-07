"""Session flags — mirrors LeftFilterSessionHelper / DsidSessionHelper."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class RefreshSessionState:
    calendar_yesterday: bool = False


@dataclass
class DsidSessionState:
    filters_orders: bool = False
    filters_line_items: bool = False
    dsid_column_orders: bool = False
    dsid_column_line_items: bool = False
    orders_row_index: int = -1
    orders_dsid_value: str = ""
    line_items_row_index: int = -1
    line_items_dsid_value: str = ""


refresh_state = RefreshSessionState()
dsid_state = DsidSessionState()
