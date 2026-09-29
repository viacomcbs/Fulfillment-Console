package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-310 | FF-LI-SORT-COL-type-DESC: Sort "Type" descending on Line Items tab. */
public class TC_LI_310_SORT_type_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 310)
    @Description("TC-LI-310 FF-LI-SORT-COL-type-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-310_FF-LI-SORT-COL-type-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
