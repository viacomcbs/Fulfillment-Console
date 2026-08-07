package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_025;

public class FF_LD_025_ValidateOlderLineItemDifferentiation extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_025)
    @Description("FF_LD_025 — Older line item differentiation")
    public void validateOlderLineItemDifferentiation() throws InterruptedException {
        validationUtil.validateOlderLineItemDifferentiation(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
