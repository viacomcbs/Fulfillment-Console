package com.paramount.test.ff.uitests.tests.tablerefresh.orders;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.TableRefreshTestRailCaseIds.O_002;

@LeftFilterEmailScenario(
        manualId = "FF_TR_002",
        scenario = "Orders — Click Refresh table triggers filterOrder API"
)
public class TR_O_002_ValidateRefreshCallsFilterOrderApi extends TableRefreshOrdersBaseTest {

    @Test(priority = 1)
    @TmsLink(O_002)
    @Description("FF_TR_002 — Orders Refresh table click calls filterOrder GraphQL API")
    public void validateRefreshCallsFilterOrderApi() throws InterruptedException {
        softAssert = new com.paramount.test.ff.common.util.SoftAssert(
                "validateRefreshCallsFilterOrderApi", getClass().getSimpleName());
        tableRefreshUtil.validateRefreshTriggersFilterOrder(softAssert);
        softAssert.assertAll();
    }
}
