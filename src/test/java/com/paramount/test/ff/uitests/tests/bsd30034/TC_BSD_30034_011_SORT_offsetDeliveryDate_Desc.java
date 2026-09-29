package com.paramount.test.ff.uitests.tests.bsd30034;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public class TC_BSD_30034_011_SORT_offsetDeliveryDate_Desc extends BSD30034SortBaseTest {

    private static final String COLUMN_NAME = "Offset Delivery date";
    private static final String SECTION = "order";

    @Test(priority = 11)
    @Description("BSD-30034-011: Verify descending sort on Offset Delivery Date column (Orders grid)")
    public void validateOffsetDeliveryDateSortDescending() throws InterruptedException {
        initSoftAssert("BSD-30034-011_SORT_offsetDeliveryDate_Desc");
        runSortDescendingTest(COLUMN_NAME, SECTION, "DATE");
        softAssert.assertAll();
    }
}
