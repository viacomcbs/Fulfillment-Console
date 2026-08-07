package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_012;

public class FF_LD_012_ValidateMaterialIdInLineItemsGrid extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_012)
    @Description("FF_LD_012 — Material ID in Line Items grid")
    public void validateMaterialIdInLineItemsGrid() throws InterruptedException {
        leftFilterPanelUtil.navigateToTab(com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab.LINE_ITEMS);
        Thread.sleep(com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS);
        validationUtil.validateMaterialIdInLineItemsGrid(softAssert);
        softAssert.assertAll();
    }
}
