package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 153 | FF-SORT-COL-episodeName-ASC: Sort "Episode name" ASC (order). */
public class TC_153_SORT_episodeName_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 153;
    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 153)
    @Description("TC-153 FF-SORT-COL-episodeName-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-153_FF-SORT-COL-episodeName-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
