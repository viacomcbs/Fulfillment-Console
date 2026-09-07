package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus;

import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;

/**
 * Line Items Order Status left-filter tests — Status is always on the grid by default;
 * Order Status and Line Item Status are not configurable in Manage columns.
 */
public abstract class LeftFilterOrderStatusLineItemsTabBaseTest extends LeftFilterLineItemsTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
