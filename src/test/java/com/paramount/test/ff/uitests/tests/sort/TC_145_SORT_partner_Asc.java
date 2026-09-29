package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 145 | FF-SORT-COL-partner-ASC: Sort "Partner" ASC (order). */
public class TC_145_SORT_partner_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 145;
    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 145)
    @Description("TC-145 FF-SORT-COL-partner-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-145_FF-SORT-COL-partner-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
