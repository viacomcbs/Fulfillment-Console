package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC110 — SeasonNumber Basic. */
public class LF_O_TC110_SeasonNumber_BasicTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SEASON_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 10;

    @Test(priority = 1)
    @Description("TC110: SeasonNumber — Basic")
    public void tc110_seasonNumberBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc110_seasonNumberBasic", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.BASIC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
