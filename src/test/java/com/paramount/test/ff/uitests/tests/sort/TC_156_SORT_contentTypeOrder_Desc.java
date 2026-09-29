package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 156 | FF-SORT-COL-contentTypeOrder-DESC: Sort "Content type" DESC (order). */
public class TC_156_SORT_contentTypeOrder_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 156;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 156)
    @Description("TC-156 FF-SORT-COL-contentTypeOrder-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-156_FF-SORT-COL-contentTypeOrder-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
