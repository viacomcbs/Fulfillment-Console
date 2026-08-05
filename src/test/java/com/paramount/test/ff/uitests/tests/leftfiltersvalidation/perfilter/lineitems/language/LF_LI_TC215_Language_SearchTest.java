package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.language;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC215 — Language Search. */
public class LF_LI_TC215_Language_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.LANGUAGE.getDisplayName();
    private static final int FILTER_INDEX = 15;

    @Test(priority = 1)
    @Description("TC215: Language — Search")
    public void tc215_languageSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc215_languageSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
