package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_026;

public class FF_LD_026_ValidateRevisedStatusesDisplayed extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_026)
    @Description("FF_LD_026 — Revised statuses displayed")
    public void validateRevisedStatusesDisplayed() throws InterruptedException {
        validationUtil.validateRevisedStatusesDisplayed(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
