package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-343 | FF-LI-SORT-COL-priority-ASC: Sort "Priority" ascending on Line Items tab. */
public class TC_LI_343_SORT_priority_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 343)
    @Description("TC-LI-343 FF-LI-SORT-COL-priority-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-343_FF-LI-SORT-COL-priority-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
