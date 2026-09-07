package com.paramount.test.ff.uitests.tests.tablerefresh.lineitems;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.LI_001;

@LeftFilterEmailScenario(
        manualId = "FF_TR_004",
        scenario = "Line Items — Refresh table button visible near Export in table toolbar"
)
public class TR_LI_001_ValidateRefreshButtonVisible extends TableRefreshLineItemsBaseTest {

    @Test(priority = 1)
    @TmsLink(LI_001)
    @Description("FF_TR_004 — Line Items tab Refresh table button visible near Export button")
    public void validateRefreshButtonVisible() {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshButtonVisible", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshTableButtonVisibleNearExport(softAssert);
        softAssert.assertAll();
    }
}
