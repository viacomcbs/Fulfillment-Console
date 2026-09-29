package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-357 | FF-LI-SORT-COL-lastUpdated-ASC: Sort "Last updated" ascending on Line Items tab. */
public class TC_LI_357_SORT_lastUpdated_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "lastUpdated";
    private static final String COLUMN_NAME = "Last updated";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 357)
    @Description("TC-LI-357 FF-LI-SORT-COL-lastUpdated-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-357_FF-LI-SORT-COL-lastUpdated-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
