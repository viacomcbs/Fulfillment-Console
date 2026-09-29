package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC107 — Partner option list order (first viewport only; alphabetical sample = first 3 labels).
 */
public class LF_O_TC107_Partner_OptionOrderTest extends LeftFilterPartnerOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 1)
    @Description("TC107: Partner — Select all first; first 3 visible options alphabetical; non-zero before zero")
    public void tc107_partnerOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc107_partnerOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
