package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Brand left-filter tests — Brand is an order-level column (Order columns accordion).
 * Table sync reads the Brand column on the main Orders grid (no row expand / line items).
 */
public abstract class LeftFilterBrandOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.BRAND;
    }
}
