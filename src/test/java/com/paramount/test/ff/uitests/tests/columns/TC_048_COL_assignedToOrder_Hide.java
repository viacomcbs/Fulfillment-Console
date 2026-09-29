package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 048 | FF-MC-COL-assignedToOrder-HIDE: Disable "Assigned to" (order). */
public class TC_048_COL_assignedToOrder_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 48;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";

    @Test(priority = 48)
    @Description("TC-048 FF-MC-COL-assignedToOrder-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-048_FF-MC-COL-assignedToOrder-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
