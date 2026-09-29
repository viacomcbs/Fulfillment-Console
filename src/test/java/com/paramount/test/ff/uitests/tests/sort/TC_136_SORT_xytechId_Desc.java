package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 136 | FF-SORT-COL-xytechId-DESC: Sort "Xytech ID" DESC (order). */
public class TC_136_SORT_xytechId_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 136;
    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 136)
    @Description("TC-136 FF-SORT-COL-xytechId-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-136_FF-SORT-COL-xytechId-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
