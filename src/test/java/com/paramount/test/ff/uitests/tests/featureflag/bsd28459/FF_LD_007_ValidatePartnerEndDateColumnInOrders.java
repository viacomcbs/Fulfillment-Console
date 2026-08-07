package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_007;

public class FF_LD_007_ValidatePartnerEndDateColumnInOrders extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_007)
    @Description("FF_LD_007 — Partner End Date column in Orders")
    public void validatePartnerEndDateColumnInOrders() throws InterruptedException {
        validationUtil.validatePartnerEndDateColumnInOrders(softAssert);
        softAssert.assertAll();
    }
}
