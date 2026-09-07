package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Environment left-filter tests — no Manage columns setup (Environment is not a table column).
 * Table sync verifies filter option count vs. Orders table total count only.
 */
public abstract class LeftFilterEnvironmentOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
