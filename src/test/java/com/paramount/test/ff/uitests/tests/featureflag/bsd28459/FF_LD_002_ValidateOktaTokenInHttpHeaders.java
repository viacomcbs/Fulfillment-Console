package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_002;

public class FF_LD_002_ValidateOktaTokenInHttpHeaders extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_002)
    @Description("FF_LD_002 — Okta token sent on API requests")
    public void validateOktaTokenInHttpHeaders() throws InterruptedException {
        validationUtil.validateOktaTokenInHttpHeaders(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
