package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_008;

public class FF_LD_008_ValidateUuidDisplayedInOrdersView extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_008)
    @Description("FF_LD_008 — UUID displayed in Orders view")
    public void validateUuidDisplayedInOrdersView() throws InterruptedException {
        validationUtil.validateUuidDisplayedInOrdersView(softAssert);
        softAssert.assertAll();
    }
}
