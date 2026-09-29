package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 150 | FF-SORT-COL-partnerProfile-DESC: Sort "Partner Profile" DESC (order). */
public class TC_150_SORT_partnerProfile_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 150;
    private static final String COLUMN_ID = "partnerProfile";
    private static final String COLUMN_NAME = "Partner Profile";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 150)
    @Description("TC-150 FF-SORT-COL-partnerProfile-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-150_FF-SORT-COL-partnerProfile-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
