package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-303 | FF-LI-SORT-COL-season-ASC: Sort "Season" ascending on Line Items tab. */
public class TC_LI_303_SORT_season_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 303)
    @Description("TC-LI-303 FF-LI-SORT-COL-season-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-303_FF-LI-SORT-COL-season-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
