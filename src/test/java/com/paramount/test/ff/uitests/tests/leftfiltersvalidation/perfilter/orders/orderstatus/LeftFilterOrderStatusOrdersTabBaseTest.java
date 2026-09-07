package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Order Status left-filter tests — Status is always visible on the Orders grid
 * (no Manage columns setup required). Table sync reads the Status column on the main grid.
 */
public abstract class LeftFilterOrderStatusOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
