package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-319 | FF-LI-SORT-COL-xytechId-ASC: Sort "Xytech ID" ascending on Line Items tab. */
public class TC_LI_319_SORT_xytechId_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 319)
    @Description("TC-LI-319 FF-LI-SORT-COL-xytechId-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-319_FF-LI-SORT-COL-xytechId-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
