package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 017 | FF-MC-COL-orderId-SHOW: Enable "Order ID" (order). */
public class TC_017_COL_orderId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 17;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";

    @Test(priority = 17)
    @Description("TC-017 FF-MC-COL-orderId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-017_FF-MC-COL-orderId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
