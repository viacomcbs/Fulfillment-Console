package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-341 | FF-LI-SORT-COL-currentSystem-ASC: Sort "Current system" ascending on Line Items tab. */
public class TC_LI_341_SORT_currentSystem_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 341)
    @Description("TC-LI-341 FF-LI-SORT-COL-currentSystem-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-341_FF-LI-SORT-COL-currentSystem-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
