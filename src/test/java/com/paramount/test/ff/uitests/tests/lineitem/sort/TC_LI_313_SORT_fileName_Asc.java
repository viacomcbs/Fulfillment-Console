package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-313 | FF-LI-SORT-COL-fileName-ASC: Sort "File name" ascending on Line Items tab. */
public class TC_LI_313_SORT_fileName_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 313)
    @Description("TC-LI-313 FF-LI-SORT-COL-fileName-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-313_FF-LI-SORT-COL-fileName-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
