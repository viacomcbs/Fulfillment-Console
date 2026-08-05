package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC806 — Partner Scroll. */
public class LF_LI_TC806_Partner_ScrollTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.PARTNER.getDisplayName();
    private static final int FILTER_INDEX = 6;

    @Test(priority = 1)
    @Description("TC806: Partner — Scroll")
    public void tc806_partnerScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc806_partnerScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
