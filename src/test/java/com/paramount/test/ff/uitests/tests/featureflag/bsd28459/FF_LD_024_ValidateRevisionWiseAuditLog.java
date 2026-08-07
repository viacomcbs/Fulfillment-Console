package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_024;

public class FF_LD_024_ValidateRevisionWiseAuditLog extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_024)
    @Description("FF_LD_024 — Revision-wise audit log")
    public void validateRevisionWiseAuditLog() throws InterruptedException {
        validationUtil.validateRevisionWiseAuditLog(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
