package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-356 | FF-LI-SORT-COL-language-DESC: Sort "Language" descending on Line Items tab. */
public class TC_LI_356_SORT_language_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 356)
    @Description("TC-LI-356 FF-LI-SORT-COL-language-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-356_FF-LI-SORT-COL-language-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
