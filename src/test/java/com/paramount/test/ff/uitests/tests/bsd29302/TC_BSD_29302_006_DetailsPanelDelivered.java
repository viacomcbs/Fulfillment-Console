package com.paramount.test.ff.uitests.tests.bsd29302;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_006_DetailsPanelDelivered extends BSD29302BaseTest {

    @Test(priority = 6)
    @Description("BSD-29302-006: Details panel shows Shipping Dock Package ID for Delivered order")
    public void validateDetailsPanelField() throws InterruptedException {
        initSoftAssert("BSD-29302-006_DetailsPanelDelivered");
        packageDetailsUtil.verifyDetailsPanelShippingDockField();
        softAssert.assertAll();
    }
}