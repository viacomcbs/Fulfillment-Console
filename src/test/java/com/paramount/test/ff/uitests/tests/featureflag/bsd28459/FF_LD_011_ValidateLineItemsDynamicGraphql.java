package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_011;

public class FF_LD_011_ValidateLineItemsDynamicGraphql extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_011)
    @Description("FF_LD_011 — Line Items dynamic GraphQL")
    public void validateLineItemsDynamicGraphql() throws InterruptedException {
        validationUtil.validateLineItemsDynamicGraphql(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
