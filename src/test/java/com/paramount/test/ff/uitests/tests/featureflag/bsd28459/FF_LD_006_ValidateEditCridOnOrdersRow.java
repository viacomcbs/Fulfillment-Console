package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_006;

public class FF_LD_006_ValidateEditCridOnOrdersRow extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_006)
    @Description("FF_LD_006 — Edit CRID on Orders row")
    public void validateEditCridOnOrdersRow() throws InterruptedException {
        validationUtil.validateEditCridOnOrdersRow(softAssert);
        softAssert.assertAll();
    }
}
