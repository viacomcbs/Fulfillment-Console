package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-320 | FF-LI-SORT-COL-xytechId-DESC: Sort "Xytech ID" descending on Line Items tab. */
public class TC_LI_320_SORT_xytechId_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 320)
    @Description("TC-LI-320 FF-LI-SORT-COL-xytechId-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-320_FF-LI-SORT-COL-xytechId-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
