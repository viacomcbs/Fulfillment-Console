package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_019;

public class FF_LD_019_ValidateBigFilterCountsBehaviour extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_019)
    @Description("FF_LD_019 — Big filter counts behaviour")
    public void validateBigFilterCountsBehaviour() throws InterruptedException {
        validationUtil.validateBigFilterCountsBehaviour(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
