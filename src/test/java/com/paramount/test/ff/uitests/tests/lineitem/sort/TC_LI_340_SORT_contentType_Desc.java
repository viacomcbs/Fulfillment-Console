package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-340 | FF-LI-SORT-COL-contentType-DESC: Sort "Content type" descending on Line Items tab. */
public class TC_LI_340_SORT_contentType_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "contentType";
    private static final String COLUMN_NAME = "Content type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 340)
    @Description("TC-LI-340 FF-LI-SORT-COL-contentType-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-340_FF-LI-SORT-COL-contentType-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
