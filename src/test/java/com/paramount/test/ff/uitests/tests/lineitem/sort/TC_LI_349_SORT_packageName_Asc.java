package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-349 | FF-LI-SORT-COL-packageName-ASC: Sort "Package name" ascending on Line Items tab. */
public class TC_LI_349_SORT_packageName_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "packageName";
    private static final String COLUMN_NAME = "Package name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 349)
    @Description("TC-LI-349 FF-LI-SORT-COL-packageName-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-349_FF-LI-SORT-COL-packageName-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
