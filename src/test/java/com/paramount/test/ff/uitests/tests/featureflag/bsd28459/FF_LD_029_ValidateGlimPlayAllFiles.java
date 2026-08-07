package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_029;

public class FF_LD_029_ValidateGlimPlayAllFiles extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_029)
    @Description("FF_LD_029 — GLIM Play All files")
    public void validateGlimPlayAllFiles() throws InterruptedException {
        validationUtil.validateGlimPlayAllFiles(softAssert);
        softAssert.assertAll();
    }
}
