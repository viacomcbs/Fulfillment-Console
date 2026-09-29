package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-360 | FF-LI-SORT-COL-startTime-DESC: Sort "Start time" descending on Line Items tab. */
public class TC_LI_360_SORT_startTime_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "startTime";
    private static final String COLUMN_NAME = "Start time";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 360)
    @Description("TC-LI-360 FF-LI-SORT-COL-startTime-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-360_FF-LI-SORT-COL-startTime-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
