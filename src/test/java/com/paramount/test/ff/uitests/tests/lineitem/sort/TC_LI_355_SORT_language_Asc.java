package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-355 | FF-LI-SORT-COL-language-ASC: Sort "Language" ascending on Line Items tab. */
public class TC_LI_355_SORT_language_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 355)
    @Description("TC-LI-355 FF-LI-SORT-COL-language-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-355_FF-LI-SORT-COL-language-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
