package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 155 | FF-SORT-COL-contentTypeOrder-ASC: Sort "Content type" ASC (order). */
public class TC_155_SORT_contentTypeOrder_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 155;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 155)
    @Description("TC-155 FF-SORT-COL-contentTypeOrder-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-155_FF-SORT-COL-contentTypeOrder-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
