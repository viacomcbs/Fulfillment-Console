package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_027;

public class FF_LD_027_ValidateUnifiedHistoryWithApproval extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_027)
    @Description("FF_LD_027 — Unified history with approval")
    public void validateUnifiedHistoryWithApproval() throws InterruptedException {
        validationUtil.validateUnifiedHistoryWithApproval(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
