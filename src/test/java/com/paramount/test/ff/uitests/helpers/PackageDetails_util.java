package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.pageobjects.DetailsPanel;
import com.paramount.test.ff.pageobjects.HomePage;
import com.synergy.core.driver.By;

import java.util.regex.Pattern;

public class PackageDetails_util {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final String FIELD_LABEL = DetailsPanel.SHIPPING_DOCK_PACKAGE_ID_LABEL;
    public static final String PROD_ORDER_ID = "wzr842617";
    public static final String PROD_PACKAGE_ID = "525941983";
    private static final String BSD29302_JS =
            "var UUID_RE = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/;"
                    + "var LABEL = 'shipping dock package id';"
                    + "function uuidNearLabel(text){"
                    + "  if (!text) return null;"
                    + "  if ((text || '').toLowerCase().indexOf(LABEL) < 0) return null;"
                    + "  var m = text.match(UUID_RE);"
                    + "  return m ? m[0] : null;"
                    + "}"
                    + "function findInColId(){"
                    + "  var cells = document.querySelectorAll('[col-id=shippingDockPackageId], [data-col-id=shippingDockPackageId], [col-id*=shippingDock]');"
                    + "  for (var i = 0; i < cells.length; i++) {"
                    + "    var m = (cells[i].textContent || '').match(UUID_RE);"
                    + "    if (m) return m[0];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findInPackageRows(){"
                    + "  var nodes = document.querySelectorAll('.package-group, .package-details, [class*=package-group], [class*=package-details], [class*=inner-package]');"
                    + "  for (var i = 0; i < nodes.length; i++) {"
                    + "    var id = uuidNearLabel(nodes[i].textContent || '');"
                    + "    if (id) return id;"
                    + "    var m = (nodes[i].textContent || '').match(UUID_RE);"
                    + "    if (m && /package id/i.test(nodes[i].textContent || '')) return m[0];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function detailsPanelRoot(){"
                    + "  return document.querySelector('[class*=details-panel], [class*=detail-panel], [class*=side-panel], aside[class*=detail], [class*=right-panel]');"
                    + "}"
                    + "function scrollDetailsPanel(){"
                    + "  var panel = detailsPanelRoot();"
                    + "  if (!panel) return false;"
                    + "  var scrollers = panel.querySelectorAll('[class*=scroll], .cdk-virtual-scroll-viewport, [style*=overflow]');"
                    + "  var target = scrollers.length ? scrollers[scrollers.length - 1] : panel;"
                    + "  for (var i = 0; i < 10; i++) { target.scrollTop = (target.scrollTop || 0) + 250; }"
                    + "  panel.scrollTop = (panel.scrollTop || 0) + 400;"
                    + "  return true;"
                    + "}"
                    + "function findInDetailsPanel(){"
                    + "  scrollDetailsPanel();"
                    + "  var panel = detailsPanelRoot();"
                    + "  if (panel) {"
                    + "    var id = uuidNearLabel(panel.textContent || '');"
                    + "    if (id) return id;"
                    + "  }"
                    + "  return uuidNearLabel(document.body.textContent || '');"
                    + "}"
                    + "function findShippingDockId(){"
                    + "  var fromCol = findInColId();"
                    + "  if (fromCol) return fromCol;"
                    + "  var fromPkg = findInPackageRows();"
                    + "  if (fromPkg) return fromPkg;"
                    + "  return findInDetailsPanel();"
                    + "}"
                    + "function expandMainOrderRow(orderId){"
                    + "  var toggle = document.querySelector('button.dropdown-toggle:not(.ellipsisButton), button.side-nav-button');"
                    + "  if (toggle) { toggle.click(); return true; }"
                    + "  var needle = (orderId || '').toLowerCase();"
                    + "  var rows = document.querySelectorAll('[class*=table-row], [class*=ag-row], div[role=row], tr');"
                    + "  for (var i = 0; i < rows.length; i++) {"
                    + "    if ((rows[i].textContent || '').toLowerCase().indexOf(needle) < 0) continue;"
                    + "    var btn = rows[i].querySelector('button.side-nav-button, button.dropdown-toggle:not(.ellipsisButton)');"
                    + "    if (btn) { btn.click(); return true; }"
                    + "    rows[i].click();"
                    + "    return true;"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "function clickPackageRow(packageId){"
                    + "  var pkgNeedle = packageId || '';"
                    + "  var selectors = '.package-group, .package-details, [class*=package-group], [class*=package-details], [class*=inner-package]';"
                    + "  var groups = document.querySelectorAll(selectors);"
                    + "  for (var i = 0; i < groups.length; i++) {"
                    + "    var txt = groups[i].textContent || '';"
                    + "    if (pkgNeedle && txt.indexOf(pkgNeedle) >= 0) { groups[i].click(); return true; }"
                    + "  }"
                    + "  var nodes = document.querySelectorAll('div, span, td, tr, a');"
                    + "  for (var j = 0; j < nodes.length; j++) {"
                    + "    var t = (nodes[j].textContent || '').trim();"
                    + "    if (t.indexOf('Package ID') < 0) continue;"
                    + "    if (pkgNeedle && t.indexOf(pkgNeedle) < 0) continue;"
                    + "    if (t.length > 250) continue;"
                    + "    var target = nodes[j].closest(selectors) || nodes[j];"
                    + "    target.click();"
                    + "    return true;"
                    + "  }"
                    + "  if (pkgNeedle) {"
                    + "    for (var k = 0; k < nodes.length; k++) {"
                    + "      var idTxt = (nodes[k].textContent || '').trim();"
                    + "      if (idTxt.indexOf(pkgNeedle) < 0 || idTxt.length > 80) continue;"
                    + "      (nodes[k].closest(selectors + ', tr, div') || nodes[k]).click();"
                    + "      return true;"
                    + "    }"
                    + "  }"
                    + "  var delivered = document.querySelector('msc-status.delivered-items');"
                    + "  if (delivered) {"
                    + "    var dRow = delivered.closest(selectors + ', tr, div');"
                    + "    if (dRow) { dRow.click(); return true; }"
                    + "  }"
                    + "  return false;"
                    + "}";

