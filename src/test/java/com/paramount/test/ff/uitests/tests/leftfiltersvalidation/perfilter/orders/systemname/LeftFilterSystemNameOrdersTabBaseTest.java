package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.systemname;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders System Name left-filter tests — no Manage columns setup.
 * Table sync verifies filter option count vs. Orders table total count only (Environment-style).
 */
public abstract class LeftFilterSystemNameOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.SYSTEM_NAME.getDisplayName();
    }

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
