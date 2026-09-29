package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-339 | FF-LI-SORT-COL-contentType-ASC: Sort "Content type" ascending on Line Items tab. */
public class TC_LI_339_SORT_contentType_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "contentType";
    private static final String COLUMN_NAME = "Content type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 339)
    @Description("TC-LI-339 FF-LI-SORT-COL-contentType-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-339_FF-LI-SORT-COL-contentType-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
