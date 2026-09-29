package com.paramount.test.ff.uitests.tests.bsd29174;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29174_001_TooltipVisibleOnHover extends BSD29174BaseTest {

    @Test(priority = 1)
    @Description("BSD-29174-001: Order status tooltip appears on hover in Orders listing")
    public void validateTooltipVisibleOnHover() throws InterruptedException {
        initSoftAssert("BSD-29174-001_TooltipVisibleOnHover");
        orderStatusTooltipUtil.verifyTooltipVisibleOnHover(
                KNOWN_ORDER_ID, PRIMARY_ORDER_ID, ALT_ORDER_ID);
        softAssert.assertAll();
    }
}
