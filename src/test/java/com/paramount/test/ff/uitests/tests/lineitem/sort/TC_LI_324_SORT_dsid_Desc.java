package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-324 | FF-LI-SORT-COL-dsid-DESC: Sort "DSID" descending on Line Items tab. */
public class TC_LI_324_SORT_dsid_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 324)
    @Description("TC-LI-324 FF-LI-SORT-COL-dsid-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-324_FF-LI-SORT-COL-dsid-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
