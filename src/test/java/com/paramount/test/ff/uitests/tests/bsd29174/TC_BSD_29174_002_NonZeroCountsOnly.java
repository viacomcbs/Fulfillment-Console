package com.paramount.test.ff.uitests.tests.bsd29174;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29174_002_NonZeroCountsOnly extends BSD29174BaseTest {

    @Test(priority = 2)
    @Description("BSD-29174-002: Tooltip shows only non-zero LI status counts per wireframe")
    public void validateNonZeroCountsOnly() throws InterruptedException {
        initSoftAssert("BSD-29174-002_NonZeroCountsOnly");
        orderStatusTooltipUtil.verifyNonZeroCountsOnly(PRIMARY_ORDER_ID);
        orderStatusTooltipUtil.verifyNonZeroCountsOnly(ALT_ORDER_ID);
        softAssert.assertAll();
    }
}