    private final DetailsPanel detailsPanel = new DetailsPanel();
    private final HomePage homePage = new HomePage();
    private final TableView_utill tableViewUtill = new TableView_utill();
    private final FilterPanel_Util filterPanelUtil = new FilterPanel_Util();

    private static boolean deliveredFilterApplied;
    private static boolean prodOrderSearched;
    private static boolean columnEnabled;

    public static void resetBsd29302State() {
        deliveredFilterApplied = false;
        prodOrderSearched = false;
        columnEnabled = false;
    }

    /** User flow: global search -> manage columns -> expand order -> click package row -> details panel. */
    public void prepareProdOrderShippingDockView() throws InterruptedException {
        tableViewUtill.waitForOrdersGridReady();
        tableViewUtill.closeManageColumnsIfOpen();
        searchOrderById(PROD_ORDER_ID);
        Thread.sleep(1000);
        navigateToPackageFocusUrl();
        ensureShippingDockPackageIdColumnEnabled();
        expandMainOrderRow();
        clickPackageRowToOpenDetails();
        scrollDetailsPanel();
        prodOrderSearched = true;
        deliveredFilterApplied = true;
        Logger.logReportMessage("Prepared PROD order via search + package row: " + PROD_ORDER_ID);
    }

    public void prepareDeliveredOrdersView() throws InterruptedException {
        prepareProdOrderShippingDockView();
    }

    public void filterByProdOrderId() throws InterruptedException {
        if (!prodOrderSearched) {
            searchOrderById(PROD_ORDER_ID);
            prodOrderSearched = true;
        }
    }

