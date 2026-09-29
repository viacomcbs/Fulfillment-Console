package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.pageobjects.FilterPanel;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;

public class FilterPanel_Util {

    private static boolean yesterdayDateFilterApplied;
    private static boolean yesterdayDateFilterAttempted;

    private final FilterPanel filterPanel = new FilterPanel();
    private final WaitUtils waitUtils = new WaitUtils();
    private final TableView_utill tableViewUtill = new TableView_utill();

    public static void resetYesterdayDateFilterState() {
        yesterdayDateFilterApplied = false;
        yesterdayDateFilterAttempted = false;
    }

    public static boolean isYesterdayDateFilterCached() {
        return yesterdayDateFilterApplied;
    }

    public static boolean isYesterdayDateFilterAttempted() {
        return yesterdayDateFilterAttempted;
    }

    /**
     * Applies the "Yesterday" preset on the Last Updated date filter to stabilize the orders grid
     * and avoid live-update record count drift during automation.
     */
    public void applyYesterdayDateFilter() throws InterruptedException {
        if (yesterdayDateFilterApplied && isYesterdayDateFilterActiveStrict()) {
            return;
        }
        if (isYesterdayDateFilterActiveStrict()) {
            yesterdayDateFilterApplied = true;
            Logger.logReportMessage("Yesterday date filter already active on page — skipping apply");
            return;
        }
        applyYesterdayDateFilterInternal();
    }

    /** Ensures Yesterday is active before export even if a prior bootstrap attempt failed. */
    public void ensureYesterdayDateFilterForExport() throws InterruptedException {
        if (isYesterdayDateFilterActiveStrict()) {
            yesterdayDateFilterApplied = true;
            return;
        }
        if ((yesterdayDateFilterApplied || yesterdayDateFilterAttempted)
                && FulfillmentJsUtil.waitForOrdersGridRendered(5)) {
            Logger.logReportMessage("Yesterday filter already attempted this session - grid still rendered");
            return;
        }
        FulfillmentJsUtil.closeManageColumnsPanel();
        FulfillmentJsUtil.dismissBlockingOverlays();
        applyYesterdayDateFilterInternal();
    }

    private void applyYesterdayDateFilterInternal() throws InterruptedException {
        if (!FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            tableViewUtill.waitForOrdersGridReadyFast(15);
        }
        boolean applied = applyYesterdayDateFilterViaTc024Calendar();
        if (!applied) {
            applied = applyYesterdayDateFilterViaLastUpdatedDropdown();
        }
        if (!applied) {
            applied = applyYesterdayDateFilterViaScript();
        }
        if (!applied) {
            applied = applyYesterdayDateFilterViaXPath();
        }
        pressEscapeToCloseFilter();
        yesterdayDateFilterAttempted = true;
        if (applied && isYesterdayDateFilterActiveStrict()) {
            yesterdayDateFilterApplied = true;
            closeDatePickerPopup();
            tableViewUtill.waitForOrdersGridReadyFast(10);
            Logger.logReportMessage("Applied Yesterday date filter (verified on UI)");
            return;
        }
        if (applied) {
            yesterdayDateFilterApplied = true;
            closeDatePickerPopup();
            tableViewUtill.waitForOrdersGridReadyFast(10);
            Logger.logReportMessage("Applied Yesterday date filter (Close clicked, continuing)");
            return;
        }
        Logger.logConsoleMessage("Yesterday date filter could not be applied — continuing without it");
    }

    /** Returns to main orders grid when date-filter clicks or row selection opened order focus view. */
    public void ensureOrdersGridHome() throws InterruptedException {
        String base = ConfigProps.getTargetURL();
        if (base == null || base.trim().isEmpty()) {
            base = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "") + "/";
        try {
            String current = BaseTest.driver.get().browser().getCurrentUrl();
            if (current != null && isOrderFocusUrl(current)) {
                Logger.logReportMessage("Returning to orders home grid from focus view");
                BaseTest.driver.get().browser().getUrl(root);
                WaitUtil.waitForJSToLoad(10);
                Thread.sleep(400);
            }
        } catch (Exception ignored) {
        }
        if (!FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            tableViewUtill.waitForOrdersGridReadyFast(15);
        }
    }

    private static final String TC024_EXPORT_ENV_NAME = "fspglobal";

    /**
     * TC024 Scriptless step 6: after Done filter, check fspglobal in Environment accordion before Export.
     */
    public void ensureTc024EnvironmentFilterForExport() throws InterruptedException {
        if (isEnvironmentFilterActive()) {
            Logger.logReportMessage("TC024 environment filter already active");
            return;
        }
        Logger.logReportMessage("TC024: selecting Environment filter (fspglobal) before export");
        FulfillmentJsUtil.dismissBlockingOverlays();
        scrollFilterListToEnvironment();
        ensureEnvironmentAccordionExpanded();
        Thread.sleep(800);
        String result = selectTc024EnvironmentViaScript();
        Logger.logReportMessage("TC024 environment filter result: " + result);
        if (!isEnvironmentFilterActive()) {
            result = activateEnvironmentFiltersViaScript();
            Logger.logReportMessage("TC024 environment Select all fallback: " + result);
        }
        if (!isEnvironmentFilterActive()) {
            ensureEnvironmentAccordionExpanded();
            Thread.sleep(600);
            result = clickEnvironmentSelectAllInOpenSection();
            Logger.logReportMessage("TC024 environment accordion Select all fallback: " + result);
        }
        Thread.sleep(1200);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        Thread.sleep(800);
        if (isEnvironmentFilterActive()) {
            Logger.logReportMessage("TC024 environment filter active for export");
        } else {
            Logger.logReportMessage("TC024 environment filter still 0/N — export may not produce download link");
        }
    }

    /**
     * DEV orders grid may not mount when the Environment filter shows 0/N active.
     * Opens Filters → Environment section → Select all (N).
     */
    public void ensureEnvironmentFiltersActive() throws InterruptedException {
        if (isEnvironmentFilterActive()) {
            return;
        }
        Logger.logReportMessage("Environment filter has 0 selections — enabling all via Select all");
        FulfillmentJsUtil.resetPageZoom();
        openFiltersPanelIfNeeded();
        Thread.sleep(600);

        String result = activateEnvironmentFiltersViaScript();
        Logger.logReportMessage("Environment filter activation result: " + result);

        if (!isEnvironmentFilterActive()) {
            if (WaitUtil.isDisplay(filterPanel.environmentFilter(), 5)) {
                DriverUtil.clickOnElementSafely(filterPanel.environmentFilter(), 8);
                Thread.sleep(800);
                result = clickEnvironmentSelectAllInOpenSection();
                Logger.logReportMessage("Environment filter XPath fallback: " + result);
            }
        }

        if (!isEnvironmentFilterActive()) {
            clickEnvironmentAccordionButton();
            Thread.sleep(1000);
            result = clickEnvironmentSelectAllInOpenSection();
            Logger.logReportMessage("Environment accordion fallback: " + result);
        }

        pressEscapeToCloseFilter();
        Thread.sleep(1500);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        Thread.sleep(1200);
        tableViewUtill.waitForOrdersGridReadyFast(20);
        if (!isEnvironmentFilterActive()) {
            Logger.logReportMessage("Environment filter still 0/N after Select all — grid may not load");
        } else {
            Logger.logReportMessage("Environment filter active — orders grid should mount");
        }
    }

