package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 175 | FF-SORT-COL-partnerGoLive-ASC: Sort "Partner go live" ASC (order). */
public class TC_175_SORT_partnerGoLive_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 175;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 175)
    @Description("TC-175 FF-SORT-COL-partnerGoLive-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-175_FF-SORT-COL-partnerGoLive-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
