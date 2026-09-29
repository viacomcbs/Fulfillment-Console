package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-351 | FF-LI-SORT-COL-packageId-ASC: Sort "Package ID" ascending on Line Items tab. */
public class TC_LI_351_SORT_packageId_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "packageId";
    private static final String COLUMN_NAME = "Package ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 351)
    @Description("TC-LI-351 FF-LI-SORT-COL-packageId-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-351_FF-LI-SORT-COL-packageId-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
