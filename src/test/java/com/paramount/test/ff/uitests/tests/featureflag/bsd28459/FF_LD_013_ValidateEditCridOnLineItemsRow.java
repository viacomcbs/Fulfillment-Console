package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_013;

public class FF_LD_013_ValidateEditCridOnLineItemsRow extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_013)
    @Description("FF_LD_013 — Edit CRID on Line Items row")
    public void validateEditCridOnLineItemsRow() throws InterruptedException {
        leftFilterPanelUtil.navigateToTab(com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab.LINE_ITEMS);
        Thread.sleep(com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS);
        validationUtil.validateEditCridOnLineItemsRow(softAssert);
        softAssert.assertAll();
    }
}
