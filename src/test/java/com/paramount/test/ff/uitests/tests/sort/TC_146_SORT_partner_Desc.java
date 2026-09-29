package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 146 | FF-SORT-COL-partner-DESC: Sort "Partner" DESC (order). */
public class TC_146_SORT_partner_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 146;
    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 146)
    @Description("TC-146 FF-SORT-COL-partner-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-146_FF-SORT-COL-partner-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
