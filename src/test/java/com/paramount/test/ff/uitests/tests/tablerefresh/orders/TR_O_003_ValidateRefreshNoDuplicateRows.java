package com.paramount.test.ff.uitests.tests.tablerefresh.orders;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.O_003;

@LeftFilterEmailScenario(
        manualId = "FF_TR_003",
        scenario = "Orders — Refresh table does not duplicate Order ID values (Manage columns enables column if hidden)"
)
public class TR_O_003_ValidateRefreshNoDuplicateRows extends TableRefreshOrdersBaseTest {

    @Test(priority = 1)
    @TmsLink(O_003)
    @Description("FF_TR_003 — Orders Refresh table does not duplicate rows in grid")
    public void validateRefreshNoDuplicateRows() throws InterruptedException {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshNoDuplicateRows", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshDoesNotDuplicateOrdersRows(softAssert);
        softAssert.assertAll();
    }
}
