package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-350 | FF-LI-SORT-COL-packageName-DESC: Sort "Package name" descending on Line Items tab. */
public class TC_LI_350_SORT_packageName_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "packageName";
    private static final String COLUMN_NAME = "Package name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 350)
    @Description("TC-LI-350 FF-LI-SORT-COL-packageName-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-350_FF-LI-SORT-COL-packageName-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
