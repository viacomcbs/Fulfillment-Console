package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 051 | FF-MC-COL-priority-SHOW: Enable "Priority" (order). */
public class TC_051_COL_priority_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 51;
    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String SECTION = "order";

    @Test(priority = 51)
    @Description("TC-051 FF-MC-COL-priority-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-051_FF-MC-COL-priority-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
