package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 179 | FF-SORT-COL-deliveryProtocol-ASC: Sort "Delivery Protocol" ASC (order). */
public class TC_179_SORT_deliveryProtocol_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 179;
    private static final String COLUMN_ID = "deliveryProtocol";
    private static final String COLUMN_NAME = "Delivery Protocol";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 179)
    @Description("TC-179 FF-SORT-COL-deliveryProtocol-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-179_FF-SORT-COL-deliveryProtocol-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
