package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-359 | FF-LI-SORT-COL-startTime-ASC: Sort "Start time" ascending on Line Items tab. */
public class TC_LI_359_SORT_startTime_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "startTime";
    private static final String COLUMN_NAME = "Start time";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 359)
    @Description("TC-LI-359 FF-LI-SORT-COL-startTime-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-359_FF-LI-SORT-COL-startTime-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
