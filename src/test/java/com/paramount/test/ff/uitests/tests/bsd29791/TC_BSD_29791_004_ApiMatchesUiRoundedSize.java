package com.paramount.test.ff.uitests.tests.bsd29791;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29791_004_ApiMatchesUiRoundedSize extends BSD29791BaseTest {

    @Test(priority = 4)
    @Description("BSD-29791-004: UI rounded File Size matches archiveFileSize from filterOrders API")
    public void validateApiMatchesUiRoundedSize() throws InterruptedException {
        initSoftAssert("BSD-29791-004_ApiMatchesUiRoundedSize");
        tarSizeUtil.verifyUiRoundedMatchesApiBytes();
        softAssert.assertAll();
    }
}
