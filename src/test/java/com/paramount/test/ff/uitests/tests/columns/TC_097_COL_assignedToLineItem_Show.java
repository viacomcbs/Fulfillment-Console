package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 097 | FF-MC-COL-assignedToLineItem-SHOW: Enable "Assigned to" (lineitem). */
public class TC_097_COL_assignedToLineItem_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 97;
    private static final String COLUMN_ID = "assignedToLineItem";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "lineitem";

    @Test(priority = 97)
    @Description("TC-097 FF-MC-COL-assignedToLineItem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-097_FF-MC-COL-assignedToLineItem-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
