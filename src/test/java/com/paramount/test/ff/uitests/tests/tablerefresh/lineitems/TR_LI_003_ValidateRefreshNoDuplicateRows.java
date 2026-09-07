package com.paramount.test.ff.uitests.tests.tablerefresh.lineitems;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.LI_003;

@LeftFilterEmailScenario(
        manualId = "FF_TR_006",
        scenario = "Line Items — Refresh table does not duplicate LineItem ID values (Manage columns enables column if hidden)"
)
public class TR_LI_003_ValidateRefreshNoDuplicateRows extends TableRefreshLineItemsBaseTest {

    @Test(priority = 1)
    @TmsLink(LI_003)
    @Description("FF_TR_006 — Line Items Refresh table does not duplicate rows in grid")
    public void validateRefreshNoDuplicateRows() throws InterruptedException {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshNoDuplicateRows", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshDoesNotDuplicateLineItemsRows(softAssert);
        softAssert.assertAll();
    }
}
