package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus;

import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Line Item Status left-filter tests — Status is always on the grid by default;
 * Order Status and Line Item Status are not configurable in Manage columns.
 */
public abstract class LeftFilterLineItemStatusOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
