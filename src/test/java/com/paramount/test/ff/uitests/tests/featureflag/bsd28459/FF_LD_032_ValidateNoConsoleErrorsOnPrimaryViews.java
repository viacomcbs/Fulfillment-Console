package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_032;

public class FF_LD_032_ValidateNoConsoleErrorsOnPrimaryViews extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_032)
    @Description("FF_LD_032 — No console errors on primary views")
    public void validateNoConsoleErrorsOnPrimaryViews() throws InterruptedException {
        validationUtil.validateNoConsoleErrorsOnPrimaryViews(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
