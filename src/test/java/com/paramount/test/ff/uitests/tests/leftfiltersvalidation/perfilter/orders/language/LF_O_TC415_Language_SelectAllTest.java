package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.language;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC415 — Language Select all. */
public class LF_O_TC415_Language_SelectAllTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LANGUAGE.getDisplayName();
    private static final int FILTER_INDEX = 15;

    @Test(priority = 1)
    @Description("TC415: Language — Select all")
    public void tc415_languageSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc415_languageSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
