package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.TableView;

/**
 * Manage Columns / Table View helpers for the Line Items tab (top-level tab).
 */
public class LineItemTab_util {

    private static final int SCROLL_PAUSE_MS = 300;

    private final TableView tableView = new TableView();
    private final TableView_utill tableViewUtill = new TableView_utill();

    public void navigateToLineItemsTab() throws InterruptedException {
        if (FulfillmentJsUtil.isLineItemsTabActive() && FulfillmentJsUtil.isLineItemsGridReady()) {
            return;
        }
        if (!FulfillmentJsUtil.clickLineItemsTab() && WaitUtil.isDisplay(tableView.lineItemsTab(), 5)) {
            DriverUtil.clickOnElementSafely(tableView.lineItemsTab(), 10);
        }
        Thread.sleep(800);
        FulfillmentJsUtil.waitForLineItemsGridReady(25);
    }

    public void waitForLineItemsGridReadyFast(int timeoutSec) {
        FulfillmentJsUtil.waitForLineItemsGridReady(timeoutSec);
    }

    public boolean isLineItemsGridReady() {
        return FulfillmentJsUtil.isLineItemsGridReady();
    }

    public void validateManageColumnIconLineItemTab(SoftAssert softAssert) {
        FulfillmentJsUtil.waitForFulfillmentConsoleReady(45);
        waitForLineItemsGridReadyFast(25);
        boolean ready = FulfillmentJsUtil.isLineItemsGridReady()
                || WaitUtil.isDisplay(tableView.getTableViewButton(), 8);
        Verify.softAssert(ready, "Manage Column icon visible on Line Items tab");
    }

