package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-307 | FF-LI-SORT-COL-lineItemId-ASC: Sort "LineItem ID" ascending on Line Items tab. */
public class TC_LI_307_SORT_lineItemId_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "lineItemId";
    private static final String COLUMN_NAME = "LineItem ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 307)
    @Description("TC-LI-307 FF-LI-SORT-COL-lineItemId-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-307_FF-LI-SORT-COL-lineItemId-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
