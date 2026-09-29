package com.paramount.test.ff.uitests.tests.bsd29302;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_005_NonDeliveredAbsent extends BSD29302BaseTest {

    @Test(priority = 5)
    @Description("BSD-29302-005: Shipping Dock Package ID NOT shown for non-Delivered orders")
    public void validateAbsentForNonDeliveredOrders() throws InterruptedException {
        initSoftAssert("BSD-29302-005_NonDeliveredAbsent");
        packageDetailsUtil.ensureShippingDockPackageIdColumnEnabled();
        packageDetailsUtil.verifyShippingDockIdAbsentForNonDeliveredOrders();
        softAssert.assertAll();
    }
}