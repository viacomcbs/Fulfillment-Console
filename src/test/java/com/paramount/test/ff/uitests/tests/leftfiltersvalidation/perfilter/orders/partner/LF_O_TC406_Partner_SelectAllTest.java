package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.PARTNER_SELECT_ALL_SEARCH_TEXT;

/**
 * TC406 — Partner Select all (large list):
 * disabled when total options &gt; 1000 + tooltip on hover; enabled after in-filter search narrows options.
 */
public class LF_O_TC406_Partner_SelectAllTest extends LeftFilterPartnerOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 1)
    @Description("TC406: Partner — Select all disabled >1000, tooltip on hover, enabled after search")
    public void tc406_partnerSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc406_partnerSelectAll", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validatePartnerSelectAllSmoke(softAssert, FILTER, PARTNER_SELECT_ALL_SEARCH_TEXT);
        softAssert.assertAll();
    }
}
