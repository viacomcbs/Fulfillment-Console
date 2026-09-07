package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC109 — Flag option list order: Select All → Is not flagged → Is flagged → reason children.
 * Separator before Is flagged appears only when Is flagged count is 0.
 */
public class LF_O_TC109_Flag_OptionOrderTest extends LeftFilterFlagOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FLAG.getDisplayName();

    @Test(priority = 1)
    @Description("TC109: Flag — Option list order and Is flagged separator")
    public void tc109_flagOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc109_flagOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateFlagOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
