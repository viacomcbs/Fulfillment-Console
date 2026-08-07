package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_017;

public class FF_LD_017_ValidateRefactoredFilterPanelRenders extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_017)
    @Description("FF_LD_017 — Refactored filter panel")
    public void validateRefactoredFilterPanelRenders() throws InterruptedException {
        validationUtil.validateRefactoredFilterPanelRenders(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
