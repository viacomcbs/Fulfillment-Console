package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 025 | FF-MC-COL-orderStartDate-SHOW: Enable "Order start date" (order). */
public class TC_025_COL_orderStartDate_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 25;
    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String SECTION = "order";

    @Test(priority = 25)
    @Description("TC-025 FF-MC-COL-orderStartDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-025_FF-MC-COL-orderStartDate-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
