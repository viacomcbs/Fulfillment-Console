package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-348 | FF-LI-SORT-COL-fastTrack-DESC: Sort "Fast Track" descending on Line Items tab. */
public class TC_LI_348_SORT_fastTrack_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 348)
    @Description("TC-LI-348 FF-LI-SORT-COL-fastTrack-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-348_FF-LI-SORT-COL-fastTrack-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
