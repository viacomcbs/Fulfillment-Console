package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.errormessage;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders tab Error message filter tests — ensures {@link ManageColumnOptions#ERROR_MESSAGES} is on the grid. */
public abstract class LeftFilterErrorMessageOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.ERROR_MESSAGE.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.ERROR_MESSAGES;
    }
}
