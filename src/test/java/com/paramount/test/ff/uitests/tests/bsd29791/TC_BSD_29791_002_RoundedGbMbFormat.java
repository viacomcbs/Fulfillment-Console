package com.paramount.test.ff.uitests.tests.bsd29791;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29791_002_RoundedGbMbFormat extends BSD29791BaseTest {

    @Test(priority = 2)
    @Description("BSD-29791-002: File Size label displays archive size rounded to GB or MB")
    public void validateRoundedGbMbFormat() throws InterruptedException {
        initSoftAssert("BSD-29791-002_RoundedGbMbFormat");
        tarSizeUtil.verifyRoundedGbMbFormat();
        softAssert.assertAll();
    }
}
