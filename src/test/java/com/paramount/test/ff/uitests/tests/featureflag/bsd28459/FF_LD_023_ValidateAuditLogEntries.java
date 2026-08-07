package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_023;

public class FF_LD_023_ValidateAuditLogEntries extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_023)
    @Description("FF_LD_023 — Audit log entries")
    public void validateAuditLogEntries() throws InterruptedException {
        validationUtil.validateAuditLogEntries(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
