package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_022;

public class FF_LD_022_ValidateHistoryLogFunctionality extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_022)
    @Description("FF_LD_022 — History log functionality")
    public void validateHistoryLogFunctionality() throws InterruptedException {
        validationUtil.validateHistoryLogFunctionality(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
