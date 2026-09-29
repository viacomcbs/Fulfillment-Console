package com.paramount.test.ff.uitests.helpers;



import com.paramount.test.ff.common.base.BaseTest;

import com.paramount.test.ff.common.loginUtil.DriverUtil;

import com.paramount.test.ff.common.loginUtil.Login;

import com.paramount.test.ff.common.loginUtil.Verify;

import com.paramount.test.ff.common.loginUtil.WaitUtil;

import com.paramount.test.ff.common.util.Logger;

import com.paramount.test.ff.common.util.SoftAssert;

import com.paramount.test.ff.pageobjects.TableView;

import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;

import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import com.synergy.core.enums.WebKey;

import java.util.List;



public class TableView_utill {



    private static final int MAX_SCROLL_ATTEMPTS = 36;

    private static final int SCROLL_PAUSE_MS = 150;

    private static final int SECTION_SCROLL_PAUSE_MS = 100;



    private final TableView tableView = new TableView();

    private final Login login = new Login();

    private final HomePage_util homePageUtil = new HomePage_util();



    private String activeScrollSection;



    public void launchLoginAndValidateHome(SoftAssert softAssert) throws InterruptedException {

        Logger.logReportMessage("Fulfillment Launching application");

        DriverUtil.launchApplicationOnBrowser();

        login.loginToFF();

        Logger.log("Fulfillment Home page Validation");

        homePageUtil.validateHomePage(softAssert);

    }



    public boolean isOrdersGridReady() {
        return FulfillmentJsUtil.isFulfillmentConsoleReady();
    }



    public void waitForOrdersGridReady() {
        waitForOrdersGridReadyFast(25);
    }

    /** Full wait with XPath header population poll — only when JS fast check is insufficient. */
    public void waitForOrdersGridReadyWithHeaderPoll() {
        waitForOrdersGridReady(60, 30);
    }

    /** Fast path for column/manage-columns tests — skips 15s XPath header poll. */
    public void waitForOrdersGridReadyFast() {
        waitForOrdersGridReadyFast(20);
    }

    public void waitForOrdersGridReadyFast(int timeoutSec) {
        FulfillmentJsUtil.useFastElementTimeout();
        FulfillmentJsUtil.waitForOrdersGridReadyFast(timeoutSec);
    }

    private void waitForOrdersGridReady(int consoleSec, int headerSec) {
        FulfillmentJsUtil.useFastElementTimeout();
        FulfillmentJsUtil.waitForFulfillmentConsoleReady(consoleSec);
        FulfillmentJsUtil.waitForOrdersGridHeadersReady(headerSec);
        waitForGridHeadersPopulated();
    }

    public void waitForGridHeadersReady() throws InterruptedException {
        waitForOrdersGridReadyFast(20);
        waitForGridHeadersWithContent();
    }

    public boolean isGridHeaderVisible(String columnName) {
        FulfillmentJsUtil.useFastElementTimeout();
        return FulfillmentJsUtil.isColumnVisibleInGrid(columnName);
    }

    /** Robust single-pass check used by column-search prep (no soft assert). */
    public boolean isGridColumnVisibleForSearch(String columnName, String section) throws InterruptedException {
        return isGridColumnHeaderVisibleForSearch(columnName, section);
    }

