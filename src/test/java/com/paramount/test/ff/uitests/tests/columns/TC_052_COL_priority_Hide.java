package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 052 | FF-MC-COL-priority-HIDE: Disable "Priority" (order). */
public class TC_052_COL_priority_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 52;
    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String SECTION = "order";

    @Test(priority = 52)
    @Description("TC-052 FF-MC-COL-priority-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-052_FF-MC-COL-priority-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
