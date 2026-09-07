package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;

/**
 * Shared Manage columns setup for left-filter suites: enable Activity Type on the current tab
 * regardless of which saved table view is active.
 */
public final class AutomationTableViewSetupUtil {

    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();

    /**
     * Once per session per tab/column: skip Manage columns when {@code columnLabel} is already on the
     * view; otherwise enable it, Save when the button is available, and confirm Added Columns in the modal.
     */
    public void ensureColumnEnabled(SoftAssert softAssert, ConsoleTab tab, String columnLabel)
            throws InterruptedException {
        if (LeftFilterSessionHelper.isManageColumnReady(tab, columnLabel)) {
            Logger.logMessage(columnLabel + " Manage columns setup already done this session — skip");
            return;
        }
        Logger.logMessage("Manage columns setup on " + tab.getTabLabel()
                + " — ensure " + columnLabel + " is enabled on current table view");
        manageColumns.ensureColumnEnabledForView(softAssert, columnLabel);
        LeftFilterSessionHelper.markManageColumnReady(tab, columnLabel);
    }

    /**
     * Once per session per tab: open Manage columns, ensure Activity Type is checked, click Save changes
     * only when that button is enabled, otherwise close the panel and continue.
     */
    public void ensureAAutomationView(SoftAssert softAssert, ConsoleTab tab) throws InterruptedException {
        if (LeftFilterSessionHelper.isAAutomationViewReady(tab)) {
            Logger.logMessage("Activity Type Manage columns setup already done this session — skip");
            return;
        }
        ensureColumnEnabled(softAssert, tab, ManageColumnOptions.ACTIVITY_TYPE);
        LeftFilterSessionHelper.markAAutomationViewReady(tab);
    }

    public boolean ensureAutomationViewSelected(SoftAssert softAssert, ConsoleTab tab) throws InterruptedException {
        ensureAAutomationView(softAssert, tab);
        return true;
    }
}
