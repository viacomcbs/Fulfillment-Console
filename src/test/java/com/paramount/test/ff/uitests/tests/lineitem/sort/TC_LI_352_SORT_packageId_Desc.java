package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-352 | FF-LI-SORT-COL-packageId-DESC: Sort "Package ID" descending on Line Items tab. */
public class TC_LI_352_SORT_packageId_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "packageId";
    private static final String COLUMN_NAME = "Package ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 352)
    @Description("TC-LI-352 FF-LI-SORT-COL-packageId-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-352_FF-LI-SORT-COL-packageId-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
