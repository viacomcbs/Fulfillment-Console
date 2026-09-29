package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-322 | FF-LI-SORT-COL-assetId-DESC: Sort "Asset ID" descending on Line Items tab. */
public class TC_LI_322_SORT_assetId_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 322)
    @Description("TC-LI-322 FF-LI-SORT-COL-assetId-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-322_FF-LI-SORT-COL-assetId-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
