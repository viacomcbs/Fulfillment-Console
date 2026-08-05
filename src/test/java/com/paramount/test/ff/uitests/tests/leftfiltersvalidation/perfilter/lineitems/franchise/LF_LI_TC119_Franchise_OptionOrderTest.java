package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.franchise;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC119 — Franchise Option list order. */
public class LF_LI_TC119_Franchise_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.FRANCHISE.getDisplayName();
    private static final int FILTER_INDEX = 18;

    @Test(priority = 1)
    @Description("TC119: Franchise — Option list order")
    public void tc119_franchiseOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc119_franchiseOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
