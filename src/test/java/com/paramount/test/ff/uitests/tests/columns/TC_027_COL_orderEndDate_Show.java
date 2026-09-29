package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 027 | FF-MC-COL-orderEndDate-SHOW: Enable "Order end date" (order). */
public class TC_027_COL_orderEndDate_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 27;
    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String SECTION = "order";

    @Test(priority = 27)
    @Description("TC-027 FF-MC-COL-orderEndDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-027_FF-MC-COL-orderEndDate-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
