package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-321 | FF-LI-SORT-COL-assetId-ASC: Sort "Asset ID" ascending on Line Items tab. */
public class TC_LI_321_SORT_assetId_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 321)
    @Description("TC-LI-321 FF-LI-SORT-COL-assetId-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-321_FF-LI-SORT-COL-assetId-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
