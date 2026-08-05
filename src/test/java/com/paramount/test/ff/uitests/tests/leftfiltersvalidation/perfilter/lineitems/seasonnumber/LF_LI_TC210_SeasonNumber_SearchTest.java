package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC210 — SeasonNumber Search. */
public class LF_LI_TC210_SeasonNumber_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.SEASON_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 10;

    @Test(priority = 1)
    @Description("TC210: SeasonNumber — Search")
    public void tc210_seasonNumberSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc210_seasonNumberSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
