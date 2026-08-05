package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.language;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC116 — Language Option list order. */
public class LF_LI_TC116_Language_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.LANGUAGE.getDisplayName();
    private static final int FILTER_INDEX = 15;

    @Test(priority = 1)
    @Description("TC116: Language — Option list order")
    public void tc116_languageOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc116_languageOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
