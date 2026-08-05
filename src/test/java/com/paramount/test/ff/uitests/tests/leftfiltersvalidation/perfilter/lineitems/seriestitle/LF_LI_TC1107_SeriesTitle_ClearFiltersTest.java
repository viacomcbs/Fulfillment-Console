package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.seriestitle;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1107 — SeriesTitle Clear filters. */
public class LF_LI_TC1107_SeriesTitle_ClearFiltersTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.SERIES_TITLE.getDisplayName();
    private static final int FILTER_INDEX = 7;

    @Test(priority = 1)
    @Description("TC1107: SeriesTitle — Clear filters")
    public void tc1107_seriesTitleClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1107_seriesTitleClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
