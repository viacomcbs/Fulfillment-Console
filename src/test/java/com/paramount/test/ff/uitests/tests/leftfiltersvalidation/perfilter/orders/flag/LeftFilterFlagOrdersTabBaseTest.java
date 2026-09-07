package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Flag filter tests — Status column is default on Orders grid; no Manage columns setup needed. */
public abstract class LeftFilterFlagOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
