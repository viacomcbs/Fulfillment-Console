package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_028;

public class FF_LD_028_ValidateGlimSidecarFilesLoad extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_028)
    @Description("FF_LD_028 — GLIM sidecar files load")
    public void validateGlimSidecarFilesLoad() throws InterruptedException {
        validationUtil.validateGlimSidecarFilesLoad(softAssert);
        softAssert.assertAll();
    }
}
