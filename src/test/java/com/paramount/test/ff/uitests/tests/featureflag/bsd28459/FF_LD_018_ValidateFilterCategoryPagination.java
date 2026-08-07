package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_018;

public class FF_LD_018_ValidateFilterCategoryPagination extends FeatureFlagCleanupBaseTest {

    @Test
    @TmsLink(LD_018)
    @Description("FF_LD_018 — Filter category pagination")
    public void validateFilterCategoryPagination() throws InterruptedException {
        validationUtil.validateFilterCategoryPagination(leftFilterPanelUtil, softAssert);
        softAssert.assertAll();
    }
}
