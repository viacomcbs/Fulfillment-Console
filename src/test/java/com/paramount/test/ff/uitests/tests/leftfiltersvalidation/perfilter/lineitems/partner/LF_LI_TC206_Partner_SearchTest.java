package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC206 — Partner Search. */
public class LF_LI_TC206_Partner_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.PARTNER.getDisplayName();
    private static final int FILTER_INDEX = 6;

    @Test(priority = 1)
    @Description("TC206: Partner — Search")
    public void tc206_partnerSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc206_partnerSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