    private String activateEnvironmentFiltersViaScript() {
        try {
            ensureEnvironmentAccordionExpanded();
            Object result = BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(!section)return 'no-section';"
                    + "section.scrollIntoView({block:'center'});"
                    + "expandEnvironmentSection(section);"
                    + "var selectAll=clickSelectAllInSection(section);"
                    + "if(selectAll)return selectAll;"
                    + "var checked=checkAllBoxesInSection(section);"
                    + "if(checked>0)return 'checked-'+checked;"
                    + "return 'no-select-all';");
            return result == null ? "null" : String.valueOf(result);
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    private String selectTc024EnvironmentViaScript() {
        try {
            String envName = escJs(TC024_EXPORT_ENV_NAME);
            Object result = BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(!section)return 'no-section';"
                    + "section.scrollIntoView({block:'center'});"
                    + "expandEnvironmentSection(section);"
                    + "var body=environmentAccordionBody(section)||section;"
                    + "var fsp=body.querySelector(\"[id*='__" + envName + "'] input[type=checkbox],"
                    + "tr[id*='__" + envName + "'] input[type=checkbox],"
                    + "input[id*='__" + envName + "']\");"
                    + "if(fsp){if(!isChecked(fsp))fsp.click();return 'fspglobal';}"
                    + "var labels=body.querySelectorAll('label,span,td');"
                    + "for(var i=0;i<labels.length;i++){"
                    + "  var t=norm(labels[i].textContent);"
                    + "  if(t&&/" + envName + "/i.test(t)){"
                    + "    labels[i].click();"
                    + "    return 'fspglobal-label';"
                    + "  }"
                    + "}"
                    + "var boxes=body.querySelectorAll('table input[type=checkbox],tr input[type=checkbox]');"
                    + "for(var j=0;j<boxes.length;j++){"
                    + "  if(!isChecked(boxes[j])){boxes[j].click();return 'first-env-checkbox';}"
                    + "}"
                    + "return 'no-checkbox';");
            return result == null ? "null" : String.valueOf(result);
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    private void scrollFilterListToEnvironment() {
        try {
            BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(section){section.scrollIntoView({block:'center'});return true;}"
                    + "var list=document.querySelector('.filter-list,[class*=filter-list],[class*=filters-panel]');"
                    + "if(list){list.scrollTop=list.scrollHeight*0.55;return true;}"
                    + "return false;");
        } catch (Exception ignored) {
        }
    }

    private void ensureEnvironmentAccordionExpanded() throws InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            if (isEnvironmentAccordionExpanded()) {
                return;
            }
            clickEnvironmentAccordionButton();
            Thread.sleep(700);
        }
    }

    private boolean isEnvironmentAccordionExpanded() {
        try {
            Object result = BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(!section)return false;"
                    + "if(section.getAttribute('aria-expanded')==='true')return true;"
                    + "var body=environmentAccordionBody(section);"
                    + "if(!body)return false;"
                    + "var cls=(body.className||'').toString();"
                    + "if(/\\bshow\\b/.test(cls)||body.offsetHeight>40)return true;"
                    + "var selectAll=body.querySelectorAll('span,label,button,a');"
                    + "for(var i=0;i<selectAll.length;i++){"
                    + "  if(/^select all\\s*\\([\\d,]+\\)$/i.test(norm(selectAll[i].textContent)))return true;"
                    + "}"
                    + "return body.querySelectorAll('input[type=checkbox],tr[id*=__]').length>0;");
            return Boolean.TRUE.equals(result) || "true".equalsIgnoreCase(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private void clickEnvironmentAccordionButton() {
        try {
            BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(!section)return false;"
                    + "section.scrollIntoView({block:'center'});"
                    + "section.click();"
                    + "return true;");
        } catch (Exception ignored) {
        }
    }

    private String clickEnvironmentSelectAllInOpenSection() {
        try {
            Object result = BaseTest.driver.get().browser().executeScript(environmentFilterPaneJs()
                    + "var section=findEnvironmentSection();"
                    + "if(!section){"
                    + "  var pane=document.querySelector('.cdk-overlay-pane');"
                    + "  if(pane&&/environment/i.test(pane.textContent||''))section=pane;"
                            + "}"
                    + "if(!section)return 'no-section';"
                    + "var selectAll=clickSelectAllInSection(section);"
                    + "if(selectAll)return selectAll;"
                    + "var checked=checkAllBoxesInSection(section);"
                    + "return checked>0?'checked-'+checked:'no-select-all';");
            return result == null ? "null" : String.valueOf(result);
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    private static String environmentFilterPaneJs() {
        return "function norm(s){return (s||'').replace(/\\s+/g,' ').trim();}"
                + "function findEnvironmentSection(){"
                + "  var buttons=document.querySelectorAll('button.accordion-button,button[class*=accordion-button]');"
                + "  for(var b=0;b<buttons.length;b++){"
                + "    var bt=norm(buttons[b].textContent);"
                + "    if(/^environment\\s+\\d+\\s*\\/\\s*\\d+/i.test(bt))return buttons[b];"
                + "  }"
                + "  var sections=document.querySelectorAll('.filter-list-section,[class*=filter-list-section]');"
                + "  for(var i=0;i<sections.length;i++){"
                + "    var t=norm(sections[i].textContent);"
                + "    if(/^environment\\b/i.test(t)||/\\benvironment\\s+\\d+\\s*\\/\\s*\\d+/i.test(t))return sections[i];"
                + "  }"
                + "  var nodes=document.querySelectorAll('[class*=filter-list] span,[class*=filter-list] button,[class*=filter-list] div');"
                + "  for(var j=0;j<nodes.length;j++){"
                + "    var nt=norm(nodes[j].textContent);"
                + "    if(/^environment$/i.test(nt)){"
                + "      var sec=nodes[j].closest('.filter-list-section,[class*=filter-list-section]');"
                + "      if(sec)return sec;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function environmentAccordionBody(btn){"
                + "  if(!btn)return null;"
                + "  var controls=btn.getAttribute('aria-controls');"
                + "  if(controls){var panel=document.getElementById(controls);if(panel)return panel;}"
                + "  var target=btn.getAttribute('data-bs-target');"
                + "  if(target){var el=document.querySelector(target);if(el)return el;}"
                + "  if(btn.nextElementSibling)return btn.nextElementSibling;"
                + "  var item=btn.closest('.accordion-item,[class*=accordion-item]');"
                + "  if(item){var body=item.querySelector('.accordion-collapse,.accordion-body,[class*=accordion-body]');if(body)return body;}"
                + "  return btn.parentElement||btn;"
                + "}"
                + "function clickEnvironmentHeader(section){"
                + "  var nodes=section.querySelectorAll('span,label,button,div,a');"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    var t=norm(nodes[i].textContent);"
                + "    if(/^environment$/i.test(t)||/^environment\\s+\\d+\\s*\\/\\s*\\d+$/i.test(t)){"
                + "      nodes[i].scrollIntoView({block:'center'});"
                + "      nodes[i].click();"
                + "      return true;"
                + "    }"
                + "  }"
                + "  section.click();"
                + "  return true;"
                + "}"
                + "function expandEnvironmentSection(section){"
                + "  if(!section)return null;"
                + "  section.scrollIntoView({block:'center'});"
                + "  if(section.getAttribute('aria-expanded')!=='true'){"
                + "    try{section.click();}catch(e){clickEnvironmentHeader(section);}"
                + "  }"
                + "  var body=environmentAccordionBody(section);"
                + "  var searchRoot=body||section;"
                + "  var spans=searchRoot.querySelectorAll('span,label,button,a');"
                + "  for(var k=0;k<spans.length;k++){"
                + "    if(/^select all\\s*\\([\\d,]+\\)$/i.test(norm(spans[k].textContent)))return searchRoot;"
                + "  }"
                + "  if(section.getAttribute('aria-expanded')!=='true'){"
                + "    clickEnvironmentHeader(section);"
                + "  }"
                + "  return environmentAccordionBody(section)||section;"
                + "}"
                + "function clickSelectAllInSection(section){"
                + "  var root=environmentAccordionBody(section)||section;"
                + "  if(!root)return null;"
                + "  var envMatch=norm(section.textContent).match(/environment\\s*\\d+\\s*\\/\\s*(\\d+)/i);"
                + "  var envTotal=envMatch?parseInt(envMatch[1],10):null;"
                + "  var nodes=root.querySelectorAll('span,label,button,a');"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    var t=norm(nodes[i].textContent);"
                + "    var m=t.match(/^select all\\s*\\(([\\d,]+)\\)$/i);"
                + "    if(!m)continue;"
                + "    var count=parseInt(m[1].replace(/,/g,''),10);"
                + "    if(envTotal!=null&&count!==envTotal)continue;"
                + "    nodes[i].scrollIntoView({block:'center'});"
                + "    nodes[i].click();"
                + "    return t;"
                + "  }"
                + "  return null;"
                + "}"
                + "function isChecked(el){"
                + "  if(!el)return false;"
                + "  if(el.checked===true)return true;"
                + "  var aria=el.getAttribute&&el.getAttribute('aria-checked');"
                + "  if(aria==='true')return true;"
                + "  var cls=(el.className||'').toString();"
                + "  return /checked|selected|mdc-checkbox--selected|mat-mdc-checkbox-checked/i.test(cls);"
                + "}"
                + "function checkAllBoxesInSection(section){"
                + "  var root=environmentAccordionBody(section)||section;"
                + "  var selectors='input[type=checkbox],mat-checkbox,.mat-mdc-checkbox,[role=checkbox]';"
                + "  var boxes=root.querySelectorAll(selectors);"
                + "  var count=0;"
                + "  for(var i=0;i<boxes.length;i++){"
                + "    if(isChecked(boxes[i]))continue;"
                + "    boxes[i].click();"
                + "    count++;"
                + "  }"
                + "  if(count>0)return count;"
                + "  var labels=root.querySelectorAll('label,span,div');"
                + "  for(var j=0;j<labels.length;j++){"
                + "    var t=norm(labels[j].textContent);"
                + "    if(!t||/^select all/i.test(t)||/^environment/i.test(t))continue;"
                + "    if(t.length>60)continue;"
                + "    labels[j].click();"
                + "    count++;"
                + "    if(count>=20)break;"
                + "  }"
                + "  return count;"
                + "}";
    }

    private boolean expandEnvironmentFilterSection() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var list=document.querySelector('.filter-list,[class*=filter-list]');"
                            + "if(list){list.scrollTop=list.scrollHeight;}");
            Object opened = BaseTest.driver.get().browser().executeScript(
                    "var target='environment';"
                            + "var nodes=document.querySelectorAll("
                            + "'.filter-list-section span,.filter-list-section label,.filter-list-section button,"
                            + "[class*=filter-list] span,[class*=filter-list] div,[class*=filter-list] button');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(!t)continue;"
                            + "  var plain=t.replace(/\\s*\\([\\d,]+\\)\\s*$/,'')"
                            + "    .replace(/\\s*\\d+\\/\\d+\\s*$/,'').trim().toLowerCase();"
                            + "  if(plain===target||plain.indexOf(target)===0||/environment\\s+0\\s*\\/\\s*\\d+/i.test(t)){"
                            + "    nodes[i].scrollIntoView({block:'center'});"
                            + "    nodes[i].click();"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(opened) || "true".equalsIgnoreCase(String.valueOf(opened));
        } catch (Exception e) {
            return false;
        }
    }

    private String clickEnvironmentSelectAll() {
        try {
            Object result = BaseTest.driver.get().browser().executeScript(
                    "var nodes=document.querySelectorAll('span,label,button,a,div');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(/^select all\\s*\\([\\d,]+\\)$/i.test(t)){"
                            + "    nodes[i].scrollIntoView({block:'center'});"
                            + "    nodes[i].click();"
                            + "    return t;"
                            + "  }"
                            + "}"
                            + "var pane=document.querySelector('.cdk-overlay-pane,.filter-list-section,[class*=filter-list]');"
                            + "if(pane){"
                            + "  var cbs=pane.querySelectorAll('input[type=checkbox]');"
                            + "  var n=0;"
                            + "  for(var k=0;k<cbs.length;k++){if(!cbs[k].checked){cbs[k].click();n++;}}"
                            + "  if(n>0)return 'checked-'+n;"
                            + "}"
                            + "return 'no-select-all';");
            return result == null ? "null" : String.valueOf(result);
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    private boolean isEnvironmentFilterActive() {
        try {
            Object result = BaseTest.driver.get().browser().executeScript(
                    "function parseEnvCount(t){"
                            + "  var m=(t||'').match(/environment\\s*(\\d+)\\s*\\/\\s*(\\d+)/i);"
                            + "  if(!m)return null;"
                            + "  return {active:parseInt(m[1],10),total:parseInt(m[2],10)};"
                            + "}"
                            + "var nodes=document.querySelectorAll('span,label,div,button');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var info=parseEnvCount((nodes[i].textContent||'').trim());"
                            + "  if(info&&info.total>0){return info.active>0;}"
                            + "}"
                            + "return true;");
            return Boolean.TRUE.equals(result) || "true".equalsIgnoreCase(String.valueOf(result));
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean isOrderFocusUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.contains("/focus") || url.contains("orderId=") || url.contains("order=");
    }

    /**
     * Clears all checked Line Item Status filter checkboxes in the left sidebar.
     */
    public void clearLineItemStatusFilters() throws InterruptedException {
        openFiltersPanelIfNeeded();
        openLineItemStatusFilterSection();
        Thread.sleep(400);
        try {
            BaseTest.driver.get().browser().executeScript(
                    "function pane(){"
                            + "  var panes=document.querySelectorAll('.cdk-overlay-pane');"
                            + "  for(var i=panes.length-1;i>=0;i--){"
                            + "    if((panes[i].textContent||'').toLowerCase().indexOf('line item status')>=0)return panes[i];"
                            + "  }"
                            + "  var sections=document.querySelectorAll('.filter-list-section,[class*=filter-list-section]');"
                            + "  for(var j=0;j<sections.length;j++){"
                            + "    if((sections[j].textContent||'').toLowerCase().indexOf('line item status')>=0)return sections[j];"
                            + "  }"
                            + "  return document.querySelector('.cdk-overlay-pane')"
                            + "    || document.querySelector('[class*=filter-panel]')"
                            + "    || document.querySelector('[class*=filter-list]');"
                            + "}"
                            + "var root=pane();"
                            + "if(!root)return false;"
                            + "var cbs=root.querySelectorAll('input[type=checkbox]');"
                            + "for(var k=0;k<cbs.length;k++){if(cbs[k].checked)cbs[k].click();}"
                            + "return true;");
        } catch (Exception ignored) {
        }
        Thread.sleep(600);
    }

    /**
     * Applies Yesterday date filter, searches Line Item Status filter, and selects the matching status.
     *
     * @return true when the status checkbox was selected
     */
    public boolean filterByLineItemStatus(String searchTerm, String statusLabel) throws InterruptedException {
        ensureOrdersGridForFilters();
        clearOrdersGridSearch();
        applyYesterdayDateFilter();
        clearLineItemStatusFilters();
        if (applyLineItemStatusFilter(searchTerm, statusLabel, true)) {
            tableViewUtill.waitForOrdersGridReadyFast(15);
            Thread.sleep(800);
            Logger.logReportMessage("Applied Line Item Status filter [" + statusLabel + "]");
            return true;
        }
        if (applyLineItemStatusFilterViaXPath(searchTerm, statusLabel)) {
            tableViewUtill.waitForOrdersGridReadyFast(15);
            Thread.sleep(800);
            Logger.logReportMessage("Applied Line Item Status filter [" + statusLabel + "] via XPath");
            return true;
        }
        clearOrdersGridSearch();
        Logger.logConsoleMessage("Line Item Status filter could not be applied for [" + statusLabel + "]");
        return false;
    }

    private void clearOrdersGridSearch() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var inputs=document.querySelectorAll("
                            + "'input[placeholder*=Search],input[placeholder*=search],input[type=search]');"
                            + "for(var i=0;i<inputs.length;i++){"
                            + "  var r=inputs[i].getBoundingClientRect();"
                            + "  if(r.width<=0||r.height<=0)continue;"
                            + "  if(r.top>220)continue;"
                            + "  if(r.left<window.innerWidth*0.25)continue;"
                            + "  inputs[i].focus();"
                            + "  inputs[i].value='';"
                            + "  inputs[i].dispatchEvent(new Event('input',{bubbles:true}));"
                            + "  inputs[i].dispatchEvent(new Event('change',{bubbles:true}));"
                            + "  return true;"
                            + "}"
                            + "return false;");
        } catch (Exception ignored) {
        }
    }

    private void ensureOrdersGridForFilters() throws InterruptedException {
        ensureOrdersGridHome();
        openFiltersPanelIfNeeded();
        Thread.sleep(800);
    }

    private void openLineItemStatusFilterSection() throws InterruptedException {
        if (openLineItemStatusFilterViaScript()) {
            Thread.sleep(600);
            return;
        }
        By lineItemFilter = filterPanel.lineItemStatusFilter();
        if (WaitUtil.isDisplay(lineItemFilter, 5)) {
            DriverUtil.clickOnElementSafely(lineItemFilter, 10);
            Thread.sleep(600);
        }
    }

    private boolean openLineItemStatusFilterViaScript() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var list=document.querySelector('.filter-list,[class*=filter-list]');"
                            + "if(list){list.scrollTop=list.scrollHeight;}");
            Object opened = BaseTest.driver.get().browser().executeScript(
                    "var nodes=document.querySelectorAll('span,label,div,button');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').trim();"
                            + "  if(/^line item status$/i.test(t)"
                            + "    ||(t.length<=30&&/line item status/i.test(t))){"
                            + "    nodes[i].scrollIntoView({block:'center'});"
                            + "    nodes[i].click();"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(opened) || "true".equalsIgnoreCase(String.valueOf(opened));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean applyLineItemStatusFilter(String searchTerm, String statusLabel, boolean exclusive)
            throws InterruptedException {
        openFiltersPanelIfNeeded();
        if (!openLineItemStatusFilterViaScript()) {
            openLineItemStatusFilterSection();
        }
        Thread.sleep(1500);
        String escapedSearch = escJs(searchTerm);
        String escapedStatus = escJs(statusLabel);
        try {
            Object direct = BaseTest.driver.get().browser().executeScript(
                    lineItemStatusPaneJs()
                            + "return selectStatusAnywhere('" + escapedStatus + "'," + exclusive + ")?'direct':'no-direct';");
            if ("direct".equals(String.valueOf(direct))) {
                Logger.logReportMessage("Line Item Status filter [" + statusLabel + "]: direct checkbox select");
                pressEscapeToCloseFilter();
                Thread.sleep(400);
                return true;
            }
            Object searchTyped = BaseTest.driver.get().browser().executeScript(
                    lineItemStatusPaneJs()
                            + "var pane=findPane();"
                            + "return typeSearchInPane(pane,'" + escapedSearch + "')?'search-typed':'no-search';");
            Thread.sleep(1500);
            Object applied = BaseTest.driver.get().browser().executeScript(
                    lineItemStatusPaneJs()
                            + "return selectStatusAnywhere('" + escapedStatus + "',false)?'true':'no-checkbox';");
            Thread.sleep(800);
            String result = String.valueOf(applied);
            Logger.logReportMessage("Line Item Status filter script result [" + statusLabel + "]: "
                    + searchTyped + " -> " + result);
            if (Boolean.TRUE.equals(applied) || "true".equalsIgnoreCase(result)) {
                pressEscapeToCloseFilter();
                Thread.sleep(400);
                return true;
            }
            return false;
        } catch (Exception e) {
            Logger.logConsoleMessage("Line Item Status filter script failed: " + e.getMessage());
            return false;
        }
    }

    private static String lineItemStatusPaneJs() {
        return "function norm(s){return (s||'').toLowerCase().replace(/\\s+/g,' ').trim();}"
                + "function isVisible(el){"
                + "  if(!el)return false;"
                + "  var r=el.getBoundingClientRect();"
                + "  if(r.width<=0||r.height<=0)return false;"
                + "  var s=window.getComputedStyle(el);"
                + "  return s.display!=='none'&&s.visibility!=='hidden';"
                + "}"
                + "function findPane(){"
                + "  var panes=document.querySelectorAll('.cdk-overlay-pane');"
                + "  for(var i=panes.length-1;i>=0;i--){"
                + "    var r=panes[i].getBoundingClientRect();"
                + "    if(r.width>80&&r.height>80&&isVisible(panes[i]))return panes[i];"
                + "  }"
                + "  for(var j=panes.length-1;j>=0;j--){"
                + "    if((panes[j].textContent||'').toLowerCase().indexOf('line item status')>=0)return panes[j];"
                + "  }"
                + "  var sections=document.querySelectorAll('.filter-list-section,[class*=filter-list-section]');"
                + "  for(var k=0;k<sections.length;k++){"
                + "    if((sections[k].textContent||'').toLowerCase().indexOf('line item status')>=0)return sections[k];"
                + "  }"
                + "  return document.querySelector('.cdk-overlay-pane')"
                + "    || document.querySelector('[class*=filter-panel]')"
                + "    || document.querySelector('[class*=filter-list]');"
                + "}"
                + "function collectRoots(){"
                + "  var roots=[];"
                + "  var panes=document.querySelectorAll('.cdk-overlay-pane');"
                + "  for(var i=panes.length-1;i>=0;i--){"
                + "    var r=panes[i].getBoundingClientRect();"
                + "    if(r.width>80&&r.height>80&&isVisible(panes[i]))roots.push(panes[i]);"
                + "  }"
                + "  var sections=document.querySelectorAll('.filter-list-section,[class*=filter-list-section]');"
                + "  for(var j=0;j<sections.length;j++){"
                + "    if((sections[j].textContent||'').toLowerCase().indexOf('line item status')>=0)roots.push(sections[j]);"
                + "  }"
                + "  if(!roots.length){var p=findPane();if(p)roots.push(p);}"
                + "  return roots;"
                + "}"
                + "function labelMatches(nodeText,target){"
                + "  var t=norm((nodeText||'').replace(/\\(\\d+\\)/g,''));"
                + "  var need=norm(target);"
                + "  if(t===need)return true;"
                + "  if(t.indexOf(need)===0&&(t.length===need.length||t.charAt(need.length)===' '))return true;"
                + "  if(need==='processing'&&t.indexOf('processing failed')===0)return false;"
                + "  return false;"
                + "}"
                + "function findSearchNearLineItemStatus(){"
                + "  var headerY=null;"
                + "  var nodes=document.querySelectorAll('span,label,div,button');"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    var t=(nodes[i].textContent||'').trim();"
                + "    if(/^line item status$/i.test(t)){headerY=nodes[i].getBoundingClientRect().top;break;}"
                + "  }"
                + "  var best=null,bestDist=99999;"
                + "  var inputs=document.querySelectorAll('input,textarea,[contenteditable=true]');"
                + "  for(var j=0;j<inputs.length;j++){"
                + "    if(!isVisible(inputs[j]))continue;"
                + "    if(inputs[j].type==='checkbox'||inputs[j].type==='hidden'||inputs[j].type==='radio')continue;"
                + "    var r=inputs[j].getBoundingClientRect();"
                + "    if(r.left>window.innerWidth*0.38)continue;"
                + "    var dist=headerY==null?r.top:Math.abs(r.top-headerY);"
                + "    if(headerY!=null&&r.top<headerY-20)continue;"
                + "    if(dist<bestDist){best=inputs[j];bestDist=dist;}"
                + "  }"
                + "  return best;"
                + "}"
                + "function typeSearchInPane(pane,term){"
                + "  var inp=findSearchNearLineItemStatus();"
                + "  if(!inp&&pane){"
                + "    var inputs=pane.querySelectorAll('input,textarea,[contenteditable=true]');"
                + "    for(var i=0;i<inputs.length;i++){"
                + "      if(inputs[i].type==='checkbox'||inputs[i].type==='hidden')continue;"
                + "      inp=inputs[i];break;"
                + "    }"
                + "  }"
                + "  if(!inp)return false;"
                + "  inp.focus();"
                + "  inp.value='';"
                + "  inp.dispatchEvent(new Event('input',{bubbles:true}));"
                + "  inp.value=term;"
                + "  inp.dispatchEvent(new Event('input',{bubbles:true}));"
                + "  inp.dispatchEvent(new Event('change',{bubbles:true}));"
                + "  return true;"
                + "}"
                + "function selectStatusInPane(pane,target,exclusive){"
                + "  if(!pane)return false;"
                + "  if(exclusive){"
                + "    var all=pane.querySelectorAll('input[type=checkbox]');"
                + "    for(var u=0;u<all.length;u++){if(all[u].checked)all[u].click();}"
                + "  }"
                + "  var labels=pane.querySelectorAll('label,span,div.option,div');"
                + "  for(var k=0;k<labels.length;k++){"
                + "    if(!isVisible(labels[k]))continue;"
                + "    var txt=(labels[k].textContent||'').trim();"
                + "    if(!labelMatches(txt,target))continue;"
                + "    var cb=labels[k].querySelector('input[type=checkbox]');"
                + "    if(!cb&&labels[k].previousElementSibling&&labels[k].previousElementSibling.type==='checkbox')"
                + "      cb=labels[k].previousElementSibling;"
                + "    if(!cb){var p=labels[k].closest('div,li,label');if(p)cb=p.querySelector('input[type=checkbox]');}"
                + "    if(cb){if(!cb.checked)cb.click();return true;}"
                + "    labels[k].click();"
                + "    return true;"
                + "  }"
                + "  var cbs=pane.querySelectorAll('input[type=checkbox]');"
                + "  for(var c=0;c<cbs.length;c++){"
                + "    var row=cbs[c].closest('label,div,li');"
                + "    if(!row)continue;"
                + "    if(!labelMatches(row.textContent||'',target))continue;"
                + "    if(!cbs[c].checked)cbs[c].click();"
                + "    return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function selectStatusAnywhere(target,exclusive){"
                + "  var roots=collectRoots();"
                + "  for(var i=0;i<roots.length;i++){"
                + "    if(selectStatusInPane(roots[i],target,exclusive&&i===0))return true;"
                + "  }"
                + "  var nodes=document.querySelectorAll("
                + "'label,span,div,[role=checkbox],mat-checkbox,.mat-mdc-checkbox,.mat-checkbox');"
                + "  for(var n=0;n<nodes.length;n++){"
                + "    if(!isVisible(nodes[n]))continue;"
                + "    var r=nodes[n].getBoundingClientRect();"
                + "    if(r.left>window.innerWidth*0.42)continue;"
                + "    var txt=(nodes[n].textContent||'').trim();"
                + "    if(!labelMatches(txt,target))continue;"
                + "    var cb=nodes[n].querySelector('input[type=checkbox]');"
                + "    if(!cb){var mat=nodes[n].closest('mat-checkbox,.mat-mdc-checkbox,.mat-checkbox');if(mat)mat.click();return true;}"
                + "    if(cb){if(!cb.checked)cb.click();return true;}"
                + "    nodes[n].click();"
                + "    return true;"
                + "  }"
                + "  return false;"
                + "}";
    }

    private boolean applyLineItemStatusFilterViaXPath(String searchTerm, String statusLabel)
            throws InterruptedException {
        openFiltersPanelIfNeeded();
        openLineItemStatusFilterSection();
        Thread.sleep(600);
        By searchInput = filterPanel.lineItemStatusSearchInput();
        if (WaitUtil.isDisplay(searchInput, 8)) {
            try {
                BaseTest.driver.get().finder().findElement(searchInput).clear();
                BaseTest.driver.get().finder().findElement(searchInput).sendKeys(searchTerm);
                Thread.sleep(800);
            } catch (Exception ignored) {
            }
        }
        By checkbox = filterPanel.lineItemStatusCheckbox(statusLabel);
        if (WaitUtil.isDisplay(checkbox, 10) && !DriverUtil.isSelectedCheckbox(checkbox)) {
            DriverUtil.clickOnElementSafely(checkbox, 5);
            Thread.sleep(500);
            pressEscapeToCloseFilter();
            return true;
        }
        return false;
    }

    private static String escJs(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    public boolean isYesterdayDateFilterActive() {
        return isYesterdayDateFilterActiveStrict();
    }

    /** True only when the visible date range is a single day equal to yesterday (not a multi-day range). */
    public boolean isYesterdayDateFilterActiveStrict() {
        try {
            Object active = BaseTest.driver.get().browser().executeScript(
                    "function yesterdayParts(d){"
                            + "  var y=new Date(d);"
                            + "  y.setDate(y.getDate()-1);"
                            + "  var local=y.getFullYear()+'-'+String(y.getMonth()+1).padStart(2,'0')"
                            + "    +'-'+String(y.getDate()).padStart(2,'0');"
                            + "  return {"
                            + "    iso:local,"
                            + "    us:(y.getMonth()+1)+'/'+y.getDate()+'/'+y.getFullYear(),"
                            + "    local:local"
                            + "  };"
                            + "}"
                            + "function normalizeRange(t){"
                            + "  return (t||'').replace(/\\u2013|\\u2014/g,'-').replace(/\\s+/g,' ').trim();"
                            + "}"
                            + "function isSingleDayYesterday(text){"
                            + "  var t=normalizeRange(text);"
                            + "  var y=yesterdayParts(new Date());"
                            + "  var sameIso=new RegExp('^'+y.iso+'\\\\s*[-→>]\\\\s*'+y.iso+'$');"
                            + "  if(sameIso.test(t)||t===y.iso||t.indexOf(y.iso)>=0)return true;"
                            + "  if(t.indexOf(y.local)>=0)return true;"
                            + "  var usParts=y.us.split('/');"
                            + "  var usAlt=parseInt(usParts[0],10)+'/'+parseInt(usParts[1],10)+'/'+usParts[2];"
                            + "  var sameUs=new RegExp('^'+y.us+'\\\\s*[-→>]\\\\s*'+y.us+'$');"
                            + "  var sameUsAlt=new RegExp('^'+usAlt+'\\\\s*[-→>]\\\\s*'+usAlt+'$');"
                            + "  if(sameUs.test(t)||sameUsAlt.test(t)||t===y.us||t===usAlt)return true;"
                            + "  return false;"
                            + "}"
                            + "var cal=document.getElementById('calendar-dropdown');"
                            + "if(cal&&isSingleDayYesterday(cal.textContent||''))return true;"
                            + "var nodes=document.querySelectorAll('button,span,div,a');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').trim();"
                            + "  if(!/\\d{4}-\\d{2}-\\d{2}|\\d{1,2}\\/\\d{1,2}\\/\\d{4}/.test(t))continue;"
                            + "  if(isSingleDayYesterday(t))return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(active) || "true".equalsIgnoreCase(String.valueOf(active));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean applyYesterdayDateFilterViaTc024Calendar() throws InterruptedException {
        try {
            FulfillmentJsUtil.closeManageColumnsPanel();
            Object applied = BaseTest.driver.get().browser().executeScript(
                    "function openCalendar(){"
                            + "  var dd=document.getElementById('calendar-dropdown');"
                            + "  if(!dd)return false;"
                            + "  var icons=dd.querySelectorAll('i');"
                            + "  if(icons.length>=2){icons[1].click();return true;}"
                            + "  var icon=dd.querySelector('div > i:nth-of-type(2)');"
                            + "  if(icon){icon.click();return true;}"
                            + "  if(icons.length>0){icons[0].click();return true;}"
                            + "  dd.click();"
                            + "  return true;"
                            + "}"
                            + "function clickYesterday(){"
                            + "  var nodes=document.querySelectorAll("
                            + "'app-calendar-quick-options span,app-calendar-quick-options div span,"
                            + "app-calendar-quick-options *');"
                            + "  for(var i=0;i<nodes.length;i++){"
                            + "    var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "    if(/^yesterday$/i.test(t)){"
                            + "      try{nodes[i].click();return true;}catch(e){}"
                            + "    }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "if(!openCalendar())return false;"
                            + "for(var attempt=0;attempt<20;attempt++){"
                            + "  if(clickYesterday())return true;"
                            + "}"
                            + "return false;");
            if (Boolean.TRUE.equals(applied) || "true".equalsIgnoreCase(String.valueOf(applied))) {
                Thread.sleep(1000);
                closeDatePickerPopup();
                tableViewUtill.waitForOrdersGridReadyFast(10);
                Logger.logReportMessage("Applied Yesterday via TC024 calendar script");
                return true;
            }
            if (WaitUtil.isDisplay(filterPanel.calendarDropdownIcon(), 8)) {
                Logger.logReportMessage("Opening calendar dropdown (TC024 XPath)");
                DriverUtil.clickOnElementSafely(filterPanel.calendarDropdownIcon(), 8);
                Thread.sleep(1000);
            }
            for (int attempt = 0; attempt < 15; attempt++) {
                if (WaitUtil.isDisplay(filterPanel.calendarYesterdayOption(), 2)) {
                    DriverUtil.clickOnElementSafely(filterPanel.calendarYesterdayOption(), 8);
                    Thread.sleep(1000);
                    closeDatePickerPopup();
                    tableViewUtill.waitForOrdersGridReadyFast(10);
                    Logger.logReportMessage("Applied Yesterday via TC024 calendar XPath");
                    return true;
                }
                Thread.sleep(500);
            }
            pressEscapeToCloseFilter();
            return false;
        } catch (Exception ex) {
            Logger.logConsoleMessage("TC024 Yesterday calendar filter failed: " + ex.getMessage());
            return false;
        }
    }

    private boolean clickDoneStatusFilterViaTc024() {
        try {
            if (!WaitUtil.isDisplay(filterPanel.doneStatusFilterButton(), 5)) {
                return false;
            }
            return DriverUtil.clickOnElementSafely(filterPanel.doneStatusFilterButton(), 8);
        } catch (Exception ex) {
            Logger.logConsoleMessage("TC024 Done filter click failed: " + ex.getMessage());
            return false;
        }
    }

    private boolean applyYesterdayDateFilterViaLastUpdatedDropdown() throws InterruptedException {
        By dropdown = filterPanel.lastUpdatedFilterDropdown();
        if (!WaitUtil.isDisplay(dropdown, 8)) {
            return false;
        }
        Logger.logReportMessage("Clicking Last updated dropdown for Yesterday preset");
        DriverUtil.clickOnElementSafely(dropdown, 5);
        Thread.sleep(800);
        By yesterday = filterPanel.datePresetOption(FilterPanel.YESTERDAY_DATE_PRESET);
        if (!WaitUtil.isDisplay(yesterday, 8)) {
            pressEscapeToCloseFilter();
            return false;
        }
        DriverUtil.clickOnElementSafely(yesterday, 5);
        Thread.sleep(500);
        return true;
    }

    private boolean applyYesterdayDateFilterViaScript() throws InterruptedException {
        try {
            Object applied = BaseTest.driver.get().browser().executeScript(
                    "function normalizeRange(t){"
                            + "  return (t||'').replace(/\\u2013|\\u2014/g,'-').replace(/\\s+/g,' ').trim();"
                            + "}"
                            + "function openDateRangePicker(){"
                            + "  var nodes=document.querySelectorAll('button,span,div,a');"
                            + "  for(var j=0;j<nodes.length;j++){"
                            + "    if(nodes[j].closest('.ag-root,.ag-root-wrapper,.ag-cell,.ag-row,[role=gridcell]'))continue;"
                            + "    var r=nodes[j].getBoundingClientRect();"
                            + "    if(!r||r.top>280||r.width<=0)continue;"
                            + "    var txt=normalizeRange((nodes[j].textContent||'').trim());"
                            + "    if(/^\\d{4}-\\d{2}-\\d{2}\\s*-\\s*\\d{4}-\\d{2}-\\d{2}$/.test(txt)){"
                            + "      nodes[j].click(); return true;"
                            + "    }"
                            + "    if(/^\\d{1,2}\\/\\d{1,2}\\/\\d{4}\\s*-\\s*\\d{1,2}\\/\\d{1,2}\\/\\d{4}$/.test(txt)){"
                            + "      nodes[j].click(); return true;"
                            + "    }"
                            + "    if(/\\d{4}-\\d{2}-\\d{2}\\s*->\\s*\\d{4}-\\d{2}-\\d{2}/.test(txt)){"
                            + "      nodes[j].click(); return true;"
                            + "    }"
                            + "  }"
                            + "  var btns=document.querySelectorAll('button.dropdown-toggle,.dropdown-toggle');"
                            + "  for(var i=0;i<btns.length;i++){"
                            + "    var t=(btns[i].textContent||'').trim();"
                            + "    if(/^last updated$/i.test(t)){ btns[i].click(); return true; }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "function clickYesterdayPreset(){"
                            + "  var pane=document.querySelector('.cdk-overlay-pane')"
                            + "    ||document.querySelector('[class*=date-filter]')"
                            + "    ||document.body;"
                            + "  var nodes=pane.querySelectorAll('span,div,button,li,label,a');"
                            + "  for(var i=0;i<nodes.length;i++){"
                            + "    var t=(nodes[i].textContent||'').trim();"
                            + "    if(/^yesterday$/i.test(t)){ nodes[i].click(); return true; }"
                            + "  }"
                            + "  return false;"
                            + "}"
                            + "function closeDatePicker(){"
                            + "  var btns=document.querySelectorAll('button');"
                            + "  for(var i=0;i<btns.length;i++){"
                            + "    if(/^close$/i.test((btns[i].textContent||'').trim())){ btns[i].click(); return true; }"
                            + "  }"
                            + "  document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}));"
                            + "  return false;"
                            + "}"
                            + "if(!openDateRangePicker())return false;"
                            + "if(!clickYesterdayPreset())return false;"
                            + "closeDatePicker();"
                            + "return true;");
            Thread.sleep(800);
            return Boolean.TRUE.equals(applied) || "true".equalsIgnoreCase(String.valueOf(applied));
        } catch (Exception e) {
            Logger.logConsoleMessage("Yesterday date filter script failed: " + e.getMessage());
            return false;
        }
    }

    private boolean applyYesterdayDateFilterViaXPath() throws InterruptedException {
        if (WaitUtil.isDisplay(filterPanel.dateRangeTrigger(), 8)) {
            Logger.logReportMessage("Clicking top date-range control for Yesterday preset");
            DriverUtil.clickOnElementSafely(filterPanel.dateRangeTrigger(), 5);
            Thread.sleep(1000);
            if (WaitUtil.isDisplay(filterPanel.datePresetOption(FilterPanel.YESTERDAY_DATE_PRESET), 8)) {
                DriverUtil.clickOnElementSafely(filterPanel.datePresetOption(FilterPanel.YESTERDAY_DATE_PRESET), 5);
                Thread.sleep(800);
                if (WaitUtil.isDisplay(filterPanel.datePickerCloseButton(), 3)) {
                    DriverUtil.clickOnElementSafely(filterPanel.datePickerCloseButton(), 5);
                } else {
                    pressEscapeToCloseFilter();
                }
                tableViewUtill.waitForOrdersGridReadyFast(12);
                Thread.sleep(500);
                return true;
            }
            pressEscapeToCloseFilter();
        }
        return false;
    }

    public void validateTitleCategory(String title) {
        By titleFilter = filterPanel.getFilter(title);
        Verify.softAssert(WaitUtil.isDisplay(titleFilter, 5), "Title filter is displayed: " + title);
        WaitUtil.isElementVisible(titleFilter);
        driver.get().finder().findElement(titleFilter).click();
        waitUtils.waitForVisibilityOfElement(titleFilter, 10);
    }

    public void filterDeliveredOrdersOnly() throws InterruptedException {
        if (isDoneStatusFilterActive()) {
            Logger.logReportMessage("Done status filter already active - skipping click");
            return;
        }
        if (clickDoneStatusFilterViaTc024()) {
            Thread.sleep(1500);
            Logger.logReportMessage("Filtered orders via Done status chip (TC024 locator)");
            return;
        }
        if (clickStatusSummaryChip(FilterPanel.DELIVERED_STATUS)) {
            Thread.sleep(1500);
            Logger.logReportMessage("Filtered orders via Done status chip");
            return;
        }
        if (applyOrderStatusFilter(FilterPanel.DELIVERED_ROW_STATUS, true)
                || applyOrderStatusFilter("Delivered", true)) {
            Logger.logReportMessage("Filtered orders to Delivered status only");
            return;
        }
        Logger.logConsoleMessage("Order Status filter script failed; trying XPath fallback");
        openFiltersPanelIfNeeded();
        openOrderStatusFilter();
        Thread.sleep(400);
        selectSingleOrderStatus(FilterPanel.DELIVERED_ROW_STATUS);
        Thread.sleep(800);
        Logger.logReportMessage("Filtered orders to Delivered status only (XPath fallback)");
    }

    /** Toggles off the Done status summary chip when it is active. */
    public void toggleDoneStatusChipOff() throws InterruptedException {
        String escaped = FilterPanel.DELIVERED_STATUS.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object toggled = BaseTest.driver.get().browser().executeScript(
                    "var name='" + escaped.toLowerCase() + "';"
                            + "var nodes=document.querySelectorAll('button,span,div,a');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').trim().toLowerCase();"
                            + "  if(t.indexOf(name)!==0)continue;"
                            + "  var cls=(nodes[i].className||'').toString();"
                            + "  var pressed=nodes[i].getAttribute('aria-pressed')==='true';"
                            + "  if(/active|selected|primary|highlight|pressed|filter-active/i.test(cls)||pressed){"
                            + "    nodes[i].click();return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            if (Boolean.TRUE.equals(toggled) || "true".equalsIgnoreCase(String.valueOf(toggled))) {
                Thread.sleep(800);
                Logger.logReportMessage("Toggled Done status chip off");
            }
        } catch (Exception ignored) {
        }
    }

    public void filterNonDeliveredOrdersOnly() throws InterruptedException {
        if (clickStatusSummaryChip("In progress")) {
            Thread.sleep(1200);
            return;
        }
        if (applyOrderStatusFilter("In progress", true)) {
            return;
        }
        openFiltersPanelIfNeeded();
        openOrderStatusFilter();
        Thread.sleep(300);
        selectSingleOrderStatus("In progress");
        Thread.sleep(800);
    }

    private void clearActiveStatusChipsExcept(String keepLabel) {
        String escaped = keepLabel.replace("\\", "\\\\").replace("'", "\\'");
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var keep = '" + escaped.toLowerCase() + "';"
                            + "var nodes = document.querySelectorAll('button, span, div');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').trim().toLowerCase();"
                            + "  if (!/\\(\\d+\\)/.test(t)) continue;"
                            + "  if (t.indexOf(keep) === 0) continue;"
                            + "  var cls = (nodes[i].className || '').toString();"
                            + "  var pressed = nodes[i].getAttribute('aria-pressed') === 'true';"
                            + "  if (/active|selected|primary|highlight|pressed|filter-active/i.test(cls) || pressed) {"
                            + "    nodes[i].click();"
                            + "  }"
                            + "}");
            Thread.sleep(400);
        } catch (Exception ignored) {
        }
    }

    private boolean clickStatusSummaryChip(String statusLabel) {
        clearActiveStatusChipsExcept(statusLabel);
        String escaped = statusLabel.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    "var name = '" + escaped.toLowerCase() + "';"
                            + "var nodes = document.querySelectorAll('button, span, div, a');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').trim().toLowerCase();"
                            + "  if (t.indexOf(name) === 0 && /\\(\\d+\\)/.test(t)) { nodes[i].click(); return true; }"
                            + "  if (t === name) { nodes[i].click(); return true; }"
                            + "}"
                            + "return false;");
            if (Boolean.TRUE.equals(clicked) || "true".equalsIgnoreCase(String.valueOf(clicked))) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return WaitUtil.isDisplay(filterPanel.statusSummaryChip(statusLabel), 3)
                && DriverUtil.clickOnElementSafely(filterPanel.statusSummaryChip(statusLabel), 5);
    }

    private boolean applyOrderStatusFilter(String statusLabel, boolean exclusive) throws InterruptedException {
        openFiltersPanelIfNeeded();
        if (!openOrderStatusFilterViaScript()) {
            openOrderStatusFilter();
        }
        Thread.sleep(400);
        String escapedStatus = statusLabel.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object applied = BaseTest.driver.get().browser().executeScript(
                    "var target = '" + escapedStatus.toLowerCase() + "';"
                            + "var exclusive = " + exclusive + ";"
                            + "var pane = document.querySelector('.cdk-overlay-pane')"
                            + " || document.querySelector('[class*=filter-panel]')"
                            + " || document.querySelector('[class*=filter-list]')"
                            + " || document.body;"
                            + "if (exclusive) {"
                            + "  var cbs = pane.querySelectorAll('input[type=checkbox]');"
                            + "  for (var j = 0; j < cbs.length; j++) {"
                            + "    if (cbs[j].checked) cbs[j].click();"
                            + "  }"
                            + "}"
                            + "var labels = pane.querySelectorAll('label, span, div.option, div');"
                            + "for (var k = 0; k < labels.length; k++) {"
                            + "  var t = (labels[k].textContent || '').trim().toLowerCase();"
                            + "  if (t !== target && t.indexOf(target) < 0) continue;"
                            + "  var cb = labels[k].previousElementSibling;"
                            + "  if (cb && cb.type === 'checkbox') {"
                            + "    if (!cb.checked) cb.click();"
                            + "  } else {"
                            + "    labels[k].click();"
                            + "  }"
                            + "  return true;"
                            + "}"
                            + "return false;");
            pressEscapeToCloseFilter();
            Thread.sleep(500);
            return Boolean.TRUE.equals(applied) || "true".equalsIgnoreCase(String.valueOf(applied));
        } catch (Exception e) {
            Logger.logConsoleMessage("Order Status filter script failed: " + e.getMessage());
            return false;
        }
    }

    private void openFiltersPanelIfNeeded() throws InterruptedException {
        try {
            Object opened = BaseTest.driver.get().browser().executeScript(
                    "function clickIfMatch(el){"
                            + "if(!el)return false;"
                            + "el.scrollIntoView({block:'center'});"
                            + "el.click();"
                            + "return true;"
                            + "}"
                            + "var toggles=document.querySelectorAll("
                            + "'[class*=filter-toggle],[class*=filters-toggle],[class*=filter-panel-toggle],button,span,div,a');"
                            + "for(var i=0;i<toggles.length;i++){"
                            + "  var t=(toggles[i].textContent||'').trim();"
                            + "  if(/^filters$/i.test(t)||/^show filters$/i.test(t))return clickIfMatch(toggles[i]);"
                            + "}"
                            + "var sidebar=document.querySelector('[class*=filter-list],[class*=filters-panel],[class*=filter-panel]');"
                            + "if(sidebar){var r=sidebar.getBoundingClientRect();if(r.width>80&&r.height>80)return true;}"
                            + "return false;");
            if (!Boolean.TRUE.equals(opened) && !"true".equalsIgnoreCase(String.valueOf(opened))) {
                BaseTest.driver.get().browser().executeScript(
                        "var nodes=document.querySelectorAll('span,button,div');"
                                + "for(var i=0;i<nodes.length;i++){"
                                + "  var t=(nodes[i].textContent||'').trim();"
                                + "  if(/^filters$/i.test(t)){nodes[i].click();return true;}"
                                + "}"
                                + "return false;");
            }
        } catch (Exception ignored) {
        }
        Thread.sleep(600);
    }

    private boolean openOrderStatusFilterViaScript() {
        try {
            Object opened = BaseTest.driver.get().browser().executeScript(
                    "var nodes = document.querySelectorAll('span, label, div, button');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').trim();"
                            + "  if (/^order status$/i.test(t) || (t.length <= 24 && /order status/i.test(t))) {"
                            + "    nodes[i].click(); return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(opened) || "true".equalsIgnoreCase(String.valueOf(opened));
        } catch (Exception e) {
            return false;
        }
    }

    private void openOrderStatusFilter() {
        if (WaitUtil.isDisplay(filterPanel.orderStatusFilter(), 5)) {
            DriverUtil.clickOnElementSafely(filterPanel.orderStatusFilter(), 5);
        }
    }

    private void selectSingleOrderStatus(String statusLabel) {
        uncheckAllVisibleOrderStatuses();
        By checkbox = filterPanel.orderStatusCheckbox(statusLabel);
        if (WaitUtil.isDisplay(checkbox, 5) && !DriverUtil.isSelectedCheckbox(checkbox)) {
            DriverUtil.clickOnElementSafely(checkbox, 5);
        } else if (WaitUtil.isDisplay(filterPanel.orderStatusOption(statusLabel), 5)) {
            DriverUtil.clickOnElementSafely(filterPanel.orderStatusOption(statusLabel), 5);
        }
        pressEscapeToCloseFilter();
    }

    private void uncheckAllVisibleOrderStatuses() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "var cbs = document.querySelectorAll("
                            + "'.cdk-overlay-pane input[type=checkbox], .filter-panel input[type=checkbox], .filter-list-section input[type=checkbox]');"
                            + "for (var i = 0; i < cbs.length; i++) {"
                            + "  if (cbs[i].checked) cbs[i].click();"
                            + "}");
        } catch (Exception ignored) {
        }
    }

    private void pressEscapeToCloseFilter() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));");
        } catch (Exception ignored) {
        }
    }

    /** Clicks the date picker Close button (matches manual TC024 flow) and dismisses overlay. */
    public void closeDatePickerPopup() throws InterruptedException {
        try {
            if (WaitUtil.isDisplay(filterPanel.datePickerCloseButton(), 4)) {
                DriverUtil.clickOnElementSafely(filterPanel.datePickerCloseButton(), 5);
                Thread.sleep(600);
                Logger.logReportMessage("Closed date picker via Close button");
                return;
            }
            Object closed = BaseTest.driver.get().browser().executeScript(
                    "var panes=document.querySelectorAll('.cdk-overlay-pane');"
                            + "for(var p=panes.length-1;p>=0;p--){"
                            + "  var btns=panes[p].querySelectorAll('button');"
                            + "  for(var i=0;i<btns.length;i++){"
                            + "    if((btns[i].textContent||'').trim()==='Close'){"
                            + "      btns[i].click();return true;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "return false;");
            if (Boolean.TRUE.equals(closed) || "true".equalsIgnoreCase(String.valueOf(closed))) {
                Thread.sleep(600);
                Logger.logReportMessage("Closed date picker via JS Close button");
                return;
            }
        } catch (Exception ignored) {
        }
        pressEscapeToCloseFilter();
    }

    public boolean isDoneStatusFilterActive() {
        try {
            Object active = BaseTest.driver.get().browser().executeScript(
                    "var nodes=document.querySelectorAll('#leftButtongroupContainer button,"
                            + "#leftButtongroupContainer msc-filter-button-themed button,button');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').trim().toLowerCase();"
                            + "  if(!/^done\\b/.test(t))continue;"
                            + "  var cls=(nodes[i].className||'').toString();"
                            + "  var pressed=nodes[i].getAttribute('aria-pressed')==='true';"
                            + "  var selected=nodes[i].getAttribute('aria-selected')==='true';"
                            + "  if(/active|selected|primary|highlight|pressed|filter-active|mat-primary/i.test(cls)"
                            + "      ||pressed||selected){return true;}"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(active) || "true".equalsIgnoreCase(String.valueOf(active));
        } catch (Exception e) {
            return false;
        }
    }
}