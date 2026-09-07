package com.paramount.test.ff.uitests.helpers.managecolumns;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions.Section;

/**
 * Shared Manage columns panel actions for all Fulfillment Console automation suites
 * (left-filter per-filter tests, table refresh, DSID, PTS packaging, feature flags).
 */
public class ManageColumnsUtil extends BaseTest {

    public static final int DEFAULT_PANEL_WAIT_S = 10;
    public static final int DEFAULT_PANEL_SETTLE_MS = 800;
    public static final int DEFAULT_CHECKBOX_WAIT_S = 30;
    public static final int DEFAULT_POLL_MS = 400;

    private final TableView tableView = new TableView();
    private final PtsPackagingIdPage ptsPage = new PtsPackagingIdPage();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    /**
     * Ensures {@code columnLabel} is enabled on the current table view.
     * Skips Manage columns entirely when the column is already visible on the grid (order columns)
     * or already checked in the panel (line-item columns). When enabling, clicks Save only if available,
     * then confirms Added Columns in the save-changes modal when a column was added.
     */
    public void ensureColumnEnabledForView(SoftAssert softAssert, String columnLabel) throws InterruptedException {
        Section section = ManageColumnOptions.defaultSectionForColumn(columnLabel);
        if (isColumnAlreadyEnabledOnView(columnLabel, section)) {
            Logger.logMessage(columnLabel + " already enabled on current view — skip Manage columns entirely");
            return;
        }
        if (section == Section.ORDER) {
            enableOrderLevelColumn(softAssert, columnLabel);
        } else if (ManageColumnOptions.ACTIVITY_TYPE.equals(columnLabel)) {
            enableActivityTypeColumn(softAssert);
        } else {
            enableColumnInManagePanel(softAssert, section, columnLabel);
        }
    }

    private boolean isColumnAlreadyEnabledOnView(String columnLabel, Section section) {
        if (section == Section.ORDER) {
            return isOrderColumnVisibleOnGrid(columnLabel);
        }
        return false;
    }

    /** True when {@code columnLabel} is already on the Orders grid (header or body cells in DOM). */
    public boolean isOrderColumnVisibleOnGrid(String columnLabel) {
        try {
            if (isOrderColumnPresentOnGridViaJs(columnLabel)) {
                return true;
            }
            By header = leftFilterPanel.tableColumnHeader(columnLabel);
            By cells = leftFilterPanel.tableColumnCells(columnLabel);
            if (WaitUtil.isDisplayFast(header, 1) || WaitUtil.isDisplayFast(cells, 1)) {
                return true;
            }
            scrollOrdersGridForColumn(columnLabel);
            return isOrderColumnPresentOnGridViaJs(columnLabel)
                    || WaitUtil.isDisplayFast(header, 2)
                    || WaitUtil.isDisplayFast(cells, 2);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not detect " + columnLabel + " on Orders grid: " + e.getMessage());
            return false;
        }
    }

    /** Detects order column in table DOM even when horizontally off-screen (avoids long scroll loops). */
    private boolean isOrderColumnPresentOnGridViaJs(String columnLabel) {
        try {
            String cssClass = toOrdersGridColumnCssClass(columnLabel);
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    "var cls='" + cssClass + "';"
                            + "var name='" + escaped + "';"
                            + "var sel='app-fulfillment-main-table-container td.'+cls"
                            + "+',app-fulfillment-orders-main-table td.'+cls"
                            + "+',app-fulfillment-main-table-container td[class*=\"'+cls+'\"]'"
                            + "+',app-fulfillment-orders-main-table td[class*=\"'+cls+'\"]'"
                            + "+',app-fulfillment-main-table-container th'"
                            + "+',app-fulfillment-orders-main-table th';"
                            + "var nodes=document.querySelectorAll(sel);"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(nodes[i].tagName==='TD'||t===name||t.indexOf(name)>=0){return true;}"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private static String toOrdersGridColumnCssClass(String columnLabel) {
        if (columnLabel == null || columnLabel.trim().isEmpty()) {
            return "";
        }
        switch (columnLabel.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "status":
                return "revised-status-col";
            case "brand":
                return "brand-col";
            case "assigned to":
                return "assigned-to-col";
            default:
                return columnLabel.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + "-col";
        }
    }

    private void scrollOrdersGridForColumn(String columnLabel) {
        try {
            By header = leftFilterPanel.tableColumnHeader(columnLabel);
            By cells = leftFilterPanel.tableColumnCells(columnLabel);
            String[] scrollSelectors = {
                    "app-fulfillment-main-table-container .custom-table-wrapper",
                    "app-fulfillment-main-table-container .table-container",
                    "msc-custom-table .custom-table-wrapper"
            };
            for (String selector : scrollSelectors) {
                for (int step = 0; step <= 5; step++) {
                    driver.get().browser().executeScript(
                            "var el=document.querySelector('" + selector + "');"
                                    + "if(!el){return;}"
                                    + "var max=Math.max(el.scrollWidth-el.clientWidth,0);"
                                    + "el.scrollLeft=Math.round(max*" + step + "/5);");
                    Thread.sleep(80);
                    if (WaitUtil.isDisplayFast(header, 1) || WaitUtil.isDisplayFast(cells, 1)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Orders grid for " + columnLabel + ": " + e.getMessage());
        }
    }

    /**
     * Resolves the table Manage columns panel opened by {@code #tableViewButton}.
     * Prefers visible {@code div.cdk-overlay-pane msc-custom-wrapper-dropdown} (gear dropdown).
     * Rejects the left-filter {@code multi-table-column-container} whose header is "Filters".
     * Scriptless TC041 left-filter steps ({@code #accordionButton}, generic scroll) are intentionally not used.
     */
    private static final String FIND_MANAGE_COLUMNS_ROOT_JS =
            "function findManageColumnsRoot(){"
                    + "function vis(el){if(!el)return false;var r=el.getBoundingClientRect();"
                    + "return r.width>0&&r.height>0;}"
                    + "function isFiltersPanel(t){"
                    + "  return t.indexOf('filters')>=0&&t.indexOf('order columns')<0"
                    + "    &&t.indexOf('line item columns')<0;"
                    + "}"
                    + "function isColPanel(root){"
                    + "  if(!vis(root))return false;"
                    + "  var t=(root.textContent||'').toLowerCase();"
                    + "  if(isFiltersPanel(t))return false;"
                    + "  if(t.indexOf('order columns')>=0||t.indexOf('line item columns')>=0)return true;"
                    + "  if(root.querySelector('.dropdown-reset-container'))return true;"
                    + "  if(root.querySelector('div.draggable-item label.option-label'))return true;"
                    + "  return false;"
                    + "}"
                    + "var overlays=[].slice.call(document.querySelectorAll('div.cdk-overlay-pane'));"
                    + "for(var o=0;o<overlays.length;o++){"
                    + "  if(!vis(overlays[o]))continue;"
                    + "  var drop=overlays[o].querySelector('msc-custom-wrapper-dropdown');"
                    + "  if(drop&&isColPanel(drop))return drop;"
                    + "  var mc=overlays[o].querySelector('div.multi-table-column-container');"
                    + "  if(mc&&isColPanel(mc))return mc;"
                    + "}"
                    + "var drops=[].slice.call(document.querySelectorAll('msc-custom-wrapper-dropdown'));"
                    + "for(var d=0;d<drops.length;d++){if(isColPanel(drops[d]))return drops[d];}"
                    + "var roots=[].slice.call(document.querySelectorAll('div.multi-table-column-container'));"
                    + "for(var i=0;i<roots.length;i++){if(isColPanel(roots[i]))return roots[i];}"
                    + "var gear=document.querySelector('#tableViewButton');"
                    + "if(gear){"
                    + "  var host=gear.closest('app-fulfillment-main-table-container');"
                    + "  if(!host){host=gear.parentElement;}"
                    + "  for(var depth=0;depth<12&&host;depth++){"
                    + "    var panels=host.querySelectorAll("
                    + "'msc-custom-wrapper-dropdown,div.multi-table-column-container');"
                    + "    for(var j=0;j<panels.length;j++){if(isColPanel(panels[j]))return panels[j];}"
                    + "    host=host.parentElement;"
                    + "  }"
                    + "}"
                    + "return null;"
                    + "}";

    public boolean isPanelOpen() {
        return isPanelOpenInDom();
    }

    public boolean isPanelOpen(int waitSec) throws InterruptedException {
        long endTime = System.currentTimeMillis() + (waitSec * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (isPanelOpenInDom()) {
                return true;
            }
            Thread.sleep(200);
        }
        return isPanelOpenInDom();
    }

    public boolean isPanelOpenInDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS + "return findManageColumnsRoot()!=null;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return WaitUtil.isDisplayFast(tableView.manageColumnsPanelRoot(), 1);
        }
    }

    /** Waits until the table-view Manage columns panel (Order/Line item columns) is open — not left Filters. */
    private boolean waitForTableManageColumnsPanel(int waitSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (waitSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isPanelOpenInDom()) {
                return true;
            }
            Thread.sleep(200);
        }
        return isPanelOpenInDom();
    }

    public void openPanel(SoftAssert softAssert) throws InterruptedException {
        openPanel(softAssert, 2, null);
    }

    public void openPanel(SoftAssert softAssert, int maxAttempts) throws InterruptedException {
        openPanel(softAssert, maxAttempts, null);
    }

    public void openPanel(SoftAssert softAssert, int maxAttempts, String preferredOrderColumnLabel)
            throws InterruptedException {
        if (isPanelOpen(2)) {
            Logger.logReportMessage("Manage columns panel already open");
            return;
        }
        DriverUtil.scrollToElement(tableView.getTableViewButton());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Verify.softAssert1(DriverUtil.clickOnElement(tableView.getTableViewButton(), 5)
                            || DriverUtil.clickOnElementJs(tableView.getTableViewButton(), 3),
                    "Clicked Manage columns gear (#tableViewButton) attempt " + attempt, softAssert);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
            if (isPanelOpenInDom()) {
                engageManageColumnsPanelKeepOpen(preferredOrderColumnLabel);
                Verify.softAssert1(true, "Manage columns panel is open", softAssert);
                return;
            }
            if (isPanelOpen(DEFAULT_PANEL_WAIT_S)) {
                engageManageColumnsPanelKeepOpen(preferredOrderColumnLabel);
                Verify.softAssert1(true, "Manage columns panel is open", softAssert);
                return;
            }
            Logger.logReportMessage("Manage columns panel not open after attempt " + attempt);
        }
        Verify.softAssert1(isPanelOpen(5), "Manage columns panel is open", softAssert);
    }

