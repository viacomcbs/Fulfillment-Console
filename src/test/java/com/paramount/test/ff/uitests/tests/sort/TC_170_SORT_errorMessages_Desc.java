package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 170 | FF-SORT-COL-errorMessages-DESC: Sort "Error messages" DESC (order). */
public class TC_170_SORT_errorMessages_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 170;
    private static final String COLUMN_ID = "errorMessages";
    private static final String COLUMN_NAME = "Error messages";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 170)
    @Description("TC-170 FF-SORT-COL-errorMessages-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-170_FF-SORT-COL-errorMessages-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
