package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 043 | FF-MC-COL-lastUpdatedOrder-SHOW: Enable "Last updated" (order). */
public class TC_043_COL_lastUpdatedOrder_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 43;
    private static final String COLUMN_ID = "lastUpdatedOrder";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "order";

    @Test(priority = 43)
    @Description("TC-043 FF-MC-COL-lastUpdatedOrder-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-043_FF-MC-COL-lastUpdatedOrder-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
