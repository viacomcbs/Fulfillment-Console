package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.assignedto;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Assigned To left-filter tests — Assigned to is a line-item-level column on the Orders grid
 * (flat list above LINE ITEM COLUMNS). Table sync reads initials (e.g. Akilandeswari Sundararajan → AS)
 * or blank cells when Unassigned is selected.
 */
public abstract class LeftFilterAssignedToOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.ASSIGNED_TO;
    }
}