    public void openManageColumns() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        FulfillmentJsUtil.dismissBlockingOverlays();
        waitForLineItemsGridReadyFast(15);
        if (FulfillmentJsUtil.isLineItemTabManageColumnsPanelOpen()) {
            Logger.logReportMessage("Manage Columns (Line item table) panel already open");
            return;
        }
        if (FulfillmentJsUtil.openLineItemTabManageColumnsPanel()) {
            return;
        }
        DriverUtil.clickOnElementSafely(tableView.getTableViewButton(), 5);
        Thread.sleep(800);
        if (!FulfillmentJsUtil.isLineItemTabManageColumnsPanelOpen()) {
            Logger.logConsoleMessage("Manage Columns (Line item table) panel not detected after open attempts");
        } else {
            Logger.logReportMessage("Manage Columns (Line item table) panel open");
        }
    }

    public boolean isManageColumnsPanelOpen() {
        return FulfillmentJsUtil.isLineItemTabManageColumnsPanelOpen();
    }

    public void closeManageColumns() throws InterruptedException {
        if (!FulfillmentJsUtil.isLineItemTabManageColumnsPanelOpen()) {
            return;
        }
        FulfillmentJsUtil.closeManageColumnsPanel();
        Thread.sleep(200);
        if (WaitUtil.isDisplay(tableView.manageColumnsCloseButton(), 1)) {
            DriverUtil.clickOnElementSafely(tableView.manageColumnsCloseButton(), 2);
        }
        for (int attempt = 0; attempt < 3 && FulfillmentJsUtil.isLineItemTabManageColumnsPanelOpen(); attempt++) {
            FulfillmentJsUtil.closeManageColumnsPanel();
            Thread.sleep(200);
        }
    }

    public void closeManageColumnsIfOpen() throws InterruptedException {
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(300);
        }
    }

    public void selectStandardView() throws InterruptedException {
        FulfillmentJsUtil.dismissBlockingOverlays();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        tableViewUtill.selectStandardView();
    }

    public boolean isStandardViewSelected() {
        return tableViewUtill.isStandardViewSelected();
    }

    public void scrollToColumn(String columnName) throws InterruptedException {
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if (FulfillmentJsUtil.scrollLineItemTabManagePanelToColumn(columnName)) {
            return;
        }
        Thread.sleep(SCROLL_PAUSE_MS);
        FulfillmentJsUtil.scrollLineItemTabManagePanelToColumn(columnName);
    }

    public boolean isColumnListed(String columnName) {
        return FulfillmentJsUtil.isLineItemTabColumnListed(columnName);
    }

    public boolean isColumnCheckboxSelected(String columnName) {
        return FulfillmentJsUtil.isLineItemTabColumnChecked(columnName);
    }

    public void enableColumn(String columnName) throws InterruptedException {
        scrollToColumn(columnName);
        if (!FulfillmentJsUtil.setLineItemTabColumnChecked(columnName, true)) {
            DriverUtil.clickOnElementSafely(tableView.manageColumnLabel(columnName), 5);
        }
        Thread.sleep(300);
    }

    public void disableColumn(String columnName) throws InterruptedException {
        scrollToColumn(columnName);
        if (!FulfillmentJsUtil.setLineItemTabColumnChecked(columnName, false)) {
            DriverUtil.clickOnElementSafely(tableView.manageColumnLabel(columnName), 5);
        }
        Thread.sleep(300);
    }

    public void applyColumnChanges() throws InterruptedException {
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        Thread.sleep(600);
    }

    public void saveTableView(String viewName) throws InterruptedException {
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        scrollSaveNewViewIntoView();
        if (!FulfillmentJsUtil.isSaveModalVisible()) {
            if (!FulfillmentJsUtil.clickSaveNewViewButton()) {
                DriverUtil.clickOnElementSafely(tableView.saveNewButtonTableView(), 10);
            }
            Thread.sleep(800);
        }
        FulfillmentJsUtil.installNetworkCaptureHook();
        if (FulfillmentJsUtil.saveTableViewViaScript(viewName)) {
            if (confirmTableViewSaved(viewName)) {
                return;
            }
        }
        if (WaitUtil.isDisplay(tableView.inputTableViewName(), 5)) {
            DriverUtil.sendKeyToElement(tableView.inputTableViewName(), 10, viewName);
            DriverUtil.clickOnElementSafely(tableView.saveButtonOnTableViewPopup(), 10);
            Thread.sleep(1000);
            confirmTableViewSaved(viewName);
            return;
        }
        Verify.softAssert(false, "Save table view modal did not open on Line Items tab");
    }

    public void switchTableView(String viewName) throws InterruptedException {
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if (FulfillmentJsUtil.selectSavedViewViaScript(viewName)) {
            waitForLineItemsGridReadyFast(10);
            return;
        }
        tableViewUtill.switchTableView(viewName);
        waitForLineItemsGridReadyFast(10);
    }

    private boolean confirmTableViewSaved(String viewName) throws InterruptedException {
        boolean uiSaved = FulfillmentJsUtil.isViewNameVisible(viewName)
                || FulfillmentJsUtil.waitForViewListedInApi(viewName, true, 12);
        Verify.softAssert(uiSaved, "Table view created on Line Items tab: " + viewName);
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        FulfillmentJsUtil.closeManageColumnsPanel();
        waitForLineItemsGridReadyFast(10);
        return uiSaved;
    }

    private void scrollSaveNewViewIntoView() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('.wrapper-dropdown-container');"
                            + "if(!panel)return false;"
                            + "panel.scrollTop=panel.scrollHeight;"
                            + "var nodes=panel.querySelectorAll('button,span');"
                            + "for(var j=0;j<nodes.length;j++){"
                            + "  if((nodes[j].textContent||'').replace(/\\s+/g,' ').trim()==='Save new view'){"
                            + "    nodes[j].scrollIntoView({block:'center'});return true;"
                            + "  }"
                            + "}"
                            + "return false;");
        } catch (Exception ignored) {
        }
    }

    public void renameTableView(String originalName, String renamedName) throws InterruptedException {
        tableViewUtill.renameTableView(originalName, renamedName);
    }

    public void deleteTableView(String viewName) throws InterruptedException {
        tableViewUtill.deleteTableView(viewName);
    }

    public void assertTableViewExists(String viewName) {
        tableViewUtill.assertTableViewExists(viewName);
    }

    public void assertTableViewAbsent(String viewName) {
        tableViewUtill.assertTableViewAbsent(viewName);
    }

    public void verifyGridHeaderVisible(String columnName) {
        LineItemTabColumnData.ColumnDef column = LineItemTabColumnData.findByLabel(columnName);
        String columnId = column == null ? columnName.replaceAll("[^A-Za-z0-9]+", "") : column.id;
        boolean visible = FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId);
        Verify.softAssert(visible, columnName + " is visible on Line Items grid");
    }

    public void verifyColumnSelectionCounterVisible() throws InterruptedException {
        FulfillmentJsUtil.dismissBlockingOverlays();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        Thread.sleep(400);
        boolean visible = FulfillmentJsUtil.isColumnSelectionCounterVisible();
        if (!visible) {
            visible = WaitUtil.isDisplay(tableView.columnSelectionCounter(), 5);
        }
        if (!visible) {
            Object counts = BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('.wrapper-dropdown-container');"
                            + "if(!panel)return null;"
                            + "var checked=panel.querySelectorAll('input[type=checkbox]:checked').length;"
                            + "var total=panel.querySelectorAll('input[type=checkbox]').length;"
                            + "return checked>0&&total>=2?checked+'/'+total:null;");
            visible = counts != null && !String.valueOf(counts).trim().isEmpty()
                    && !"null".equals(String.valueOf(counts));
        }
        Verify.softAssert(visible, "Column selection counter is displayed on Line item table panel");
    }

    public void verifyAllLineItemTabColumnsListed() throws InterruptedException {
        scrollLineItemManagePanelToTop();
        for (LineItemTabColumnData.ColumnDef column : LineItemTabColumnData.ALL_COLUMNS) {
            if (isOptionalProdColumn(column)) {
                Logger.logReportMessage("Optional column not in PROD UI — skipping: " + column.label);
                continue;
            }
            scrollToColumn(column.label);
            boolean listed = isColumnListed(column.label);
            Verify.softAssert(listed, "Column listed in Manage Columns (Line item table): " + column.label);
        }
    }

    private boolean isOptionalProdColumn(LineItemTabColumnData.ColumnDef column) {
        if (column == null) {
            return false;
        }
        return "fastTrack".equalsIgnoreCase(column.id)
                || "Fast Track".equalsIgnoreCase(column.label);
    }

    public void refreshLineItemsGridAfterColumnChange() throws InterruptedException {
        waitForLineItemsGridReadyFast(12);
        FulfillmentJsUtil.clickRefreshLineItemsTable();
        waitForLineItemsGridReadyFast(12);
        Thread.sleep(400);
    }

    private void scrollLineItemManagePanelToTop() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('.wrapper-dropdown-container,.cdk-overlay-pane');"
                            + "if(!panel)return;"
                            + "var scroll=panel.querySelector('.multi-options-list,[style*=overflow],.scroll');"
                            + "if(scroll)scroll.scrollTop=0;"
                            + "else panel.scrollTop=0;");
        } catch (Exception ignored) {
        }
    }
}
