package com.paramount.test.ff.uitests.tests.bsd30034;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public class TC_BSD_30034_010_SORT_offsetDeliveryDate_Asc extends BSD30034SortBaseTest {

    private static final String COLUMN_NAME = "Offset Delivery date";
    private static final String SECTION = "order";

    @Test(priority = 10)
    @Description("BSD-30034-010: Verify ascending sort on Offset Delivery Date column (Orders grid)")
    public void validateOffsetDeliveryDateSortAscending() throws InterruptedException {
        initSoftAssert("BSD-30034-010_SORT_offsetDeliveryDate_Asc");
        runSortAscendingTest(COLUMN_NAME, SECTION, "DATE");
        softAssert.assertAll();
    }
}
