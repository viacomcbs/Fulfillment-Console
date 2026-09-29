package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.jobtype;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders Job type left-filter tests — TC622/604 expand first order; no Manage columns setup. */
public abstract class LeftFilterJobTypeOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.JOB_TYPE.getDisplayName();
    }

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