    /**
     * PROD may auto-close an idle Manage columns panel — click a visible Order-columns label immediately
     * so the panel stays open before scrolling to Activity Type. Prefers an unchecked order column.
     */
    public boolean engageManageColumnsPanelKeepOpen() throws InterruptedException {
        return engageManageColumnsPanelKeepOpen(null);
    }

    public boolean engageManageColumnsPanelKeepOpen(String preferredOrderColumnLabel) throws InterruptedException {
        if (!isPanelOpenInDom()) {
            Logger.logConsoleMessage("Cannot engage Manage columns — panel not open");
            return false;
        }
        expandAllManageColumnAccordions();
        Thread.sleep(400);
        if (preferredOrderColumnLabel != null && !preferredOrderColumnLabel.isBlank()) {
            scrollToColumnInManagePanelLikePts(Section.ORDER, preferredOrderColumnLabel);
            Thread.sleep(300);
            if (DriverUtil.clickOnElement(tableView.orderColumnLabel(preferredOrderColumnLabel), 3)) {
                Logger.logMessage("Engaged Manage columns — clicked order column label ("
                        + preferredOrderColumnLabel + ")");
                Thread.sleep(500);
                return isPanelOpenInDom();
            }
        }
        try {
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return 'missing';}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function rowChecked(row){"
                            + "  if(!row){return false;}"
                            + "  var cb=row.querySelector('input[type=checkbox]');"
                            + "  return cb&&(cb.checked||cb.getAttribute('aria-checked')==='true');"
                            + "}"
                            + "var labels=root.querySelectorAll("
                            + "'div.draggable-item label.option-label,div.draggable-item label.form-check-label');"
                            + "var unchecked=null,any=null;"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if(!inOrderSection(labels[i])){continue;}"
                            + "  var r=labels[i].getBoundingClientRect();"
                            + "  if(r.width<=0||r.height<=0){continue;}"
                            + "  var row=labels[i].closest('div.draggable-item,a.cdk-drag.option-item');"
                            + "  any=labels[i];"
                            + "  if(!rowChecked(row)){unchecked=labels[i];break;}"
                            + "}"
                            + "var target=unchecked||any;"
                            + "if(!target){return 'none';}"
                            + "target.scrollIntoView({block:'center',inline:'nearest'});"
                            + "target.click();"
                            + "return trim(target.textContent);");
            String status = String.valueOf(result);
            if ("missing".equals(status) || "none".equals(status)) {
                Logger.logConsoleMessage("Manage columns engage — no order column label to click");
                return false;
            }
            Logger.logMessage("Engaged Manage columns — clicked order column label (" + status
                    + ") to keep panel open");
            Thread.sleep(500);
            return isPanelOpenInDom();
        } catch (Exception e) {
            Logger.logConsoleMessage("Manage columns engage failed: " + e.getMessage());
            return false;
        }
    }

    public void enableActivityTypeColumn(SoftAssert softAssert) throws InterruptedException {
        String columnLabel = ManageColumnOptions.ACTIVITY_TYPE;
        Logger.logMessage("Manage columns — open panel, enable Activity Type checkbox if needed");
        openPanel(softAssert, 2);
        if (!waitForTableManageColumnsPanel(DEFAULT_PANEL_WAIT_S)) {
            Verify.softAssert1(false, "Manage columns panel open for Activity Type setup", softAssert);
            return;
        }
        expandAllManageColumnAccordions();
        Thread.sleep(400);
        scrollManageColumnsPanelToTop();

        boolean activityTypeVisible = scrollActivityTypeForTc620(columnLabel);
        Verify.softAssert1(activityTypeVisible,
                "Activity Type visible in Manage columns after scroll", softAssert);
        if (!activityTypeVisible) {
            closePanelSafely(softAssert);
            return;
        }
        if (isActivityTypeCheckedInOpenManagePanel()) {
            Logger.logMessage(columnLabel + " already enabled in Manage columns — skip Save, close panel");
            closePanelSafely(softAssert);
            return;
        }

        clickActivityTypeInManagePanelLikePts(softAssert);
        saveChangesWhenNeeded(softAssert, columnLabel);
    }

    /** @deprecated use {@link #enableActivityTypeColumn}. */
    public void enableActivityTypeForTc620(SoftAssert softAssert) throws InterruptedException {
        enableActivityTypeColumn(softAssert);
    }

    /**
     * Open Manage columns on Orders, enable an order-level column (e.g. Brand, PTS Packaging ID)
     * under the Order columns accordion — Save when enabled, otherwise close.
     */
    public void enableOrderLevelColumn(SoftAssert softAssert, String columnLabel) throws InterruptedException {
        if (isOrderColumnVisibleOnGrid(columnLabel)) {
            Logger.logMessage(columnLabel + " already visible on Orders grid — skip Manage columns entirely");
            return;
        }
        Logger.logMessage("Manage columns — open panel, enable order-level column: " + columnLabel);
        openPanel(softAssert, 2, columnLabel);
        if (!waitForTableManageColumnsPanel(DEFAULT_PANEL_WAIT_S)) {
            Verify.softAssert1(false, "Manage columns panel open for " + columnLabel + " setup", softAssert);
            return;
        }
        expandAllManageColumnAccordions();
        Thread.sleep(400);
        scrollManageColumnsPanelToTop();
        Section section = Section.ORDER;
        scrollToColumnInManagePanelLikePts(section, columnLabel);
        Verify.softAssert1(WaitUtil.isDisplayFast(columnLabel(section, columnLabel), 5),
                columnLabel + " visible in Order columns after scroll", softAssert);

        if (isOrderLevelCheckboxSelected(columnLabel)) {
            Logger.logMessage(columnLabel + " already checked in Manage columns — skip Save, close panel");
            closePanelSafely(softAssert);
            return;
        }

        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        boolean toggled = clickColumnCheckboxIfUnchecked(section, checkbox, label, columnLabel);
        Verify.softAssert1(toggled || isOrderLevelCheckboxSelected(columnLabel),
                "Enabled " + columnLabel + " in Manage columns", softAssert);

        saveChangesWhenNeeded(softAssert, columnLabel);
    }

    private void enableColumnInManagePanel(SoftAssert softAssert, Section section, String columnLabel)
            throws InterruptedException {
        Logger.logMessage("Manage columns — open panel, enable column: " + columnLabel);
        openPanel(softAssert, 2);
        if (!waitForTableManageColumnsPanel(DEFAULT_PANEL_WAIT_S)) {
            Verify.softAssert1(false, "Manage columns panel open for " + columnLabel + " setup", softAssert);
            return;
        }
        expandAllManageColumnAccordions();
        Thread.sleep(400);
        scrollLabelIntoView(section, columnLabel);
        if (isColumnChecked(section, columnLabel)) {
            Logger.logMessage(columnLabel + " already checked in Manage columns — skip Save, close panel");
            closePanelSafely(softAssert);
            return;
        }
        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        boolean toggled = clickColumnCheckboxIfUnchecked(section, checkbox, label, columnLabel);
        Verify.softAssert1(toggled || isColumnChecked(section, columnLabel),
                "Enabled " + columnLabel + " in Manage columns", softAssert);
        saveChangesWhenNeeded(softAssert, columnLabel);
    }

    /** Clicks Save when enabled; ignores when not. Confirms Added Columns modal when a column was added. */
    private void saveChangesWhenNeeded(SoftAssert softAssert, String addedColumnLabel) throws InterruptedException {
        if (WaitUtil.isDisplayFast(ptsPage.saveChangesButtonInPanel(), 2) || clickSaveChangesButtonViaJs()) {
            Logger.logMessage("Save changes enabled — clicking Save for " + addedColumnLabel);
            saveChangesIfPresent(softAssert, addedColumnLabel);
        } else {
            Logger.logMessage("Save changes not available — closing Manage columns and continuing");
            closePanelSafely(softAssert);
        }
    }

    /**
     * Scroll Activity Type into view — flat line-item list (above LINE ITEM COLUMNS) and accordion list.
     * Mirrors {@link com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil} scroll/click.
     */
    private boolean scrollActivityTypeForTc620(String columnLabel) throws InterruptedException {
        if (scrollActivityTypeIntoViewViaJs(columnLabel)) {
            return true;
        }
        Section section = ManageColumnOptions.defaultSectionForColumn(columnLabel);
        scrollToColumnInManagePanelLikePts(section, columnLabel);
        if (WaitUtil.isDisplayFast(columnLabel(section, columnLabel), 3)) {
            return true;
        }
        prepareLineItemColumnsSectionInManagePanel();
        scrollToColumnInManagePanelLikePts(Section.LINE_ITEM, columnLabel);
        if (WaitUtil.isDisplayFast(columnLabel(Section.LINE_ITEM, columnLabel), 3)) {
            return true;
        }
        if (scrollUntilLineItemColumnVisible(columnLabel)) {
            return true;
        }
        return scrollActivityTypeIntoViewViaJs(columnLabel);
    }

    /** PTS Packaging {@code scrollToColumnInManagePanel} — scroll list container then label into view. */
    private void scrollToColumnInManagePanelLikePts(Section section, String columnLabel) {
        By label = columnLabel(section, columnLabel);
        try {
            if (section == Section.ORDER) {
                if (WaitUtil.isDisplayFast(ptsPage.orderColumnsListContainer(), 2)) {
                    DesktopBrowserElement container = driver.get().finder().findElement(
                            ptsPage.orderColumnsListContainer());
                    container.executeScript("this.scrollTop = this.scrollHeight;");
                }
            } else {
                By list = tableView.lineItemColumnsScrollList();
                if (WaitUtil.isDisplayFast(list, 2)) {
                    DesktopBrowserElement container = driver.get().finder().findElement(list);
                    scrollListToRevealLabel(container, label);
                } else if (WaitUtil.isDisplayFast(ptsPage.lineItemColumnsDropList(), 2)) {
                    DesktopBrowserElement container = driver.get().finder().findElement(
                            ptsPage.lineItemColumnsDropList());
                    scrollListToRevealLabel(container, label);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns list for " + columnLabel + ": "
                    + e.getMessage());
        }
        DriverUtil.scrollToElement(label);
        try {
            driver.get().finder().findElement(label).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});");
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scrollIntoView " + columnLabel + ": " + e.getMessage());
        }
    }

    private void scrollListToRevealLabel(DesktopBrowserElement container, By label) {
        try {
            if (WaitUtil.isDisplayFast(label, 1)) {
                return;
            }
            container.executeScript("this.scrollTop = 0;");
            if (WaitUtil.isDisplayFast(label, 1)) {
                return;
            }
            Object maxObj = container.executeScript(
                    "return Math.max(this.scrollHeight - this.clientHeight, 0);");
            int max = maxObj instanceof Number ? ((Number) maxObj).intValue() : 0;
            int step = Math.max(60, max / 12);
            for (int pos = step; pos <= max; pos += step) {
                container.executeScript("this.scrollTop = " + pos + ";");
                if (WaitUtil.isDisplayFast(label, 1)) {
                    return;
                }
            }
            container.executeScript("this.scrollTop = this.scrollHeight;");
        } catch (Exception ignored) {
            // best effort
        }
    }

    private boolean scrollActivityTypeIntoViewViaJs(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function inPackageSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^package columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function findLabel(){"
                            + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                            + "  for(var i=0;i<labels.length;i++){"
                            + "    if(trim(labels[i].textContent)!==name){continue;}"
                            + "    if(inOrderSection(labels[i])||inPackageSection(labels[i])){continue;}"
                            + "    return labels[i];"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "function labelVisible(label){"
                            + "  if(!label){return false;}"
                            + "  var r=label.getBoundingClientRect();"
                            + "  return r.width>0&&r.height>0;"
                            + "}"
                            + "function scrollToLabel(){"
                            + "  var label=findLabel();"
                            + "  if(!label){return false;}"
                            + "  label.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  return labelVisible(label);"
                            + "}"
                            + "function scrollables(){"
                            + "  var list=[];"
                            + "  var seen=new Set();"
                            + "  function add(el){if(!el||seen.has(el))return;seen.add(el);list.push(el);}"
                            + "  add(root);"
                            + "  root.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                            + ").forEach(add);"
                            + "  if(root.tagName&&root.tagName.toLowerCase()==='msc-custom-wrapper-dropdown'){"
                            + "    root.querySelectorAll('div').forEach(function(div){"
                            + "      if(div.scrollHeight>div.clientHeight+20){add(div);}"
                            + "    });"
                            + "  }"
                            + "  return list;"
                            + "}"
                            + "if(scrollToLabel()){return true;}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  el.scrollTop=0;"
                            + "  if(scrollToLabel()){return true;}"
                            + "  var step=Math.max(80,Math.floor((el.clientHeight||300)*0.18));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=step;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    if(scrollToLabel()){return true;}"
                            + "  }"
                            + "  el.scrollTop=max;"
                            + "  if(scrollToLabel()){return true;}"
                            + "}"
                            + "return scrollToLabel();");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS scroll to Activity Type failed: " + e.getMessage());
            return false;
        }
    }

    /** PTS {@code clickColumnInManagePanel} — label click, then checkbox/label JS fallback. */
    private void clickActivityTypeInManagePanelLikePts(SoftAssert softAssert) throws InterruptedException {
        String columnLabel = ManageColumnOptions.ACTIVITY_TYPE;
        Section section = ManageColumnOptions.defaultSectionForColumn(columnLabel);
        By label = columnLabel(section, columnLabel);
        By checkbox = columnCheckbox(section, columnLabel);
        scrollActivityTypeForTc620(columnLabel);
        Thread.sleep(300);
        if (DriverUtil.clickOnElement(label, 5)) {
            Verify.softAssert1(true,
                    "Clicked " + columnLabel + " label in Manage columns (PTS pattern)", softAssert);
            Thread.sleep(400);
            return;
        }
        try {
            driver.get().finder().findElement(checkbox).executeScript("arguments[0].click();");
            Verify.softAssert1(true,
                    "JS-clicked " + columnLabel + " checkbox in Manage columns (PTS pattern)", softAssert);
        } catch (Exception e) {
            try {
                driver.get().finder().findElement(label).executeScript("arguments[0].click();");
                Verify.softAssert1(true,
                        "JS-clicked " + columnLabel + " label in Manage columns (PTS pattern)", softAssert);
            } catch (Exception ex) {
                if (clickActivityTypeLabelJsInManagePanel(columnLabel)) {
                    Verify.softAssert1(true,
                            "JS-clicked " + columnLabel + " via Manage columns root search", softAssert);
                } else {
                    Verify.softAssert1(false,
                            "Failed to toggle " + columnLabel + " in Manage columns: " + ex.getMessage(),
                            softAssert);
                }
            }
        }
        Thread.sleep(400);
    }

    private boolean clickActivityTypeLabelJsInManagePanel(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function inPackageSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^package columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if(trim(labels[i].textContent)!==name){continue;}"
                            + "  if(inOrderSection(labels[i])||inPackageSection(labels[i])){continue;}"
                            + "  labels[i].scrollIntoView({block:'center',inline:'nearest'});"
                            + "  labels[i].click();"
                            + "  return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    /** @deprecated TC620 uses {@link #openPanel} + PTS scroll/click; kept for callers that need engage logging. */
    public String engageTc620ManageColumnsPanel() throws InterruptedException {
        if (engageManageColumnsPanelKeepOpen()) {
            return "engaged";
        }
        return "none";
    }

    /** @deprecated TC620 uses {@link #openPanel}; kept for compatibility. */
    private boolean openAndEngageManageColumnsForTc620(SoftAssert softAssert) throws InterruptedException {
        openPanel(softAssert, 2);
        return waitForTableManageColumnsPanel(DEFAULT_PANEL_WAIT_S);
    }

    private void openPanelForTc620(SoftAssert softAssert) throws InterruptedException {
        openAndEngageManageColumnsForTc620(softAssert);
    }

    public void closePanel() throws InterruptedException {
        closePanelSafely(null);
    }

    public void closePanel(SoftAssert softAssert) throws InterruptedException {
        closePanelSafely(softAssert);
    }

    /** Closes Manage columns only when the dropdown panel is open — never toggles gear on a closed panel. */
    public void closePanelSafely(SoftAssert softAssert) throws InterruptedException {
        if (!isPanelOpenInDom()) {
            Logger.logMessage("Manage columns panel already closed — skip gear toggle");
            return;
        }
        try {
            driver.get().browser().executeScript(
                    "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', keyCode:27, bubbles:true}));");
            Thread.sleep(400);
        } catch (Exception ignored) {
            // best effort
        }
        if (!isPanelOpenInDom()) {
            Logger.logMessage("Manage columns panel closed via Escape");
            return;
        }
        DriverUtil.clickOnElement(tableView.getTableViewButton(), 5);
        Thread.sleep(500);
        if (isPanelOpenInDom()) {
            Logger.logConsoleMessage("Manage columns panel still open after gear toggle — retry Escape");
            try {
                driver.get().browser().executeScript(
                        "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', keyCode:27, bubbles:true}));");
                Thread.sleep(300);
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    public void expandLineItemColumnsAccordionIfNeeded() {
        expandAllManageColumnAccordions();
    }

    /** Expands Order columns, Package columns, and Line item columns accordions when collapsed. */
    public void expandAllManageColumnAccordions() {
        try {
            driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "var toggles=root.querySelectorAll("
                            + "'.accordion-button.collapsed,.accordion-header .accordion-button[aria-expanded=\"false\"],"
                            + "msc-multi-select-accordion .accordion-button.collapsed');"
                            + "toggles.forEach(function(btn){try{btn.click();}catch(e){}});"
                            + "return toggles.length;");
            Thread.sleep(500);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not expand Manage columns accordions: " + e.getMessage());
        }
    }

    public void scrollAllLists() {
        try {
            driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "function scrollEl(el){if(!el)return;el.scrollTop=el.scrollHeight;}"
                            + "var panel=findManageColumnsRoot();"
                            + "if(!panel){return;}"
                            + "scrollEl(panel);"
                            + "panel.querySelectorAll('.options-list-scrollbar,.cdk-drop-list,.cdk-virtual-scroll-viewport')"
                            + ".forEach(scrollEl);");
            Thread.sleep(500);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns lists: " + e.getMessage());
        }
    }

    public void scrollLineItemList() {
        scrollAllLists();
    }

    public void scrollLabelIntoView(String columnLabel) {
        scrollLabelIntoView(Section.ORDER_LINE_ITEM, columnLabel);
    }

    /** Expands accordions and scrolls Manage columns panel to the line-item section (Activity Type lives here). */
    public void prepareLineItemColumnsSectionInManagePanel() throws InterruptedException {
        expandAllManageColumnAccordions();
        Thread.sleep(400);
        try {
            driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "root.querySelectorAll("
                            + "'.accordion-button.collapsed,.accordion-header .accordion-button[aria-expanded=\"false\"]'"
                            + ").forEach(function(btn){try{btn.click();}catch(e){}});"
                            + "var headers=root.querySelectorAll('.table-column-name');"
                            + "for(var i=0;i<headers.length;i++){"
                            + "  var t=(headers[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(!/line item columns/i.test(t)){continue;}"
                            + "  headers[i].scrollIntoView({block:'start',inline:'nearest'});"
                            + "  root.scrollTop=Math.max(0,headers[i].offsetTop-24);"
                            + "  return true;"
                            + "}"
                            + "root.scrollTop=Math.floor(root.scrollHeight*0.55);"
                            + "return true;");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns to line-item section: " + e.getMessage());
        }
    }

    public void scrollLabelIntoView(Section section, String columnLabel) {
        try {
            if (section == Section.ORDER) {
                scrollToColumnInManagePanelLikePts(section, columnLabel);
                return;
            }
            if (section == Section.ORDER_LINE_ITEM) {
                try {
                    prepareLineItemColumnsSectionInManagePanel();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return;}"
                            + "function trim(el){return (el.textContent||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header));"
                            + "}"
                            + "function scrollables(){"
                            + "  var list=[root];"
                            + "  root.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                            + ").forEach(function(el){list.push(el);});"
                            + "  return list;"
                            + "}"
                            + "function scrollToLabel(){"
                            + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                            + "  for(var i=0;i<labels.length;i++){"
                            + "    if(trim(labels[i])!==name||inOrderSection(labels[i])){continue;}"
                            + "    labels[i].scrollIntoView({block:'center',inline:'nearest'});"
                            + "    return true;"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "if(scrollToLabel()){return;}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  var step=Math.max(60,Math.floor((el.clientHeight||280)*0.4));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    if(scrollToLabel()){return;}"
                            + "  }"
                            + "}");
            Thread.sleep(300);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns label " + columnLabel + ": " + e.getMessage());
        }
    }

    /** Enables every unchecked checkbox in the open Manage columns panel. Returns count toggled. */
    public int selectAllUncheckedCheckboxes() {
        scrollManageColumnsPanelToTop();
        expandAllManageColumnAccordions();
        int totalToggled = 0;
        for (int pass = 0; pass < 3; pass++) {
            int toggled = selectAllUncheckedCheckboxesInOpenPanel();
            totalToggled += toggled;
            if (toggled == 0) {
                break;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return totalToggled;
    }

    public boolean isColumnChecked(String columnLabel) {
        return isColumnChecked(ManageColumnOptions.defaultSectionForColumn(columnLabel), columnLabel);
    }

    public boolean isColumnChecked(Section section, String columnLabel) {
        scrollLabelIntoView(section, columnLabel);
        if (section == Section.ORDER_LINE_ITEM) {
            return isOrderLineItemFlatCheckboxSelected(columnLabel);
        }
        if (section == Section.ORDER) {
            return isOrderLevelCheckboxSelected(columnLabel);
        }
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "var labels=[].slice.call(root.querySelectorAll("
                            + "'label.option-label,label.form-check-label,label'));"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if((labels[i].textContent||'').replace(/\\s+/g,' ').trim()!==name){continue;}"
                            + "  var container=labels[i].closest('.option-container,.draggable-item,a.option-item,.option');"
                            + "  var cb=container?container.querySelector('input[type=checkbox]'):null;"
                            + "  if(!cb){"
                            + "    cb=labels[i].previousElementSibling;"
                            + "    if(!cb||cb.type!=='checkbox'){return false;}"
                            + "  }"
                            + "  return !!cb.checked;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read Manage columns checkbox for " + columnLabel + ": "
                    + e.getMessage());
            return false;
        }
    }

    /**
     * True when Activity Type is checked at line-item level on Orders Manage columns
     * (LINE ITEM COLUMNS section). Requires panel open.
     */
    public boolean isActivityTypeCheckedOnOrdersView() {
        if (!isPanelOpenInDom()) {
            return false;
        }
        return isActivityTypeCheckedInOpenManagePanel();
    }

    /** Reads Activity Type checked state while Manage columns panel is already open (no gear toggle). */
    public boolean isActivityTypeCheckedInOpenManagePanel() {
        if (!isPanelOpenInDom()) {
            return false;
        }
        try {
            if (isVisibleLineItemColumnChecked(ManageColumnOptions.ACTIVITY_TYPE)) {
                return true;
            }
            scrollUntilLineItemColumnVisible(ManageColumnOptions.ACTIVITY_TYPE);
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (isVisibleLineItemColumnChecked(ManageColumnOptions.ACTIVITY_TYPE)) {
            return true;
        }
        return isLineItemColumnCheckedAfterScroll(ManageColumnOptions.ACTIVITY_TYPE);
    }

    /**
     * Scrolls Manage columns (panel + line-item virtual list) until {@code columnLabel} is visible.
     */
    public boolean scrollUntilLineItemColumnVisible(String columnLabel) throws InterruptedException {
        prepareLineItemColumnsSectionInManagePanel();
        Thread.sleep(300);
        By label = tableView.lineItemLevelColumnLabelInScrollList(columnLabel);
        if (WaitUtil.isDisplayFast(label, 2)) {
            DriverUtil.scrollToElement(label);
            Logger.logMessage(columnLabel + " already visible in Manage columns line-item list");
            return true;
        }
        if (scrollLineItemColumnIntoViewViaJs(columnLabel)) {
            Thread.sleep(300);
        }
        for (int pass = 0; pass < 3 && !WaitUtil.isDisplayFast(label, 2); pass++) {
            scrollLineItemColumnIntoViewViaJs(columnLabel);
            Thread.sleep(300);
        }
        if (WaitUtil.isDisplayFast(label, DEFAULT_CHECKBOX_WAIT_S)) {
            DriverUtil.scrollToElement(label);
            Logger.logMessage("Scrolled Manage columns until " + columnLabel + " is visible");
            return true;
        }
        Logger.logConsoleMessage("Could not scroll Manage columns until " + columnLabel + " is visible");
        return false;
    }

    /** Reads checkbox after scroll — JS visible-row read first (Bootstrap hides native input). */
    public boolean isLineItemColumnCheckedAfterScroll(String columnLabel) {
        if (isVisibleLineItemColumnChecked(columnLabel)) {
            return true;
        }
        By checkbox = tableView.lineItemLevelColumnCheckbox(columnLabel);
        By label = tableView.lineItemLevelColumnLabelInScrollList(columnLabel);
        try {
            if (WaitUtil.isDisplayFast(checkbox, 2)) {
                return DriverUtil.isSelectedCheckbox(checkbox);
            }
            if (WaitUtil.isDisplayFast(label, 1)) {
                scrollLineItemColumnIntoViewViaJs(columnLabel);
                if (WaitUtil.isDisplayFast(checkbox, 2)) {
                    return DriverUtil.isSelectedCheckbox(checkbox);
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return isVisibleLineItemColumnChecked(columnLabel);
    }

    /**
     * Native label click on the LINE ITEM COLUMNS {@code draggable-item} row — never Order columns.
     */
    public boolean clickLineItemColumnNativeAfterScroll(String columnLabel) throws InterruptedException {
        By label = tableView.lineItemLevelColumnLabelInScrollList(columnLabel);
        if (isLineItemColumnCheckedAfterScroll(columnLabel)) {
            Logger.logMessage(columnLabel + " already checked in LINE ITEM COLUMNS — skip native click");
            return false;
        }
        if (!WaitUtil.isDisplayFast(label, 3)) {
            Logger.logConsoleMessage("Native click skipped — " + columnLabel
                    + " not visible in LINE ITEM COLUMNS after scroll");
            return false;
        }
        if (!isLabelAtLineItemLevel(columnLabel)) {
            Logger.logConsoleMessage("Native click blocked — " + columnLabel
                    + " label is not at line-item level in Manage columns");
            return false;
        }
        DriverUtil.scrollToElement(label);
        Thread.sleep(300);
        if (DriverUtil.clickOnElement(label, DEFAULT_CHECKBOX_WAIT_S)) {
            Logger.logMessage("Native-clicked " + columnLabel
                    + " on LINE ITEM COLUMNS draggable-item row (not Order columns level)");
            Thread.sleep(400);
            return true;
        }
        Logger.logConsoleMessage("Native click failed for LINE ITEM COLUMNS row " + columnLabel + " at " + label);
        return false;
    }

    /** True when {@code columnLabel} is line-item level (flat list or LINE ITEM COLUMNS), not Order/Package. */
    private boolean isLabelAtLineItemLevel(String columnLabel) {
        if (isLabelInLineItemColumnsSection(columnLabel)) {
            return true;
        }
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function inPackageSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^package columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "var labels=root.querySelectorAll('label.option-label,label.form-check-label,label');"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if(trim(labels[i].textContent)!==name){continue;}"
                            + "  if(inOrderSection(labels[i])||inPackageSection(labels[i])){continue;}"
                            + "  var r=labels[i].getBoundingClientRect();"
                            + "  return r.width>0&&r.height>0;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    /** True when {@code columnLabel} resolves to a row under LINE ITEM COLUMNS, not Order columns. */
    private boolean isLabelInLineItemColumnsSection(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function isLineItemSection(section){"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/line item columns/i.test(trim(header.textContent));"
                            + "}"
                            + "var labels=root.querySelectorAll('label.option-label,label.form-check-label,label');"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if(trim(labels[i].textContent)!==name){continue;}"
                            + "  var row=labels[i].closest('div.draggable-item,a.cdk-drag.option-item');"
                            + "  if(!row){continue;}"
                            + "  var r=row.getBoundingClientRect();"
                            + "  if(r.width<=0||r.height<=0){continue;}"
                            + "  var section=row.closest('.multi-options-list,.table-column');"
                            + "  return isLineItemSection(section);"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not verify LINE ITEM COLUMNS section for " + columnLabel
                    + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Native label click on visible line-item row — scroll first, skip if checked, no JS click fallback.
     */
    public boolean clickLineItemColumnNativeOnly(String columnLabel) throws InterruptedException {
        if (!scrollUntilLineItemColumnVisible(columnLabel)) {
            return false;
        }
        return clickLineItemColumnNativeAfterScroll(columnLabel);
    }

    /**
     * @deprecated Trial uses {@link #clickLineItemColumnNativeAfterScroll} only — no JS click.
     */
    public boolean clickLineItemColumnJsThenNative(String columnLabel) throws InterruptedException {
        return clickLineItemColumnNativeAfterScroll(columnLabel);
    }

    /** JS label click on already-scrolled line-item row (uses visible label in LINE ITEM COLUMNS). */
    public boolean clickLineItemColumnLabelJsAfterScroll(String columnLabel) throws InterruptedException {
        if (isLineItemColumnCheckedAfterScroll(columnLabel)) {
            Logger.logMessage(columnLabel + " already checked after scroll — skip JS label click");
            return false;
        }
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return 'missing';}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function findLabel(){"
                            + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                            + "  for(var i=0;i<labels.length;i++){"
                            + "    if(trim(labels[i].textContent)!==name){continue;}"
                            + "    if(inOrderSection(labels[i])){continue;}"
                            + "    var r=labels[i].getBoundingClientRect();"
                            + "    if(r.width>0&&r.height>0){return labels[i];}"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "function rowChecked(label){"
                            + "  var row=label.closest('div.draggable-item,a.cdk-drag.option-item,.option-container');"
                            + "  if(!row){return false;}"
                            + "  var cb=row.querySelector('input[type=checkbox]');"
                            + "  return cb&&(cb.checked||cb.getAttribute('aria-checked')==='true');"
                            + "}"
                            + "var label=findLabel();"
                            + "if(!label){return 'missing';}"
                            + "if(rowChecked(label)){return 'already';}"
                            + "label.scrollIntoView({block:'center',inline:'nearest'});"
                            + "label.click();"
                            + "return 'clicked';");
            String status = String.valueOf(result);
            if ("clicked".equals(status)) {
                Logger.logMessage("JS-clicked " + columnLabel + " label after scroll (line-item list)");
                Thread.sleep(400);
                return true;
            }
            if ("already".equals(status)) {
                Logger.logMessage(columnLabel + " already checked — skip JS label click");
                return false;
            }
            Logger.logConsoleMessage("Could not JS-click " + columnLabel + " label after scroll");
            return false;
        } catch (Exception e) {
            Logger.logConsoleMessage("JS label click after scroll failed for " + columnLabel + ": " + e.getMessage());
            return false;
        }
    }

    private boolean scrollLineItemColumnIntoViewViaJs(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                            + "function inOrderSection(node){"
                            + "  var section=node.closest('.multi-options-list,.table-column');"
                            + "  if(!section){return false;}"
                            + "  var header=section.querySelector('.table-column-name');"
                            + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                            + "}"
                            + "function findLabel(){"
                            + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                            + "  for(var i=0;i<labels.length;i++){"
                            + "    if(trim(labels[i].textContent)!==name){continue;}"
                            + "    if(inOrderSection(labels[i])){continue;}"
                            + "    return labels[i];"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "function labelVisible(label){"
                            + "  if(!label){return false;}"
                            + "  var r=label.getBoundingClientRect();"
                            + "  var vh=window.innerHeight||document.documentElement.clientHeight;"
                            + "  return r.width>0&&r.height>0&&r.top>=0&&r.bottom<=vh+2;"
                            + "}"
                            + "function scrollToLabel(){"
                            + "  var label=findLabel();"
                            + "  if(!label){return false;}"
                            + "  label.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  return labelVisible(label);"
                            + "}"
                            + "function scrollables(){"
                            + "  var list=[root];"
                            + "  root.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                            + ").forEach(function(el){list.push(el);});"
                            + "  return list;"
                            + "}"
                            + "if(scrollToLabel()){return true;}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  el.scrollTop=0;"
                            + "  if(scrollToLabel()){return true;}"
                            + "  var step=Math.max(40,Math.floor((el.clientHeight||300)*0.22));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=step;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    if(scrollToLabel()){return true;}"
                            + "  }"
                            + "  el.scrollTop=max;"
                            + "  if(scrollToLabel()){return true;}"
                            + "}"
                            + "return scrollToLabel();");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS scroll to " + columnLabel + " failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads checked state from the visible {@code div.draggable-item} row in LINE ITEM COLUMNS.
     * Bootstrap hides the native input (0-size rect) — row/label visibility is used instead.
     */
    public boolean isVisibleLineItemColumnChecked(String columnLabel) {
        Boolean jsChecked = readVisibleLineItemColumnCheckedViaJs(columnLabel);
        return Boolean.TRUE.equals(jsChecked);
    }

    private Boolean readVisibleLineItemColumnCheckedViaJs(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    lineItemColumnRowScriptPrefix(escaped)
                            + "var hit=findVisibleRow();"
                            + "if(hit){return rowChecked(hit);}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  el.scrollTop=0;"
                            + "  hit=findVisibleRow();"
                            + "  if(hit){return rowChecked(hit);}"
                            + "  var step=Math.max(50,Math.floor((el.clientHeight||280)*0.35));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    hit=findVisibleRow();"
                            + "    if(hit){return rowChecked(hit);}"
                            + "  }"
                            + "}"
                            + "hit=findAnyRow();"
                            + "return hit?rowChecked(hit):false;");
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            if ("true".equals(String.valueOf(result))) {
                return true;
            }
            if ("false".equals(String.valueOf(result))) {
                return false;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Shared JS helpers for draggable-item line-item column rows in Manage columns. */
    private static String lineItemColumnRowScriptPrefix(String escapedColumnName) {
        return "var name='" + escapedColumnName + "';"
                + FIND_MANAGE_COLUMNS_ROOT_JS
                + "var root=findManageColumnsRoot();"
                + "if(!root){return false;}"
                + "function trim(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                + "function rowVisible(row){"
                + "  if(!row){return false;}"
                + "  var r=row.getBoundingClientRect();"
                + "  return r.width>0&&r.height>0;"
                + "}"
                + "function inOrderSection(row){"
                + "  var section=row.closest('.multi-options-list,.table-column');"
                + "  if(!section){return false;}"
                + "  var header=section.querySelector('.table-column-name');"
                + "  return header&&/^order columns$/i.test(trim(header.textContent));"
                + "}"
                + "function rowLabel(row){"
                + "  return row.querySelector('label.option-label,label.form-check-label,label');"
                + "}"
                + "function rowMatches(row){"
                + "  if(!row||inOrderSection(row)){return false;}"
                + "  var label=rowLabel(row);"
                + "  return label&&trim(label.textContent)===name;"
                + "}"
                + "function rowChecked(row){"
                + "  if(!row){return false;}"
                + "  var cb=row.querySelector('input[type=checkbox]');"
                + "  if(!cb){return false;}"
                + "  if(cb.checked||cb.getAttribute('aria-checked')==='true'){return true;}"
                + "  try{if(cb.matches(':checked')){return true;}}catch(e){}"
                + "  if(row.querySelector('input[type=checkbox]:checked')){return true;}"
                + "  try{"
                + "    var st=window.getComputedStyle(cb);"
                + "    var bgImg=st.backgroundImage||'';"
                + "    if(bgImg&&bgImg!=='none'){return true;}"
                + "  }catch(e2){}"
                + "  return false;"
                + "}"
                + "function findAnyRow(){"
                + "  var hit=findRowInNodes(root.querySelectorAll('div.draggable-item,a.cdk-drag.option-item'));"
                + "  if(hit){return hit;}"
                + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                + "  for(var i=0;i<labels.length;i++){"
                + "    if(trim(labels[i].textContent)!==name){continue;}"
                + "    var row=labels[i].closest('div.draggable-item,a.cdk-drag.option-item');"
                + "    if(row&&!inOrderSection(row)){return row;}"
                + "  }"
                + "  return null;"
                + "}"
                + "function findRowInNodes(nodes){"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    if(rowMatches(nodes[i])){return nodes[i];}"
                + "  }"
                + "  return null;"
                + "}"
                + "function findVisibleRow(){"
                + "  var nodes=root.querySelectorAll('div.draggable-item,a.cdk-drag.option-item');"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    if(rowMatches(nodes[i])&&rowVisible(nodes[i])){return nodes[i];}"
                + "  }"
                + "  var labels=root.querySelectorAll('label.option-label,label.form-check-label');"
                + "  for(var j=0;j<labels.length;j++){"
                + "    if(trim(labels[j].textContent)!==name){continue;}"
                + "    var row=labels[j].closest('div.draggable-item,a.cdk-drag.option-item');"
                + "    if(!row||inOrderSection(row)||!rowVisible(row)){continue;}"
                + "    return row;"
                + "  }"
                + "  return null;"
                + "}"
                + "function scrollables(){"
                + "  var list=[root];"
                + "  root.querySelectorAll("
                + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                + ").forEach(function(el){list.push(el);});"
                + "  return list;"
                + "}";
    }

    private boolean isLineItemColumnCheckedViaJs(String columnLabel) {
        return isVisibleLineItemColumnChecked(columnLabel);
    }

    /**
     * JS label click on the visible line-item column row — no native/Selenium click.
     * Skips when the row is already checked (avoids accidental uncheck).
     */
    public boolean clickLineItemColumnLabelJsOnly(String columnLabel) throws InterruptedException {
        prepareLineItemColumnsSectionInManagePanel();
        Thread.sleep(400);
        if (isVisibleLineItemColumnChecked(columnLabel)) {
            Logger.logMessage(columnLabel + " already checked on visible row — skip JS label click");
            return false;
        }
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    lineItemColumnRowScriptPrefix(escaped)
                            + "function jsClickLabel(){"
                            + "  var row=findVisibleRow()||findAnyRow();"
                            + "  if(!row){return 'missing';}"
                            + "  row.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  if(rowChecked(row)){return 'already';}"
                            + "  var label=rowLabel(row);"
                            + "  if(!label){return 'missing';}"
                            + "  label.click();"
                            + "  return 'clicked';"
                            + "}"
                            + "var out=jsClickLabel();"
                            + "if(out==='clicked'||out==='already'){return out;}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  var step=Math.max(50,Math.floor((el.clientHeight||280)*0.35));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    out=jsClickLabel();"
                            + "    if(out==='clicked'||out==='already'){return out;}"
                            + "  }"
                            + "}"
                            + "return jsClickLabel();");
            String status = String.valueOf(result);
            if ("clicked".equals(status)) {
                Logger.logMessage("JS-clicked " + columnLabel + " label on visible draggable-item row");
                return true;
            }
            if ("already".equals(status)) {
                Logger.logMessage(columnLabel + " already checked after scroll — skip JS label click");
                return false;
            }
            Logger.logConsoleMessage("Could not JS-click " + columnLabel + " label — row not found");
            return false;
        } catch (Exception e) {
            Logger.logConsoleMessage("JS label click failed for " + columnLabel + ": " + e.getMessage());
            return false;
        }
    }

    /** Reads line-item-level checkbox (draggable-item / LINE ITEM COLUMNS section). */
    private boolean isOrderLineItemFlatCheckboxSelected(String columnLabel) {
        if (isLineItemColumnCheckedViaJs(columnLabel)) {
            return true;
        }
        By checkbox = tableView.lineItemLevelColumnCheckbox(columnLabel);
        try {
            scrollLabelIntoView(Section.ORDER_LINE_ITEM, columnLabel);
            if (WaitUtil.isDisplayFast(checkbox, 3)) {
                return DriverUtil.isSelectedCheckbox(checkbox);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return isLineItemColumnCheckedViaJs(columnLabel);
    }

    /** Reads order-level checkbox under the Order columns accordion (e.g. PTS Packaging ID). */
    private boolean isOrderLevelCheckboxSelected(String columnLabel) {
        if (isOrderLevelColumnCheckedViaJs(columnLabel, false)) {
            return true;
        }
        By checkbox = tableView.orderColumnCheckbox(columnLabel);
        try {
            if (WaitUtil.isDisplayFast(checkbox, 2)) {
                return DriverUtil.isSelectedCheckbox(checkbox);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return false;
    }

    /** Toggles line-item-level checkbox on the visible {@code draggable-item} row only if unchecked. */
    public boolean ensureLineItemLevelColumnCheckedViaJs(String columnLabel) {
        try {
            prepareLineItemColumnsSectionInManagePanel();
            Thread.sleep(300);
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    lineItemColumnRowScriptPrefix(escaped)
                            + "function toggleVisibleRow(){"
                            + "  var row=findVisibleRow()||findAnyRow();"
                            + "  if(!row){return false;}"
                            + "  row.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  if(rowChecked(row)){return true;}"
                            + "  var label=rowLabel(row);"
                            + "  var cb=row.querySelector('input[type=checkbox]');"
                            + "  if(label){try{label.click();}catch(e){}}"
                            + "  if(cb&&!rowChecked(row)){try{cb.click();}catch(e2){}}"
                            + "  return rowChecked(row);"
                            + "}"
                            + "if(toggleVisibleRow()){return true;}"
                            + "var scrollEls=scrollables();"
                            + "for(var s=0;s<scrollEls.length;s++){"
                            + "  var el=scrollEls[s];"
                            + "  var step=Math.max(50,Math.floor((el.clientHeight||280)*0.35));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    if(toggleVisibleRow()){return true;}"
                            + "  }"
                            + "}"
                            + "return toggleVisibleRow();");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS ensureLineItemLevelColumnChecked failed for " + columnLabel + ": "
                    + e.getMessage());
            return false;
        }
    }

    /** @deprecated use {@link #ensureLineItemLevelColumnCheckedViaJs}. */
    public boolean ensureOrderLineItemFlatColumnCheckedViaJs(String columnLabel) {
        return ensureLineItemLevelColumnCheckedViaJs(columnLabel);
    }

    private boolean isOrderLevelColumnCheckedViaJs(String columnLabel, boolean toggleIfUnchecked) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "var headers=root.querySelectorAll('.table-column-name');"
                            + "var orderSection=null;"
                            + "for(var h=0;h<headers.length;h++){"
                            + "  var ht=(headers[h].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(/^order columns$/i.test(ht)){orderSection=headers[h].closest('.multi-options-list,.table-column');break;}"
                            + "}"
                            + "if(!orderSection){return false;}"
                            + "var labels=orderSection.querySelectorAll('label.option-label,label');"
                            + "for(var i=0;i<labels.length;i++){"
                            + "  if((labels[i].textContent||'').replace(/\\s+/g,' ').trim()!==name){continue;}"
                            + "  var cb=labels[i].previousElementSibling;"
                            + "  if(!cb||cb.type!=='checkbox'){"
                            + "    cb=labels[i].closest('.option-container,.draggable-item')?.querySelector('input[type=checkbox]');"
                            + "  }"
                            + "  return cb?!!cb.checked:false;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ensures {@code columnLabel} is checked (blue) before closing Manage columns.
     * Uses scroll + label click when bulk JS pass toggled zero boxes.
     */
    public void ensureColumnChecked(String columnLabel, SoftAssert softAssert) throws InterruptedException {
        expandAllManageColumnAccordions();
        scrollLabelIntoView(columnLabel);
        Thread.sleep(300);
        if (isColumnChecked(columnLabel)) {
            Logger.logMessage(columnLabel + " already checked in Manage columns — skipping click");
            return;
        }
        By checkbox = tableView.lineItemColumnCheckbox(columnLabel);
        By label = tableView.lineItemColumnLabel(columnLabel);
        if (waitForCheckboxWithoutBulkScroll(Section.ORDER_LINE_ITEM, columnLabel, 8)) {
            clickColumnCheckboxIfUnchecked(checkbox, label, columnLabel);
            Thread.sleep(300);
        }
        if (!isColumnChecked(columnLabel)) {
            ensureColumnCheckedViaJs(columnLabel);
        }
        Verify.softAssert1(isColumnChecked(columnLabel),
                columnLabel + " checkbox is checked (blue) before closing Manage columns", softAssert);
    }

    /** Activity Type only — never bulk-scrolls or toggles other columns. */
    public void ensureActivityTypeOnlyChecked(SoftAssert softAssert) throws InterruptedException {
        String columnLabel = ManageColumnOptions.ACTIVITY_TYPE;
        scrollUntilLineItemColumnVisible(columnLabel);
        if (isActivityTypeCheckedOnOrdersView()) {
            Logger.logMessage(columnLabel + " already checked at line-item level — no click (avoids accidental uncheck)");
            return;
        }
        clickLineItemColumnNativeAfterScroll(columnLabel);
        Thread.sleep(400);
        Verify.softAssert1(isActivityTypeCheckedOnOrdersView(),
                columnLabel + " checked (line item level) on " + ManageColumnOptions.AUTOMATION_VIEW_NAME,
                softAssert);
    }

    /** @deprecated prefer {@link #ensureActivityTypeOnlyChecked}; kept for other suite callers. */
    public void ensureCriticalColumnsChecked(SoftAssert softAssert) throws InterruptedException {
        ensureActivityTypeOnlyChecked(softAssert);
    }

    /**
     * After Set as default on {@link ManageColumnOptions#AUTOMATION_VIEW_NAME}: enable essential columns
     * for the active tab (Activity Type + LineItem ID on Orders expanded grid).
     */
    public void ensureEssentialColumnsChecked(SoftAssert softAssert, ConsoleTab tab) throws InterruptedException {
        String[] columns = tab == ConsoleTab.LINE_ITEMS
                ? ManageColumnOptions.ESSENTIAL_LINE_ITEM_COLUMNS
                : ManageColumnOptions.ESSENTIAL_ORDERS_LINE_ITEM_COLUMNS;
        expandAllManageColumnAccordions();
        scrollAllLists();
        for (String column : columns) {
            ensureColumnChecked(column, softAssert);
        }
    }

    /** Scrolls virtual lists and checks {@code columnLabel} via JS when Selenium locators miss. */
    public boolean ensureColumnCheckedViaJs(String columnLabel) {
        return ensureLineItemLevelColumnCheckedViaJs(columnLabel)
                || ensureColumnCheckedViaJs(Section.LINE_ITEM, columnLabel);
    }

    public boolean ensureColumnCheckedViaJs(Section section, String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var name='" + escaped + "';"
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return false;}"
                            + "function tryCheck(){"
                            + "  var nodes=root.querySelectorAll("
                            + "'label.option-label,label.form-check-label,label,a.option-item label,.option label,span.option-label');"
                            + "  for(var i=0;i<nodes.length;i++){"
                            + "    var text=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "    if(text!==name){continue;}"
                            + "    nodes[i].scrollIntoView({block:'center',inline:'nearest'});"
                            + "    var container=nodes[i].closest("
                            + "'.option-container,.draggable-item,a.option-item,.option,.form-check');"
                            + "    var cb=container?container.querySelector('input[type=checkbox]'):null;"
                            + "    if(!cb&&nodes[i].previousElementSibling"
                            + "&&nodes[i].previousElementSibling.type==='checkbox'){"
                            + "      cb=nodes[i].previousElementSibling;"
                            + "    }"
                            + "    if(!cb||cb.disabled){return false;}"
                            + "    if(!cb.checked){try{nodes[i].click();}catch(e){}try{cb.click();}catch(e2){}}"
                            + "    return !!cb.checked;"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "function scrollables(){"
                            + "  var list=[root];"
                            + "  root.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                            + ").forEach(function(el){list.push(el);});"
                            + "  return list;"
                            + "}"
                            + "for(var s=0;s<scrollables().length;s++){"
                            + "  var el=scrollables()[s];"
                            + "  el.scrollTop=0;"
                            + "  if(tryCheck()){return true;}"
                            + "  var step=Math.max(40,Math.floor((el.clientHeight||300)*0.35));"
                            + "  var max=Math.max(el.scrollHeight-el.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    el.scrollTop=pos;"
                            + "    if(tryCheck()){return true;}"
                            + "  }"
                            + "  el.scrollTop=max;"
                            + "  if(tryCheck()){return true;}"
                            + "}"
                            + "return tryCheck();");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS ensureColumnChecked failed for " + columnLabel + ": " + e.getMessage());
            return false;
        }
    }

    private void scrollManageColumnsPanelToTop() {
        try {
            driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var panel=findManageColumnsRoot();"
                            + "if(!panel){return;}"
                            + "panel.scrollTop=0;"
                            + "panel.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list'"
                            + ").forEach(function(el){el.scrollTop=0;});");
            Thread.sleep(200);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns panel to top: " + e.getMessage());
        }
    }

    /**
     * Virtual-scroll aware: scroll the Manage columns panel top-to-bottom in small steps,
     * checking each visible unchecked box at every stop (Activity Type and other columns
     * appear naturally while scrolling — no label search needed).
     */
    private int selectAllUncheckedCheckboxesInOpenPanel() {
        try {
            Object result = driver.get().browser().executeScript(
                    FIND_MANAGE_COLUMNS_ROOT_JS
                            + "var root=findManageColumnsRoot();"
                            + "if(!root){return 0;}"
                            + "function panelViewport(){"
                            + "  var p=root.getBoundingClientRect();"
                            + "  return {top:p.top+2,bottom:p.bottom-2};"
                            + "}"
                            + "function isVisibleLabel(label){"
                            + "  var r=label.getBoundingClientRect();"
                            + "  if(r.width<=0||r.height<=0){return false;}"
                            + "  var p=panelViewport();"
                            + "  return r.bottom>p.top&&r.top<p.bottom;"
                            + "}"
                            + "function clickUncheckedLabels(){"
                            + "  var toggled=0;"
                            + "  root.querySelectorAll('label.option-label,label.form-check-label,label').forEach(function(label){"
                            + "    if(!isVisibleLabel(label)){return;}"
                            + "    var container=label.closest('.option-container,.draggable-item,a.option-item,.option');"
                            + "    var cb=container?container.querySelector('input[type=checkbox]'):null;"
                            + "    if(!cb){cb=label.previousElementSibling;}"
                            + "    if(!cb||cb.disabled||cb.checked){return;}"
                            + "    try{label.click();toggled++;}catch(e){try{cb.click();toggled++;}catch(e2){}}"
                            + "  });"
                            + "  return toggled;"
                            + "}"
                            + "function collectScrollables(){"
                            + "  var list=[root];"
                            + "  root.querySelectorAll("
                            + "'.options-list-scrollbar,.cdk-virtual-scroll-viewport,.cdk-drop-list,.multi-options-list'"
                            + ").forEach(function(el){"
                            + "  if(el.scrollHeight>el.clientHeight+5){list.push(el);}"
                            + "});"
                            + "  return list;"
                            + "}"
                            + "function scrollAndCheck(scrollEl){"
                            + "  var total=0;"
                            + "  scrollEl.scrollTop=0;"
                            + "  var step=Math.max(35,Math.floor((scrollEl.clientHeight||320)*0.3));"
                            + "  var max=Math.max(scrollEl.scrollHeight-scrollEl.clientHeight,0);"
                            + "  for(var pos=0;pos<=max;pos+=step){"
                            + "    scrollEl.scrollTop=pos;"
                            + "    total+=clickUncheckedLabels();"
                            + "  }"
                            + "  scrollEl.scrollTop=max;"
                            + "  total+=clickUncheckedLabels();"
                            + "  return total;"
                            + "}"
                            + "var total=0;"
                            + "collectScrollables().forEach(function(el){total+=scrollAndCheck(el);});"
                            + "return total;");
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            return Integer.parseInt(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not select all Manage columns checkboxes: " + e.getMessage());
            return 0;
        }
    }

    public By columnCheckbox(Section section, String columnLabel) {
        switch (section) {
            case ORDER:
                return tableView.orderColumnCheckbox(columnLabel);
            case PACKAGE:
                return By.XPath("//div[contains(@class,'table-column')]//div[contains(text(), 'Package columns')]"
                        + "//following::label[normalize-space()='" + columnLabel + "']"
                        + "/preceding-sibling::input[@type='checkbox']");
            case ORDER_LINE_ITEM:
                return tableView.lineItemLevelColumnCheckbox(columnLabel);
            case LINE_ITEM:
                return tableView.lineItemColumnCheckbox(columnLabel);
            default:
                return tableView.CheckboxOnTableView(columnLabel);
        }
    }

    public By columnLabel(Section section, String columnLabel) {
        switch (section) {
            case ORDER:
                return tableView.OrderColumnNameOnTableView(columnLabel);
            case PACKAGE:
                return tableView.PackageColumnNameOnTableView(columnLabel);
            case ORDER_LINE_ITEM:
                return tableView.lineItemLevelColumnLabel(columnLabel);
            case LINE_ITEM:
                return tableView.lineItemColumnLabel(columnLabel);
            default:
                return tableView.CheckboxOnTableView(columnLabel);
        }
    }

    public void validateColumnListed(Section section, String columnLabel, SoftAssert softAssert) {
        Verify.softAssert1(WaitUtil.isDisplay(columnLabel(section, columnLabel), DEFAULT_PANEL_WAIT_S),
                columnLabel + " listed under " + section.name() + " in Manage columns", softAssert);
    }

    public boolean waitForCheckbox(Section section, String columnLabel, int totalSeconds)
            throws InterruptedException {
        return waitForCheckboxWithoutBulkScroll(section, columnLabel, totalSeconds);
    }

    private boolean waitForCheckboxWithoutBulkScroll(Section section, String columnLabel, int totalSeconds)
            throws InterruptedException {
        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        long deadline = System.currentTimeMillis() + (totalSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            scrollLabelIntoView(section, columnLabel);
            if (WaitUtil.isDisplayFast(checkbox, 2) || WaitUtil.isDisplayFast(label, 2)) {
                return true;
            }
            Thread.sleep(DEFAULT_POLL_MS);
        }
        scrollLabelIntoView(section, columnLabel);
        return WaitUtil.isDisplayFast(checkbox, 1) || WaitUtil.isDisplayFast(label, 1);
    }

    public void enableColumnIfNeeded(Section section, String columnLabel, SoftAssert softAssert)
            throws InterruptedException {
        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        if (!WaitUtil.isDisplayFast(checkbox, 3) && !WaitUtil.isDisplayFast(label, 3)) {
            validateColumnListed(section, columnLabel, softAssert);
        }
        if (DriverUtil.isSelectedCheckbox(checkbox)) {
            Logger.logReportMessage(columnLabel + " already enabled in Manage columns");
            return;
        }
        boolean toggled = clickColumnCheckboxIfUnchecked(checkbox, label, columnLabel);
        Verify.softAssert1(toggled, "Enabled " + columnLabel + " in Manage columns", softAssert);
        saveChangesWhenNeeded(softAssert, columnLabel);
    }

    public boolean clickColumnCheckbox(By checkbox, By label, String columnLabel) {
        return clickColumnCheckboxIfUnchecked(checkbox, label, columnLabel);
    }

    /** Clicks only when {@code columnLabel} is not already checked — prevents double-toggle uncheck. */
    public boolean clickColumnCheckboxIfUnchecked(Section section, By checkbox, By label, String columnLabel) {
        if (ManageColumnOptions.ACTIVITY_TYPE.equals(columnLabel)) {
            if (isActivityTypeCheckedOnOrdersView()) {
                Logger.logMessage(columnLabel + " already checked at line-item level — skip click");
                return true;
            }
            try {
                if (!scrollUntilLineItemColumnVisible(columnLabel)) {
                    return false;
                }
                return clickLineItemColumnNativeAfterScroll(columnLabel)
                        || isActivityTypeCheckedOnOrdersView();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } else if (isColumnChecked(section, columnLabel)) {
            Logger.logMessage(columnLabel + " already checked — skip click");
            return true;
        }
        scrollLabelIntoView(section, columnLabel);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (DriverUtil.clickOnElement(label, 5)) {
            Logger.logMessage("Clicked " + columnLabel + " label in Manage columns");
            return verifyColumnCheckedAfterClick(section, columnLabel);
        }
        if (DriverUtil.clickOnElement(checkbox, 5)) {
            Logger.logMessage("Clicked " + columnLabel + " checkbox in Manage columns");
            return verifyColumnCheckedAfterClick(section, columnLabel);
        }
        try {
            if (!verifyColumnCheckedAfterClick(section, columnLabel)) {
                driver.get().finder().findElement(checkbox).executeScript(
                        "if(!arguments[0].checked){arguments[0].click();}");
            }
            if (verifyColumnCheckedAfterClick(section, columnLabel)) {
                Logger.logMessage("JS-clicked " + columnLabel + " checkbox in Manage columns");
                return true;
            }
        } catch (Exception e) {
            try {
                if (!verifyColumnCheckedAfterClick(section, columnLabel)) {
                    driver.get().finder().findElement(label).executeScript("arguments[0].click();");
                }
                if (verifyColumnCheckedAfterClick(section, columnLabel)) {
                    Logger.logMessage("JS-clicked " + columnLabel + " label in Manage columns");
                    return true;
                }
            } catch (Exception ex) {
                Logger.logConsoleMessage("Failed to toggle " + columnLabel + " in Manage columns: " + ex.getMessage());
            }
        }
        return verifyColumnCheckedAfterClick(section, columnLabel);
    }

    private boolean verifyColumnCheckedAfterClick(Section section, String columnLabel) {
        if (ManageColumnOptions.ACTIVITY_TYPE.equals(columnLabel)) {
            return isActivityTypeCheckedOnOrdersView();
        }
        return isColumnChecked(section, columnLabel);
    }

    /** Clicks only when {@code columnLabel} is not already checked — prevents double-toggle uncheck. */
    public boolean clickColumnCheckboxIfUnchecked(By checkbox, By label, String columnLabel) {
        Section section = ManageColumnOptions.defaultSectionForColumn(columnLabel);
        return clickColumnCheckboxIfUnchecked(section, checkbox, label, columnLabel);
    }

    public void saveChangesIfPresent(SoftAssert softAssert) throws InterruptedException {
        saveChangesIfPresent(softAssert, null);
    }

    public void saveChangesIfPresent(SoftAssert softAssert, String expectedAddedColumn) throws InterruptedException {
        By saveBtn = ptsPage.saveChangesButtonInPanel();
        if (WaitUtil.isDisplayFast(saveBtn, 2)) {
            Verify.softAssert1(DriverUtil.clickOnElement(saveBtn, 5),
                    "Clicked Save changes in Manage columns", softAssert);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
        } else if (clickSaveChangesButtonViaJs()) {
            Verify.softAssert1(true, "JS-clicked Save changes in Manage columns", softAssert);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
        } else {
            Logger.logMessage("Save changes button not available — skipping save");
            return;
        }
        confirmSaveChangesModal(softAssert, expectedAddedColumn);
        waitForPanelClosed(3);
    }

    /**
     * Waits for the save-changes confirmation modal, verifies Added Columns lists {@code expectedAddedColumn}
     * when provided, then clicks Save changes on the modal.
     */
    private void confirmSaveChangesModal(SoftAssert softAssert, String expectedAddedColumn)
            throws InterruptedException {
        if (!WaitUtil.isDisplayFast(ptsPage.saveChangesConfirmDialogAny(), 8)) {
            Logger.logMessage("No Save changes confirmation modal appeared — continuing");
            return;
        }
        if (expectedAddedColumn != null && !expectedAddedColumn.isBlank()) {
            boolean listed = WaitUtil.isDisplay(ptsPage.saveChangesConfirmAddedColumn(expectedAddedColumn), 8);
            Verify.softAssert1(listed,
                    "Save changes modal lists Added Columns: \"" + expectedAddedColumn + "\"", softAssert);
            if (listed) {
                Logger.logMessage("Save changes modal confirms Added Columns: \"" + expectedAddedColumn + "\"");
            }
        }
        if (WaitUtil.isDisplayFast(ptsPage.saveChangesConfirmButton(), 3)) {
            DriverUtil.clickOnElement(ptsPage.saveChangesConfirmButton(), 5);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
            Logger.logMessage("Confirmed Save changes on modal");
        }
    }

    /** Clicks only the enabled button whose label is exactly {@code Save changes} (not Save new view). */
    private boolean clickSaveChangesButtonViaJs() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var buttons=document.querySelectorAll('button.save-button, button.btn.save-button');"
                            + "for(var i=0;i<buttons.length;i++){"
                            + "  var b=buttons[i];"
                            + "  var label=(b.textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(label!=='Save changes'||b.disabled){continue;}"
                            + "  b.click();"
                            + "  return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS Save changes click failed: " + e.getMessage());
            return false;
        }
    }

    private void waitForPanelClosed(int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (!isPanelOpenInDom()) {
                return;
            }
            Thread.sleep(200);
        }
    }

    /**
     * Scrolls the open panel, checks any visible unchecked boxes, and clicks Save changes when
     * anything was toggled. Use on an existing {@link ManageColumnOptions#AUTOMATION_VIEW_NAME} view.
     *
     * @return number of checkboxes enabled this pass
     */
    public int syncUncheckedColumnsAndSaveChanges(SoftAssert softAssert) throws InterruptedException {
        int toggled = selectAllUncheckedCheckboxes();
        ensureCriticalColumnsChecked(softAssert);
        if (toggled > 0) {
            Logger.logMessage("Manage columns: enabled " + toggled + " unchecked checkbox(es) — saving changes");
            saveChangesIfPresent(softAssert);
        }
        return toggled;
    }

    /** Maps legacy {@code FeatureFlagColumnUtil.ColumnSection} values. */
    public static Section toSection(com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection section) {
        return section == com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection.ORDER
                ? Section.ORDER : Section.LINE_ITEM;
    }
}
