package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_003;

public class FF_LD_003_ValidateOrdersViewDynamicGraphql extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_003)
    @Description("FF_LD_003 — Orders view dynamic GraphQL")
    public void validateOrdersViewDynamicGraphql() throws InterruptedException {
        validationUtil.validateOrdersViewDynamicGraphql(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
