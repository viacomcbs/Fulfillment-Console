package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 047 | FF-MC-COL-assignedToOrder-SHOW: Enable "Assigned to" (order). */
public class TC_047_COL_assignedToOrder_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 47;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";

    @Test(priority = 47)
    @Description("TC-047 FF-MC-COL-assignedToOrder-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-047_FF-MC-COL-assignedToOrder-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
