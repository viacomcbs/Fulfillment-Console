package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 158 | FF-SORT-COL-orderType-DESC: Sort "Order Type" DESC (order). */
public class TC_158_SORT_orderType_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 158;
    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 158)
    @Description("TC-158 FF-SORT-COL-orderType-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-158_FF-SORT-COL-orderType-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
