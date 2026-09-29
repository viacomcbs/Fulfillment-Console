package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-309 | FF-LI-SORT-COL-type-ASC: Sort "Type" ascending on Line Items tab. */
public class TC_LI_309_SORT_type_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 309)
    @Description("TC-LI-309 FF-LI-SORT-COL-type-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-309_FF-LI-SORT-COL-type-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
