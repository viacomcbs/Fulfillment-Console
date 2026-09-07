package com.paramount.test.ff.uitests.tests.tablevalidation.bsd29967;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967SessionHelper;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967WorkflowUtil;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.Bsd29967TestRailCaseIds.O_001;

/**
 * FF_BSD29967_O_001 — Yesterday + round-robin 3 non-zero Environment workflows + mandatory UWFFSP;
 * skip workflows with no (i) icon; max 2 orders per workflow when DSIDs blank; validate FC Details DSIDs.
 */
public class FF_BSD29967_O_001_ValidateMultiSubmissionOrderDetailsDsid extends Bsd29967OrdersBaseTest {

    public static final String BSD29967_ORDERS_SETUP_GROUP = "bsd29967OrdersSetup";

    private final Bsd29967WorkflowUtil workflowUtil = new Bsd29967WorkflowUtil();

    @Test(groups = BSD29967_ORDERS_SETUP_GROUP)
    @TmsLink(O_001)
    @Description("FF_BSD29967_O_001 — Orders: round-robin 3 non-zero + UWFFSP Environment workflows; (i) row Details DSIDs; max 2 orders per workflow when DSIDs blank")
    public void validateMultiSubmissionOrderDetailsDsid() throws InterruptedException {
        workflowUtil.validateAllNonZeroEnvironmentWorkflows(
                leftFilterPanelUtil, columnUtil, detailsUtil, opsConsoleUtil, softAssert);
        softAssert.assertAll();
        Logger.logReportMessage("BSD-29967 O_001 complete — PASS="
                + Bsd29967SessionHelper.getPassedEnvironmentCount()
                + ", FAIL=" + Bsd29967SessionHelper.getFailedEnvironmentCount()
                + ", SKIP=" + Bsd29967SessionHelper.getSkippedEnvironmentCount());
    }
}
