package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 176 | FF-SORT-COL-partnerGoLive-DESC: Sort "Partner go live" DESC (order). */
public class TC_176_SORT_partnerGoLive_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 176;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 176)
    @Description("TC-176 FF-SORT-COL-partnerGoLive-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-176_FF-SORT-COL-partnerGoLive-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
