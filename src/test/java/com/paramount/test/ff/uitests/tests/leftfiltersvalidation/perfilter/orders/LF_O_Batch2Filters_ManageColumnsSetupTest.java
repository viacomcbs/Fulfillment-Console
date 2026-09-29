package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.AutomationTableViewSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * One-time Manage columns setup for 2-filter batch runs (Brand + Submitted By).
 * Runs after login/Yesterday and before any filter scenarios.
 */
public class LF_O_Batch2Filters_ManageColumnsSetupTest extends LeftFilterOrdersTabBaseTest {

    private final AutomationTableViewSetupUtil tableSetup = new AutomationTableViewSetupUtil();

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Batch setup — enable Brand and Submitted By in Manage columns (one pass)")
    public void setup_manageColumnsBrandAndSubmittedBy() throws InterruptedException {
        softAssert = new SoftAssert("setup_manageColumnsBrandAndSubmittedBy", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        tableSetup.ensureOrderColumnsEnabledOnce(softAssert, ConsoleTab.ORDERS,
                ManageColumnOptions.BRAND, ManageColumnOptions.SUBMITTED_BY);
        softAssert.assertAll();
    }
}
