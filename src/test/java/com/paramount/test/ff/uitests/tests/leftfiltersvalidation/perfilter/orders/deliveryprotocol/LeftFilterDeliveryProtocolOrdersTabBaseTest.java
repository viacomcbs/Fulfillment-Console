package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Delivery Protocol left-filter tests — no Manage columns setup.
 * Table sync: count match + package-level Endpoint Info Delivery Protocol validation.
 */
public abstract class LeftFilterDeliveryProtocolOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    }

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }
}
