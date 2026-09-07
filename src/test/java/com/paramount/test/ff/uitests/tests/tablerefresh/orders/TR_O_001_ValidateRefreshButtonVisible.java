package com.paramount.test.ff.uitests.tests.tablerefresh.orders;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.O_001;

@LeftFilterEmailScenario(
        manualId = "FF_TR_001",
        scenario = "Orders — Refresh table button visible near Export in table toolbar"
)
public class TR_O_001_ValidateRefreshButtonVisible extends TableRefreshOrdersBaseTest {

    @Test(priority = 1)
    @TmsLink(O_001)
    @Description("FF_TR_001 — Orders tab Refresh table button visible near Export button")
    public void validateRefreshButtonVisible() {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshButtonVisible", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshTableButtonVisibleNearExport(softAssert);
        softAssert.assertAll();
    }
}
