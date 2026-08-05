package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.contenttype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC221 — ContentType Search. */
public class LF_LI_TC221_ContentType_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.CONTENT_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 21;

    @Test(priority = 1)
    @Description("TC221: ContentType — Search")
    public void tc221_contentType_search() throws InterruptedException {
        softAssert = new SoftAssert("tc221_contentType_search", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
