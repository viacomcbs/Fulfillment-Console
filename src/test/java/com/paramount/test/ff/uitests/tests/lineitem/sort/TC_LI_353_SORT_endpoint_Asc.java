package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-353 | FF-LI-SORT-COL-endpoint-ASC: Sort "Endpoint" ascending on Line Items tab. */
public class TC_LI_353_SORT_endpoint_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "endpoint";
    private static final String COLUMN_NAME = "Endpoint";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 353)
    @Description("TC-LI-353 FF-LI-SORT-COL-endpoint-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-353_FF-LI-SORT-COL-endpoint-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
