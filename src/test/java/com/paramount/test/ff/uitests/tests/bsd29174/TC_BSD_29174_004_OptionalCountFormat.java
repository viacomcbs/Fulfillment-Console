package com.paramount.test.ff.uitests.tests.bsd29174;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29174_004_OptionalCountFormat extends BSD29174BaseTest {

    @Test(priority = 4)
    @Description("BSD-29174-004: Optional LI counts displayed as 'Status (optional) - count'")
    public void validateOptionalCountFormat() throws InterruptedException {
        initSoftAssert("BSD-29174-004_OptionalCountFormat");
        orderStatusTooltipUtil.verifyOptionalCountFormat(PRIMARY_ORDER_ID);
        orderStatusTooltipUtil.verifyOptionalCountFormat(ALT_ORDER_ID);
        softAssert.assertAll();
    }
}
