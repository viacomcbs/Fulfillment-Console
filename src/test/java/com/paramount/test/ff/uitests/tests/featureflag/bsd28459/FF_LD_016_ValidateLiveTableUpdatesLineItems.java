package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_016;

public class FF_LD_016_ValidateLiveTableUpdatesLineItems extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_016)
    @Description("FF_LD_016 — Live table updates (LI)")
    public void validateLiveTableUpdatesLineItems() throws InterruptedException {
        validationUtil.validateLiveTableUpdatesLineItems(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
