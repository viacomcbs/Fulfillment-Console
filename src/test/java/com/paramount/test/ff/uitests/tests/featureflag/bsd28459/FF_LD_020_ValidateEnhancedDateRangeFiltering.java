package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_020;

public class FF_LD_020_ValidateEnhancedDateRangeFiltering extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_020)
    @Description("FF_LD_020 — Enhanced date range filtering")
    public void validateEnhancedDateRangeFiltering() throws InterruptedException {
        validationUtil.validateEnhancedDateRangeFiltering(softAssert);
        softAssert.assertAll();
    }
}
