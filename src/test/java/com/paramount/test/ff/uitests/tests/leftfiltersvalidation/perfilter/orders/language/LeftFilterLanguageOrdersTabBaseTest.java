package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.language;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Orders Language left-filter tests — Language line-item column on expanded order grid.
 * Table sync expands an order and verifies at least one line item Language matches the filter.
 */
public abstract class LeftFilterLanguageOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.LANGUAGE.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.LANGUAGE;
    }
}
