package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 177 | FF-SORT-COL-partnerEndDate-ASC: Sort "Partner end date" ASC (order). */
public class TC_177_SORT_partnerEndDate_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 177;
    private static final String COLUMN_ID = "partnerEndDate";
    private static final String COLUMN_NAME = "Partner end date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 177)
    @Description("TC-177 FF-SORT-COL-partnerEndDate-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-177_FF-SORT-COL-partnerEndDate-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
