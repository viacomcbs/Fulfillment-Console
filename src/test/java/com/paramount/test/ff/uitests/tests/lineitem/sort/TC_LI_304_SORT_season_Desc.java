package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-304 | FF-LI-SORT-COL-season-DESC: Sort "Season" descending on Line Items tab. */
public class TC_LI_304_SORT_season_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 304)
    @Description("TC-LI-304 FF-LI-SORT-COL-season-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-304_FF-LI-SORT-COL-season-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
