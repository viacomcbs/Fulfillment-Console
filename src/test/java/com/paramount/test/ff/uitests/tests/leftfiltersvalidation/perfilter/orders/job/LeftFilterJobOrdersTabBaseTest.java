package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders Job type left-filter tests — TC622 expands first order; no Manage columns setup. */
public abstract class LeftFilterJobOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.JOB.getDisplayName();
    }

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
