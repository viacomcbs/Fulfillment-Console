package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 184 | FF-SORT-COL-ptsPackagingId-DESC: Sort "PTS Packaging ID" DESC (order). */
public class TC_184_SORT_ptsPackagingId_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 184;
    private static final String COLUMN_ID = "ptsPackagingId";
    private static final String COLUMN_NAME = "PTS Packaging ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 184)
    @Description("TC-184 FF-SORT-COL-ptsPackagingId-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-184_FF-SORT-COL-ptsPackagingId-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
