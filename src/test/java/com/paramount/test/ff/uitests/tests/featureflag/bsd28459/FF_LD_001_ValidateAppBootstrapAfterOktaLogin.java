package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_001;

public class FF_LD_001_ValidateAppBootstrapAfterOktaLogin extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_001)
    @Description("FF_LD_001 — App bootstrap after Okta login")
    public void validateAppBootstrapAfterOktaLogin() throws InterruptedException {
        validationUtil.validateAppBootstrapAfterOktaLogin(softAssert);
        softAssert.assertAll();
    }
}
