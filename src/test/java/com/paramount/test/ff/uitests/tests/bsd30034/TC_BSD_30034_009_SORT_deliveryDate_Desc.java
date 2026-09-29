package com.paramount.test.ff.uitests.tests.bsd30034;


import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public class TC_BSD_30034_009_SORT_deliveryDate_Desc extends BSD30034SortBaseTest {

    private static final String COLUMN_NAME = "Delivery date";
    private static final String SECTION = "order";

    @Test(priority = 9)
    @Description("BSD-30034-009: Verify descending sort on Delivery Date column (Orders grid)")
    public void validateDeliveryDateSortDescending() throws InterruptedException {
        initSoftAssert("BSD-30034-009_SORT_deliveryDate_Desc");
        runSortDescendingTest(COLUMN_NAME, SECTION, "DATE");
        softAssert.assertAll();
    }
}
