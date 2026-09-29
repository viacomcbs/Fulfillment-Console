package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;

/**
 * Activity Type Orders left-filter suite optimizations (Synergy video learnings):
 * <ul>
 *   <li>30s post-login home settle before calendar (inherited from orders base)</li>
 *   <li>TC120 expands Activity Type once — accordion stays open until TC1120 teardown</li>
 *   <li>No {@code clearAllActiveFiltersIfPresent} between tests (avoids mid-suite deselect ~12 min)</li>
 * </ul>
 */
public abstract class LeftFilterActivityTypeOrdersTabBaseTest extends LeftFilterOrdersTabBaseTest {

    protected static final String FILTER = OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName();

    @Override
    protected String keepExpandedFilterName() {
        return FILTER;
    }

    @Override
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.ACTIVITY_TYPE;
    }
}
