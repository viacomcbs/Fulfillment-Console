package com.paramount.test.ff.uitests.tests.bsd30034;

import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public class TC_BSD_30034_006_SEARCH_deliveryDate extends BSD30034SearchBaseTest {

    private static final String COLUMN_NAME = "Delivery date";
    private static final String SECTION = "order";

    @Test(priority = 6)
    @Description("BSD-30034-006: Verify column search/filter on Delivery Date (Orders grid)")
    public void validateDeliveryDateColumnSearch() throws InterruptedException {
        initSoftAssert("BSD-30034-006_SEARCH_deliveryDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, true, "DATE");
        softAssert.assertAll();
    }
}