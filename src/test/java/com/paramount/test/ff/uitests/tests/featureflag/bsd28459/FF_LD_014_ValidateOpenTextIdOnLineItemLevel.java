package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_014;

public class FF_LD_014_ValidateOpenTextIdOnLineItemLevel extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_014)
    @Description("FF_LD_014 — Open Text ID on line item level")
    public void validateOpenTextIdOnLineItemLevel() throws InterruptedException {
        leftFilterPanelUtil.navigateToTab(com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab.LINE_ITEMS);
        Thread.sleep(com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS);
        validationUtil.validateOpenTextIdOnLineItemLevel(softAssert);
        softAssert.assertAll();
    }
}
