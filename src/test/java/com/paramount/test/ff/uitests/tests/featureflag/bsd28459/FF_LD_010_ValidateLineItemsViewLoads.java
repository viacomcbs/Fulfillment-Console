package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_010;

public class FF_LD_010_ValidateLineItemsViewLoads extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_010)
    @Description("FF_LD_010 — Line Items view loads")
    public void validateLineItemsViewLoads() throws InterruptedException {
        validationUtil.validateLineItemsViewLoads(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
