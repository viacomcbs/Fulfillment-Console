package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 151 | FF-SORT-COL-submittedBy-ASC: Sort "Submitted by" ASC (order). */
public class TC_151_SORT_submittedBy_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 151;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 151)
    @Description("TC-151 FF-SORT-COL-submittedBy-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-151_FF-SORT-COL-submittedBy-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
