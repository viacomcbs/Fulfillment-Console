package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC216 — DemandSystem Search. */
public class LF_LI_TC216_DemandSystem_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.DEMAND_SYSTEM.getDisplayName();
    private static final int FILTER_INDEX = 16;

    @Test(priority = 1)
    @Description("TC216: DemandSystem — Search")
    public void tc216_demandSystemSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc216_demandSystemSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
