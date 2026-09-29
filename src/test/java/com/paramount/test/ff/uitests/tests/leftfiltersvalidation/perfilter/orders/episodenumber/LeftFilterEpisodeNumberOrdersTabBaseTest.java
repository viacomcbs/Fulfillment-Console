package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.episodenumber;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/** Orders Episode number left-filter tests — Title, Season, Episode column (range filter UI). */
public abstract class LeftFilterEpisodeNumberOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected String keepExpandedFilterName() {
        return OrdersLeftFilter.EPISODE_NUMBER.getDisplayName();
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.TITLE_SEASON_EPISODE;
    }
}
