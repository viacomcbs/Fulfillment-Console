package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_009;

public class FF_LD_009_ValidateOrderHistoryV2InOrderDetails extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_009)
    @Description("FF_LD_009 — Order History v2 in Details")
    public void validateOrderHistoryV2InOrderDetails() throws InterruptedException {
        validationUtil.validateOrderHistoryV2InOrderDetails(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
