package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 139 | FF-SORT-COL-dsid-ASC: Sort "DSID" ASC (order). */
public class TC_139_SORT_dsid_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 139;
    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 139)
    @Description("TC-139 FF-SORT-COL-dsid-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-139_FF-SORT-COL-dsid-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
