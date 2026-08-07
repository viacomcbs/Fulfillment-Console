package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_030;

public class FF_LD_030_ValidateTestEndpointConnection extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_030)
    @Description("FF_LD_030 — Test endpoint connection")
    public void validateTestEndpointConnection() throws InterruptedException {
        validationUtil.validateTestEndpointConnection(softAssert);
        softAssert.assertAll();
    }
}
