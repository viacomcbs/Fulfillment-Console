package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 041 | FF-MC-COL-orderType-SHOW: Enable "Order Type" (order). */
public class TC_041_COL_orderType_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 41;
    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String SECTION = "order";

    @Test(priority = 41)
    @Description("TC-041 FF-MC-COL-orderType-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-041_FF-MC-COL-orderType-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
