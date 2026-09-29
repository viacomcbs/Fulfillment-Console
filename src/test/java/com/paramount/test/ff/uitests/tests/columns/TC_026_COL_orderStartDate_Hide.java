package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 026 | FF-MC-COL-orderStartDate-HIDE: Disable "Order start date" (order). */
public class TC_026_COL_orderStartDate_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 26;
    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String SECTION = "order";

    @Test(priority = 26)
    @Description("TC-026 FF-MC-COL-orderStartDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-026_FF-MC-COL-orderStartDate-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
