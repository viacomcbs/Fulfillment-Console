package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.AutomationTableViewSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * One-time Manage columns setup for Batch 6 (Line Item Status + Error message).
 * Line Item Status is on the grid by default; Error messages column must be enabled once.
 */
public class LF_O_Batch2Filters_LineItemStatusErrorMessage_ManageColumnsSetupTest extends LeftFilterOrdersTabBaseTest {

    private final AutomationTableViewSetupUtil tableSetup = new AutomationTableViewSetupUtil();

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Batch 6 setup — enable Error messages in Manage columns (one pass)")
    public void setup_manageColumnsLineItemStatusAndErrorMessage() throws InterruptedException {
        softAssert = new SoftAssert("setup_manageColumnsLineItemStatusAndErrorMessage", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        tableSetup.ensureOrderColumnsEnabledOnce(softAssert, ConsoleTab.ORDERS,
                ManageColumnOptions.ERROR_MESSAGES);
        softAssert.assertAll();
    }
}
