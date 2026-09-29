package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 154 | FF-SORT-COL-episodeName-DESC: Sort "Episode name" DESC (order). */
public class TC_154_SORT_episodeName_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 154;
    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 154)
    @Description("TC-154 FF-SORT-COL-episodeName-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-154_FF-SORT-COL-episodeName-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
