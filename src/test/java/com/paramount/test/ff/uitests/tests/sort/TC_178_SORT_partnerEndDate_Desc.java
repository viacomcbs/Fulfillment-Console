package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 178 | FF-SORT-COL-partnerEndDate-DESC: Sort "Partner end date" DESC (order). */
public class TC_178_SORT_partnerEndDate_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 178;
    private static final String COLUMN_ID = "partnerEndDate";
    private static final String COLUMN_NAME = "Partner end date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 178)
    @Description("TC-178 FF-SORT-COL-partnerEndDate-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-178_FF-SORT-COL-partnerEndDate-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
