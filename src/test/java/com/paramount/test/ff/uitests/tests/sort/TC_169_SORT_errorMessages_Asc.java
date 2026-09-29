package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 169 | FF-SORT-COL-errorMessages-ASC: Sort "Error messages" ASC (order). */
public class TC_169_SORT_errorMessages_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 169;
    private static final String COLUMN_ID = "errorMessages";
    private static final String COLUMN_NAME = "Error messages";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 169)
    @Description("TC-169 FF-SORT-COL-errorMessages-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-169_FF-SORT-COL-errorMessages-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
