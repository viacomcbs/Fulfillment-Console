package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 063 | FF-MC-COL-deliveryProtocol-SHOW: Enable "Delivery Protocol" (order). */
public class TC_063_COL_deliveryProtocol_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 63;
    private static final String COLUMN_ID = "deliveryProtocol";
    private static final String COLUMN_NAME = "Delivery Protocol";
    private static final String SECTION = "order";

    @Test(priority = 63)
    @Description("TC-063 FF-MC-COL-deliveryProtocol-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-063_FF-MC-COL-deliveryProtocol-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
