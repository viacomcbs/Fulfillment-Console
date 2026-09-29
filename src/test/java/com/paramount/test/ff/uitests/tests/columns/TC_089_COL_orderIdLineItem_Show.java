package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 089 | FF-MC-COL-orderIdLineItem-SHOW: Enable "Order ID" (lineitem). */
public class TC_089_COL_orderIdLineItem_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 89;
    private static final String COLUMN_ID = "orderIdLineItem";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 89)
    @Description("TC-089 FF-MC-COL-orderIdLineItem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-089_FF-MC-COL-orderIdLineItem-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
