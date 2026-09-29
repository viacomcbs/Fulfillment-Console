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
        if (manageColumns.isOrderColumnVisibleOnGrid(columnLabel)) {
            LeftFilterSessionHelper.markManageColumnReady(tab, columnLabel);
        } else {
            Logger.logMessage(columnLabel + " not visible on grid after Manage columns — will retry on next test");
        }
    }

    /**
     * Once per session: open Manage columns one time and enable all listed columns for the current run
     * (order-level and order-line-item flat list columns in a single panel pass).
     */
    public void ensureBatchColumnsEnabledOnce(SoftAssert softAssert, ConsoleTab tab, String... columnLabels)
            throws InterruptedException {
        if (columnLabels == null || columnLabels.length == 0) {
            return;
        }
        java.util.List<String> pending = new java.util.ArrayList<>();
        for (String columnLabel : columnLabels) {
            if (columnLabel == null || columnLabel.isBlank()) {
                continue;
            }
            if (LeftFilterSessionHelper.isManageColumnReady(tab, columnLabel)) {
                Logger.logMessage(columnLabel + " Manage columns setup already done this session — skip");
            } else {
                pending.add(columnLabel);
            }
        }
        if (pending.isEmpty()) {
            return;
        }
        Logger.logReportMessage("Manage columns batch setup on " + tab.getTabLabel()
                + " — enable columns for this run: " + pending);
        manageColumns.ensureBatchColumnsForView(softAssert, pending.toArray(new String[0]));
        for (String columnLabel : pending) {
            LeftFilterSessionHelper.markManageColumnReady(tab, columnLabel);
        }
    }

    /** @deprecated use {@link #ensureBatchColumnsEnabledOnce} — kept for 2-filter suite compatibility */
    public void ensureOrderColumnsEnabledOnce(SoftAssert softAssert, ConsoleTab tab, String... columnLabels)
            throws InterruptedException {
        ensureBatchColumnsEnabledOnce(softAssert, tab, columnLabels);
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
        if (LeftFilterSessionHelper.isManageColumnReady(tab, ManageColumnOptions.ACTIVITY_TYPE)) {
            LeftFilterSessionHelper.markAAutomationViewReady(tab);
        }
    }

    public boolean ensureAutomationViewSelected(SoftAssert softAssert, ConsoleTab tab) throws InterruptedException {
        ensureAAutomationView(softAssert, tab);
        return true;
    }
}
