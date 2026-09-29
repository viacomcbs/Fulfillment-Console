package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 149 | FF-SORT-COL-partnerProfile-ASC: Sort "Partner Profile" ASC (order). */
public class TC_149_SORT_partnerProfile_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 149;
    private static final String COLUMN_ID = "partnerProfile";
    private static final String COLUMN_NAME = "Partner Profile";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 149)
    @Description("TC-149 FF-SORT-COL-partnerProfile-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-149_FF-SORT-COL-partnerProfile-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
