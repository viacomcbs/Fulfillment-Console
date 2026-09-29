package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-358 | FF-LI-SORT-COL-lastUpdated-DESC: Sort "Last updated" descending on Line Items tab. */
public class TC_LI_358_SORT_lastUpdated_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "lastUpdated";
    private static final String COLUMN_NAME = "Last updated";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 358)
    @Description("TC-LI-358 FF-LI-SORT-COL-lastUpdated-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-358_FF-LI-SORT-COL-lastUpdated-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
