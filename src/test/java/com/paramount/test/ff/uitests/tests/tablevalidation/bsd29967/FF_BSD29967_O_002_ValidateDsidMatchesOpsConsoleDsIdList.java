package com.paramount.test.ff.uitests.tests.tablevalidation.bsd29967;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967EnvironmentResult;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967SessionHelper;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import java.util.List;

import static com.paramount.test.ff.uitests.testrail.Bsd29967TestRailCaseIds.O_002;
import static com.paramount.test.ff.uitests.tests.tablevalidation.bsd29967.FF_BSD29967_O_001_ValidateMultiSubmissionOrderDetailsDsid.BSD29967_ORDERS_SETUP_GROUP;

/**
 * FF_BSD29967_O_002 — Summary: per-Environment FC Details DSIDs vs ops-console dsIdList results from O_001 loop.
 */
public class FF_BSD29967_O_002_ValidateDsidMatchesOpsConsoleDsIdList extends Bsd29967OrdersBaseTest {

    @Test(dependsOnGroups = BSD29967_ORDERS_SETUP_GROUP)
    @TmsLink(O_002)
    @Description("FF_BSD29967_O_002 — Orders: per-Environment DSID compare summary")
    public void validateDsidMatchesOpsConsoleDsIdList() {
        List<Bsd29967EnvironmentResult> results = Bsd29967SessionHelper.getEnvironmentResults();
        Verify.softAssert1(!results.isEmpty(),
                "O_001 recorded at least one Environment workflow result", softAssert);

        for (Bsd29967EnvironmentResult result : results) {
            if (Bsd29967EnvironmentResult.STATUS_SKIP.equals(result.getStatus())) {
                Logger.logReportMessage("BSD-29967 O_002 SKIP — Environment=" + result.getEnvironment()
                        + ": " + result.getMessage());
                continue;
            }
            Logger.logReportMessage("BSD-29967 O_002 — Environment=" + result.getEnvironment()
                    + ", Order=" + result.getOrderId()
                    + ", Status=" + result.getStatus()
                    + ", FC=" + result.getFcDsids()
                    + ", Ops=" + result.getOpsDsids());
            Verify.softAssert1(Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus()),
                    result.getEnvironment() + ": FC Details DSIDs match ops-console dsIdList", softAssert);
        }

        Verify.softAssert1(Bsd29967SessionHelper.getFailedEnvironmentCount() == 0,
                "No Environment workflow DSID mismatches (FAIL="
                        + Bsd29967SessionHelper.getFailedEnvironmentCount() + ")", softAssert);
        softAssert.assertAll();
    }
}
