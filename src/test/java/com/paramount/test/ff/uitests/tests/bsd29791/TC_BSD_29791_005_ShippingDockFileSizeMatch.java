package com.paramount.test.ff.uitests.tests.bsd29791;

import com.paramount.test.ff.common.loginUtil.Verify;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29791_005_ShippingDockFileSizeMatch extends BSD29791BaseTest {

    @Test(priority = 5)
    @Description("BSD-29791-005: File Size in FF Console matches Shipping Dock Delivery Activity (UI only)")
    public void validateShippingDockFileSizeMatchesFfConsole() throws InterruptedException {
        initSoftAssert("BSD-29791-005_ShippingDockFileSizeMatch");

        Long sdBytes = shippingDockUtil.readArchiveFileSizeBytesFromShippingDock(PROD_ORDER_ID);
        Verify.softAssert(sdBytes != null && sdBytes > 0,
                "Shipping Dock ARCHIVE_COMPLETE file size read from UI for order " + PROD_ORDER_ID
                        + " (bytes: " + sdBytes + ")");

        if (sdBytes != null && sdBytes > 0) {
            launchFulfillmentConsole();
            shippingDockUtil.compareFileSizeWithFfConsole(tarSizeUtil, sdBytes, null);
        }

        softAssert.assertAll();
    }
}
