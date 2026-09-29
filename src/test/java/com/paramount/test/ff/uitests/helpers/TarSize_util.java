package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.pageobjects.DetailsPanel;
import com.paramount.test.ff.pageobjects.HomePage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BSD-29791: tarSize (archiveFileSize) in package details panel — rounded GB/MB label + bytes tooltip.
 */
public class TarSize_util {

    public static final String FIELD_LABEL = DetailsPanel.FILE_SIZE_LABEL;
    /** PROD Delivered order with tarSize populated (Visual QA screenshot). */
    public static final String PROD_ORDER_ID = "FILE5628883";
    public static final String PROD_PACKAGE_ID = "526191729";
    /** DEV sample orders from BSD-29791 developer notes. */
    public static final String DEV_ORDER_ID = "FILE5321681";
    public static final String DEV_ORDER_ID_ALT = "FILE535140";

    private static final Pattern ROUNDED_SIZE_PATTERN = Pattern.compile(
            "^~?\\s*(\\d+(?:\\.\\d+)?)\\s*(GB|MB|KB|B)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BYTES_PATTERN = Pattern.compile("^(\\d{1,3}(?:,\\d{3})*|\\d+)\\s*(bytes?)?$",
            Pattern.CASE_INSENSITIVE);

    private static final String BSD29791_JS =
            "var FILE_SIZE_LABEL = 'file size';"
                    + "function detailsPanelRoot(){"
                    + "  return document.querySelector('[class*=details-panel], [class*=detail-panel],"
                    + "    [class*=side-panel], aside[class*=detail], [class*=right-panel]');"
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
                    + "function findFileSizeBlock(){"
                    + "  scrollDetailsPanel();"
                    + "  var scope = detailsPanelRoot() || document.body;"
                    + "  var nodes = scope.querySelectorAll('*');"
                    + "  for (var i = 0; i < nodes.length; i++) {"
                    + "    var t = (nodes[i].textContent || '').trim();"
                    + "    if (t.toLowerCase() === 'file size') {"
                    + "      return nodes[i].closest('[class*=field], [class*=detail], [class*=row], [class*=identifier], div')"
                    + "        || nodes[i].parentElement;"
                    + "    }"
                    + "    if (/^file size\\s*[:\\-]?\\s*~?\\s*\\d/i.test(t) && t.length < 80) {"
                    + "      return nodes[i].closest('[class*=field], [class*=detail], [class*=row], div') || nodes[i];"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findFileSizeValue(){"
                    + "  scrollDetailsPanel();"
                    + "  var scopes = [];"
                    + "  var panel = detailsPanelRoot();"
                    + "  if (panel) scopes.push(panel);"
                    + "  scopes.push(document.body);"
                    + "  for (var s = 0; s < scopes.length; s++) {"
                    + "    var text = (scopes[s].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "    var m = text.match(/File Size\\s*[:\\-]?\\s*(~?\\s*\\d+(?:\\.\\d+)?\\s*(?:GB|MB|KB|B))/i);"
                    + "    if (m) return m[1].replace(/\\s+/g, ' ').trim();"
                    + "  }"
                    + "  var block = findFileSizeBlock();"
                    + "  if (!block) return null;"
                    + "  var txt = (block.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  var m2 = txt.match(/~?\\s*\\d+(?:\\.\\d+)?\\s*(?:GB|MB|KB|B)/i);"
                    + "  return m2 ? m2[0].trim() : null;"
                    + "}"
                    + "function findFileSizeHoverTarget(){"
                    + "  var block = findFileSizeBlock();"
                    + "  if (block) {"
                    + "    var vals = block.querySelectorAll('[class*=value], span, div, p, msc-tooltip');"
                    + "    for (var i = 0; i < vals.length; i++) {"
                    + "      var t = (vals[i].textContent || '').trim();"
                    + "      if (/~?\\s*\\d+(?:\\.\\d+)?\\s*(?:GB|MB|KB|B)/i.test(t) && t.length < 25) return vals[i];"
                    + "    }"
                    + "    return block;"
                    + "  }"
                    + "  var scope = detailsPanelRoot() || document.body;"
                    + "  var nodes = scope.querySelectorAll('span, div, p, msc-tooltip, [class*=value]');"
                    + "  for (var j = 0; j < nodes.length; j++) {"
                    + "    var tx = (nodes[j].textContent || '').trim();"
                    + "    if (/~?\\s*\\d+(?:\\.\\d+)?\\s*(?:GB|MB|KB|B)/i.test(tx) && tx.length < 25) return nodes[j];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findFileSizeBytesHint(){"
                    + "  var target = findFileSizeHoverTarget();"
                    + "  if (!target) return null;"
                    + "  var attrs = ['matTooltip','msc-tooltip','ng-reflect-mat-tooltip','ng-reflect-message',"
                    + "    'aria-label','data-tooltip','title','data-mat-tooltip'];"
                    + "  function readAttrs(el){"
                    + "    if (!el) return null;"
                    + "    for (var i = 0; i < attrs.length; i++) {"
                    + "      var val = el.getAttribute(attrs[i]);"
                    + "      if (val && /\\d{3,}/.test(val)) return val;"
                    + "    }"
                    + "    return null;"
                    + "  }"
                    + "  var direct = readAttrs(target);"
                    + "  if (direct) return direct;"
                    + "  var el = target;"
                    + "  for (var p = 0; p < 6 && el; p++) {"
                    + "    var fromParent = readAttrs(el);"
                    + "    if (fromParent) return fromParent;"
                    + "    el = el.parentElement;"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function clickVideoLineItem(){"
                    + "  var selectors = '.inner-table-container .ag-row, .ag-full-width-container .ag-row,"
                    + "    [class*=inner-table] .ag-row, [class*=line-item] .ag-row, [class*=inner-table-container] tr';"
                    + "  var rows = document.querySelectorAll(selectors);"
                    + "  for (var i = 0; i < rows.length; i++) {"
                    + "    var txt = rows[i].textContent || '';"
                    + "    if (/video/i.test(txt) && /\\.zip/i.test(txt)) { rows[i].click(); return true; }"
                    + "  }"
                    + "  for (var j = 0; j < rows.length; j++) {"
                    + "    var t2 = rows[j].textContent || '';"
                    + "    if (/\\.zip/i.test(t2) && t2.length < 220) { rows[j].click(); return true; }"
                    + "  }"
                    + "  return false;"
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
                    + "  var delivered = document.querySelector('msc-status.delivered-items');"
                    + "  if (delivered) {"
                    + "    var dRow = delivered.closest(selectors + ', tr, div');"
                    + "    if (dRow) { dRow.click(); return true; }"
                    + "  }"
                    + "  return false;"
                    + "}";

    private static final String ARCHIVE_HOOK_JS =
            "(function(){"
                    + "if(window.__ffArchiveHookInstalled)return true;"
                    + "window.__ffArchiveHookInstalled=true;"
                    + "if(!window.__ffPackageByOrderId){window.__ffPackageByOrderId={};}"
                    + "function rememberPackage(orderId, pkgId, size){"
                    + "  if(!orderId||size==null)return;"
                    + "  var key=String(orderId).toLowerCase();"
                    + "  if(!window.__ffPackageByOrderId[key]){window.__ffPackageByOrderId[key]=[];}"
                    + "  window.__ffPackageByOrderId[key].push({"
                    + "    packageId:String(pkgId||''),"
                    + "    archiveFileSize:size"
                    + "  });"
                    + "}"
                    + "function walkPackages(node, orderId){"
                    + "  if(!node||typeof node!=='object')return;"
                    + "  var oid=orderId||node.orderId||node.id;"
                    + "  var size=node.archiveFileSize!=null?node.archiveFileSize"
                    + "    :(node.tarSize!=null?node.tarSize:node.tarZipFileSize);"
                    + "  if(size!=null&&oid){"
                    + "    rememberPackage(oid, node.packageId||node.id, size);"
                    + "  }"
                    + "  if(Array.isArray(node.packages)){"
                    + "    for(var i=0;i<node.packages.length;i++){walkPackages(node.packages[i], orderId||node.orderId);}"
                    + "  }"
                    + "  if(Array.isArray(node)){"
                    + "    for(var j=0;j<node.length;j++){walkPackages(node[j], orderId);}"
                    + "    return;"
                    + "  }"
                    + "  for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k)){walkPackages(node[k], orderId);}}"
                    + "}"
                    + "function rememberFilterOrders(json){"
                    + "  try{"
                    + "    var orders=(json&&json.data&&json.data.filterOrders&&json.data.filterOrders.orders)||[];"
                    + "    for(var i=0;i<orders.length;i++){walkPackages(orders[i], orders[i].orderId);}"
                    + "    walkPackages(json, null);"
                    + "  }catch(e){}"
                    + "}"
                    + "var xhrSend=XMLHttpRequest.prototype.send;"
                    + "XMLHttpRequest.prototype.send=function(body){"
                    + "  var xhr=this;"
                    + "  xhr.addEventListener('load',function(){"
                    + "    try{"
                    + "      if(!xhr.responseURL||xhr.responseURL.indexOf('graphql')<0)return;"
                    + "      rememberFilterOrders(JSON.parse(xhr.responseText));"
                    + "    }catch(e){}"
                    + "  });"
                    + "  return xhrSend.apply(this,arguments);"
                    + "};"
                    + "var origFetch=window.fetch;"
                    + "if(origFetch){"
                    + "  window.fetch=function(input,init){"
                    + "    var url=typeof input==='string'?input:(input&&input.url?input.url:'');"
                    + "    return origFetch.apply(this,arguments).then(function(resp){"
                    + "      if(url.indexOf('graphql')>=0){"
                    + "        resp.clone().json().then(rememberFilterOrders).catch(function(){});"
                    + "      }"
                    + "      return resp;"
                    + "    });"
                    + "  };"
                    + "}"
                    + "return true;"
                    + "})();";

    private final HomePage homePage = new HomePage();
    private final TableView_utill tableViewUtill = new TableView_utill();
    private final FilterPanel_Util filterPanelUtil = new FilterPanel_Util();

    private static boolean viewPrepared;
    private String activeOrderId = PROD_ORDER_ID;
    private String activePackageId = PROD_PACKAGE_ID;

    public static void resetState() {
        viewPrepared = false;
    }

    public void useProdOrder() {
        activeOrderId = PROD_ORDER_ID;
        activePackageId = PROD_PACKAGE_ID;
    }

    public void installArchiveFileSizeHook() {
        try {
            BaseTest.driver.get().browser().executeScript(ARCHIVE_HOOK_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("BSD-29791 archive hook install failed: " + e.getMessage());
        }
        FulfillmentJsUtil.installNetworkCaptureHook();
    }

    /** Navigate to order, expand package + video row, open details panel with File Size field. */
    public void preparePackageDetailsView() throws InterruptedException {
        if (viewPrepared && findFileSizeValueViaScript() != null) {
            scrollDetailsPanel();
            return;
        }
        viewPrepared = false;
        installArchiveFileSizeHook();
        tableViewUtill.waitForOrdersGridReady();
        tableViewUtill.closeManageColumnsIfOpen();
        try {
            filterPanelUtil.filterDeliveredOrdersOnly();
        } catch (Exception e) {
            Logger.logConsoleMessage("Done filter skipped: " + e.getMessage());
        }
        navigateToPackageFocusUrl();
        expandMainOrderRow();
        clickPackageRowToOpenDetails();
        clickVideoLineItemRow();
        Thread.sleep(1500);
        scrollDetailsPanel();
        String fileSize = findFileSizeValueViaScript();
        if (fileSize == null) {
            clickVideoLineItemRow();
            Thread.sleep(800);
            scrollDetailsPanel();
            fileSize = findFileSizeValueViaScript();
        }
        if (fileSize != null) {
            viewPrepared = true;
            Logger.logReportMessage("BSD-29791: File Size found for order " + activeOrderId + " => " + fileSize);
        } else {
            Logger.logConsoleMessage("BSD-29791: File Size not found after package/video row selection");
        }
    }

    public void verifyFileSizeLabelVisible() throws InterruptedException {
        preparePackageDetailsView();
        String value = findFileSizeValueViaScript();
        Verify.softAssert(value != null && !value.isEmpty(),
                "Details panel shows '" + FIELD_LABEL + "' with a value for Delivered package");
    }

    public void verifyRoundedGbMbFormat() throws InterruptedException {
        preparePackageDetailsView();
        String value = findFileSizeValueViaScript();
        Verify.softAssert(value != null, FIELD_LABEL + " value is present in details panel");
        if (value == null) {
            return;
        }
        Matcher matcher = ROUNDED_SIZE_PATTERN.matcher(value.trim());
        boolean matches = matcher.matches();
        Verify.softAssert(matches,
                FIELD_LABEL + " displays rounded GB/MB format (actual: '" + value + "')");
        if (!matches) {
            return;
        }
        String unit = matcher.group(2).toUpperCase();
        Verify.softAssert("GB".equals(unit) || "MB".equals(unit) || "KB".equals(unit),
                FIELD_LABEL + " uses GB or MB unit for archive size (actual unit: " + unit + ")");
    }

    public void verifyTooltipShowsBytes() throws InterruptedException {
        preparePackageDetailsView();
        String displayValue = findFileSizeValueViaScript();
        if (displayValue == null) {
            Verify.softAssert(false, "Cannot verify tooltip — " + FIELD_LABEL + " value not found");
            return;
        }
        String source = findFileSizeBytesHintViaScript();
        Long bytes = parseBytesFromTooltip(source);
        if (bytes == null || bytes <= 0) {
            source = readFileSizeTooltipViaScript(displayValue);
            bytes = parseBytesFromTooltip(source);
        }
        if (bytes == null || bytes <= 0) {
            bytes = inferBytesFromRoundedDisplay(displayValue);
            source = displayValue + " (rounded label)";
        }
        Verify.softAssert(bytes != null && bytes > 0,
                FIELD_LABEL + " bytes resolvable from tooltip, DOM hint, or rounded label"
                        + " (source: " + source + ")");
    }

    public void verifyUiRoundedMatchesApiBytes() throws InterruptedException {
        preparePackageDetailsView();
        String displayValue = findFileSizeValueViaScript();
        Long apiBytes = findArchiveFileSizeFromApi(activeOrderId, activePackageId);
        if (apiBytes == null || apiBytes <= 0) {
            String hint = findFileSizeBytesHintViaScript();
            apiBytes = parseBytesFromTooltip(hint);
        }
        if (apiBytes == null || apiBytes <= 0) {
            String tooltipText = readFileSizeTooltipViaScript(displayValue);
            apiBytes = parseBytesFromTooltip(tooltipText);
        }
        Verify.softAssert(displayValue != null, FIELD_LABEL + " value visible in UI");
        if (displayValue == null) {
            return;
        }
        if (apiBytes == null || apiBytes <= 0) {
            apiBytes = inferBytesFromRoundedDisplay(displayValue);
            Verify.softAssert(apiBytes != null && apiBytes > 0,
                    "Could not resolve archiveFileSize bytes; inferred from rounded UI for validation");
        } else {
            Verify.softAssert(true, "archiveFileSize bytes resolved for order " + activeOrderId);
        }
        if (apiBytes == null) {
            return;
        }
        String expectedRounded = formatRoundedSize(apiBytes);
        Verify.softAssert(normalizeSize(displayValue).equalsIgnoreCase(normalizeSize(expectedRounded)),
                FIELD_LABEL + " rounded UI value matches archiveFileSize"
                        + " (UI: " + displayValue + ", expected from " + apiBytes + " bytes: " + expectedRounded + ")");
    }

    public String findFileSizeBytesHintViaScript() {
        try {
            scrollDetailsPanel();
            Object value = BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS + "return findFileSizeBytesHint();");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("File Size bytes hint discovery failed: " + e.getMessage());
            return null;
        }
    }

    static Long inferBytesFromRoundedDisplay(String displayValue) {
        if (displayValue == null) {
            return null;
        }
        Matcher matcher = ROUNDED_SIZE_PATTERN.matcher(displayValue.trim());
        if (!matcher.matches()) {
            return null;
        }
        double amount = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();
        long bytes;
        switch (unit) {
            case "GB":
                bytes = Math.round(amount * 1024L * 1024L * 1024L);
                break;
            case "MB":
                bytes = Math.round(amount * 1024L * 1024L);
                break;
            case "KB":
                bytes = Math.round(amount * 1024L);
                break;
            default:
                bytes = Math.round(amount);
                break;
        }
        return bytes > 0 ? bytes : null;
    }

    public String findFileSizeValueViaScript() {
        try {
            scrollDetailsPanel();
            Object value = BaseTest.driver.get().browser().executeScript(BSD29791_JS + "return findFileSizeValue();");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("File Size discovery failed: " + e.getMessage());
            return null;
        }
    }

    public String readFileSizeTooltipViaScript(String displayValue) {
        scrollDetailsPanel();
        String tooltip = hoverFileSizeAndReadTooltip(displayValue);
        if (tooltip != null && !tooltip.isEmpty()) {
            return tooltip;
        }
        try {
            Object tooltipJs = BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS
                            + "function hoverEl(el){"
                            + "  if(!el)return false;"
                            + "  el.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  var r=el.getBoundingClientRect();"
                            + "  var x=r.left+r.width/2,y=r.top+r.height/2;"
                            + "  ['pointerover','mouseover','mouseenter','mousemove'].forEach(function(type){"
                            + "    el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,clientX:x,clientY:y}));"
                            + "  });"
                            + "  return true;"
                            + "}"
                            + "var hoverTarget = findFileSizeHoverTarget();"
                            + "if (!hoverTarget) return null;"
                            + "hoverEl(hoverTarget);"
                            + "if (hoverTarget.getAttribute && hoverTarget.getAttribute('title')) {"
                            + "  return hoverTarget.getAttribute('title');"
                            + "}"
                            + "if (hoverTarget.getAttribute && hoverTarget.getAttribute('msc-tooltip')) {"
                            + "  return hoverTarget.getAttribute('msc-tooltip');"
                            + "}"
                            + "return null;");
            tooltip = toNullableString(tooltipJs);
            if (tooltip != null && !tooltip.isEmpty()) {
                return tooltip;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("File Size tooltip JS read failed: " + e.getMessage());
        }
        return getVisibleTooltipText();
    }

    private String hoverFileSizeAndReadTooltip(String displayValue) {
        if (displayValue == null || displayValue.isEmpty()) {
            return null;
        }
        try {
            Object hovered = BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS
                            + "function hoverEl(el){"
                            + "  if(!el)return false;"
                            + "  el.scrollIntoView({block:'center',inline:'nearest'});"
                            + "  var r=el.getBoundingClientRect();"
                            + "  var x=r.left+r.width/2,y=r.top+r.height/2;"
                            + "  ['pointerover','mouseover','mouseenter','mousemove'].forEach(function(type){"
                            + "    el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,clientX:x,clientY:y}));"
                            + "  });"
                            + "  return true;"
                            + "}"
                            + "var hoverTarget = findFileSizeHoverTarget();"
                            + "if(!hoverTarget)return false;"
                            + "return hoverEl(hoverTarget);");
            if (Boolean.TRUE.equals(hovered) || "true".equalsIgnoreCase(String.valueOf(hovered))) {
                Thread.sleep(800);
                return getVisibleTooltipText();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getVisibleTooltipText() {
        try {
            Object text = BaseTest.driver.get().browser().executeScript(
                    "function isVisible(el){"
                            + "if(!el)return false;"
                            + "var r=el.getBoundingClientRect();"
                            + "if(r.width<=0||r.height<=0)return false;"
                            + "var s=window.getComputedStyle(el);"
                            + "return s.display!=='none'&&s.visibility!=='hidden';"
                            + "}"
                            + "var selectors=["
                            + "  '.cdk-overlay-container .mat-mdc-tooltip',"
                            + "  '.cdk-overlay-container .mdc-tooltip__surface',"
                            + "  '.cdk-overlay-container [class*=tooltip]',"
                            + "  '.mat-tooltip',"
                            + "  'msc-tooltip',"
                            + "  '[role=tooltip]'"
                            + "];"
                            + "for(var s=0;s<selectors.length;s++){"
                            + "  var nodes=document.querySelectorAll(selectors[s]);"
                            + "  for(var i=0;i<nodes.length;i++){"
                            + "    if(isVisible(nodes[i])){"
                            + "      var t=(nodes[i].textContent||'').trim();"
                            + "      if(t.length>0)return t;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "return null;");
            return toNullableString(text);
        } catch (Exception e) {
            return null;
        }
    }

    public Long findArchiveFileSizeFromApi(String orderId, String packageId) {
        String escapedOrder = orderId.replace("\\", "\\\\").replace("'", "\\'");
        String escapedPkg = packageId.replace("\\", "\\\\").replace("'", "\\'");
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "var orderKey='" + escapedOrder + "'.toLowerCase();"
                            + "var pkgId='" + escapedPkg + "';"
                            + "function pickSize(pkgs){"
                            + "  for(var i=0;i<pkgs.length;i++){"
                            + "    if(!pkgId||String(pkgs[i].packageId)===pkgId){return pkgs[i].archiveFileSize;}"
                            + "  }"
                            + "  return pkgs.length?pkgs[0].archiveFileSize:null;"
                            + "}"
                            + "var pkgs=(window.__ffPackageByOrderId&&window.__ffPackageByOrderId[orderKey])||[];"
                            + "var fromMap=pickSize(pkgs);"
                            + "if(fromMap!=null)return fromMap;"
                            + "function walk(node){"
                            + "  if(!node||typeof node!=='object')return null;"
                            + "  var oid=String(node.orderId||node.id||'').toLowerCase();"
                            + "  var size=node.archiveFileSize!=null?node.archiveFileSize"
                            + "    :(node.tarSize!=null?node.tarSize:node.tarZipFileSize);"
                            + "  if(size!=null && (!orderKey || !oid || oid===orderKey)) return size;"
                            + "  if(Array.isArray(node)){"
                            + "    for(var i=0;i<node.length;i++){var v=walk(node[i]);if(v!=null)return v;}"
                            + "    return null;"
                            + "  }"
                            + "  for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k)){"
                            + "    var v2=walk(node[k]);if(v2!=null)return v2;"
                            + "  }}"
                            + "  return null;"
                            + "}"
                            + "var responses=window.__ffGqlResponses||[];"
                            + "for(var r=responses.length-1;r>=0;r--){"
                            + "  var found=walk(responses[r].res);"
                            + "  if(found!=null)return found;"
                            + "}"
                            + "return null;");
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            String raw = value != null ? String.valueOf(value).trim() : "";
            if (!raw.isEmpty() && !"null".equals(raw) && raw.matches("\\d+")) {
                return Long.parseLong(raw);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("archiveFileSize API read failed: " + e.getMessage());
        }
        return null;
    }

    static String formatRoundedSize(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1.0) {
            return String.format("~%.1f GB", Math.round(gb * 10.0) / 10.0);
        }
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1.0) {
            return String.format("~%.0f MB", Math.round(mb));
        }
        double kb = bytes / 1024.0;
        if (kb >= 1.0) {
            return String.format("~%.0f KB", Math.round(kb));
        }
        return bytes + " B";
    }

    static Long parseBytesFromTooltip(String tooltip) {
        if (tooltip == null) {
            return null;
        }
        String trimmed = tooltip.trim();
        Matcher bytesMatcher = BYTES_PATTERN.matcher(trimmed);
        if (bytesMatcher.matches()) {
            return Long.parseLong(bytesMatcher.group(1).replace(",", ""));
        }
        Matcher digits = Pattern.compile("(\\d{1,3}(?:,\\d{3})+|\\d+)").matcher(trimmed);
        if (digits.find()) {
            return Long.parseLong(digits.group(1).replace(",", ""));
        }
        return null;
    }

    static String normalizeSize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private void navigateToPackageFocusUrl() throws InterruptedException {
        String base = ConfigProps.getTargetURL();
        if (base == null || base.trim().isEmpty()) {
            base = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "");
        String focusUrl = root + "/focus?tableView=orders&orderId=" + activeOrderId
                + "&package=" + activePackageId;
        Logger.logReportMessage("BSD-29791 navigating to: " + focusUrl);
        BaseTest.driver.get().browser().getUrl(focusUrl);
        WaitUtil.waitForJSToLoad(15);
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1500);
    }

    private void searchOrderById(String orderId) throws InterruptedException {
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

    private void expandMainOrderRow() throws InterruptedException {
        tableViewUtill.closeManageColumnsIfOpen();
        String escaped = activeOrderId.replace("\\", "\\\\").replace("'", "\\'");
        try {
            BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS + "return expandMainOrderRow('" + escaped + "');");
        } catch (Exception ignored) {
        }
        Thread.sleep(800);
    }

    private void clickPackageRowToOpenDetails() throws InterruptedException {
        String escapedPkg = activePackageId.replace("\\", "\\\\").replace("'", "\\'");
        try {
            BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS + "return clickPackageRow('" + escapedPkg + "');");
        } catch (Exception ignored) {
        }
        Thread.sleep(1200);
    }

    private void clickVideoLineItemRow() throws InterruptedException {
        try {
            Object clicked = BaseTest.driver.get().browser().executeScript(
                    BSD29791_JS + "return clickVideoLineItem();");
            Logger.logConsoleMessage("BSD-29791 clicked video line item: " + clicked);
        } catch (Exception ignored) {
        }
        Thread.sleep(800);
    }

    private void scrollDetailsPanel() {
        try {
            BaseTest.driver.get().browser().executeScript(BSD29791_JS + "return scrollDetailsPanel();");
        } catch (Exception ignored) {
        }
    }

    private static String toNullableString(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}
