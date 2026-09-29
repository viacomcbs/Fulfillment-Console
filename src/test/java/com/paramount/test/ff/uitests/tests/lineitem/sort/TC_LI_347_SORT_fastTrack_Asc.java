package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-347 | FF-LI-SORT-COL-fastTrack-ASC: Sort "Fast Track" ascending on Line Items tab. */
public class TC_LI_347_SORT_fastTrack_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 347)
    @Description("TC-LI-347 FF-LI-SORT-COL-fastTrack-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-347_FF-LI-SORT-COL-fastTrack-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
