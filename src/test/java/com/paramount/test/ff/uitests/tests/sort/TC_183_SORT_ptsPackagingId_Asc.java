package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 183 | FF-SORT-COL-ptsPackagingId-ASC: Sort "PTS Packaging ID" ASC (order). */
public class TC_183_SORT_ptsPackagingId_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 183;
    private static final String COLUMN_ID = "ptsPackagingId";
    private static final String COLUMN_NAME = "PTS Packaging ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 183)
    @Description("TC-183 FF-SORT-COL-ptsPackagingId-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-183_FF-SORT-COL-ptsPackagingId-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
