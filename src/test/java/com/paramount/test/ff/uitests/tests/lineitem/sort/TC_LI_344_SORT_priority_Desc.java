package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-344 | FF-LI-SORT-COL-priority-DESC: Sort "Priority" descending on Line Items tab. */
public class TC_LI_344_SORT_priority_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 344)
    @Description("TC-LI-344 FF-LI-SORT-COL-priority-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-344_FF-LI-SORT-COL-priority-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
