package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Submitted by left-filter tests — Submitted By is an order-level column (Order columns accordion).
 * Table sync reads initials in the Submitted By column on the main Orders grid (no row expand).
 */
public abstract class LeftFilterSubmittedByOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.SUBMITTED_BY;
    }
}
