package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-334 | FF-LI-SORT-COL-brand-DESC: Sort "Brand" descending on Line Items tab. */
public class TC_LI_334_SORT_brand_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 334)
    @Description("TC-LI-334 FF-LI-SORT-COL-brand-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-334_FF-LI-SORT-COL-brand-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
