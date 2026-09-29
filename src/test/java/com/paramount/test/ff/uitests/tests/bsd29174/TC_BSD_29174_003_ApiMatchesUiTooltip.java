package com.paramount.test.ff.uitests.tests.bsd29174;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29174_003_ApiMatchesUiTooltip extends BSD29174BaseTest {

    @Test(priority = 3)
    @Description("BSD-29174-003: UI tooltip matches filterOrders statusTooltipDetails API response")
    public void validateApiMatchesUiTooltip() throws InterruptedException {
        initSoftAssert("BSD-29174-003_ApiMatchesUiTooltip");
        orderStatusTooltipUtil.verifyApiMatchesUiTooltip(PRIMARY_ORDER_ID);
        softAssert.assertAll();
    }
}
