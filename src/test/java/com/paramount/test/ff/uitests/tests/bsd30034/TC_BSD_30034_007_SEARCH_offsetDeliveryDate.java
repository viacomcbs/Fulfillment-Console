package com.paramount.test.ff.uitests.tests.bsd30034;


import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;

@Link(name = "BSD-30034", url = "https://paramount.atlassian.net/browse/BSD-30034")
public class TC_BSD_30034_007_SEARCH_offsetDeliveryDate extends BSD30034SearchBaseTest {

    private static final String COLUMN_NAME = "Offset Delivery date";
    private static final String SECTION = "order";

    @Test(priority = 7)
    @Description("BSD-30034-007: Verify column search/filter on Offset Delivery Date (Orders grid)")
    public void validateOffsetDeliveryDateColumnSearch() throws InterruptedException {
        initSoftAssert("BSD-30034-007_SEARCH_offsetDeliveryDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, true, "DATE");
        softAssert.assertAll();
    }
}
