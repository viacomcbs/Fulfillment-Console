package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 065 | FF-MC-COL-deliveryOffset-SHOW: Enable "Delivery Offset" (order). */
public class TC_065_COL_deliveryOffset_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 65;
    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Delivery Offset";
    private static final String SECTION = "order";

    @Test(priority = 65)
    @Description("TC-065 FF-MC-COL-deliveryOffset-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-065_FF-MC-COL-deliveryOffset-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
