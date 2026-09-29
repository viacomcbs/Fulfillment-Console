package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seasonnumber;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders Season number left-filter tests — Title, Season, Episode column (range filter UI). */
public abstract class LeftFilterSeasonNumberOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.SEASON_NUMBER.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.TITLE_SEASON_EPISODE;
    }
}
