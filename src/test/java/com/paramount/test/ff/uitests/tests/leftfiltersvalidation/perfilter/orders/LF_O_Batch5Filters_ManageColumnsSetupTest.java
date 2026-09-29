package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.AutomationTableViewSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * One-time Manage columns setup for 5-filter batch runs (People &amp; distribution metadata group).
 * Enables Submitted By, Assigned to, Brand, Partner, and Content type in a single panel pass.
 */
public class LF_O_Batch5Filters_ManageColumnsSetupTest extends LeftFilterOrdersTabBaseTest {

    private final AutomationTableViewSetupUtil tableSetup = new AutomationTableViewSetupUtil();

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Batch setup — enable 5 filter columns in Manage columns (one pass)")
    public void setup_manageColumnsPeopleAndDistributionBatch() throws InterruptedException {
        softAssert = new SoftAssert("setup_manageColumnsPeopleAndDistributionBatch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        tableSetup.ensureBatchColumnsEnabledOnce(softAssert, ConsoleTab.ORDERS,
                ManageColumnOptions.SUBMITTED_BY,
                ManageColumnOptions.ASSIGNED_TO,
                ManageColumnOptions.BRAND,
                ManageColumnOptions.PARTNER,
                ManageColumnOptions.CONTENT_TYPE);
        softAssert.assertAll();
    }
}
