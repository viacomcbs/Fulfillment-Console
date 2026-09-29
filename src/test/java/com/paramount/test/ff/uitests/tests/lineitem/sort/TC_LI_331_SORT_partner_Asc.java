package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-331 | FF-LI-SORT-COL-partner-ASC: Sort "Partner" ascending on Line Items tab. */
public class TC_LI_331_SORT_partner_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 331)
    @Description("TC-LI-331 FF-LI-SORT-COL-partner-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-331_FF-LI-SORT-COL-partner-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
