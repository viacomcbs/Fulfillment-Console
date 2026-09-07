package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC208 — Flag Search. */
public class LF_LI_TC208_Flag_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.FLAG.getDisplayName();
    private static final int FILTER_INDEX = 8;

    @Test(priority = 1)
    @Description("TC208: Flag — Search")
    public void tc208_flagSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc208_flagSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
