package com.paramount.test.ff.uitests.tests.tablerefresh.lineitems;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.LI_002;

@LeftFilterEmailScenario(
        manualId = "FF_TR_005",
        scenario = "Line Items — Click Refresh table triggers LineItemViewLanding API"
)
public class TR_LI_002_ValidateRefreshCallsLineItemViewLandingApi extends TableRefreshLineItemsBaseTest {

    @Test(priority = 1)
    @TmsLink(LI_002)
    @Description("FF_TR_005 — Line Items Refresh table click calls LineItemViewLanding GraphQL API")
    public void validateRefreshCallsLineItemViewLandingApi() throws InterruptedException {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshCallsLineItemViewLandingApi", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshTriggersLineItemViewLanding(softAssert);
        softAssert.assertAll();
    }
}
