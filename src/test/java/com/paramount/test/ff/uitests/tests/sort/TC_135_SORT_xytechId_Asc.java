package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 135 | FF-SORT-COL-xytechId-ASC: Sort "Xytech ID" ASC (order). */
public class TC_135_SORT_xytechId_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 135;
    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 135)
    @Description("TC-135 FF-SORT-COL-xytechId-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-135_FF-SORT-COL-xytechId-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
