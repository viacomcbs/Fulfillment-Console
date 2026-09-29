package com.paramount.test.ff.uitests.tests.bsd29302;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_004_PackageLevelDelivered extends BSD29302BaseTest {

    @Test(priority = 4)
    @Description("BSD-29302-004: Shipping Dock Package ID shown on Delivered order package row")
    public void validatePackageLevelOnDeliveredOrder() throws InterruptedException {
        initSoftAssert("BSD-29302-004_PackageLevelDelivered");
        packageDetailsUtil.verifyPackageLevelShippingDockIdDisplayed();
        softAssert.assertAll();
    }
}