package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC106 — Partner Basic. */
public class LF_O_TC106_Partner_BasicTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();
    private static final int FILTER_INDEX = 6;

    @Test(priority = 1)
    @Description("TC106: Partner — Basic")
    public void tc106_partnerBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc106_partnerBasic", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.BASIC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
