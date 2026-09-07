package com.paramount.test.ff.uitests.helpers.dsid;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.DsidPage;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** DSID column + Details panel helpers for BSD-29870. */
public class DsidColumnUtil {

    private static final int PANEL_WAIT_S = 10;
    private static final int GRID_WAIT_S = 15;
    private static final int PANEL_SETTLE_MS = 800;
    private static final int MAX_ROWS_TO_SCAN = 25;

    private final DsidPage dsidPage = new DsidPage();
    private final FeatureFlagColumnUtil columnUtil = new FeatureFlagColumnUtil();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    public void applyMetadataOnlyDeliveryPartnerFilter(LeftFilterPanelUtil filterUtil, SoftAssert softAssert)
            throws InterruptedException {
        filterUtil.ensureLeftFilterPanelOpen();
        scrollFilterLabelIntoView(OrdersLeftFilter.JOB_TYPE.getDisplayName());
        Verify.softAssert1(filterUtil.waitForFilterHeaderVisible(OrdersLeftFilter.JOB_TYPE.getDisplayName(), 20),
                OrdersLeftFilter.JOB_TYPE.getDisplayName() + " filter header visible", softAssert);
        scrollFilterLabelIntoView(OrdersLeftFilter.PARTNER.getDisplayName());
        Verify.softAssert1(filterUtil.waitForFilterHeaderVisible(OrdersLeftFilter.PARTNER.getDisplayName(), 20),
                OrdersLeftFilter.PARTNER.getDisplayName() + " filter header visible", softAssert);

        filterUtil.expandFilter(OrdersLeftFilter.JOB_TYPE.getDisplayName());
        if (filterUtil.hasFilterSearchInput(OrdersLeftFilter.JOB_TYPE.getDisplayName())) {
            filterUtil.searchFilterOptions(OrdersLeftFilter.JOB_TYPE.getDisplayName(), DsidPage.JOB_TYPE_VALUE);
        }
        filterUtil.selectFilterOption(OrdersLeftFilter.JOB_TYPE.getDisplayName(), DsidPage.JOB_TYPE_VALUE);

        filterUtil.expandFilter(OrdersLeftFilter.PARTNER.getDisplayName());
        if (filterUtil.hasFilterSearchInput(OrdersLeftFilter.PARTNER.getDisplayName())) {
            filterUtil.searchFilterOptions(OrdersLeftFilter.PARTNER.getDisplayName(), DsidPage.PARTNER_VALUE);
        }
        filterUtil.selectFilterOption(OrdersLeftFilter.PARTNER.getDisplayName(), DsidPage.PARTNER_VALUE);

        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1500);
        Verify.softAssert1(WaitUtil.isDisplay(leftFilterPanel.tableRecordCountLabel(), GRID_WAIT_S),
                "Table refreshed after Job type + Partner filters", softAssert);
        Logger.logReportMessage("Applied filters: Job type=" + DsidPage.JOB_TYPE_VALUE
                + ", Partner=" + DsidPage.PARTNER_VALUE);
    }

    public void enableDsidColumnIfNeeded(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        if (section == ColumnSection.ORDER && DsidSessionHelper.isDsidColumnEnabledOnOrders()) {
            return;
        }
        if (section == ColumnSection.LINE_ITEM && DsidSessionHelper.isDsidColumnEnabledOnLineItems()) {
            return;
        }
        columnUtil.openManageColumnsPanel(softAssert);
        columnUtil.validateColumnListed(section, DsidPage.COLUMN_LABEL, softAssert);
        columnUtil.enableColumnIfNeeded(section, DsidPage.COLUMN_LABEL, softAssert);
        columnUtil.closeManageColumnsPanel(softAssert);
        columnUtil.validateColumnHeaderInGrid(DsidPage.COLUMN_LABEL, softAssert);
        scrollGridToDsidColumn();
        if (section == ColumnSection.ORDER) {
            DsidSessionHelper.markDsidColumnEnabledOnOrders();
        } else {
            DsidSessionHelper.markDsidColumnEnabledOnLineItems();
        }
    }

    public void validateAtLeastOneRowShowsDsid(ColumnSection section, SoftAssert softAssert)
            throws InterruptedException {
        scrollGridToDsidColumn();
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1000);

        DsidRowMatch match = findFirstRowWithDsid();
        Verify.softAssert1(match.rowIndex >= 0 && !match.value.isEmpty(),
                "At least one " + section.name() + " row shows populated " + DsidPage.COLUMN_LABEL
                        + " (scanned up to " + MAX_ROWS_TO_SCAN + " rows)", softAssert);

        if (match.rowIndex >= 0) {
            Logger.logReportMessage("Found " + DsidPage.COLUMN_LABEL + " on row " + (match.rowIndex + 1)
                    + ": " + match.value);
            if (section == ColumnSection.ORDER) {
                DsidSessionHelper.setOrdersRowWithDsid(match.rowIndex, match.value);
            } else {
                DsidSessionHelper.setLineItemsRowWithDsid(match.rowIndex, match.value);
            }
        }
    }

    public void openDetailsForStoredRow(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        int rowIndex = section == ColumnSection.ORDER
                ? DsidSessionHelper.getOrdersRowIndexWithDsid()
                : DsidSessionHelper.getLineItemsRowIndexWithDsid();
        Verify.softAssert1(rowIndex >= 0,
                "Row with " + DsidPage.COLUMN_LABEL + " was captured in prior test", softAssert);

        scrollGridToDsidColumn();
        By rowLocator = dsidPage.gridRowByIndex(rowIndex + 1);
        Verify.softAssert1(WaitUtil.isDisplay(rowLocator, GRID_WAIT_S),
                "Grid row " + (rowIndex + 1) + " visible for Details panel", softAssert);
        DriverUtil.clickOnElement(rowLocator, PANEL_WAIT_S);
        Thread.sleep(1500);
        openDetailsPanelIfPresent();
        Verify.softAssert1(isDetailsPanelOpen(),
                "Details panel open after clicking row " + (rowIndex + 1), softAssert);
    }

    /** All DSID values from the open Details panel (BSD-29967). */
    public List<String> readAllDetailsPanelDsids(SoftAssert softAssert) {
        List<String> values = readAllDsidValuesFromDetailsPanel();
        if (values.isEmpty()) {
            values = readDsidValuesViaJs();
        }
        Verify.softAssert1(!values.isEmpty(),
                "Details panel contains at least one " + DsidPage.DETAILS_PANEL_LABEL + " value", softAssert);
        Logger.logReportMessage("Details panel DSID values (" + values.size() + "): " + values);
        return values;
    }

    public void validateTableDsidMatchesDetailsPanelLastDsid(ColumnSection section, SoftAssert softAssert) {
        String tableValue = section == ColumnSection.ORDER
                ? DsidSessionHelper.getOrdersTableDsid()
                : DsidSessionHelper.getLineItemsTableDsid();
        String detailsValue = readLastDsidFromDetailsPanel(softAssert);

        Verify.softAssert1(!detailsValue.isEmpty(),
                DsidPage.DETAILS_PANEL_LABEL + " populated in Details panel (last value)", softAssert);
        Verify.softAssert1(normalizeDsid(tableValue).equals(normalizeDsid(detailsValue)),
                DsidPage.COLUMN_LABEL + " in grid matches last value in Details panel (table='"
                        + tableValue + "', detailsLast='" + detailsValue + "')", softAssert);
    }

    private void openDetailsPanelIfPresent() throws InterruptedException {
        if (isDetailsPanelOpen()) {
            return;
        }
        if (WaitUtil.isDisplay(dsidPage.detailsButton(), 5)) {
            DriverUtil.clickOnElement(dsidPage.detailsButton(), PANEL_WAIT_S);
            Thread.sleep(1500);
        }
    }

    private boolean isDetailsPanelOpen() {
        return WaitUtil.isDisplayFast(dsidPage.detailsPanelDsidSection(), 3)
                || WaitUtil.isDisplayFast(dsidPage.detailsPanelDsidValues(), 3);
    }

    private String readLastDsidFromDetailsPanel(SoftAssert softAssert) {
        List<String> values = readAllDsidValuesFromDetailsPanel();
        if (values.isEmpty()) {
            values = readDsidValuesViaJs();
        }
        Verify.softAssert1(!values.isEmpty(),
                "Details panel contains at least one " + DsidPage.DETAILS_PANEL_LABEL + " value", softAssert);
        if (values.isEmpty()) {
            return "";
        }
        String last = values.get(values.size() - 1);
        Logger.logReportMessage("Details panel DSID values: " + values + " — using last: " + last);
        return last;
    }

    private List<String> readAllDsidValuesFromDetailsPanel() {
        List<String> values = new ArrayList<>();
        try {
            for (DesktopBrowserElement el : driver.get().finder().findElements(dsidPage.detailsPanelDsidValues())) {
                String text = el.getText().trim();
                if (isPopulatedDsid(text)) {
                    values.add(text);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Details panel DSID read via Synergy failed: " + e.getMessage());
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private List<String> readDsidValuesViaJs() {
        List<String> values = new ArrayList<>();
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = 'DSID';"
                            + "var values = [];"
                            + "function pushVal(t) {"
                            + "  t = (t || '').replace(/\\s+/g, ' ').trim();"
                            + "  if (t && t !== '-' && values.indexOf(t) < 0) values.push(t);"
                            + "}"
                            + "var labels = document.querySelectorAll('.label, div.label, span.label');"
                            + "for (var i = 0; i < labels.length; i++) {"
                            + "  if ((labels[i].textContent || '').trim().toUpperCase() !== label) continue;"
                            + "  var root = labels[i].closest('.field-row, .field, .detail-row, div');"
                            + "  if (!root) root = labels[i].parentElement;"
                            + "  if (!root) continue;"
                            + "  var spans = root.querySelectorAll('.value span, .value, [class*=\"demandSystemId\"],"
                            + "    msc-copy-text-to-clipboard-chip, button.copy-button-wrapper');"
                            + "  for (var j = 0; j < spans.length; j++) {"
                            + "    pushVal(spans[j].getAttribute('cdkcopytoclipboard')"
                            + "      || spans[j].getAttribute('ng-reflect-copy')"
                            + "      || spans[j].getAttribute('data-clipboard-text')"
                            + "      || spans[j].innerText || spans[j].textContent);"
                            + "  }"
                            + "}"
                            + "return values;"
                            + "})()");
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        String text = String.valueOf(item).trim();
                        if (isPopulatedDsid(text)) {
                            values.add(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Details panel DSID JS read failed: " + e.getMessage());
        }
        return values;
    }

    private DsidRowMatch findFirstRowWithDsid() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = 'DSID';"
                            + "var maxRows = " + MAX_ROWS_TO_SCAN + ";"
                            + "function findHeader() {"
                            + "  var ths = document.querySelectorAll('th');"
                            + "  for (var i = 0; i < ths.length; i++) {"
                            + "    var tip = (ths[i].getAttribute('title') || '').trim();"
                            + "    var text = (ths[i].innerText || ths[i].textContent || '').replace(/\\s+/g,' ').trim();"
                            + "    if (tip === label || text === label || text.indexOf(label) >= 0) return ths[i];"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "function readCell(td) {"
                            + "  if (!td) return '';"
                            + "  var tip = (td.getAttribute('title') || '').trim();"
                            + "  if (tip && tip !== '-') return tip;"
                            + "  var copyEl = td.querySelector('button.copy-button-wrapper, msc-copy-text-to-clipboard-chip,"
                            + "    [cdkcopytoclipboard], [data-clipboard-text]');"
                            + "  if (copyEl) {"
                            + "    var attr = copyEl.getAttribute('cdkcopytoclipboard')"
                            + "      || copyEl.getAttribute('ng-reflect-copy')"
                            + "      || copyEl.getAttribute('data-clipboard-text') || '';"
                            + "    if (attr.trim()) return attr.trim();"
                            + "  }"
                            + "  var text = (td.innerText || td.textContent || '').replace(/\\s+/g,' ').trim();"
                            + "  return (text && text !== '-') ? text : '';"
                            + "}"
                            + "function findRows() {"
                            + "  var sels = ['#orderTable tbody tr', 'tbody tr.row', 'tbody tr'];"
                            + "  for (var s = 0; s < sels.length; s++) {"
                            + "    var nodes = document.querySelectorAll(sels[s]);"
                            + "    if (nodes && nodes.length) return nodes;"
                            + "  }"
                            + "  return [];"
                            + "}"
                            + "var header = findHeader();"
                            + "if (!header) return '-1|';"
                            + "var colIdx = header.cellIndex;"
                            + "var rows = findRows();"
                            + "var limit = Math.min(rows.length, maxRows);"
                            + "for (var r = 0; r < limit; r++) {"
                            + "  var tds = rows[r].querySelectorAll('td');"
                            + "  var val = readCell(tds[colIdx]);"
                            + "  if (val) return r + '|' + val;"
                            + "}"
                            + "return '-1|';"
                            + "})()");
            return parseRowMatchResult(result);
        } catch (Exception e) {
            Logger.logConsoleMessage("DSID row scan failed: " + e.getMessage());
            return new DsidRowMatch(-1, "");
        }
    }

    private DsidRowMatch parseRowMatchResult(Object result) {
        if (result == null) {
            return new DsidRowMatch(-1, "");
        }
        String text = String.valueOf(result).trim();
        if (!text.contains("|")) {
            return new DsidRowMatch(-1, "");
        }
        String[] parts = text.split("\\|", 2);
        try {
            int rowIndex = Integer.parseInt(parts[0].trim());
            String value = parts.length > 1 ? parts[1].trim() : "";
            return new DsidRowMatch(rowIndex, value);
        } catch (NumberFormatException e) {
            return new DsidRowMatch(-1, "");
        }
    }

    private void scrollFilterLabelIntoView(String filterName) {
        try {
            String safeName = filterName.replace("'", "\\'");
            driver.get().browser().executeScript(
                    "var name = '" + safeName + "';"
                            + "var labels = document.querySelectorAll('span.accordion-label');"
                            + "for (var i = 0; i < labels.length; i++) {"
                            + "  if ((labels[i].textContent || '').trim() === name) {"
                            + "    labels[i].scrollIntoView({block:'center'});"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "return false;");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Filter scroll failed for " + filterName + ": " + e.getMessage());
        }
    }

    private boolean scrollGridToDsidColumn() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = 'DSID';"
                            + "function findHeader() {"
                            + "  var ths = document.querySelectorAll('th');"
                            + "  for (var i = 0; i < ths.length; i++) {"
                            + "    var tip = (ths[i].getAttribute('title') || '').trim();"
                            + "    var text = (ths[i].innerText || ths[i].textContent || '').trim();"
                            + "    if (tip === label || text === label) return ths[i];"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "function scrollEl(el) {"
                            + "  if (!el) return;"
                            + "  el.scrollIntoView({block:'nearest', inline:'nearest'});"
                            + "  var p = el;"
                            + "  while (p) {"
                            + "    if (p.scrollWidth > p.clientWidth + 1) {"
                            + "      p.scrollLeft = p.scrollWidth;"
                            + "    }"
                            + "    p = p.parentElement;"
                            + "  }"
                            + "}"
                            + "var th = findHeader();"
                            + "scrollEl(th);"
                            + "var containers = document.querySelectorAll('.custom-table-wrapper, .table-container, #orderTable');"
                            + "for (var c = 0; c < containers.length; c++) {"
                            + "  if (containers[c].scrollWidth > containers[c].clientWidth + 1) {"
                            + "    containers[c].scrollLeft = containers[c].scrollWidth;"
                            + "  }"
                            + "}"
                            + "return th != null;"
                            + "})()");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isPopulatedDsid(String text) {
        return text != null && !text.trim().isEmpty() && !"-".equals(text.trim());
    }

    private static String normalizeDsid(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static final class DsidRowMatch {
        private final int rowIndex;
        private final String value;

        private DsidRowMatch(int rowIndex, String value) {
            this.rowIndex = rowIndex;
            this.value = value == null ? "" : value;
        }
    }
}
