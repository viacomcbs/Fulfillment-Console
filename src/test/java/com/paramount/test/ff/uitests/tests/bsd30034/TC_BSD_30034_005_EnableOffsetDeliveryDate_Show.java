package com.paramount.test.ff.uitests.tests.bsd30034;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_005_EnableOffsetDeliveryDate_Show extends BSD30034BaseTest {

    @Test(priority = 5)
    @Description("BSD-30034-005: Enable Offset Delivery Date column - header visible on Orders grid")
    public void validateEnableOffsetDeliveryDateShowsHeader() throws InterruptedException {
        initSoftAssert("BSD-30034-005_EnableOffsetDeliveryDate_Show");
        runColumnShowTest(OFFSET_DELIVERY_DATE_NAME, SECTION);
        softAssert.assertAll();
    }
}