package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-333 | FF-LI-SORT-COL-brand-ASC: Sort "Brand" ascending on Line Items tab. */
public class TC_LI_333_SORT_brand_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 333)
    @Description("TC-LI-333 FF-LI-SORT-COL-brand-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-333_FF-LI-SORT-COL-brand-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
