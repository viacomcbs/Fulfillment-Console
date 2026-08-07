package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_015;

public class FF_LD_015_ValidateLiveFilterCountUpdatesLineItems extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_015)
    @Description("FF_LD_015 — Live filter count updates (LI)")
    public void validateLiveFilterCountUpdatesLineItems() throws InterruptedException {
        validationUtil.validateLiveFilterCountUpdatesLineItems(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
