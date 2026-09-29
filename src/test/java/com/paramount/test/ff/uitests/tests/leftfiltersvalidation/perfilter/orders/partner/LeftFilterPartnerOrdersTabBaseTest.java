package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Partner left-filter tests — Partner is an order-level column (Order columns accordion).
 * Table sync reads the Partner column on the main Orders grid (no row expand / line items).
 */
public abstract class LeftFilterPartnerOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.PARTNER.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.PARTNER;
    }
}
