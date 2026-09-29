package com.paramount.test.ff.uitests.tests.bsd29302;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_007_CopyToClipboard extends BSD29302BaseTest {

    @Test(priority = 7)
    @Description("BSD-29302-007: Copy icon copies Shipping Dock Package ID to clipboard")
    public void validateCopyIconCopiesToClipboard() throws InterruptedException {
        initSoftAssert("BSD-29302-007_CopyToClipboard");
        packageDetailsUtil.selectDeliveredPackageRowWithShippingDockId();
        packageDetailsUtil.verifyCopyShippingDockIdToClipboard();
        softAssert.assertAll();
    }
}