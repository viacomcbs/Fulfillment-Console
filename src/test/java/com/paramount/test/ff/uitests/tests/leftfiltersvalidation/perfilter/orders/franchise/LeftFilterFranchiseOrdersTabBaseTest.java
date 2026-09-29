package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.franchise;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Franchise left-filter tests — no Manage columns setup (Franchise is not a table column).
 * Table sync verifies filter option count vs. Orders table total count only.
 */
public abstract class LeftFilterFranchiseOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.FRANCHISE.getDisplayName();
    }

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
