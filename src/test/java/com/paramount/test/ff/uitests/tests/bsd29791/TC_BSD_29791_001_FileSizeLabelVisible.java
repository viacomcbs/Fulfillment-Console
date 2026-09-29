package com.paramount.test.ff.uitests.tests.bsd29791;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29791_001_FileSizeLabelVisible extends BSD29791BaseTest {

    @Test(priority = 1)
    @Description("BSD-29791-001: Package details panel shows File Size (tarSize) for Delivered order")
    public void validateFileSizeLabelVisible() throws InterruptedException {
        initSoftAssert("BSD-29791-001_FileSizeLabelVisible");
        tarSizeUtil.verifyFileSizeLabelVisible();
        softAssert.assertAll();
    }
}
