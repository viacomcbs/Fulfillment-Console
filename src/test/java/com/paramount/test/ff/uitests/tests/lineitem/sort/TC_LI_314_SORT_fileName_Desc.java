package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-314 | FF-LI-SORT-COL-fileName-DESC: Sort "File name" descending on Line Items tab. */
public class TC_LI_314_SORT_fileName_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 314)
    @Description("TC-LI-314 FF-LI-SORT-COL-fileName-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-314_FF-LI-SORT-COL-fileName-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
