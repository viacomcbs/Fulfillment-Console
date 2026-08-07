package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_005;

public class FF_LD_005_ValidateMaterialIdVisibleInOrdersGrid extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_005)
    @Description("FF_LD_005 — Material ID visible in Orders grid")
    public void validateMaterialIdVisibleInOrdersGrid() throws InterruptedException {
        validationUtil.validateMaterialIdVisibleInOrdersGrid(softAssert);
        softAssert.assertAll();
    }
}
