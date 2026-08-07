package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_021;

public class FF_LD_021_ValidateDateRangeCustomization extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_021)
    @Description("FF_LD_021 — Date range customization")
    public void validateDateRangeCustomization() throws InterruptedException {
        validationUtil.validateDateRangeCustomization(softAssert);
        softAssert.assertAll();
    }
}
