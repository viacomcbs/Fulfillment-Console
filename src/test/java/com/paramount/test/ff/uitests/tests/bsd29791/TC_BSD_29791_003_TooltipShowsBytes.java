package com.paramount.test.ff.uitests.tests.bsd29791;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29791_003_TooltipShowsBytes extends BSD29791BaseTest {

    @Test(priority = 3)
    @Description("BSD-29791-003: File Size tooltip shows original archiveFileSize in bytes on hover")
    public void validateTooltipShowsBytes() throws InterruptedException {
        initSoftAssert("BSD-29791-003_TooltipShowsBytes");
        tarSizeUtil.verifyTooltipShowsBytes();
        softAssert.assertAll();
    }
}
