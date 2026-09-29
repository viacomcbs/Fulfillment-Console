package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 098 | FF-MC-COL-assignedToLineItem-HIDE: Disable "Assigned to" (lineitem). */
public class TC_098_COL_assignedToLineItem_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 98;
    private static final String COLUMN_ID = "assignedToLineItem";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "lineitem";

    @Test(priority = 98)
    @Description("TC-098 FF-MC-COL-assignedToLineItem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-098_FF-MC-COL-assignedToLineItem-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
