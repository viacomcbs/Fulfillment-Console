package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_001;

/**
 * FF_PTS_O_001 — one-time Orders setup: Manage columns, Automation view, PTS column enabled,
 * moved to first, Save changes. Calendar (Yesterday default) runs once on login in base setup.
 */
public class FF_PTS_O_001_ValidateColumnAvailableInTableView extends PtsPackagingOrdersBaseTest {

    public static final String PTS_ORDERS_SETUP_GROUP = "ptsOrdersSetup";

    @Test(groups = PTS_ORDERS_SETUP_GROUP)
    @TmsLink(O_001)
    @Description("FF_PTS_O_001 — Enable PTS Packaging ID on Automation view (Orders); reorder to first and save")
    public void validateColumnAvailableInTableView() throws InterruptedException {
        ptsPackagingUtil.openManageColumnsPanel(softAssert);
        ptsPackagingUtil.validateColumnListedInManagePanel(ColumnSection.ORDER, softAssert);
        ptsPackagingUtil.configureAutomationViewWithPtsColumn(ColumnSection.ORDER, softAssert);
        softAssert.assertAll();
    }
}
