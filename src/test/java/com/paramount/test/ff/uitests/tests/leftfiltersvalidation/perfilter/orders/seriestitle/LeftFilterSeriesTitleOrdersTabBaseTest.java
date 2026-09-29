package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seriestitle;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders Series title left-filter tests — Title, Season, Episode order column (large list). */
public abstract class LeftFilterSeriesTitleOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.SERIES_TITLE.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.TITLE_SEASON_EPISODE;
    }
}
