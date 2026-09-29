package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-308 | FF-LI-SORT-COL-lineItemId-DESC: Sort "LineItem ID" descending on Line Items tab. */
public class TC_LI_308_SORT_lineItemId_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "lineItemId";
    private static final String COLUMN_NAME = "LineItem ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 308)
    @Description("TC-LI-308 FF-LI-SORT-COL-lineItemId-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-308_FF-LI-SORT-COL-lineItemId-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
