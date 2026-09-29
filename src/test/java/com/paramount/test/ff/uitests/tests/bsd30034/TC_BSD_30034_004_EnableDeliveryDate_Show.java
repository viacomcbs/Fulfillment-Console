package com.paramount.test.ff.uitests.tests.bsd30034;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_004_EnableDeliveryDate_Show extends BSD30034BaseTest {

    @Test(priority = 4)
    @Description("BSD-30034-004: Enable Delivery Date column - header visible on Orders grid")
    public void validateEnableDeliveryDateShowsHeader() throws InterruptedException {
        initSoftAssert("BSD-30034-004_EnableDeliveryDate_Show");
        runColumnShowTest(DELIVERY_DATE_NAME, SECTION);
        softAssert.assertAll();
    }
}