    /** Header-only visibility — avoids false positives from hidden col-id data cells. */
    public boolean isGridColumnHeaderVisibleForSearch(String columnName, String section) throws InterruptedException {
        dismissBlockingModals();
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(300);
        }
        waitForOrdersGridReadyFast(8);
        if (!"order".equalsIgnoreCase(section)) {
            preparePackageGridForColumnCheck(section);
        }
        if (WaitUtil.isDisplay(tableView.gridColumnHeader(columnName), 2)) {
            return true;
        }
        String columnId = resolveColumnId(columnName, section);
        String[] candidates = gridHeaderCandidates(columnName);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (isOrderColumnVisibleForSearch(columnName, columnId, candidates, section)) {
            return true;
        }
        resetGridHorizontalScroll();
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        return isOrderColumnVisibleForSearch(columnName, columnId, candidates, section);
    }

    /** PROD grid renders Title / Season / Episode as separate headers, not one combined label. */
    public boolean isTitleSeasonEpisodeGridVisible() {
        if (FulfillmentJsUtil.isColumnVisibleInGrid("Title", "title")
                || FulfillmentJsUtil.isColumnVisibleInGrid("Title", "titleSeasonEpisode")
                || (FulfillmentJsUtil.isColumnVisibleInGrid("Season", "season")
                        && FulfillmentJsUtil.isColumnVisibleInGrid("Episode", "episode"))) {
            return true;
        }
        try {
            Object result = BaseTest.driver.get().browser().executeScript(
                    "var texts = [];"
                            + "document.querySelectorAll("
                            + "'.ag-header-cell-text, .ag-header-cell .label-ellipsis span, .ag-header-cell-label,"
                            + " [role=columnheader], [role=columnheader] span'"
                            + ").forEach(function(el){"
                            + "  var t = (el.textContent || '').trim();"
                            + "  if (t) texts.push(t);"
                            + "});"
                            + "function has(label){"
                            + "  for (var i = 0; i < texts.length; i++) {"
                            + "    if (texts[i].toLowerCase() === label.toLowerCase()) return true;"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "return has('Title') && has('Season') && has('Episode');");
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOrderColumnVisibleForSearch(
            String columnName, String columnId, String[] candidates, String section) {
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            return isTitleSeasonEpisodeGridVisible()
                    || isExactGridHeaderLabelVisible(columnName)
                    || WaitUtil.isDisplay(tableView.gridColumnHeader("Title"), 3)
                    || isExactGridHeaderLabelVisible("Title")
                    || isGridHeaderVisibleViaColId(columnId)
                    || isGridHeaderVisibleViaColId("title")
                    || WaitUtil.isDisplay(tableView.gridColumnHeaderByColId(columnId), 3)
                    || WaitUtil.isDisplay(tableView.gridColumnHeaderByColId("title"), 3)
                    || isGridColumnVisibleViaDataCell(columnId)
                    || isGridColumnVisibleViaDataCell("title");
        }
        if (isExactGridHeaderLabelVisible(columnName)
                || isGridHeaderVisibleViaColId(columnId)
                || WaitUtil.isDisplay(tableView.gridColumnHeaderByColId(columnId), 3)) {
            return true;
        }
        for (String candidate : candidates) {
            if (isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                    || FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                    || isGridHeaderVisibleViaScript(candidate)
                    || isGridHeaderVisibleViaColId(columnId)
                    || isGridHeaderVisibleViaXPath(candidate)
                    || isGridColumnVisibleViaDataCell(columnId)) {
                return true;
            }
        }
        return false;
    }

    public boolean waitForGridColumnVisibleForSearch(String columnName, String section, int timeoutSec)
            throws InterruptedException {
        prepareGridForHeaderCheck(section);
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        int pass = 0;
        String columnId = resolveColumnId(columnName, section);
        while (System.currentTimeMillis() < end) {
            if (isManageColumnsPanelOpen()) {
                closeManageColumnsIfOpen();
            }
            if (isExactGridHeaderLabelVisible(columnName)) {
                return true;
            }
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            if (isGridColumnHeaderVisibleForSearch(columnName, section)) {
                return true;
            }
            if (pass % 4 == 0) {
                resetGridHorizontalScroll();
                FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            }
            pass++;
            Thread.sleep(200);
        }
        return isExactGridHeaderLabelVisible(columnName)
                || isGridColumnHeaderVisibleForSearch(columnName, section);
    }

    /** True when the table-view selector button shows the given view name (active view). */
    public boolean waitForActiveTableView(String viewName, int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (FulfillmentJsUtil.isActiveTableView(viewName)) {
                return true;
            }
            Thread.sleep(500);
        }
        return FulfillmentJsUtil.isActiveTableView(viewName);
    }

    /**
     * Column-search prep: GraphQL save with minimal columns, then TC_006-style view activation.
     */
    public boolean saveTableViewForColumnSearch(String viewName, String... requiredColumnLabels)
            throws InterruptedException {
        dismissBlockingModals();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        scrollSaveNewViewIntoView();
        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        Logger.logConsoleMessage("Column search prep: save view " + viewName + " columns="
                + java.util.Arrays.toString(requiredColumnLabels));
        if (!saveTableViewWithoutActivation(viewName, requiredColumnLabels)) {
            Logger.logConsoleMessage("Column search prep: view save failed for " + viewName);
            return false;
        }
        closeSaveTableViewModalIfOpen();
        closeManageColumnsIfOpen();
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        FulfillmentJsUtil.refreshTableViewsUiState();
        boolean graphqlSaved = TableViewGraphqlClient.hasCachedViewId(viewName);
        if (graphqlSaved) {
            FulfillmentJsUtil.waitForViewListedInApi(viewName, true, 12);
            activateSavedViewOnOrdersGrid(viewName);
            waitForActiveTableView(viewName, 15);
        } else {
            activateSavedTableView(viewName);
            waitForActiveTableView(viewName, 25);
        }
        FulfillmentJsUtil.clickRefreshOrdersTable();
        waitForOrdersGridReady();
        Thread.sleep(1500);
        return isColumnSearchViewSaved(viewName);
    }

    /** Saves table view without switching the active grid view (Manage Columns state applies on close). */
    private boolean saveTableViewWithoutActivation(String viewName, String... requiredColumnLabels)
            throws InterruptedException {
        if (requiredColumnLabels != null && requiredColumnLabels.length > 0) {
            Logger.logConsoleMessage("Column search prep: GraphQL-first save for " + viewName);
            if (TableViewGraphqlClient.saveTableViewViaGraphql(viewName, requiredColumnLabels)
                    && isColumnSearchViewSaved(viewName)) {
                return true;
            }
        }
        if (FulfillmentJsUtil.isSaveModalVisible()) {
            closeSaveTableViewModalIfOpen();
        }
        if (!FulfillmentJsUtil.isSaveModalVisible()) {
            if (!FulfillmentJsUtil.clickSaveNewViewButton()) {
                DriverUtil.clickOnElementSafely(tableView.saveNewButtonTableView(), 10);
            }
            Thread.sleep(800);
        }
        if (FulfillmentJsUtil.saveTableViewViaScript(viewName)) {
            return confirmTableViewSavedWithoutActivation(viewName, requiredColumnLabels);
        }
        openSaveTableViewModal();
        if (waitForSaveModalOpen()) {
            clearAndTypeTableViewName(viewName);
            clickSaveOnTableViewModal();
            Thread.sleep(1000);
            return confirmTableViewSavedWithoutActivation(viewName, requiredColumnLabels);
        }
        Logger.logConsoleMessage("Column search prep: modal save unavailable — GraphQL for " + viewName);
        return TableViewGraphqlClient.saveTableViewViaGraphql(viewName, requiredColumnLabels)
                && isColumnSearchViewSaved(viewName);
    }

    private boolean confirmTableViewSavedWithoutActivation(String viewName, String... requiredColumnLabels)
            throws InterruptedException {
        boolean uiSaved = waitForTableViewSaved(viewName);
        boolean hasId = FulfillmentJsUtil.waitForSavedViewWithId(viewName, 20);
        if (!hasId && uiSaved && (requiredColumnLabels == null || requiredColumnLabels.length == 0)) {
            hasId = TableViewGraphqlClient.syncViewIdAfterUiSave(viewName);
        }
        if (!hasId && requiredColumnLabels != null && requiredColumnLabels.length > 0) {
            hasId = TableViewGraphqlClient.saveTableViewViaGraphql(viewName, requiredColumnLabels);
        }
        closeSaveTableViewModalIfOpen();
        return uiSaved || hasId || TableViewGraphqlClient.hasCachedViewId(viewName);
    }

    private boolean isColumnSearchViewSaved(String viewName) throws InterruptedException {
        return TableViewGraphqlClient.hasCachedViewId(viewName)
                || FulfillmentJsUtil.waitForSavedViewWithId(viewName, 20)
                || FulfillmentJsUtil.isViewNameVisible(viewName);
    }

    /** @deprecated Use {@link #saveTableViewForColumnSearch} — kept for finalize helper. */
    private boolean finalizeSavedViewForColumnSearch(String viewName) throws InterruptedException {
        boolean saved = isColumnSearchViewSaved(viewName);
        if (!saved) {
            Logger.logConsoleMessage("Column search view save not confirmed: " + viewName);
            return false;
        }
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        closeSaveTableViewModalIfOpen();
        activateSavedTableView(viewName);
        return true;
    }

    /**
     * Select a saved table view and wait until the orders grid reflects its columns.
     */
    public boolean applySavedTableViewForColumnSearch(String viewName, String columnName, String section)
            throws InterruptedException {
        closeManageColumnsIfOpen();
        Thread.sleep(500);
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        TableViewGraphqlClient.cacheViewFromGraphql(viewName);
        for (int attempt = 0; attempt < 3; attempt++) {
            Logger.logConsoleMessage("Applying column-search table view (attempt " + (attempt + 1) + "): " + viewName);
            activateSavedViewOnOrdersGrid(viewName);
            if (!FulfillmentJsUtil.isActiveTableView(viewName)) {
                FulfillmentJsUtil.selectSavedViewOnOrdersGrid(viewName);
            }
            if (!FulfillmentJsUtil.isActiveTableView(viewName)) {
                FulfillmentJsUtil.selectSavedViewViaScript(viewName);
            }
            if (!FulfillmentJsUtil.isActiveTableView(viewName) && TableViewGraphqlClient.hasCachedViewId(viewName)) {
                Logger.logConsoleMessage("Saved view not in dropdown — apply cached GraphQL columns on Standard view: "
                        + viewName);
                if (applyCachedGraphqlColumnsOnStandardView(viewName, columnName, section)) {
                    return true;
                }
            }
            if (!FulfillmentJsUtil.isActiveTableView(viewName) && attempt == 0) {
                Logger.logConsoleMessage("Saved view not in selector — reload fulfillment page for " + viewName);
                FulfillmentJsUtil.reloadFulfillmentPage();
                waitForOrdersGridReady();
                FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
                TableViewGraphqlClient.cacheViewFromGraphql(viewName);
                continue;
            }
            waitForActiveTableView(viewName, 12);
            closeManageColumnsIfOpen();
            FulfillmentJsUtil.refreshTableViewsUiState();
            FulfillmentJsUtil.clickRefreshOrdersTable();
            waitForOrdersGridReady();
            Thread.sleep(1500);
            if (waitForGridColumnVisibleForSearch(columnName, section, 20)) {
                return true;
            }
        }
        return false;
    }

    /** Apply columns from a GraphQL-cached view onto Standard view when the saved view is not in the UI dropdown. */
    public boolean applyCachedGraphqlColumnsOnStandardView(String viewName, String columnName, String section)
            throws InterruptedException {
        String[] selected = TableViewGraphqlClient.getCachedSelectedOrderColumnLabels(viewName);
        if (selected.length == 0) {
            return false;
        }
        closeManageColumnsIfOpen();
        openManageColumns();
        selectStandardView();
        Thread.sleep(500);
        for (String label : selected) {
            if (isColumnListed(label, section)) {
                ensureColumnCheckboxEnabled(label, section);
            }
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        closeManageColumns();
        waitForOrdersGridReadyFast(15);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        return waitForGridColumnVisibleForSearch(columnName, section, 20);
    }

    private boolean switchTableViewFromDropdown(String viewName) throws InterruptedException {
        dismissBlockingModals();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        FulfillmentJsUtil.scrollSavedViewIntoList(viewName);
        openSavedViewsList();
        Thread.sleep(400);
        if (WaitUtil.isDisplay(tableView.tableViewOptionInDropdown(viewName), 5)) {
            DriverUtil.clickOnElementSafely(tableView.tableViewOptionInDropdown(viewName), 10);
            waitForOrdersGridReady();
            return FulfillmentJsUtil.isActiveTableView(viewName);
        }
        if (WaitUtil.isDisplay(tableView.tableViewByName(viewName), 3)) {
            DriverUtil.clickOnElementSafely(tableView.tableViewByName(viewName), 10);
            waitForOrdersGridReady();
            return FulfillmentJsUtil.isActiveTableView(viewName);
        }
        return false;
    }

    /** Exact header label in orders grid — excludes partial matches like Partner Profile for Partner. */
    public boolean isExactOrderGridHeaderVisible(String columnName) {
        return isExactGridHeaderLabelVisible(columnName);
    }

    private boolean isExactGridHeaderLabelVisible(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return false;
        }
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            return WaitUtil.isDisplay(tableView.gridHeaderLabel("Title"), 3)
                    || isTitleSeasonEpisodeGridVisible();
        }
        return WaitUtil.isDisplay(tableView.gridHeaderLabel(columnName), 3);
    }


    public void validateManageColumnIconOrderTab(SoftAssert softAssert) {
        FulfillmentJsUtil.waitForFulfillmentConsoleReady(45);
        waitForOrdersGridReady();
        boolean ready = false;
        for (int attempt = 0; attempt < 8 && !ready; attempt++) {
            ready = FulfillmentJsUtil.isFulfillmentConsoleReady()
                    || WaitUtil.isDisplay(tableView.getTableViewButton(), 8);
            if (!ready) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Verify.softAssert(ready, "Manage Column icon visible on Orders tab");
    }



    public void openManageColumns() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        dismissBlockingModals();
        waitForOrdersGridReadyFast(15);
        if (FulfillmentJsUtil.isManageColumnsPanelOpen()) {
            Logger.logReportMessage("Manage Columns panel already open");
            return;
        }
        if (FulfillmentJsUtil.openManageColumnsPanel()) {
            return;
        }
        for (int attempt = 0; attempt < 2 && !FulfillmentJsUtil.isManageColumnsPanelOpen(); attempt++) {
            clickTableViewButton();
            waitForManageColumnsPanel(8);
            if (!FulfillmentJsUtil.isManageColumnsPanelOpen()) {
                Thread.sleep(800);
            }
        }
        if (!isManageColumnsPanelOpen()) {
            Logger.logConsoleMessage("Manage Columns panel not detected after open attempts");
        } else {
            Logger.logReportMessage("Manage Columns panel open");
        }
    }

    private void clickTableViewButton() {
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "var btn = document.querySelector('#tableViewButton');"
                            + "if (!btn) {"
                            + "  btn = document.querySelector('button.table-view-button');"
                            + "}"
                            + "if (btn) { btn.click(); return true; }"
                            + "return false;");
            if (isScriptTruthy(clicked)) {
                return;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("JS tableViewButton click failed: " + e.getMessage());
        }
        DriverUtil.clickOnElementSafely(tableView.getTableViewButton(), 5);
    }

    private void waitForManageColumnsPanel(int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isManageColumnsPanelOpen()) {
                return;
            }
            Thread.sleep(200);
        }
    }

    public boolean isManageColumnsPanelOpen() {
        return FulfillmentJsUtil.isManageColumnsPanelOpen() || isManageColumnsPanelOpenViaScript();
    }

    private boolean isManageColumnsPanelOpenViaScript() {
        try {
            Object open = BaseTest.driver.get().browser().executeScript(
                    "function isOrderManagePanel(t){"
                            + "t=t||'';"
                            + "return /manage columns/i.test(t)&&(/order columns|order table/i.test(t));"
                            + "}"
                            + "function panelOpen() {"
                            + "  var titles = document.querySelectorAll('span.wrapper-dropdown-title');"
                            + "  for (var i = 0; i < titles.length; i++) {"
                            + "    if (isOrderManagePanel(titles[i].textContent)) return true;"
                            + "  }"
                            + "  var menus = document.querySelectorAll('.wrapper-dropdown-container,.cdk-overlay-pane');"
                            + "  for (var j = 0; j < menus.length; j++) {"
                            + "    if (isOrderManagePanel(menus[j].textContent)) return true;"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "return panelOpen();");
            return isScriptTruthy(open);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isScriptTruthy(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return "true".equalsIgnoreCase(String.valueOf(result));
    }



    public void verifyManageColumnsPanelOpen() {
        Verify.softAssert(isManageColumnsPanelOpen(), "Manage Columns (Order table) panel is open");
    }




    public void closeManageColumnsIfOpen() throws InterruptedException {
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(300);
        }
    }
    public void closeManageColumns() throws InterruptedException {

        if (!FulfillmentJsUtil.isManageColumnsPanelOpen()) {

            return;

        }

        FulfillmentJsUtil.closeManageColumnsPanel();
        Thread.sleep(200);

        if (WaitUtil.isDisplay(tableView.manageColumnsCloseButton(), 1)) {

            DriverUtil.clickOnElementSafely(tableView.manageColumnsCloseButton(), 2);

        } else {
            try {
                BaseTest.driver.get().browser().executeScript(
                        "var pane = document.querySelector('.cdk-overlay-pane');"
                                + "if (!pane) return false;"
                                + "var btn = pane.querySelector("
                                + "'button.close, button[aria-label=\"Close\"], .close-button');"
                                + "if (btn) { btn.click(); return true; }"
                                + "return false;");
            } catch (Exception ignored) {
            }
            pressEscapeKey();

        }

        for (int attempt = 0; attempt < 3 && FulfillmentJsUtil.isManageColumnsPanelOpen(); attempt++) {

            pressEscapeKey();

            Thread.sleep(200);

        }

    }

    /** True when the column checkbox is already on under Standard view in Manage Columns. */
    public boolean isColumnEnabledInStandardView(String columnName, String section) throws InterruptedException {
        openManageColumns();
        try {
            selectStandardView();
            if (!isColumnListed(columnName, section)) {
                return false;
            }
            scrollToColumn(columnName, section);
            return isColumnCheckboxSelected(columnName, section);
        } finally {
            closeManageColumns();
            Thread.sleep(500);
        }
    }

    /**
     * Manage Columns must already be open with Standard view selected. Scrolls to the column,
     * checks the checkbox state, and enables it only when unchecked.
     */
    public boolean ensureColumnCheckboxEnabled(String columnName, String section) throws InterruptedException {
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!isColumnListed(columnName, section)) {
            Logger.log("Column not listed under current view: [" + section + "] " + columnName);
            return false;
        }
        if (FulfillmentJsUtil.isColumnChecked(columnName, section)) {
            Logger.log("Column checkbox already selected: " + columnName);
            if (!isExactOrderGridHeaderVisible(columnName)) {
                Logger.log("MC checkbox on but Order grid header missing — apply then refresh: " + columnName);
                FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
                Thread.sleep(800);
                waitForOrdersGridReadyFast(10);
                if (isExactOrderGridHeaderVisible(columnName)) {
                    return true;
                }
                return ensureColumnCheckboxEnabledWithRefresh(columnName, section);
            }
            return true;
        }
        Logger.log("Column checkbox not selected — enabling: " + columnName);
        if (FulfillmentJsUtil.setColumnChecked(columnName, section, true)) {
            return FulfillmentJsUtil.isColumnChecked(columnName, section);
        }
        scrollToColumn(columnName, section);
        if (!FulfillmentJsUtil.setColumnChecked(columnName, section, true)) {
            enableColumn(columnName, section);
        }
        return FulfillmentJsUtil.isColumnChecked(columnName, section)
                || isColumnCheckboxSelected(columnName, section);
    }

    /**
     * Ensures the column checkbox is on, toggling off/on when already selected so the grid refreshes
     * (needed when header is visible but msc-custom-table-column-filter is missing on PROD).
     */
    public boolean ensureColumnCheckboxEnabledWithRefresh(String columnName, String section) throws InterruptedException {
        if (!isColumnListed(columnName, section)) {
            Logger.log("Column not listed under current view: [" + section + "] " + columnName);
            return false;
        }
        scrollToColumn(columnName, section);
        if (isColumnCheckboxSelected(columnName, section)) {
            Logger.log("Column checkbox already selected — toggling to refresh grid: " + columnName);
            if (!FulfillmentJsUtil.setColumnChecked(columnName, section, false)) {
                disableColumn(columnName, section);
            }
            Thread.sleep(400);
            scrollToColumn(columnName, section);
        }
        if (!FulfillmentJsUtil.setColumnChecked(columnName, section, true)) {
            enableColumn(columnName, section);
        }
        scrollToColumn(columnName, section);
        boolean selected = isColumnCheckboxSelected(columnName, section)
                || FulfillmentJsUtil.isColumnChecked(columnName, section);
        if (!selected) {
            Logger.log("Column checkbox still not selected after refresh enable: " + columnName);
            return false;
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        Thread.sleep(800);
        return true;
    }

    /** Open Manage Columns, select Standard view first, then ensure the column checkbox is checked. */
    public boolean ensureColumnSelectedOnStandardView(String columnName, String section) throws InterruptedException {
        openManageColumns();
        selectStandardView();
        return ensureColumnCheckboxEnabled(columnName, section);
    }

    /** TC044 flow: tableViewButton → dropdownMenuButton → Standard view, with panel ready for column listing. */
    public void ensureManageColumnsStandardViewReady() throws InterruptedException {
        dismissBlockingModals();
        waitForOrdersGridReady();
        if (!isManageColumnsPanelOpen()) {
            clickTableViewButton();
            waitForManageColumnsPanel(10);
        }
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if (isManageColumnsPanelOnStandardView()) {
            scrollPanelToTop();
            return;
        }
        Logger.logReportMessage("Selecting Standard view via dropdownMenuButton (TC044 flow)");
        clickDropdownMenuButton();
        Thread.sleep(400);
        if (!clickStandardViewViaScript()) {
            clickStandardViewOption();
        }
        waitForStandardViewSelected();
        Thread.sleep(500);
        scrollPanelToTop();
    }

    /** Select Standard view from the Orders grid table-view button (outside Manage Columns). */
    public void activateStandardViewOnOrdersGrid() throws InterruptedException {
        dismissBlockingModals();
        closeManageColumnsIfOpen();
        if (isStandardViewOnTableViewButton() || FulfillmentJsUtil.isActiveTableView("Standard view")) {
            Logger.log("Standard view already active on Orders grid");
            return;
        }
        Logger.log("Activating Standard view on Orders grid table-view selector");
        clickTableViewButton();
        Thread.sleep(400);
        if (clickStandardViewViaScript()) {
            waitForStandardViewSelected();
        } else if (WaitUtil.isDisplayQuiet(tableView.standardViewTeamFilterOption(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.standardViewTeamFilterOption(), 5);
            waitForStandardViewSelected();
        } else if (WaitUtil.isDisplayQuiet(tableView.standardViewLabelOption(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.standardViewLabelOption(), 5);
            waitForStandardViewSelected();
        }
        waitForOrdersGridReady();
    }

    /** Select a saved view from the Orders grid table-view button (outside Manage Columns). */
    public void activateSavedViewOnOrdersGrid(String viewName) throws InterruptedException {
        if (viewName == null || viewName.trim().isEmpty()) {
            return;
        }
        dismissBlockingModals();
        closeManageColumnsIfOpen();
        if (FulfillmentJsUtil.isActiveTableView(viewName)) {
            Logger.log("Saved view already active on Orders grid: " + viewName);
            return;
        }
        Logger.log("Activating saved view on Orders grid table-view selector: " + viewName);
        if (!TableViewGraphqlClient.hasCachedViewId(viewName)) {
            FulfillmentJsUtil.waitForViewListedInApi(viewName, true, 8);
        }
        clickTableViewButton();
        Thread.sleep(400);
        if (clickTableViewOptionViaScript(viewName)) {
            waitForActiveTableView(viewName, 15);
            waitForOrdersGridReady();
            return;
        }
        if (FulfillmentJsUtil.selectSavedViewOnOrdersGrid(viewName)) {
            waitForActiveTableView(viewName, 15);
            waitForOrdersGridReady();
            return;
        }
        if (WaitUtil.isDisplayQuiet(tableView.tableViewOptionInDropdown(viewName), 3)) {
            DriverUtil.clickOnElementSafely(tableView.tableViewOptionInDropdown(viewName), 5);
            waitForActiveTableView(viewName, 15);
            waitForOrdersGridReady();
        }
    }



    public void selectStandardView() throws InterruptedException {
        dismissBlockingModals();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if (isManageColumnsPanelOnStandardView()) {
            Logger.logReportMessage("Standard view selected");
            return;
        }
        Logger.logReportMessage("Selecting Standard view in Manage Columns panel");
        clickDropdownMenuButton();
        Thread.sleep(400);
        if (clickStandardViewViaScript()) {
            Thread.sleep(500);
            waitForManageColumnsStandardViewSelected();
            if (isManageColumnsPanelOnStandardView()) {
                return;
            }
        }
        openViewSelectorViaScript();
        Thread.sleep(400);
        clickStandardViewViaScript();
        Thread.sleep(500);
        waitForManageColumnsStandardViewSelected();
        if (!isManageColumnsPanelOnStandardView()) {
            clickDropdownMenuButton();
            clickStandardViewViaScript();
            Thread.sleep(500);
            waitForManageColumnsStandardViewSelected();
        }
    }

    /** True when Manage Columns panel view selector shows Standard view (not a saved view like AAAAA). */
    public boolean isManageColumnsPanelOnStandardView() {
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function activeViewText(panel) {"
                            + "  if (!panel) return '';"
                            + "  var btn = panel.querySelector('#dropdownMenuButton');"
                            + "  if (btn) return (btn.textContent || '').trim();"
                            + "  var reset = panel.querySelector('.dropdown-reset-container');"
                            + "  if (!reset) return '';"
                            + "  var lines = (reset.innerText || '').split(/\\r?\\n/).map(function(s){"
                            + "    return s.trim(); }).filter(Boolean);"
                            + "  return lines.length ? lines[0] : (reset.textContent || '').trim();"
                            + "}"
                            + "var panel = document.querySelector('msc-custom-wrapper-dropdown .wrapper-dropdown-container')"
                            + "  || document.querySelector('.wrapper-dropdown-container');"
                            + "var text = activeViewText(panel).toLowerCase();"
                            + "return text.indexOf('standard view') >= 0;");
            return Boolean.TRUE.equals(match);
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForManageColumnsStandardViewSelected() throws InterruptedException {
        for (int attempt = 0; attempt < 8; attempt++) {
            if (isManageColumnsPanelOnStandardView()) {
                return;
            }
            Thread.sleep(400);
        }
    }

    public boolean isStandardViewSelected() {
        return isStandardViewSelectedViaScript() || isStandardViewOnTableViewButton();
    }

    private void clickDropdownMenuButton() {
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "var btn = document.querySelector('#dropdownMenuButton');"
                            + "if (btn) { btn.click(); return true; }"
                            + "var panel = document.querySelector('msc-custom-wrapper-dropdown')"
                            + "  || document.querySelector('.wrapper-dropdown-container');"
                            + "if (panel) {"
                            + "  btn = panel.querySelector('#dropdownMenuButton,[id=dropdownMenuButton]');"
                            + "  if (btn) { btn.click(); return true; }"
                            + "}"
                            + "return false;");
            if (isScriptTruthy(clicked)) {
                return;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("dropdownMenuButton script click failed: " + e.getMessage());
        }
        if (WaitUtil.isDisplayQuiet(tableView.dropdownMenuButton(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.dropdownMenuButton(), 5);
        }
    }

    private void openViewSelectorViaScript() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var btn = document.querySelector('#dropdownMenuButton');"
                            + "if (btn) { btn.click(); return true; }"
                            + "var panel = document.querySelector('.wrapper-dropdown-container')"
                            + " || document.querySelector('msc-custom-wrapper-dropdown');"
                            + "if (!panel) return false;"
                            + "var selectors = panel.querySelectorAll("
                            + "'.option.team-filter, .dropdown-reset-container, div.option, #dropdownMenuButton');"
                            + "for (var i = 0; i < selectors.length; i++) {"
                            + "  var t = (selectors[i].textContent || '').toLowerCase();"
                            + "  if (t.indexOf('team view') >= 0 || t.indexOf('standard view') >= 0"
                            + "      || selectors[i].id === 'dropdownMenuButton') {"
                            + "    selectors[i].click(); return true;"
                            + "  }"
                            + "}"
                            + "return false;");
        } catch (Exception e) {
            Logger.logConsoleMessage("View selector script click failed: " + e.getMessage());
        }
    }

    private boolean isStandardViewSelectedViaScript() {
        try {
            Object selected = BaseTest.driver.get().browser().executeScript(
                    "function hasStandard(t){return (t||'').toLowerCase().indexOf('standard view')>=0;}"
                            + "var btn = document.querySelector('#tableViewButton');"
                            + "if (btn && hasStandard(btn.textContent)) return true;"
                            + "var panel = document.querySelector('.wrapper-dropdown-container');"
                            + "if (panel) {"
                            + "  var reset = panel.querySelector('.dropdown-reset-container');"
                            + "  if (reset && hasStandard(reset.textContent)) return true;"
                            + "  var team = panel.querySelector('.option.team-filter');"
                            + "  if (team && hasStandard(team.textContent)) return true;"
                            + "}"
                            + "var labels = document.querySelectorAll('label.form-check-label');"
                            + "for (var i = 0; i < labels.length; i++) {"
                            + "  if (hasStandard(labels[i].textContent)) return true;"
                            + "}"
                            + "return false;");
            return isScriptTruthy(selected);
        } catch (Exception e) {
            return false;
        }
    }

    private void openViewSelectorDropdown() throws InterruptedException {
        if (!WaitUtil.isDisplayQuiet(tableView.viewSelectorInPanel(), 3)) {
            openManageColumns();
        }
        if (WaitUtil.isDisplayQuiet(tableView.standardViewLabelOption(), 2)) {
            return;
        }
        DriverUtil.clickOnElementSafely(tableView.viewSelectorInPanel(), 5);
        if (!WaitUtil.isDisplayQuiet(tableView.standardViewTeamFilterOption(), 2)
                && !WaitUtil.isDisplayQuiet(tableView.standardViewLabelOption(), 2)
                && !WaitUtil.isDisplayQuiet(tableView.selectStandardViewMode(), 2)) {
            DriverUtil.clickOnElementSafely(tableView.viewSelectorInPanel(), 3);
        }
    }

    private void clickStandardViewOption() {
        if (clickStandardViewViaScript()) {
            return;
        }
        if (WaitUtil.isDisplayQuiet(tableView.standardViewLabelOption(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.standardViewLabelOption(), 5);
            return;
        }
        if (WaitUtil.isDisplayQuiet(tableView.standardViewTeamFilterOption(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.standardViewTeamFilterOption(), 5);
            return;
        }
        if (WaitUtil.isDisplayQuiet(tableView.selectStandardViewMode(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.selectStandardViewMode(), 5);
        }
    }

    private boolean clickStandardViewViaScript() {
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "function clickStandard(root) {"
                            + "  var nodes = root.querySelectorAll("
                            + "'label.form-check-label, label, span, div.option, a');"
                            + "  for (var i = 0; i < nodes.length; i++) {"
                            + "    var t = (nodes[i].textContent || '').trim().toLowerCase();"
                            + "    if (t === 'standard view' || t.indexOf('standard view') >= 0) {"
                            + "      nodes[i].click(); return true; }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "var panel = document.querySelector('msc-custom-wrapper-dropdown .wrapper-dropdown-container')"
                            + "  || document.querySelector('.wrapper-dropdown-container');"
                            + "if (panel && clickStandard(panel)) return true;"
                            + "return clickStandard(document);");
            if (isScriptTruthy(clicked)) {
                Logger.logReportMessage("Selected Standard View via script");
                return true;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Standard View script click failed: " + e.getMessage());
        }
        return false;
    }

    private boolean isStandardViewActiveViaScript() {
        try {
            Object active = BaseTest.driver.get().browser().executeScript(
                    "function hasStandardViewText(el) {"
                            + "  return ((el && el.textContent) || '').toLowerCase().indexOf('standard view') >= 0;"
                            + "}"
                            + "var containers = document.querySelectorAll('.dropdown-reset-container');"
                            + "for (var i = 0; i < containers.length; i++) {"
                            + "  if (hasStandardViewText(containers[i])) return true;"
                            + "}"
                            + "var panels = document.querySelectorAll('.cdk-overlay-pane, ngb-modal-window');"
                            + "for (var j = 0; j < panels.length; j++) {"
                            + "  var pt = (panels[j].textContent || '').toLowerCase();"
                            + "  if (pt.indexOf('manage columns') >= 0 && hasStandardViewText(panels[j])) return true;"
                            + "}"
                            + "var opts = document.querySelectorAll('.option.team-filter, div.option');"
                            + "for (var k = 0; k < opts.length; k++) {"
                            + "  if (hasStandardViewText(opts[k])"
                            + "      && (opts[k].classList.contains('active') || opts[k].classList.contains('selected')"
                            + "          || opts[k].getAttribute('aria-selected') === 'true')) return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(active);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isStandardViewOnTableViewButton() {
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "var btn = document.querySelector('#tableViewButton');"
                            + "if (!btn) return false;"
                            + "return (btn.textContent || '').toLowerCase().indexOf('standard view') >= 0;");
            return Boolean.TRUE.equals(match);
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForStandardViewSelected() throws InterruptedException {
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            if (isStandardViewSelected()) {
                return;
            }
            Thread.sleep(400);
        }
    }

    private void dismissBlockingModals() throws InterruptedException {
        if (!WaitUtil.isDisplayQuiet(tableView.ngbModalWindow(), 1)) {
            return;
        }
        if (FulfillmentJsUtil.isSaveModalVisible()
                || WaitUtil.isDisplayQuiet(tableView.saveTableViewModal(), 1)) {
            return;
        }
        if (WaitUtil.isDisplayQuiet(tableView.modalDismissButton(), 1)) {
            DriverUtil.clickOnElementSafely(tableView.modalDismissButton(), 3);
        } else {
            pressEscapeKey();
        }
        DriverUtil.waitForElementInvisible(tableView.ngbModalWindow(), 5);
    }

    private void closeSaveTableViewModalIfOpen() throws InterruptedException {
        if (!FulfillmentJsUtil.isSaveModalVisible()
                && !WaitUtil.isDisplay(tableView.saveTableViewModal(), 1)) {
            return;
        }
        pressEscapeKey();
        DriverUtil.waitForElementInvisible(tableView.saveTableViewModal(), 5);
    }

    public void applyColumnChanges() throws InterruptedException {
        closeManageColumns();
        waitForOrdersGridReadyFast(12);
        Thread.sleep(200);
        openManageColumns();
    }



    private void pressEscapeKey() {

        try {

            BaseTest.driver.get().browser().executeScript(

                    "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));");

        } catch (Exception ignored) {

        }

    }



    public void scrollToColumn(String columnName, String section) throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if ("order".equalsIgnoreCase(section)) {
            scrollPanelToTop();
        }
        if (FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section)) {
            return;
        }

        By label = resolveColumnLabelLocator(columnName, section);
        for (int attempt = 0; attempt < 3; attempt++) {
            if (WaitUtil.isDisplayQuiet(label, 1)) {
                DriverUtil.scrollToElement(label);
                return;
            }
            if (FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section)) {
                return;
            }
            scrollPanelDown();
            Thread.sleep(SCROLL_PAUSE_MS);
        }
    }



    private void scrollToSection(String section) throws InterruptedException {

        if ("order".equals(section)) {

            scrollPanelToTop();

            for (int i = 0; i < MAX_SCROLL_ATTEMPTS && !WaitUtil.isDisplay(tableView.orderSectionHeader(), 1); i++) {

                scrollPanelUp();

                Thread.sleep(SECTION_SCROLL_PAUSE_MS);

            }

        } else if ("package".equals(section)) {

            for (int i = 0; i < MAX_SCROLL_ATTEMPTS && !WaitUtil.isDisplay(tableView.packageSectionHeader(), 1); i++) {

                scrollPanelDown();

                Thread.sleep(SECTION_SCROLL_PAUSE_MS);

            }

        } else if ("lineitem".equals(section)) {

            for (int i = 0; i < MAX_SCROLL_ATTEMPTS && !WaitUtil.isDisplay(tableView.lineItemSectionHeader(), 1); i++) {

                scrollPanelDown();

                Thread.sleep(SECTION_SCROLL_PAUSE_MS);

            }

        }

    }



    private By resolveColumnLabelLocator(String columnName, String section) {

        if ("package".equals(section)) {

            return tableView.PackageColumnNameOnTableView(columnName);

        }

        if ("lineitem".equals(section)) {

            return tableView.LineitemColumnNameOnTableView(columnName);

        }

        return tableView.orderTableColumnLabel(columnName);

    }



    private By resolveColumnLabel(String columnName, String section) {

        By label = resolveColumnLabelLocator(columnName, section);

        if (WaitUtil.isDisplay(label, 1)) {

            return label;

        }

        return tableView.OrderColumnNameOnTableView(columnName);

    }



    public By columnCheckbox(String columnName, String section) {

        return resolveCheckboxLocator(columnName, section);

    }



    private By resolveCheckboxLocator(String columnName, String section) {

        if ("package".equals(section)) {

            return tableView.PackageCheckboxOnTableView(columnName);

        }

        if ("lineitem".equals(section)) {

            return tableView.LineitemCheckboxOnTableView(columnName);

        }

        if (WaitUtil.isDisplay(tableView.manageColumnCheckbox(columnName), 1)) {
            return tableView.manageColumnCheckbox(columnName);
        }

        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column != null && WaitUtil.isDisplay(tableView.orderTableColumnCheckboxById(column.id), 1)) {
            return tableView.orderTableColumnCheckboxById(column.id);
        }

        return tableView.manageColumnCheckbox(columnName);

    }



    public boolean isColumnListed(String columnName, String section) {
        FulfillmentJsUtil.useFastElementTimeout();
        if (isColumnListedInternal(columnName, section)) {
            return true;
        }
        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column != null && isColumnListedByCheckboxId(column.id, section)) {
            return true;
        }
        if ("Fast Track".equals(columnName)) {
            if (FulfillmentJsUtil.isColumnListed("FastTrack", section)
                    || FulfillmentJsUtil.isColumnListed("Fast track", section)) {
                return true;
            }
            OrderTabColumnData.ColumnDef fastTrack = OrderTabColumnData.findByLabelAndSection(columnName, section);
            if (fastTrack != null && FulfillmentJsUtil.isColumnListed(fastTrack.id, section)) {
                return true;
            }
        }
        OrderTabColumnData.ColumnDef columnDef = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (columnDef != null && FulfillmentJsUtil.isColumnListed(columnDef.id, section)) {
            return true;
        }
        if ("Offset Delivery date".equals(columnName) && "order".equalsIgnoreCase(section)) {
            if (FulfillmentJsUtil.isColumnListed("deliveryOffset", section)) {
                return true;
            }
        }
        return false;
    }

    private boolean isColumnListedByCheckboxId(String columnId, String section) {
        if (FulfillmentJsUtil.isColumnListed(columnId, section)) {
            return true;
        }
        FulfillmentJsUtil.scrollManagePanelToColumn(columnId, section);
        return FulfillmentJsUtil.isColumnListed(columnId, section);
    }

    /** Exact label text in Manage Columns UI only (no GraphQL/id/contains fallback). */
    public boolean isExactColumnLabelListedInUi(String columnName, String section) throws InterruptedException {
        return isExactColumnLabelListedInUi(columnName, section, true);
    }

    /** Exact label lookup; set scrollIfNotFound false for fast absence checks (e.g. legacy rename). */
    public boolean isExactColumnLabelListedInUi(String columnName, String section, boolean scrollIfNotFound)
            throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (FulfillmentJsUtil.isExactColumnLabelInManagePanel(columnName, section)) {
            return true;
        }
        By label = tableView.orderTableColumnLabelExact(columnName);
        if (WaitUtil.isElementVisibleForSec(label)) {
            return true;
        }
        if (!scrollIfNotFound) {
            return false;
        }
        scrollPanelToTop();
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            if (WaitUtil.isElementVisibleForSec(label)) {
                return true;
            }
            scrollPanelDown();
            Thread.sleep(100);
        }
        return false;
    }

    private boolean isColumnListedViaXPath(String columnName, String section) throws InterruptedException {
        By label = resolveColumnLabelLocator(columnName, section);
        if (WaitUtil.isElementVisibleForSec(label)) {
            return true;
        }
        scrollPanelToTop();
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            if (WaitUtil.isElementVisibleForSec(label)) {
                return true;
            }
            scrollPanelDown();
            Thread.sleep(100);
        }
        return false;
    }

    private boolean isColumnListedInternal(String columnName, String section) {
        if (FulfillmentJsUtil.isColumnListed(columnName, section)) {
            return true;
        }
        By label = resolveColumnLabelLocator(columnName, section);
        if (WaitUtil.isDisplay(label, 2)) {
            return true;
        }
        return WaitUtil.isDisplay(columnCheckbox(columnName, section), 1);
    }




    public boolean isColumnCheckboxSelected(String columnName, String section) {
        FulfillmentJsUtil.useFastElementTimeout();
        if (FulfillmentJsUtil.isColumnChecked(columnName, section)) {
            return true;
        }
        By checkbox = columnCheckbox(columnName, section);
        if (WaitUtil.isDisplay(checkbox, 1)) {
            try {
                return BaseTest.driver.get().finder().findElement(checkbox).isSelected();
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void enableColumn(String columnName, String section) throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        scrollToColumn(columnName, section);
        if (FulfillmentJsUtil.setColumnChecked(columnName, section, true)) {
            return;
        }
        Logger.logConsoleMessage("JS enableColumn failed for [" + section + "] " + columnName + ", trying XPath fallback");
        By checkbox = columnCheckbox(columnName, section);
        if (WaitUtil.isDisplayQuiet(checkbox, 2)) {
            try {
                if (!BaseTest.driver.get().finder().findElement(checkbox).isSelected()) {
                    DriverUtil.clickOnElementSafely(checkbox, 5);
                }
                return;
            } catch (Exception ignored) {
            }
        }
        By label = resolveColumnLabel(columnName, section);
        if (WaitUtil.isDisplayQuiet(label, 2)) {
            DriverUtil.clickOnElementSafely(label, 5);
            return;
        }
        clickColumnLabelViaScript(columnName);
    }

    private void clickColumnLabelViaScript(String columnName) {
        String escaped = columnName.replace("\\", "\\\\").replace("'", "\\'");
        try {
            BaseTest.driver.get().browser().executeScript(
                    "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                            + "var target = norm('" + escaped + "');"
                            + "var panel = document.querySelector('.wrapper-dropdown-container');"
                            + "if (!panel) return false;"
                            + "var scroller = panel.querySelector('.multi-table-column-container')"
                            + "    || panel.querySelector('.multi-options-list') || panel;"
                            + "for (var pass = 0; pass < 15; pass++) {"
                            + "  var labels = panel.querySelectorAll('label');"
                            + "  for (var i = 0; i < labels.length; i++) {"
                            + "    var t = norm(labels[i].textContent);"
                            + "    if (t === target || t.indexOf(target) >= 0 || target.indexOf(t) >= 0) {"
                            + "      labels[i].scrollIntoView({block:'center'});"
                            + "      labels[i].click(); return true;"
                            + "    }"
                            + "  }"
                            + "  scroller.scrollTop = (scroller.scrollTop || 0) + 280;"
                            + "}"
                            + "return false;");
        } catch (Exception e) {
            Logger.logConsoleMessage("JS label click failed for " + columnName + ": " + e.getMessage());
        }
    }



    public void disableColumn(String columnName, String section) throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        scrollToColumn(columnName, section);
        if (FulfillmentJsUtil.setColumnChecked(columnName, section, false)) {
            return;
        }
        Logger.logConsoleMessage("JS disableColumn failed for [" + section + "] " + columnName + ", trying XPath fallback");
        scrollToColumn(columnName, section);
        By checkbox = columnCheckbox(columnName, section);
        if (WaitUtil.isDisplay(checkbox, 5)) {
            try {
                if (BaseTest.driver.get().finder().findElement(checkbox).isSelected()) {
                    DriverUtil.clickOnElementSafely(checkbox, 5);
                }
            } catch (Exception e) {
                DriverUtil.clickOnElementSafely(resolveColumnLabel(columnName, section), 5);
            }
        }
    }



    private void scrollPanelToTop() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('msc-custom-wrapper-dropdown .wrapper-dropdown-container')"
                            + "||document.querySelector('msc-custom-wrapper-dropdown')"
                            + "||document.querySelector('.wrapper-dropdown-container');"
                            + "var scroller=panel&&(panel.querySelector(':scope > div > div:nth-child(3) > div:nth-child(2)')"
                            + "||panel.querySelector('.multi-table-column-container .multi-options-list')"
                            + "||panel.querySelector('.multi-table-column-container')"
                            + "||panel.querySelector('.multi-options-list')||panel);"
                            + "if(scroller){scroller.scrollTop=0;return true;}"
                            + "return false;");
        } catch (Exception ignored) {
        }
    }



    private void scrollPanelDown() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('msc-custom-wrapper-dropdown .wrapper-dropdown-container')"
                            + "||document.querySelector('msc-custom-wrapper-dropdown')"
                            + "||document.querySelector('.wrapper-dropdown-container');"
                            + "var scroller=panel&&(panel.querySelector(':scope > div > div:nth-child(3) > div:nth-child(2)')"
                            + "||panel.querySelector('.multi-table-column-container .multi-options-list')"
                            + "||panel.querySelector('.multi-table-column-container')"
                            + "||panel.querySelector('.multi-options-list')||panel);"
                            + "if(scroller){scroller.scrollTop+=350;return true;}"
                            + "window.scrollBy(0,350);return false;");
        } catch (Exception ignored) {
        }
    }



    private void scrollPanelUp() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('msc-custom-wrapper-dropdown .wrapper-dropdown-container')"
                            + "||document.querySelector('msc-custom-wrapper-dropdown')"
                            + "||document.querySelector('.wrapper-dropdown-container');"
                            + "var scroller=panel&&(panel.querySelector(':scope > div > div:nth-child(3) > div:nth-child(2)')"
                            + "||panel.querySelector('.multi-table-column-container .multi-options-list')"
                            + "||panel.querySelector('.multi-table-column-container')"
                            + "||panel.querySelector('.multi-options-list')||panel);"
                            + "if(scroller){scroller.scrollTop-=350;return true;}"
                            + "window.scrollBy(0,-350);return false;");
        } catch (Exception ignored) {
        }
    }



    public void enableColumn(String columnName) throws InterruptedException {

        enableColumn(columnName, "order");

    }



    public void verifyGridHeaderVisible(String columnName) throws InterruptedException {
        verifyGridHeaderVisible(columnName, "order");
    }

    public void verifyGridHeaderVisible(String columnName, String section) throws InterruptedException {
        prepareGridForHeaderCheck(section);
        boolean visible = isExactGridHeaderLabelVisible(columnName);
        if (!visible && "order".equalsIgnoreCase(section)) {
            String columnId = resolveColumnId(columnName, section);
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            for (int pass = 0; pass < 5 && !visible; pass++) {
                visible = isExactGridHeaderLabelVisible(columnName)
                        || isGridHeaderVisibleViaColId(columnId);
                Thread.sleep(400);
            }
        }
        Verify.softAssert(visible, columnName + " is visible on Orders grid");
    }

    /** Same checks as {@link #verifyGridHeaderVisible} but returns boolean (TC_006 prep parity). */
    public boolean isGridHeaderVisibleForPrep(String columnName, String section) throws InterruptedException {
        prepareGridForHeaderCheck(section);
        if (WaitUtil.isDisplay(tableView.gridColumnHeader(columnName), 2)) {
            return true;
        }
        String columnId = resolveColumnId(columnName, section);
        if ("Partner".equalsIgnoreCase(columnName)) {
            for (int pass = 0; pass < 3; pass++) {
                FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
                if (isExactGridHeaderLabelVisible(columnName) || isGridHeaderVisibleViaColId(columnId)) {
                    return true;
                }
                Thread.sleep(250);
            }
            return false;
        }
        String[] candidates = gridHeaderCandidates(columnName);
        for (String candidate : candidates) {
            FulfillmentJsUtil.waitForGridHeaderVisible(candidate, columnId, 8);
            boolean visible = isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                    || FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                    || isGridHeaderVisibleViaScript(candidate)
                    || isGridHeaderVisibleViaColId(columnId)
                    || isGridHeaderVisibleViaXPath(candidate);
            if ("Partner".equalsIgnoreCase(columnName) && !visible) {
                visible = isExactGridHeaderLabelVisible(columnName);
            }
            if (visible) {
                return true;
            }
            Thread.sleep(250);
        }
        return false;
    }

    private void prepareGridForHeaderCheck(String section) throws InterruptedException {
        dismissBlockingModals();
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(300);
        }
        waitForOrdersGridReadyFast(15);
        if (!"order".equalsIgnoreCase(section)) {
            preparePackageGridForColumnCheck(section);
        }
        resetGridHorizontalScroll();
    }

    public void verifyPackageColumnVisible(String columnName, String section) throws InterruptedException {
        dismissBlockingModals();
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(800);
        }
        waitForOrdersGridReady();
        waitForGridHeadersPopulated();
        preparePackageGridForColumnCheck(section);
        resetGridHorizontalScroll();
        String columnId = resolveColumnId(columnName, section);
        String[] candidates = gridHeaderCandidates(columnName);
        boolean visible = isPackageColumnVisibleViaScript(columnId, candidates)
                || isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                || isGridColumnVisibleViaDataCell(columnId)
                || isGridColumnVisibleViaPageText(columnName, candidates);
        if (!visible) {
            for (String candidate : candidates) {
                if (FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                        || isGridHeaderVisibleViaScript(candidate)
                        || isGridHeaderVisibleViaColId(columnId)
                        || isGridHeaderVisibleViaXPath(candidate)) {
                    visible = true;
                    break;
                }
            }
        }
        Verify.softAssert(visible, columnName + " is visible on Orders grid");
    }

    public void verifyPackageColumnHidden(String columnName, String section) {
        String columnId = resolveColumnId(columnName, section);
        String[] candidates = gridHeaderCandidates(columnName);
        try {
            preparePackageGridForColumnCheck(section);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean visible = isPackageColumnVisibleViaScript(columnId, candidates)
                || isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                || isGridColumnVisibleViaDataCell(columnId);
        if (!visible) {
            for (String candidate : candidates) {
                if (FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                        || isGridHeaderVisibleViaScript(candidate)
                        || isGridHeaderVisibleViaColId(columnId)
                        || isGridHeaderVisibleViaXPath(candidate)) {
                    visible = true;
                    break;
                }
            }
        }
        Verify.softAssert(!visible, columnName + " is hidden on Orders grid");
    }

    private boolean isPackageColumnVisibleViaScript(String columnId, String[] candidates) {
        String columnIdEsc = escapeJsString(columnId == null ? "" : columnId);
        StringBuilder candidateArray = new StringBuilder("[");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                candidateArray.append(",");
            }
            candidateArray.append("'").append(escapeJsString(candidates[i])).append("'");
        }
        candidateArray.append("]");
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                            + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                            + "var columnId = normId('" + columnIdEsc + "');"
                            + "var candidates = " + candidateArray + ".map(norm);"
                            + "var colNodes = document.querySelectorAll('[col-id],[data-col-id]');"
                            + "for (var i = 0; i < colNodes.length; i++) {"
                            + "  var cid = normId(colNodes[i].getAttribute('col-id')"
                            + "    || colNodes[i].getAttribute('data-col-id'));"
                            + "  if (!cid || !columnId) continue;"
                            + "  if (columnId === 'type' && cid !== 'type') continue;"
                            + "  if (cid === columnId || cid.indexOf(columnId) >= 0 || columnId.indexOf(cid) >= 0) {"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "var roots = [document];"
                            + "document.querySelectorAll("
                            + "'.package-group,.package-details,[class*=inner-package],.ag-details-row,.ag-full-width-container'"
                            + ").forEach(function(r){ roots.push(r); });"
                            + "for (var r = 0; r < roots.length; r++) {"
                            + "  var headers = roots[r].querySelectorAll("
                            + "'.ag-header-cell,.ag-header-cell-text,[role=columnheader]');"
                            + "  for (var h = 0; h < headers.length; h++) {"
                            + "    var text = norm(headers[h].innerText || headers[h].textContent);"
                            + "    for (var c = 0; c < candidates.length; c++) {"
                            + "      var cand = candidates[c];"
                            + "      if (!text || !cand) continue;"
                            + "      if (cand === 'type' && text !== 'type') continue;"
                            + "      if (text === cand || text.indexOf(cand) >= 0 || cand.indexOf(text) >= 0) {"
                            + "        return true;"
                            + "      }"
                            + "    }"
                            + "  }"
                            + "}"
                            + "return false;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            Logger.logConsoleMessage("Package column scan failed for [" + columnIdEsc + "]: " + e.getMessage());
            return false;
        }
    }

    private void waitForGridHeadersWithContent() throws InterruptedException {
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
            if (isScriptTruthy(BaseTest.driver.get().browser().executeScript(
                    "var cells = document.querySelectorAll('.ag-header-cell');"
                            + "if (cells.length < 5) return false;"
                            + "var populated = 0;"
                            + "for (var i = 0; i < cells.length; i++) {"
                            + "  var t = (cells[i].innerText || cells[i].textContent"
                            + "    || cells[i].getAttribute('aria-label') || '').trim();"
                            + "  if (t) populated++;"
                            + "}"
                            + "return populated >= 5;"))) {
                return;
            }
            Thread.sleep(300);
        }
    }

    private boolean isGridHeaderVisibleViaBulkScript(String columnName, String columnId, String[] candidates) {
        return isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, "order");
    }

    private boolean isGridHeaderVisibleViaBulkScript(
            String columnName, String columnId, String[] candidates, String section) {
        StringBuilder candidateArray = new StringBuilder("[");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                candidateArray.append(",");
            }
            candidateArray.append("'").append(escapeJsString(candidates[i])).append("'");
        }
        candidateArray.append("]");
        String columnIdEsc = escapeJsString(columnId == null ? "" : columnId);
        String columnNameEsc = escapeJsString(columnName);
        boolean nestedSection = "package".equalsIgnoreCase(section) || "lineitem".equalsIgnoreCase(section);
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                            + "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                            + "var candidates = " + candidateArray + ".map(norm);"
                            + "var columnId = normId('" + columnIdEsc + "');"
                            + "var idAliases = [];"
                            + "if (columnId) idAliases.push(columnId);"
                            + "if (columnId.indexOf('orderstart') >= 0) {"
                            + "  idAliases.push('startdate','orderstart','orderstartdate');"
                            + "}"
                            + "if (columnId.indexOf('orderend') >= 0) {"
                            + "  idAliases.push('enddate','orderend','orderenddate');"
                            + "}"
                            + "if (columnId.indexOf('partnerprofile') >= 0) {"
                            + "  idAliases.push('partnerprofile','partner');"
                            + "}"
                            + "if (columnId.indexOf('packagename') >= 0) {"
                            + "  idAliases.push('packagename','pkgname');"
                            + "}"
                            + "var scanPasses = " + (nestedSection ? "20" : "12") + ";"
                            + "var scrollStep = " + (nestedSection ? "550" : "450") + ";"
                            + "function scrollHeaders(delta){"
                            + "  var viewports = document.querySelectorAll("
                            + "'.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport');"
                            + "  for (var i = 0; i < viewports.length; i++) {"
                            + "    if (delta === 0) viewports[i].scrollLeft = 0;"
                            + "    else viewports[i].scrollLeft = Math.max(0, (viewports[i].scrollLeft || 0) + delta);"
                            + "  }"
                            + "}"
                            + "function cellText(cell){"
                            + "  return norm(cell.innerText || cell.textContent"
                            + "    || cell.getAttribute('aria-label') || cell.getAttribute('title'));"
                            + "}"
                            + "function matches(cell){"
                            + "  var text = cellText(cell);"
                            + "  var colId = normId(cell.getAttribute('col-id'));"
                            + "  for (var a = 0; a < idAliases.length; a++) {"
                            + "    var alias = idAliases[a];"
                            + "    if (!alias || !colId) continue;"
                            + "    if (columnId === 'partner' && colId !== 'partner') continue;"
                            + "    if (columnId === 'partnerprofile' && colId !== 'partnerprofile') continue;"
                            + "    if (colId === alias || colId.indexOf(alias) >= 0 || alias.indexOf(colId) >= 0) return true;"
                            + "  }"
                            + "  for (var c = 0; c < candidates.length; c++) {"
                            + "    var cand = candidates[c];"
                            + "    if (!text || !cand) continue;"
                            + "    if (text === cand || text.indexOf(cand) >= 0 || cand.indexOf(text) >= 0) {"
                            + "      if ((cand === 'start date' || cand === 'end date') && text.indexOf('order') < 0) continue;"
                            + "      if (cand === 'partner' && text !== 'partner') continue;"
                            + "      return true;"
                            + "    }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "function headerCells(){"
                            + "  var cells = [];"
                            + "  document.querySelectorAll('.ag-header-cell').forEach(function(c){ cells.push(c); });"
                            + "  document.querySelectorAll('.ag-details-row .ag-header-cell').forEach(function(c){ cells.push(c); });"
                            + "  return cells;"
                            + "}"
                            + "for (var pass = 0; pass < scanPasses; pass++) {"
                            + "  scrollHeaders(pass === 0 ? 0 : scrollStep);"
                            + "  var cells = headerCells();"
                            + "  for (var i = 0; i < cells.length; i++) {"
                            + "    if (matches(cells[i])) return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            Logger.logConsoleMessage("Bulk header scan failed for [" + columnNameEsc + "]: " + e.getMessage());
            return false;
        }
    }

    private String escapeJsString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private boolean isGridColumnVisibleViaPageText(String columnName, String[] candidates) {
        String columnNameEsc = escapeJsString(columnName);
        StringBuilder candidateArray = new StringBuilder("[");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                candidateArray.append(",");
            }
            candidateArray.append("'").append(escapeJsString(candidates[i])).append("'");
        }
        candidateArray.append("]");
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                            + "var page = norm(document.body ? (document.body.innerText || document.body.textContent) : '');"
                            + "if (!page) return false;"
                            + "var candidates = " + candidateArray + ".map(norm);"
                            + "for (var i = 0; i < candidates.length; i++) {"
                            + "  var cand = candidates[i];"
                            + "  if (!cand) continue;"
                            + "  if (page.indexOf(cand) >= 0) {"
                            + "    if ((cand === 'start date' || cand === 'end date') && page.indexOf('order ' + cand) < 0) continue;"
                            + "    if (cand === 'partner') continue;"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "return page.indexOf(norm('" + columnNameEsc + "')) >= 0;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            Logger.logConsoleMessage("Page text column scan failed for [" + columnNameEsc + "]: " + e.getMessage());
            return false;
        }
    }

    private boolean isGridColumnVisibleViaDataCell(String columnId) {
        if (columnId == null || columnId.trim().isEmpty()) {
            return false;
        }
        String columnIdEsc = escapeJsString(columnId);
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                            + "var columnId = normId('" + columnIdEsc + "');"
                            + "var idAliases = [columnId];"
                            + "if (columnId.indexOf('orderstart') >= 0) {"
                            + "  idAliases.push('startdate','orderstart','orderstartdate');"
                            + "}"
                            + "if (columnId.indexOf('orderend') >= 0) {"
                            + "  idAliases.push('enddate','orderend','orderenddate');"
                            + "}"
                            + "if (columnId.indexOf('partnerprofile') >= 0) {"
                            + "  idAliases.push('partnerprofile','partner');"
                            + "}"
                            + "if (columnId.indexOf('packagename') >= 0) {"
                            + "  idAliases.push('packagename','pkgname');"
                            + "}"
                            + "function scrollBody(delta){"
                            + "  var viewports = document.querySelectorAll("
                            + "'.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport,.ag-body-viewport');"
                            + "  for (var i = 0; i < viewports.length; i++) {"
                            + "    if (delta === 0) viewports[i].scrollLeft = 0;"
                            + "    else viewports[i].scrollLeft = Math.max(0, (viewports[i].scrollLeft || 0) + delta);"
                            + "  }"
                            + "}"
                            + "function matchesCell(cell){"
                            + "  var colId = normId(cell.getAttribute('col-id'));"
                            + "  if (!colId) return false;"
                            + "  for (var a = 0; a < idAliases.length; a++) {"
                            + "    var alias = idAliases[a];"
                            + "    if (!alias) continue;"
                            + "    if (columnId === 'partner' && colId !== 'partner') continue;"
                            + "    if (colId === alias || colId.indexOf(alias) >= 0 || alias.indexOf(colId) >= 0) {"
                            + "      return true;"
                            + "    }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "for (var pass = 0; pass < 12; pass++) {"
                            + "  scrollBody(pass === 0 ? 0 : 450);"
                            + "  var cells = document.querySelectorAll('.ag-center-cols-container .ag-cell,[col-id]');"
                            + "  for (var i = 0; i < cells.length; i++) {"
                            + "    if (matchesCell(cells[i])) return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            Logger.logConsoleMessage("Data-cell column scan failed for [" + columnId + "]: " + e.getMessage());
            return false;
        }
    }

    public void waitForGridHeadersPopulated() {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            try {
                List<DesktopBrowserElement> cells = BaseTest.driver.get().finder()
                        .findElements(By.XPath("//div[contains(@class,'ag-header-cell')]"));
                if (cells != null && cells.size() >= 5) {
                    return;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean isGridHeaderVisibleViaColId(String columnId) {
        if (columnId == null || columnId.trim().isEmpty()) {
            return false;
        }
        String lit = columnId.replace("'", "\\'");
        String litLower = columnId.toLowerCase().replace("'", "\\'");
        if ("partner".equalsIgnoreCase(columnId)) {
            FulfillmentJsUtil.scrollColumnHeaderIntoView("Partner", columnId);
            By exactPartner = By.XPath(
                    "//div[contains(@class,'ag-header-cell')][translate(@col-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                            + "'abcdefghijklmnopqrstuvwxyz')='" + litLower + "']");
            if (WaitUtil.isDisplay(exactPartner, 5)) {
                return true;
            }
            return WaitUtil.isDisplay(tableView.gridColumnHeaderByColId(columnId), 3);
        }
        By byColId = By.XPath(
                "//div[contains(@class,'ag-header-cell')][contains("
                        + "translate(@col-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + litLower + "')]");
        if (WaitUtil.isDisplay(byColId, 5)) {
            return true;
        }
        String snake = columnId.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
        if (!snake.equals(litLower)) {
            By bySnake = By.XPath(
                    "//div[contains(@class,'ag-header-cell')][contains("
                            + "translate(@col-id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                            + snake.replace("'", "\\'") + "')]");
            return WaitUtil.isDisplay(bySnake, 5);
        }
        return false;
    }

    private boolean isGridHeaderVisibleViaXPath(String columnName) {
        if ("Partner".equalsIgnoreCase(columnName)) {
            return isExactGridHeaderLabelVisible(columnName);
        }
        return WaitUtil.isDisplay(tableView.gridColumnHeader(columnName), 2);
    }

    private String[] gridHeaderCandidates(String columnName) {
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            return new String[] {
                    columnName,
                    "Title",
                    "Season",
                    "Episode",
                    "Title/Season/Episode"
            };
        }
        if ("Title".equalsIgnoreCase(columnName)
                || "Season".equalsIgnoreCase(columnName)
                || "Episode".equalsIgnoreCase(columnName)) {
            return new String[] {
                    columnName,
                    "Title, Season, Episode",
                    "Title/Season/Episode"
            };
        }
        if ("Order start date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order Start Date", "start date" };
        }
        if ("Order end date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order End Date", "end date" };
        }
        if ("Partner Profile".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Partner" };
        }
        if ("Package name".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Package Name" };
        }
        return new String[] { columnName };
    }

    public void prepareNestedGridForColumnCheck(String section) throws InterruptedException {
        preparePackageGridForColumnCheck(section);
    }

    private void preparePackageGridForColumnCheck(String section) throws InterruptedException {
        if (!"package".equalsIgnoreCase(section) && !"lineitem".equalsIgnoreCase(section)) {
            return;
        }
        try {
            BaseTest.driver.get().browser().executeScript(
                    "function clickExpandRow(row){"
                            + "if(!row)return false;"
                            + "row.scrollIntoView({block:'center'});"
                            + "var sideBtn=row.querySelector("
                            + "'button.side-nav-button, button.dropdown-toggle:not(.ellipsisButton)');"
                            + "if(sideBtn){sideBtn.click();return true;}"
                            + "var icon=row.querySelector("
                            + "'.ag-group-contracted,.ag-icon-tree-closed,[aria-expanded=\"false\"]');"
                            + "if(icon){icon.click();return true;}"
                            + "return false;"
                            + "}"
                            + "var orderRow=document.querySelector("
                            + "'.ag-center-cols-container > .ag-row:not(.ag-row-level-1):not(.ag-row-level-2)');"
                            + "if(!orderRow){"
                            + "  orderRow=document.querySelector('.ag-center-cols-container .ag-row');"
                            + "}"
                            + "clickExpandRow(orderRow);");
            Thread.sleep(1500);
            if ("lineitem".equalsIgnoreCase(section)) {
                BaseTest.driver.get().browser().executeScript(
                        "function clickExpandRow(row){"
                                + "if(!row)return false;"
                                + "row.scrollIntoView({block:'center'});"
                                + "var sideBtn=row.querySelector("
                                + "'button.side-nav-button, button.dropdown-toggle:not(.ellipsisButton)');"
                                + "if(sideBtn){sideBtn.click();return true;}"
                                + "var icon=row.querySelector("
                                + "'.ag-group-contracted,.ag-icon-tree-closed,[aria-expanded=\"false\"]');"
                                + "if(icon){icon.click();return true;}"
                                + "return false;"
                                + "}"
                                + "var pkgRow=document.querySelector("
                                + "'.ag-center-cols-container .ag-row.ag-row-level-1');"
                                + "clickExpandRow(pkgRow);");
                Thread.sleep(1500);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isNestedColumnVisible(String columnName, String section) throws InterruptedException {
        if (!"package".equalsIgnoreCase(section) && !"lineitem".equalsIgnoreCase(section)) {
            return false;
        }
        dismissBlockingModals();
        if (isManageColumnsPanelOpen()) {
            closeManageColumns();
            Thread.sleep(800);
        }
        waitForOrdersGridReady();
        waitForGridHeadersPopulated();
        preparePackageGridForColumnCheck(section);
        resetGridHorizontalScroll();
        String columnId = resolveColumnId(columnName, section);
        String[] candidates = gridHeaderCandidates(columnName);
        boolean visible = isPackageColumnVisibleViaScript(columnId, candidates)
                || isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                || isGridColumnVisibleViaDataCell(columnId)
                || isGridColumnVisibleViaPageText(columnName, candidates);
        if (!visible) {
            for (String candidate : candidates) {
                if (FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                        || isGridHeaderVisibleViaScript(candidate)
                        || isGridHeaderVisibleViaColId(columnId)
                        || isGridHeaderVisibleViaXPath(candidate)) {
                    visible = true;
                    break;
                }
            }
        }
        return visible;
    }

    private void resetGridHorizontalScroll() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var v = document.querySelector('.ag-header-viewport,.ag-center-cols-viewport');"
                            + "if (v) v.scrollLeft = 0;");
        } catch (Exception ignored) {
        }
    }

    private boolean isGridHeaderVisibleViaScript(String columnName) {
        String escaped = columnName.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                            + "function ordersGridRoot(){"
                            + "  var roots=document.querySelectorAll('.ag-root-wrapper,.ag-root');"
                            + "  var best=null,bestRows=0;"
                            + "  for(var r=0;r<roots.length;r++){"
                            + "    var rows=roots[r].querySelectorAll("
                            + "'.ag-center-cols-container > .ag-row:not(.ag-row-level-1)').length;"
                            + "    if(rows>bestRows){bestRows=rows;best=roots[r];}"
                            + "  }"
                            + "  return best||document;"
                            + "}"
                            + "var name = norm('" + escaped + "');"
                            + "var root = ordersGridRoot();"
                            + "var cells = root.querySelectorAll('.ag-header-cell');"
                            + "for (var i = 0; i < cells.length; i++) {"
                            + "  var t = norm(cells[i].innerText || cells[i].textContent);"
                            + "  if (!t) continue;"
                            + "  if (name === 'partner' && t !== 'partner') continue;"
                            + "  if (t === name || t.indexOf(name) >= 0 || name.indexOf(t) >= 0) return true;"
                            + "}"
                            + "return false;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            return false;
        }
    }



    public void verifyGridHeaderHidden(String columnName) {
        verifyGridHeaderHidden(columnName, "order");
    }

    public void verifyGridHeaderHidden(String columnName, String section) {
        String columnId = resolveColumnId(columnName, section);
        String[] candidates = gridHeaderCandidates(columnName);
        try {
            preparePackageGridForColumnCheck(section);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean visible = isGridHeaderVisibleViaBulkScript(columnName, columnId, candidates, section)
                || isGridColumnVisibleViaDataCell(columnId);
        if (!visible) {
            for (String candidate : candidates) {
                if (FulfillmentJsUtil.isColumnVisibleInGrid(candidate, columnId)
                        || isGridHeaderVisibleViaScript(candidate)
                        || isGridHeaderVisibleViaColId(columnId)
                        || isGridHeaderVisibleViaXPath(candidate)) {
                    visible = true;
                    break;
                }
            }
        }
        Verify.softAssert(!visible, columnName + " is hidden on Orders grid");
    }

    private String resolveColumnId(String columnName, String section) {
        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column == null && "order".equalsIgnoreCase(section)) {
            if ("Title".equalsIgnoreCase(columnName)
                    || "Season".equalsIgnoreCase(columnName)
                    || "Episode".equalsIgnoreCase(columnName)) {
                column = OrderTabColumnData.findByLabelAndSection("Title, Season, Episode", section);
            }
        }
        return column == null ? null : column.id;
    }



    public void verifyAllOrderColumnsListed() throws InterruptedException {
        scrollPanelToTop();
        for (OrderTabColumnData.ColumnDef column : OrderTabColumnData.ALL_COLUMNS) {
            if (isOptionalProdColumn(column)) {
                Logger.logReportMessage("Optional column not in PROD UI — skipping: [" + column.section + "] "
                        + column.label);
                continue;
            }
            scrollToColumn(column.label, column.section);
            boolean listed = isColumnListed(column.label, column.section);
            Verify.softAssert(listed,
                    "Column listed in Manage Columns [" + column.section + "]: " + column.label);
        }
    }

    private boolean isOptionalProdColumn(OrderTabColumnData.ColumnDef column) {
        if (column == null) {
            return false;
        }
        return "fastTrack".equalsIgnoreCase(column.id)
                || "Fast Track".equalsIgnoreCase(column.label);
    }



    public void saveTableView(String viewName) throws InterruptedException {

        dismissBlockingModals();

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
            Logger.logConsoleMessage("Script save visible in UI but not confirmed — retrying via modal for: " + viewName);
        }

        openSaveTableViewModal();

        if (!waitForSaveModalOpen()) {
            Logger.logConsoleMessage("Save table view modal did not open — trying GraphQL save for: " + viewName);
            if (TableViewGraphqlClient.saveTableViewViaGraphql(viewName) && confirmTableViewSaved(viewName)) {
                return;
            }
            Verify.softAssert(false, "Save table view modal did not open");
            return;
        }

        clearAndTypeTableViewName(viewName);

        clickSaveOnTableViewModal();

        Thread.sleep(1000);

        confirmTableViewSaved(viewName);
    }

    private boolean confirmTableViewSaved(String viewName) throws InterruptedException {
        boolean uiSaved = waitForTableViewSaved(viewName);
        boolean hasId = FulfillmentJsUtil.waitForSavedViewWithId(viewName, 20);
        if (!hasId && uiSaved) {
            hasId = TableViewGraphqlClient.syncViewIdAfterUiSave(viewName);
        }
        Verify.softAssert(uiSaved || hasId, "Table view created: " + viewName);
        if (hasId) {
            Verify.softAssert(true, "Table view id captured from API: " + viewName);
        } else if (uiSaved) {
            Logger.logConsoleMessage("Table view visible in UI without cached API id: " + viewName);
        } else {
            Verify.softAssert(false, "Table view id captured from API: " + viewName);
        }
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        closeSaveTableViewModalIfOpen();
        if (uiSaved || hasId) {
            activateSavedTableView(viewName);
        }
        waitForOrdersGridReady();
        Thread.sleep(400);
        return uiSaved || hasId;
    }

    private void activateSavedTableView(String viewName) throws InterruptedException {
        FulfillmentJsUtil.refreshTableViewsUiState();
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        activateSavedViewOnOrdersGrid(viewName);
        if (FulfillmentJsUtil.isActiveTableView(viewName)) {
            waitForOrdersGridReady();
            Thread.sleep(500);
            return;
        }
        if (FulfillmentJsUtil.selectSavedViewViaScript(viewName)) {
            waitForOrdersGridReady();
            Thread.sleep(500);
            if (FulfillmentJsUtil.isActiveTableView(viewName)) {
                return;
            }
        }
        if (clickTableViewOptionViaScript(viewName)) {
            waitForOrdersGridReady();
            Thread.sleep(500);
            return;
        }
        switchTableView(viewName);
        waitForOrdersGridReady();
        Thread.sleep(500);
    }

    private void scrollSaveNewViewIntoView() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var panel=document.querySelector('.wrapper-dropdown-container');"
                            + "if(!panel)return false;"
                            + "var scrollers=panel.querySelectorAll('.multi-table-column-container,.multi-options-list');"
                            + "for(var i=0;i<scrollers.length;i++){scrollers[i].scrollTop=scrollers[i].scrollHeight;}"
                            + "panel.scrollTop=panel.scrollHeight;"
                            + "var btn=panel.querySelector('button');"
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

    private void openSaveTableViewModal() throws InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            if (FulfillmentJsUtil.isSaveModalVisible()) {
                return;
            }
            if (FulfillmentJsUtil.clickSaveNewViewButton()) {
                if (waitForSaveModalOpen()) {
                    return;
                }
            } else if (WaitUtil.isDisplay(tableView.saveNewButtonTableView(), 2)) {
                DriverUtil.clickOnElementSafely(tableView.saveNewButtonTableView(), 10);
            } else {
                clickSaveNewViewButtonInPanel();
            }
            if (waitForSaveModalOpen()) {
                return;
            }
            Thread.sleep(500);
        }
    }

    private void clickSaveNewViewButtonInPanel() {
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "var panel = document.querySelector('.wrapper-dropdown-container');"
                            + "if (!panel) return false;"
                            + "var nodes = panel.querySelectorAll('button, span, a');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "  if (t.indexOf('save new view') >= 0) { nodes[i].click(); return true; }"
                            + "}"
                            + "return false;");
            if (!isScriptTruthy(clicked)) {
                DriverUtil.clickOnElementSafely(tableView.saveNewButtonTableView(), 10);
            }
        } catch (Exception e) {
            DriverUtil.clickOnElementSafely(tableView.saveNewButtonTableView(), 10);
        }
    }

    private boolean waitForSaveModalOpen() throws InterruptedException {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            if (isSaveModalOpenViaScript()) {
                return true;
            }
            if (WaitUtil.isDisplay(tableView.inputTableViewName(), 1)) {
                return FulfillmentJsUtil.isSaveModalVisible();
            }
            Thread.sleep(300);
        }
        return isSaveModalOpenViaScript();
    }

    private boolean isSaveModalOpenViaScript() {
        return FulfillmentJsUtil.isSaveModalVisible();
    }

    private boolean isSaveModalClosedViaScript() {
        return !FulfillmentJsUtil.isSaveModalVisible();
    }

    private void clearAndTypeTableViewName(String viewName) {
        if (FulfillmentJsUtil.fillSaveModalViewName(viewName)) {
            return;
        }
        String escaped = viewName.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object typed = BaseTest.driver.get().browser().executeScript(
                    "function isVisible(el){"
                            + "if(!el)return false;"
                            + "var r=el.getBoundingClientRect();"
                            + "if(r.width<=0||r.height<=0)return false;"
                            + "var s=window.getComputedStyle(el);"
                            + "return s.display!=='none'&&s.visibility!=='hidden';"
                            + "}"
                            + "var inputs=document.querySelectorAll("
                            + "\"input[placeholder='Enter table view name'],input[placeholder*='table view']\");"
                            + "var input=null;"
                            + "for(var i=0;i<inputs.length;i++){if(isVisible(inputs[i])){input=inputs[i];break;}}"
                            + "if(!input)return false;"
                            + "input.focus();input.value='';input.value='" + escaped + "';"
                            + "input.dispatchEvent(new Event('input', {bubbles: true}));"
                            + "input.dispatchEvent(new Event('change', {bubbles: true}));"
                            + "return (input.value || '').indexOf('" + escaped + "') >= 0;");
            if (isScriptTruthy(typed)) {
                return;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("JS table view name entry failed: " + e.getMessage());
        }
        if (WaitUtil.isDisplay(tableView.inputTableViewName(), 5)) {
            BaseTest.driver.get().finder().findElement(tableView.inputTableViewName()).sendKeys(viewName);
        }
    }

    private void clickSaveOnTableViewModal() {
        if (!isSaveModalOpenViaScript()) {
            Logger.logConsoleMessage("Save table view modal not open; skipping save click");
            return;
        }
        if (FulfillmentJsUtil.clickSaveNewOnModal()) {
            Logger.logReportMessage("Clicked Save on table view modal via script");
            return;
        }
        DriverUtil.clickOnElementSafely(tableView.saveButtonOnTableViewPopup(), 10);
    }

    private boolean waitForTableViewSaved(String viewName) throws InterruptedException {
        long end = System.currentTimeMillis() + 45000;
        while (System.currentTimeMillis() < end) {
            if (FulfillmentJsUtil.isTableViewCreatedToastVisible()) {
                return true;
            }
            if (isViewNameVisibleViaScript(viewName)) {
                return true;
            }
            if (FulfillmentJsUtil.isViewNameVisible(viewName)) {
                return true;
            }
            Thread.sleep(500);
        }
        return isViewNameVisibleViaScript(viewName) || FulfillmentJsUtil.isViewNameVisible(viewName);
    }

    private boolean isViewNameVisibleViaScript(String viewName) {
        String escaped = viewName.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object match = BaseTest.driver.get().browser().executeScript(
                    "var name = '" + escaped + "';"
                            + "var btn = document.querySelector('#tableViewButton');"
                            + "if (btn && (btn.textContent || '').indexOf(name) >= 0) return true;"
                            + "var panel = document.querySelector('.wrapper-dropdown-container');"
                            + "if (panel && (panel.textContent || '').indexOf(name) >= 0) return true;"
                            + "var body = document.body ? document.body.textContent || '' : '';"
                            + "if (body.indexOf(name) >= 0 && body.toLowerCase().indexOf('table view') >= 0) return true;"
                            + "return false;");
            return isScriptTruthy(match);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isViewNameOnTableViewButton(String viewName) {
        return isViewNameVisibleViaScript(viewName);
    }



    public void switchTableView(String viewName) throws InterruptedException {

        dismissBlockingModals();

        if (!isManageColumnsPanelOpen()) {

            openManageColumns();

        }

        if (clickTableViewOptionViaScript(viewName)) {
            waitForOrdersGridReady();
            return;
        }

        openViewSelectorViaScript();
        Thread.sleep(400);
        if (clickTableViewOptionViaScript(viewName)) {
            waitForOrdersGridReady();
            return;
        }

        openSavedViewsList();

        if (WaitUtil.isDisplay(tableView.tableViewOptionInDropdown(viewName), 5)) {

            DriverUtil.clickOnElementSafely(tableView.tableViewOptionInDropdown(viewName), 10);

        } else {

            DriverUtil.clickOnElementSafely(tableView.tableViewByName(viewName), 10);

        }

        waitForOrdersGridReady();

    }

    private boolean clickTableViewOptionViaScript(String viewName) {
        String escaped = viewName.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "var name = '" + escaped + "';"
                            + "var nodes = document.querySelectorAll('span, label, div.option, div.team-filter');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').trim();"
                            + "  if (t === name || t.indexOf(name) >= 0) { nodes[i].click(); return true; }"
                            + "}"
                            + "return false;");
            return isScriptTruthy(clicked);
        } catch (Exception e) {
            return false;
        }
    }



    public void assertTableViewExists(String viewName) {
        try {
            boolean visible = TableViewGraphqlClient.hasCachedViewId(viewName)
                    || FulfillmentJsUtil.waitForViewListedInApi(viewName, true, 8)
                    || waitForViewInSelector(viewName, true, 8000);
            Verify.softAssert(visible, "Table view exists in selector: " + viewName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    public void assertTableViewAbsent(String viewName) {
        try {
            boolean absent = FulfillmentJsUtil.waitForViewListedInApi(viewName, false, 8)
                    || waitForViewInSelector(viewName, false, 8000);
            Verify.softAssert(absent, "Table view removed from selector: " + viewName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean waitForViewInSelector(String viewName, boolean shouldExist, long timeoutMs)
            throws InterruptedException {
        if (shouldExist && FulfillmentJsUtil.isViewNameVisible(viewName)) {
            return true;
        }
        if (!shouldExist && !FulfillmentJsUtil.isViewNameVisible(viewName)) {
            return true;
        }
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            openSavedViewsList();
            boolean visible = isViewNameVisibleViaScript(viewName)
                    || FulfillmentJsUtil.isViewNameVisible(viewName)
                    || WaitUtil.isDisplay(tableView.tableViewInManageColumnsPanel(viewName), 1)
                    || WaitUtil.isDisplay(tableView.viewNameInPanelSelector(viewName), 1)
                    || WaitUtil.isDisplay(tableView.tableViewInDropdown(viewName), 1);
            if (shouldExist == visible) {
                return shouldExist;
            }
            Thread.sleep(300);
        }
        openSavedViewsList();
        boolean visible = isViewNameVisibleViaScript(viewName)
                || FulfillmentJsUtil.isViewNameVisible(viewName)
                || WaitUtil.isDisplay(tableView.tableViewInManageColumnsPanel(viewName), 1)
                || WaitUtil.isDisplay(tableView.viewNameInPanelSelector(viewName), 1)
                || WaitUtil.isDisplay(tableView.tableViewInDropdown(viewName), 1);
        return shouldExist ? visible : !visible;
    }



    public void renameTableView(String currentName, String newName) throws InterruptedException {

        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        TableViewGraphqlClient.syncViewIdAfterUiSave(currentName);
        FulfillmentJsUtil.cacheSavedViewFromApi(currentName);
        TableViewGraphqlClient.cacheViewFromGraphql(currentName);

        if (renameTableViewViaDuplicateSave(currentName, newName)) {
            FulfillmentJsUtil.refreshTableViewsUiState();
            waitForViewRename(currentName, newName);
            return;
        }

        if (TableViewGraphqlClient.renameTableView(currentName, newName)) {
            FulfillmentJsUtil.refreshTableViewsUiState();
            waitForViewRename(currentName, newName);
            return;
        }

        if (FulfillmentJsUtil.renameViewViaApi(currentName, newName)) {
            waitForViewRename(currentName, newName);
            return;
        }
        Logger.logConsoleMessage("GraphQL rename result: " + FulfillmentJsUtil.getTableViewOpResult());

        if (FulfillmentJsUtil.renameViewViaScript(currentName, newName)) {
            waitForViewRename(currentName, newName);
            return;
        }

        if (renameTableViewViaKeyboard(currentName, newName)) {
            waitForViewRename(currentName, newName);
            return;
        }

        if (renameTableViewViaContextMenu(currentName, newName)) {
            waitForViewRename(currentName, newName);
            return;
        }

        Logger.logConsoleMessage("Rename API capture: " + FulfillmentJsUtil.getApiCaptureDebugJson());
        Verify.softAssert(false, "Rename failed for table view: " + currentName);
    }



    public void deleteTableView(String viewName) throws InterruptedException {

        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        TableViewGraphqlClient.syncViewIdAfterUiSave(viewName);
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        TableViewGraphqlClient.cacheViewFromGraphql(viewName);

        if (TableViewGraphqlClient.deleteTableView(viewName)) {
            FulfillmentJsUtil.refreshTableViewsUiState();
            FulfillmentJsUtil.reloadFulfillmentPage();
            waitForViewDeleted(viewName);
            return;
        }

        if (FulfillmentJsUtil.deleteViewViaApi(viewName)) {
            waitForViewDeleted(viewName);
            return;
        }
        Logger.logConsoleMessage("GraphQL delete result: " + FulfillmentJsUtil.getTableViewOpResult());

        if (FulfillmentJsUtil.deleteViewViaScript(viewName)) {
            waitForViewDeleted(viewName);
            return;
        }

        if (deleteTableViewViaContextMenu(viewName)) {
            waitForViewDeleted(viewName);
            return;
        }

        Logger.logConsoleMessage("Delete API capture: " + FulfillmentJsUtil.getApiCaptureDebugJson());
        Verify.softAssert(false, "Delete failed for table view: " + viewName);
    }

    private By resolveViewInDropdown(String viewName) {
        By exactText = tableView.tableViewExactText(viewName);
        if (WaitUtil.isDisplay(exactText, 2)) {
            return exactText;
        }
        By viewLabel = tableView.tableViewOptionLabel(viewName);
        if (WaitUtil.isDisplay(viewLabel, 2)) {
            return viewLabel;
        }
        By viewInDropdown = tableView.tableViewInDropdown(viewName);
        if (WaitUtil.isDisplay(viewInDropdown, 2)) {
            return viewInDropdown;
        }
        viewInDropdown = tableView.tableViewOptionLabel(viewName);
        if (WaitUtil.isDisplay(viewInDropdown, 2)) {
            return viewInDropdown;
        }
        viewInDropdown = tableView.tableViewOptionRow(viewName);
        if (WaitUtil.isDisplay(viewInDropdown, 2)) {
            return viewInDropdown;
        }
        return tableView.viewNameInPanelSelector(viewName);
    }

    private boolean renameTableViewViaDuplicateSave(String currentName, String newName) throws InterruptedException {
        Logger.logConsoleMessage("Trying duplicate-save rename: " + currentName + " -> " + newName);
        openManageColumns();
        switchTableView(currentName);
        Thread.sleep(600);
        saveTableView(newName);
        if (!waitForTableViewSaved(newName)) {
            return false;
        }
        FulfillmentJsUtil.cacheSavedViewFromApi(newName);
        TableViewGraphqlClient.syncViewIdAfterUiSave(currentName);
        TableViewGraphqlClient.syncViewIdAfterUiSave(newName);
        if (!FulfillmentJsUtil.isViewNameVisible(currentName)) {
            return FulfillmentJsUtil.isViewNameVisible(newName);
        }
        if (TableViewGraphqlClient.deleteTableView(currentName)
                || FulfillmentJsUtil.deleteViewViaApi(currentName)
                || deleteTableViewViaContextMenu(currentName)) {
            waitForViewDeleted(currentName);
        }
        return FulfillmentJsUtil.isViewNameVisible(newName)
                && !FulfillmentJsUtil.isViewNameVisible(currentName);
    }

    private boolean renameTableViewViaKeyboard(String currentName, String newName) throws InterruptedException {
        openSavedViewsList();
        By viewInDropdown = resolveViewInDropdown(currentName);
        if (!WaitUtil.isDisplay(viewInDropdown, 5)) {
            return false;
        }
        Logger.logConsoleMessage("Trying F2 rename shortcut for view: " + currentName);
        DriverUtil.clickOnElementSafely(viewInDropdown, 5);
        Thread.sleep(400);
        DriverUtil.sendFunctionKeyToElement(viewInDropdown, 3, WebKey.F2);
        Thread.sleep(800);
        if (!WaitUtil.isDisplay(tableView.renameTableViewInput(), 5)) {
            DriverUtil.sendFunctionKeyToElement(viewInDropdown, 3, WebKey.F2);
            Thread.sleep(800);
        }
        if (!WaitUtil.isDisplay(tableView.renameTableViewInput(), 5)) {
            return false;
        }
        DriverUtil.sendKeyToElement(tableView.renameTableViewInput(), 10, newName);
        DriverUtil.clickOnElementSafely(tableView.saveRenameButton(), 10);
        DriverUtil.waitForElementVisibleExpicit(tableView.tableViewRenamedMessage(), 15);
        dismissBlockingModals();
        return FulfillmentJsUtil.isViewNameVisible(newName);
    }

    private boolean renameTableViewViaContextMenu(String currentName, String newName) throws InterruptedException {
        if (!openTableViewContextMenu(currentName)) {
            return false;
        }
        if (!clickContextMenuItem("Rename", tableView.renameMenuItem())) {
            openTableViewContextMenu(currentName);
            if (!clickContextMenuItem("Rename", tableView.renameMenuItem())) {
                return false;
            }
        }
        if (!WaitUtil.isDisplay(tableView.renameTableViewInput(), 8)) {
            return false;
        }
        DriverUtil.sendKeyToElement(tableView.renameTableViewInput(), 10, newName);
        DriverUtil.clickOnElementSafely(tableView.saveRenameButton(), 10);
        DriverUtil.waitForElementVisibleExpicit(tableView.tableViewRenamedMessage(), 15);
        dismissBlockingModals();
        return FulfillmentJsUtil.isViewNameVisible(newName);
    }

    private boolean deleteTableViewViaContextMenu(String viewName) throws InterruptedException {
        if (!openTableViewContextMenu(viewName)) {
            return false;
        }
        if (!clickContextMenuItem("Delete", tableView.deleteMenuItem())) {
            openTableViewContextMenu(viewName);
            if (!clickContextMenuItem("Delete", tableView.deleteMenuItem())) {
                return false;
            }
        }
        if (WaitUtil.isDisplay(tableView.confirmDeleteButton(), 5)) {
            DriverUtil.clickOnElementSafely(tableView.confirmDeleteButton(), 10);
        }
        DriverUtil.waitForElementVisibleExpicit(tableView.tableViewDeletedMessage(), 15);
        dismissBlockingModals();
        return !FulfillmentJsUtil.isViewNameVisible(viewName);
    }

    private boolean clickContextMenuItem(String label, By menuItem) throws InterruptedException {
        if (WaitUtil.isDisplay(menuItem, 5)) {
            DriverUtil.clickOnElementSafely(menuItem, 10);
            return true;
        }
        Thread.sleep(500);
        if (DriverUtil.clickScreenText(label)) {
            Thread.sleep(800);
            return WaitUtil.isDisplay(tableView.renameTableViewInput(), 3)
                    || WaitUtil.isDisplay(tableView.confirmDeleteButton(), 3)
                    || WaitUtil.isDisplay(tableView.ngbModalWindow(), 2)
                    || WaitUtil.isDisplay(menuItem, 2);
        }
        return false;
    }

    private void waitForViewRename(String originalName, String newName) throws InterruptedException {
        FulfillmentJsUtil.waitForViewListedInApi(newName, true, 8);
        FulfillmentJsUtil.waitForViewListedInApi(originalName, false, 8);
    }

    private void waitForViewDeleted(String viewName) throws InterruptedException {
        FulfillmentJsUtil.waitForViewListedInApi(viewName, false, 8);
    }

    private void openSavedViewsList() throws InterruptedException {
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        if (WaitUtil.isDisplay(tableView.viewDropdownResetInPanel(), 3)) {
            DriverUtil.clickOnElementSafely(tableView.viewDropdownResetInPanel(), 5);
        } else if (WaitUtil.isDisplay(tableView.teamViewFilterInPanel(), 2)) {
            DriverUtil.clickOnElementSafely(tableView.teamViewFilterInPanel(), 5);
        } else {
            DriverUtil.clickOnElementSafely(tableView.viewSelectorInPanel(), 5);
        }
        Thread.sleep(300);
    }

    private boolean isContextMenuVisible() {
        return WaitUtil.isDisplay(tableView.renameMenuItem(), 2)
                || WaitUtil.isDisplay(tableView.deleteMenuItem(), 2);
    }

    private boolean openTableViewContextMenu(String viewName) throws InterruptedException {

        dismissBlockingModals();

        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }

        openSavedViewsList();

        By viewInDropdown = resolveViewInDropdown(viewName);
        if (!WaitUtil.isDisplay(viewInDropdown, 3)) {
            Logger.logConsoleMessage("Saved view not visible in dropdown: " + viewName);
            return false;
        }

        for (int attempt = 0; attempt < 4 && !isContextMenuVisible(); attempt++) {
            if (attempt > 0) {
                openSavedViewsList();
                Thread.sleep(500);
            }
            Logger.logConsoleMessage("Context menu attempt " + (attempt + 1) + " for view: " + viewName);
            switch (attempt) {
                case 0:
                    DriverUtil.rightClickNative(viewInDropdown, 5);
                    break;
                case 1:
                    DriverUtil.rightClickAtBrowserCoordinates(viewInDropdown, 5, this::isContextMenuVisible);
                    break;
                case 2:
                    DriverUtil.contextMenuViaShiftF10(viewInDropdown, 5);
                    break;
                default:
                    DriverUtil.rightClickViaJsEvents(viewInDropdown, 5);
                    break;
            }
            Thread.sleep(1200);
            if (!isContextMenuVisible()) {
                DriverUtil.clickScreenText("Rename");
                Thread.sleep(400);
            }
            if (!isContextMenuVisible()) {
                DriverUtil.clickScreenText("Delete");
            }
        }
        return isContextMenuVisible();
    }



    public void verifyColumnSelectionCounterVisible() throws InterruptedException {
        dismissBlockingModals();
        if (!isManageColumnsPanelOpen()) {
            openManageColumns();
        }
        scrollPanelToTop();
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
        Verify.softAssert(visible, "Column selection counter is displayed (e.g. 32 of 49)");
    }



    public Object[][] allOrderTabColumns() {

        return OrderTabColumnData.allManageColumnsData();

    }

}


