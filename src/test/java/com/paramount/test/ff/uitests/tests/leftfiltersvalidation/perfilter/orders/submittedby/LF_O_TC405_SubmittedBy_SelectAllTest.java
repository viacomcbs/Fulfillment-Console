package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC405 — SubmittedBy Select all. */
public class LF_O_TC405_SubmittedBy_SelectAllTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SUBMITTED_BY.getDisplayName();
    private static final int FILTER_INDEX = 5;

    @Test(priority = 1)
    @Description("TC405: SubmittedBy — Select all")
    public void tc405_submittedBySelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc405_submittedBySelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
