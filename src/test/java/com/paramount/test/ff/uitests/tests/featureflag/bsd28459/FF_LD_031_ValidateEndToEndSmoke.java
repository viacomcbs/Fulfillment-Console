package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_031;

public class FF_LD_031_ValidateEndToEndSmoke extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_031)
    @Description("FF_LD_031 — End-to-end smoke")
    public void validateEndToEndSmoke() throws InterruptedException {
        validationUtil.validateEndToEndSmoke(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
