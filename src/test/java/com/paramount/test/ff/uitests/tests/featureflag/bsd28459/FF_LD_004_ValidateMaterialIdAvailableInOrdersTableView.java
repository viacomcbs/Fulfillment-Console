package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_004;

public class FF_LD_004_ValidateMaterialIdAvailableInOrdersTableView extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_004)
    @Description("FF_LD_004 — Material ID in Orders Table View")
    public void validateMaterialIdAvailableInOrdersTableView() throws InterruptedException {
        validationUtil.validateMaterialIdAvailableInOrdersTableView(softAssert);
        softAssert.assertAll();
    }
}
