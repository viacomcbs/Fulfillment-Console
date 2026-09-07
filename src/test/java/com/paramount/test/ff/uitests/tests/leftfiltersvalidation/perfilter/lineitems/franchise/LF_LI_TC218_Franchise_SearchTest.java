package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.franchise;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC218 — Franchise Search. */
public class LF_LI_TC218_Franchise_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.FRANCHISE.getDisplayName();
    private static final int FILTER_INDEX = 18;

    @Test(priority = 1)
    @Description("TC218: Franchise — Search")
    public void tc218_franchiseSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc218_franchiseSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
