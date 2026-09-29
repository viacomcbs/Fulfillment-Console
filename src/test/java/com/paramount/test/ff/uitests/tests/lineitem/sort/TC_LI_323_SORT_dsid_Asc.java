package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-323 | FF-LI-SORT-COL-dsid-ASC: Sort "DSID" ascending on Line Items tab. */
public class TC_LI_323_SORT_dsid_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 323)
    @Description("TC-LI-323 FF-LI-SORT-COL-dsid-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-323_FF-LI-SORT-COL-dsid-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
