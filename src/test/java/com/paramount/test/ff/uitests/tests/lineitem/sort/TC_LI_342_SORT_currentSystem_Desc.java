package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-342 | FF-LI-SORT-COL-currentSystem-DESC: Sort "Current system" descending on Line Items tab. */
public class TC_LI_342_SORT_currentSystem_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 342)
    @Description("TC-LI-342 FF-LI-SORT-COL-currentSystem-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-342_FF-LI-SORT-COL-currentSystem-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
