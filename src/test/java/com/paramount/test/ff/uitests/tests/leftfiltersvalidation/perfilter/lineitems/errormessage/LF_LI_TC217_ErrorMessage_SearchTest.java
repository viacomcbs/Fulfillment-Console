package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.errormessage;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC217 — ErrorMessage Search. */
public class LF_LI_TC217_ErrorMessage_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ERROR_MESSAGE.getDisplayName();
    private static final int FILTER_INDEX = 17;

    @Test(priority = 1)
    @Description("TC217: ErrorMessage — Search")
    public void tc217_errorMessageSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc217_errorMessageSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
