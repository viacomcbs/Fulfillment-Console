package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 249 | FF-SEARCH-COL-NEG-assignedToOrder: Negative column search for "Assigned to" (order). */
public class TC_249_SEARCH_NEG_assignedToOrder extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 249;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 249)
    @Description("TC-249 FF-SEARCH-COL-NEG-assignedToOrder: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-249_FF-SEARCH-COL-NEG-assignedToOrder");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