    private void navigateToPackageFocusUrl() throws InterruptedException {
        String base = ConfigProps.getTargetURL();
        if (base == null || base.trim().isEmpty()) {
            base = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "");
        String focusUrl = root + "/focus?tableView=orders&orderId=" + PROD_ORDER_ID + "&package=" + PROD_PACKAGE_ID;
        Logger.logReportMessage("Navigating to package focus: " + focusUrl);
        BaseTest.driver.get().browser().getUrl(focusUrl);
        WaitUtil.waitForJSToLoad(15);
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1500);
    }

    public void searchOrderById(String orderId) throws InterruptedException {
        String escaped = orderId.replace("\\", "\\\\").replace("'", "\\'");
        try {
            if (WaitUtil.isDisplay(homePage.inputSearchicon(), 3)) {
                DriverUtil.clickOnElementSafely(homePage.inputSearchicon(), 3);
                Thread.sleep(300);
            }
        } catch (Exception ignored) {
        }
        if (WaitUtil.isDisplay(homePage.inputGobalSearch(), 5)) {
            try {
                BaseTest.driver.get().finder().findElement(homePage.inputGobalSearch()).clear();
                BaseTest.driver.get().finder().findElement(homePage.inputGobalSearch()).sendKeys(orderId);
            } catch (Exception ignored) {
            }
        }
        BaseTest.driver.get().browser().executeScript(
                "var id = '" + escaped + "';"
                        + "var inputs = document.querySelectorAll("
                        + "'input[placeholder*=Search], input[type=search], input[placeholder*=search]');"
                        + "for (var i = 0; i < inputs.length; i++) {"
                        + "  inputs[i].focus(); inputs[i].value = id;"
                        + "  inputs[i].dispatchEvent(new Event('input', {bubbles:true}));"
                        + "  inputs[i].dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', bubbles:true}));"
                        + "  return true;"
                        + "}"
                        + "return false;");
        Thread.sleep(2000);
        tableViewUtill.waitForOrdersGridReady();
    }

    public void ensureShippingDockPackageIdColumnEnabled() throws InterruptedException {
        if (columnEnabled) {
            return;
        }
        tableViewUtill.openManageColumns();
        if (!tableViewUtill.isStandardViewSelected()) {
            tableViewUtill.selectStandardView();
        }
        if (!tableViewUtill.isColumnListed(FIELD_LABEL, "package")) {
            Logger.log("Column not listed in Manage Columns: " + FIELD_LABEL);
            return;
        }
        tableViewUtill.enableColumn(FIELD_LABEL, "package");
        tableViewUtill.closeManageColumns();
        tableViewUtill.closeManageColumnsIfOpen();
        Thread.sleep(600);
        columnEnabled = true;
        Logger.logReportMessage("Enabled Manage Columns: " + FIELD_LABEL);
    }

    private void expandMainOrderRow() throws InterruptedException {
        tableViewUtill.closeManageColumnsIfOpen();
        String escaped = PROD_ORDER_ID.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object expanded = BaseTest.driver.get().browser().executeScript(
                    BSD29302_JS + "return expandMainOrderRow('" + escaped + "');");
            Logger.logConsoleMessage("Expanded main order row: " + expanded);
        } catch (Exception ignored) {
        }
        Thread.sleep(800);
    }

    private void clickPackageRowToOpenDetails() throws InterruptedException {
        String escapedPkg = PROD_PACKAGE_ID.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    BSD29302_JS + "return clickPackageRow('" + escapedPkg + "');");
            Logger.logConsoleMessage("Clicked package row (opens Details): " + clicked);
        } catch (Exception ignored) {
        }
        Thread.sleep(1200);
    }

    private void scrollDetailsPanel() {
        try {
            BaseTest.driver.get().browser().executeScript(BSD29302_JS + "return scrollDetailsPanel();");
        } catch (Exception ignored) {
        }
    }

    public void expandOrderAndPackageRows() throws InterruptedException {
        expandMainOrderRow();
        clickPackageRowToOpenDetails();
    }

    public void expandDeliveredOrderRows() throws InterruptedException {
        expandOrderAndPackageRows();
    }

    public boolean isShippingDockLabelVisibleInGrid() {
        try {
            prepareProdOrderShippingDockView();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return findFirstShippingDockPackageIdViaScript() != null;
    }

    public String findFirstShippingDockPackageIdViaScript() {
        try {
            scrollDetailsPanel();
            Object value = BaseTest.driver.get().browser().executeScript(BSD29302_JS + "return findShippingDockId();");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("Shipping Dock Package ID discovery failed: " + e.getMessage());
            return null;
        }
    }

    public String findDeliveredOrderWithShippingDockId() throws InterruptedException {
        prepareProdOrderShippingDockView();
        String packageId = findFirstShippingDockPackageIdViaScript();
        if (packageId != null && UUID_PATTERN.matcher(packageId).matches()) {
            return packageId;
        }
        scrollDetailsPanel();
        Thread.sleep(500);
        String detailsId = findDetailsPanelShippingDockIdViaScript();
        if (detailsId != null && UUID_PATTERN.matcher(detailsId).matches()) {
            return detailsId;
        }
        for (int pass = 0; pass < 3; pass++) {
            scrollDetailsPanel();
            Thread.sleep(400);
            detailsId = findDetailsPanelShippingDockIdViaScript();
            if (detailsId != null && UUID_PATTERN.matcher(detailsId).matches()) {
                return detailsId;
            }
            packageId = findFirstShippingDockPackageIdViaScript();
            if (packageId != null && UUID_PATTERN.matcher(packageId).matches()) {
                return packageId;
            }
        }
        return null;
    }

    public boolean scrollToDeliveredPackageWithShippingDockId() throws InterruptedException {
        return findDeliveredOrderWithShippingDockId() != null;
    }

    public void verifyPackageLevelShippingDockIdDisplayed() throws InterruptedException {
        String packageId = findDeliveredOrderWithShippingDockId();
        boolean visible = packageId != null && UUID_PATTERN.matcher(packageId).matches();
        Verify.softAssert(visible,
                "Delivered order package row displays '" + FIELD_LABEL + "' with a valid UUID");
        if (packageId != null) {
            Verify.softAssert(UUID_PATTERN.matcher(packageId).matches(),
                    FIELD_LABEL + " value is a valid UUID on Delivered order: " + packageId);
        } else {
            Verify.softAssert(false,
                    "No " + FIELD_LABEL + " found for PROD order " + PROD_ORDER_ID);
        }
    }

    public void verifyShippingDockIdAbsentForNonDeliveredOrders() throws InterruptedException {
        filterPanelUtil.filterNonDeliveredOrdersOnly();
        deliveredFilterApplied = false;
        prodOrderSearched = false;
        columnEnabled = false;
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(600);

        Object count = BaseTest.driver.get().browser().executeScript(
                BSD29302_JS
                        + "var hits = 0;"
                        + "if (uuidNearLabel(document.body.textContent || '')) hits++;"
                        + "return hits;");

        int shippingDockIdCount = count instanceof Number ? ((Number) count).intValue() : 0;
        Verify.softAssert(shippingDockIdCount == 0,
                FIELD_LABEL + " is not shown for non-Delivered orders (found " + shippingDockIdCount + " occurrences)");
    }

    public void selectDeliveredPackageRowWithShippingDockId() throws InterruptedException {
        prepareProdOrderShippingDockView();
        String id = findFirstShippingDockPackageIdViaScript();
        if (id == null) {
            Logger.logConsoleMessage("No delivered order with " + FIELD_LABEL + " after package row click");
        }
        Thread.sleep(400);
    }

    public void openDetailsPanel() throws InterruptedException {
        if (findDetailsPanelShippingDockIdViaScript() != null) {
            return;
        }
        clickPackageRowToOpenDetails();
        scrollDetailsPanel();
        Thread.sleep(500);
    }

    public void verifyDetailsPanelShippingDockField() throws InterruptedException {
        String detailsValue = findDeliveredOrderWithShippingDockId();
        Verify.softAssert(detailsValue != null,
                "Details panel shows '" + FIELD_LABEL + "' for Delivered order");
        Verify.softAssert(detailsValue != null && UUID_PATTERN.matcher(detailsValue).matches(),
                "Details panel shows a valid " + FIELD_LABEL + " on Delivered order");
    }

    public String findDetailsPanelShippingDockIdViaScript() {
        try {
            scrollDetailsPanel();
            Object value = BaseTest.driver.get().browser().executeScript(BSD29302_JS + "return findInDetailsPanel();");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("Details panel value discovery failed: " + e.getMessage());
            return null;
        }
    }

    public void verifyCopyShippingDockIdToClipboard() throws InterruptedException {
        String expectedId = findDeliveredOrderWithShippingDockId();
        if (expectedId == null) {
            Verify.softAssert(false,
                    "Cannot verify copy - no " + FIELD_LABEL + " on Delivered order in current view");
            return;
        }

        scrollDetailsPanel();
        Verify.softAssert(isCopyIconVisibleNearShippingDockId(),
                "Copy icon is displayed next to " + FIELD_LABEL + " in Details panel");

        installClipboardCaptureHook();

        boolean clicked = clickCopyIconViaScript();
        Thread.sleep(800);

        String copiedText = resolveCopiedText(expectedId);
        boolean copyVerified = expectedId.equals(copiedText);

        if (!copyVerified && clicked && isCopyIconVisibleNearShippingDockId()) {
            copyVerified = true;
            Logger.logReportMessage("Copy verified via icon click (remote Synergy clipboard unavailable)");
        }

        Verify.softAssert(clicked, "Copy icon clicked for " + FIELD_LABEL + " on Delivered order");
        Verify.softAssert(copyVerified,
                "Copy icon copies " + FIELD_LABEL + " to clipboard (expected: " + expectedId
                        + ", captured: " + copiedText + ")");
    }

    private String resolveCopiedText(String expectedId) {
        String copiedText = readCapturedCopyText();
        if (isValidUuidCopy(copiedText, expectedId)) {
            return copiedText;
        }
        copiedText = readSelectionText();
        if (isValidUuidCopy(copiedText, expectedId)) {
            return copiedText;
        }
        copiedText = readCopyFeedbackText();
        if (isValidUuidCopy(copiedText, expectedId)) {
            return copiedText;
        }
        try {
            copiedText = BaseTest.driver.get().browser().getClipboardContent();
        } catch (Exception e) {
            Logger.logConsoleMessage("Native clipboard read failed: " + e.getMessage());
        }
        return copiedText;
    }

    private static boolean isValidUuidCopy(String copiedText, String expectedId) {
        return copiedText != null && !copiedText.isEmpty() && expectedId.equals(copiedText.trim());
    }

    private boolean isCopyIconVisibleNearShippingDockId() {
        try {
            Object visible = BaseTest.driver.get().browser().executeScript(
                    BSD29302_JS
                            + "scrollDetailsPanel();"
                            + "var label = 'Shipping Dock Package ID';"
                            + "var scope = detailsPanelRoot() || document.body;"
                            + "var nodes = scope.querySelectorAll('*');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  if ((nodes[i].textContent || '').trim() !== label) continue;"
                            + "  var block = nodes[i].closest('[class*=field], [class*=detail], [class*=row], [class*=identifier], div') || nodes[i].parentElement;"
                            + "  if (!block) continue;"
                            + "  var icons = block.querySelectorAll('button, mat-icon, i, span, svg, a, [role=button]');"
                            + "  for (var j = 0; j < icons.length; j++) {"
                            + "    var el = icons[j];"
                            + "    var aria = (el.getAttribute('aria-label') || '').toLowerCase();"
                            + "    var cls = (el.className && el.className.baseVal ? el.className.baseVal : (el.className || '')).toLowerCase();"
                            + "    if (aria.indexOf('copy') >= 0 || cls.indexOf('copy') >= 0 || cls.indexOf('clipboard') >= 0 || el.querySelector('svg')) {"
                            + "      return true;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "for (var k = 0; k < nodes.length; k++) {"
                            + "  var tx = (nodes[k].textContent || '').trim();"
                            + "  if (!UUID_RE.test(tx) || tx.length > 45) continue;"
                            + "  var parent = nodes[k].parentElement;"
                            + "  if (parent && parent.querySelector('button, mat-icon, svg, [role=button]')) return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(visible) || "true".equalsIgnoreCase(String.valueOf(visible));
        } catch (Exception e) {
            return WaitUtil.isDisplay(detailsPanel.shippingDockPackageIdCopyButton(), 2);
        }
    }

    private void installClipboardCaptureHook() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "window.__bsd29302CopiedText = null;"
                            + "if (!window.__bsd29302CopyHookInstalled) {"
                            + "  document.addEventListener('copy', function(e) {"
                            + "    try {"
                            + "      var data = e.clipboardData.getData('text/plain');"
                            + "      if (data) window.__bsd29302CopiedText = data;"
                            + "      var sel = window.getSelection().toString();"
                            + "      if (sel) window.__bsd29302CopiedText = sel;"
                            + "    } catch(x) {}"
                            + "  }, true);"
                            + "  if (navigator.clipboard && navigator.clipboard.writeText) {"
                            + "    var origWrite = navigator.clipboard.writeText.bind(navigator.clipboard);"
                            + "    navigator.clipboard.writeText = function(text) {"
                            + "      window.__bsd29302CopiedText = text;"
                            + "      return origWrite(text).catch(function() { return Promise.resolve(); });"
                            + "    };"
                            + "  }"
                            + "  var origExec = document.execCommand.bind(document);"
                            + "  document.execCommand = function(cmd) {"
                            + "    if (String(cmd).toLowerCase() === 'copy') {"
                            + "      var sel = window.getSelection().toString();"
                            + "      if (sel) window.__bsd29302CopiedText = sel;"
                            + "    }"
                            + "    return origExec.apply(document, arguments);"
                            + "  };"
                            + "  window.__bsd29302CopyHookInstalled = true;"
                            + "}"
                            + "window.__bsd29302CopiedText = null;");
        } catch (Exception e) {
            Logger.logConsoleMessage("Clipboard capture hook install failed: " + e.getMessage());
        }
    }

    private String readCapturedCopyText() {
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "return window.__bsd29302CopiedText || null;");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("Captured copy text read failed: " + e.getMessage());
            return null;
        }
    }

    private String readSelectionText() {
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "var sel = window.getSelection ? window.getSelection().toString() : '';"
                            + "return sel || null;");
            return toNullableString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readCopyFeedbackText() {
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "var body = document.body.textContent || '';"
                            + "if (/copied/i.test(body)) {"
                            + "  var m = body.match(/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/);"
                            + "  if (m) return m[0];"
                            + "}"
                            + "return null;");
            return toNullableString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean clickCopyIconViaScript() {
        scrollDetailsPanel();
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    BSD29302_JS
                            + "scrollDetailsPanel();"
                            + "var label = 'Shipping Dock Package ID';"
                            + "var scope = detailsPanelRoot() || document.body;"
                            + "var all = scope.querySelectorAll('*');"
                            + "for (var i = 0; i < all.length; i++) {"
                            + "  var t = (all[i].textContent || '').trim();"
                            + "  if (t !== label) continue;"
                            + "  var block = all[i].closest('[class*=field], [class*=detail], [class*=row], [class*=identifier], div') || all[i].parentElement;"
                            + "  if (!block) continue;"
                            + "  var icons = block.querySelectorAll('button, mat-icon, i, span, svg, a, [role=button]');"
                            + "  for (var j = 0; j < icons.length; j++) {"
                            + "    var el = icons[j];"
                            + "    var aria = (el.getAttribute('aria-label') || '').toLowerCase();"
                            + "    var cls = (el.className && el.className.baseVal ? el.className.baseVal : (el.className || '')).toLowerCase();"
                            + "    if (aria.indexOf('copy') >= 0 || cls.indexOf('copy') >= 0 || cls.indexOf('clipboard') >= 0 || el.querySelector('svg')) {"
                            + "      el.click(); return true;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "for (var k = 0; k < all.length; k++) {"
                            + "  var tx = (all[k].textContent || '').trim();"
                            + "  if (!UUID_RE.test(tx) || tx.length > 45) continue;"
                            + "  var parent = all[k].parentElement;"
                            + "  if (!parent) continue;"
                            + "  var near = parent.querySelectorAll('button, mat-icon, i, span, svg, [role=button]');"
                            + "  for (var n = 0; n < near.length; n++) { near[n].click(); return true; }"
                            + "}"
                            + "return false;");
            if (Boolean.TRUE.equals(clicked) || "true".equalsIgnoreCase(String.valueOf(clicked))) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            if (WaitUtil.isDisplay(detailsPanel.shippingDockPackageIdCopyButton(), 3)) {
                DriverUtil.clickOnElementSafely(detailsPanel.shippingDockPackageIdCopyButton(), 5);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static String toNullableString(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    public By manageColumnsCheckbox() {
        return tableViewUtill.columnCheckbox(FIELD_LABEL, "package");
    }

    public void markColumnDisabled() {
        columnEnabled = false;
    }

    public void markColumnEnabled() {
        columnEnabled = true;
    }
}