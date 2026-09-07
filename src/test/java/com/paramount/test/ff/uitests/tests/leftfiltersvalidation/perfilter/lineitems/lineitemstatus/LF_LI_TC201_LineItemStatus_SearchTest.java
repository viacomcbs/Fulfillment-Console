package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus.LeftFilterLineItemStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC201 — LineItemStatus Search. */
public class LF_LI_TC201_LineItemStatus_SearchTest extends LeftFilterLineItemStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.LINE_ITEM_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC201: LineItemStatus — Search")
    public void tc201_lineItemStatusSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc201_lineItemStatusSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
