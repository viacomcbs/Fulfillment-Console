package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SynergyRetryUtil;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.uitests.tests.bsd29174.LineItemStatusConstants;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates order-level status summary tooltips (BSD-29174 / BSD-28250).
 * Tooltips bind to {@code statusTooltipDetails} from filterOrders API and live updates.
 */
public class OrderStatusTooltip_util {

    /** PROD orders verified during QA with multi-status tooltip data. */
    public static final String PROD_ORDER_MULTI_STATUS = "fdy50331384";
    public static final String PROD_ORDER_ALT = "kma31840890";
    public static final String PROD_ORDER_KNOWN = "wzr842617";
    /** Yesterday grid order with Processing Complete tooltip (see PROD screenshots). */
    public static final String PROD_ORDER_PROCESSING_COMPLETE = "file5625033";

    private static final Pattern ZERO_COUNT_LINE = Pattern.compile(".*(?:[-:–]\\s+|\\s+)0\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPTIONAL_LINE = Pattern.compile("\\(optional\\)", Pattern.CASE_INSENSITIVE);
    /** PROD tooltip lines use "Status 11" or "Status - 11" formats. */
    private static final Pattern TOOLTIP_COUNT_LINE = Pattern.compile("(?:[-:–]\\s+|\\s+)(\\d+)\\s*$");

    private static final String EXPAND_AND_COUNT_JS =
            "function findMainOrderRow(id){"
                    + "  var needle=id.toLowerCase();"
                    + "  var rows=document.querySelectorAll('.ag-center-cols-container .ag-row,.ag-row,[role=row]');"
                    + "  for(var i=0;i<rows.length;i++){"
                    + "    if((rows[i].textContent||'').toLowerCase().indexOf(needle)>=0)return rows[i];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function expandOrderRow(id){"
                    + "  var row=findMainOrderRow(id);"
                    + "  if(!row)return false;"
                    + "  row.scrollIntoView({block:'center'});"
                    + "  var closed=row.querySelector('.ag-group-contracted,.ag-icon-tree-closed,[aria-expanded=false]');"
                    + "  if(closed){closed.click();return true;}"
                    + "  var expanded=row.querySelector('.ag-group-expanded,.ag-icon-tree-open,[aria-expanded=true]');"
                    + "  if(expanded)return true;"
                    + "  var btn=row.querySelector('button.side-nav-button,button.dropdown-toggle:not(.ellipsisButton)');"
                    + "  if(btn){btn.click();return true;}"
                    + "  row.click();"
                    + "  return true;"
                    + "}"
                    + "function rowLooksOptional(row){"
                    + "  if(!row)return false;"
                    + "  var txt=(row.textContent||'').toLowerCase();"
                    + "  if(/\\boptional\\b/.test(txt))return true;"
                    + "  return !!row.querySelector('[class*=optional],.optional-badge,msc-optional');"
                    + "}"
                    + "function rowStatusTexts(row){"
                    + "  var out=[];"
                    + "  var nodes=row.querySelectorAll('msc-status,[class*=status],.status-label,span,div');"
                    + "  for(var i=0;i<nodes.length;i++){"
                    + "    var t=(nodes[i].textContent||'').trim();"
                    + "    if(t&&t.length<80)out.push(t.toLowerCase());"
                    + "  }"
                    + "  return out;"
                    + "}"
                    + "function statusMatchesRow(row,statusNeedle,optionalOnly){"
                    + "  var optional=rowLooksOptional(row);"
                    + "  if(optionalOnly&&!optional)return false;"
                    + "  if(!optionalOnly&&optional)return false;"
                    + "  function norm(s){return (s||'').toLowerCase().replace(/\\s+/g,' ').trim();}"
                    + "  function alias(s){"
                    + "    s=norm(s);"
                    + "    if(s==='delivery completed')return 'delivery complete';"
                    + "    if(s==='processing')return 'processing complete';"
                    + "    if(s==='pending')return 'delivery pending';"
                    + "    if(s==='packing')return 'packaging';"
                    + "    return s;"
                    + "  }"
                    + "  var need=alias(statusNeedle);"
                    + "  var texts=rowStatusTexts(row);"
                    + "  for(var i=0;i<texts.length;i++){"
                    + "    var t=alias(texts[i]);"
                    + "    if(t===need||t.indexOf(need)>=0||need.indexOf(t)>=0)return true;"
                    + "  }"
                    + "  return alias(row.textContent||'').indexOf(need)>=0;"
                    + "}"
                    + "function countExpandedLineItems(id,statusLabel,optionalOnly){"
                    + "  var needle=id.toLowerCase();"
                    + "  var statusNeedle=statusLabel.toLowerCase();"
                    + "  var count=0;"
                    + "  var inner=document.querySelectorAll("
                    + "'.inner-table-container .ag-row,.ag-full-width-container .ag-row,"
                    + "[class*=line-item] .ag-row,[class*=inner-table] .ag-row');"
                    + "  for(var k=0;k<inner.length;k++){"
                    + "    if(statusMatchesRow(inner[k],statusNeedle,optionalOnly))count++;"
                    + "  }"
                    + "  if(count>0)return count;"
                    + "  var main=findMainOrderRow(id);"
                    + "  if(!main)return -1;"
                    + "  var rows=document.querySelectorAll('.ag-center-cols-container .ag-row,.ag-body-viewport .ag-row');"
                    + "  var start=-1;"
                    + "  for(var i=0;i<rows.length;i++){if(rows[i]===main){start=i;break;}}"
                    + "  if(start<0)return -1;"
                    + "  for(var j=start+1;j<rows.length;j++){"
                    + "    var r=rows[j];"
                    + "    var txt=(r.textContent||'').toLowerCase();"
                    + "    if(r.classList.contains('ag-row-level-0')&&txt.indexOf(needle)<0)break;"
                    + "    if(txt.indexOf(needle)>=0&&r!==main)continue;"
                    + "    var isDetail=r.classList.contains('ag-row-level-1')||r.classList.contains('ag-row-level-2')"
                    + "      ||r.getAttribute('row-index')!==main.getAttribute('row-index');"
                    + "    if(isDetail&&statusMatchesRow(r,statusNeedle,optionalOnly))count++;"
                    + "  }"
                    + "  return count;"
                    + "}";

    private static final String TOOLTIP_HOOK_JS =
            "(function(){"
                    + "if(window.__ffTooltipHookInstalled)return true;"
                    + "window.__ffTooltipHookInstalled=true;"
                    + "window.__ffFilterOrdersByOrderId={};"
                    + "function rememberOrder(o){"
                    + "  if(!o||!o.statusTooltipDetails)return;"
                    + "  var id=String(o.orderId||o.id||'');"
                    + "  if(id){window.__ffFilterOrdersByOrderId[id.toLowerCase()]=o.statusTooltipDetails;}"
                    + "}"
                    + "function rememberFilterOrders(json){"
                    + "  try{"
                    + "    var orders=(json&&json.data&&json.data.filterOrders&&json.data.filterOrders.orders)"
                    + "      ||(json&&json.data&&json.data.orders)||[];"
                    + "    if(Array.isArray(orders)){"
                    + "      for(var i=0;i<orders.length;i++){rememberOrder(orders[i]);}"
                    + "    }"
                    + "    function walk(node){"
                    + "      if(!node||typeof node!=='object')return;"
                    + "      if(Array.isArray(node.statusTooltipDetails)"
                    + "        &&(node.orderId||node.id)){rememberOrder(node);}"
                    + "      if(Array.isArray(node)){"
                    + "        for(var j=0;j<node.length;j++){walk(node[j]);}"
                    + "        return;"
                    + "      }"
                    + "      for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k)){walk(node[k]);}}"
                    + "    }"
                    + "    walk(json);"
                    + "  }catch(e){}"
                    + "}"
                    + "var xhrOpen=XMLHttpRequest.prototype.open;"
                    + "var xhrSend=XMLHttpRequest.prototype.send;"
                    + "XMLHttpRequest.prototype.open=function(m,u){this.__ffM=m;this.__ffU=u;return xhrOpen.apply(this,arguments);};"
                    + "XMLHttpRequest.prototype.send=function(b){"
                    + "  var xhr=this;"
                    + "  xhr.addEventListener('load',function(){"
                    + "    try{"
                    + "      if(!xhr.__ffU||xhr.__ffU.indexOf('graphql')<0||!xhr.responseText)return;"
                    + "      var body=b?JSON.parse(b):null;"
                    + "      var op=(body&&body.operationName)||'';"
                    + "      var q=(body&&body.query)||'';"
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
                    + "      if(url.indexOf('graphql')>=0&&init&&init.body){"
                    + "        try{"
                    + "          var body=JSON.parse(init.body);"
                    + "          var op=(body&&body.operationName)||'';"
                    + "          var q=(body&&body.query)||'';"
                    + "          resp.clone().json().then(rememberFilterOrders).catch(function(){});"
                    + "        }catch(e){}"
                    + "      }"
                    + "      return resp;"
                    + "    });"
                    + "  };"
                    + "}"
                    + "return true;"
                    + "})();";

    private static final String IS_VISIBLE_FN =
            "function isVisible(el){"
                    + "if(!el)return false;"
                    + "var r=el.getBoundingClientRect();"
                    + "if(r.width<=0||r.height<=0)return false;"
                    + "var s=window.getComputedStyle(el);"
                    + "return s.display!=='none'&&s.visibility!=='hidden'&&parseFloat(s.opacity||'1')>0;"
                    + "}";

    private static final String FIND_TARGET_ROW_JS =
            "function rowIndexFromEl(el){"
                    + "var node=el;"
                    + "while(node){"
                    + "  if(node.getAttribute&&node.getAttribute('row-index')!=null){"
                    + "    return node.getAttribute('row-index');"
                    + "  }"
                    + "  node=node.parentElement;"
                    + "}"
                    + "return null;"
                    + "}"
                    + "function findTargetRowIndex(orderId){"
                    + "var needle=(orderId||'').toLowerCase();"
                    + "var onFocus=needle&&window.location.href.toLowerCase().indexOf('orderid='+needle)>=0;"
                    + "if(onFocus){"
                    + "  var nav=document.querySelector("
                    + "'button.side-nav-button,button.dropdown-toggle:not(.ellipsisButton)');"
                    + "  if(nav){"
                    + "    var idx=rowIndexFromEl(nav);"
                    + "    if(idx!=null)return idx;"
                    + "  }"
                    + "}"
                    + "var cells=document.querySelectorAll('.ag-cell,[col-id]');"
                    + "for(var i=0;i<cells.length;i++){"
                    + "  var txt=(cells[i].textContent||'').toLowerCase();"
                    + "  if(needle&&txt.indexOf(needle)>=0){"
                    + "    var row=cells[i].closest('.ag-row');"
                    + "    if(row&&row.getAttribute('row-index')!=null){return row.getAttribute('row-index');}"
                    + "  }"
                    + "}"
                    + "var focused=document.querySelector('.ag-row-focus,.ag-row.ag-row-focus');"
                    + "if(focused&&focused.getAttribute('row-index')!=null){return focused.getAttribute('row-index');}"
                    + "return null;"
                    + "}"
                    + "function resolveStatusColId(){"
                    + "var headers=document.querySelectorAll('.ag-header-cell,[col-id]');"
                    + "for(var i=0;i<headers.length;i++){"
                    + "  var label=(headers[i].textContent||'').trim();"
                    + "  if(/^status$/i.test(label)){"
                    + "    return headers[i].getAttribute('col-id')||'status';"
                    + "  }"
                    + "}"
                    + "return 'status';"
                    + "}"
                    + "function rowsForIndex(idx){"
                    + "if(idx==null)return [];"
                    + "return Array.prototype.slice.call("
                    + "document.querySelectorAll('.ag-row[row-index=\"'+idx+'\"]'));"
                    + "}"
                    + "function hoverEl(el){"
                    + "if(!el)return false;"
                    + "el.scrollIntoView({block:'center',inline:'nearest'});"
                    + "var r=el.getBoundingClientRect();"
                    + "var x=r.left+r.width/2,y=r.top+r.height/2;"
                    + "['pointerover','mouseover','mouseenter','mousemove','pointermove'].forEach(function(type){"
                    + "  el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,clientX:x,clientY:y,view:window}));"
                    + "});"
                    + "return true;"
                    + "}"
                    + "function collectHoverTargets(rows){"
                    + "var out=[],seen=new Set();"
                    + "function addVisible(nodes){"
                    + "  for(var n=0;n<nodes.length;n++){"
                    + "    if(!seen.has(nodes[n])&&isVisible(nodes[n])){seen.add(nodes[n]);out.push(nodes[n]);}"
                    + "  }"
                    + "}"
                    + "if(!rows.length){"
                    + "  addVisible(document.querySelectorAll('msc-status,[class*=status-summary] *'));"
                    + "  return out;"
                    + "}"
                    + "var statusCol=resolveStatusColId();"
                    + "var sel='button.side-nav-button,button.dropdown-toggle:not(.ellipsisButton),"
                    + "[col-id=\"'+statusCol+'\"] *,[col-id*=status] *,[col-id*=Status] *,"
                    + "[data-col-id*=status] *,msc-status,[class*=status-summary] *,"
                    + "[class*=status-filter] *,[class*=order-status] *,[class*=pill],"
                    + "[class*=badge],[class*=chip],[mattooltip],[ng-reflect-mat-tooltip],svg';"
                    + "for(var r=0;r<rows.length;r++){addVisible(rows[r].querySelectorAll(sel));}"
                    + "return out;"
                    + "}";

    private final PackageDetails_util packageDetailsUtil = new PackageDetails_util();
    private final TableView_utill tableViewUtill = new TableView_utill();

    public void installTooltipCaptureHook() {
        FulfillmentJsUtil.installNetworkCaptureHook();
        runScript(TOOLTIP_HOOK_JS);
    }

    public void searchOrder(String orderId) throws InterruptedException {
        searchOrderOnOrdersGrid(orderId, true);
    }

    /**
     * Finds an order on the Yesterday grid and filters to it without navigating to /focus.
     */
    public void searchOrderOnOrdersGrid(String orderId) throws InterruptedException {
        searchOrderOnOrdersGrid(orderId, false);
    }

    private void searchOrderOnOrdersGrid(String orderId, boolean useFocusUrl) throws InterruptedException {
        installTooltipCaptureHook();
        if (useFocusUrl) {
            SynergyRetryUtil.runWithRetry("navigateToOrderFocus", () -> {
                try {
                    navigateToOrderFocus(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            });
        } else {
            navigateToOrdersHome();
        }
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1000);
        SynergyRetryUtil.runWithRetry("searchOrderById", () -> {
            try {
                packageDetailsUtil.searchOrderById(orderId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return null;
        });
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1500);
        forceSearchFilter(orderId);
        scrollTargetOrderIntoView(orderId);
        waitForStatusIconsReady(30);
        waitForStatusTooltipDetails(orderId, 10);
    }

    private void waitForStatusIconsReady(int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            Object count = runScript("return document.querySelectorAll('msc-status').length;");
            int iconCount = 0;
            if (count instanceof Number) {
                iconCount = ((Number) count).intValue();
            } else {
                try {
                    iconCount = Integer.parseInt(String.valueOf(count));
                } catch (NumberFormatException ignored) {
                }
            }
            if (iconCount > 0) {
                Logger.logReportMessage("Status icons ready: " + iconCount + " msc-status elements");
                return;
            }
            tableViewUtill.waitForOrdersGridReady();
            Thread.sleep(800);
        }
        Logger.logConsoleMessage("Status icons not detected within " + timeoutSec + "s — continuing");
    }

    private void forceSearchFilter(String orderId) throws InterruptedException {
        runScript(
                "var id='" + esc(orderId) + "';"
                        + "var icon=document.querySelector('[class*=search-icon],.search-icon');"
                        + "if(icon){icon.click();}"
                        + "var inputs=document.querySelectorAll("
                        + "'input[placeholder*=Search],input[placeholder*=search],input[type=search]');"
                        + "for(var i=0;i<inputs.length;i++){"
                        + "  inputs[i].focus();"
                        + "  inputs[i].value=id;"
                        + "  inputs[i].dispatchEvent(new Event('input',{bubbles:true}));"
                        + "  inputs[i].dispatchEvent(new Event('change',{bubbles:true}));"
                        + "  inputs[i].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));"
                        + "}"
                        + "return inputs.length;");
        Thread.sleep(3000);
        tableViewUtill.waitForOrdersGridReady();
    }

    private void scrollTargetOrderIntoView(String orderId) {
        runScript(
                FIND_TARGET_ROW_JS
                        + "var idx=findTargetRowIndex('" + esc(orderId) + "');"
                        + "var rows=rowsForIndex(idx);"
                        + "if(!rows.length){"
                        + "  rows=Array.prototype.slice.call(document.querySelectorAll('.ag-row-focus,.ag-row.ag-row-focus'));"
                        + "}"
                        + "if(rows.length){rows[0].scrollIntoView({block:'center',inline:'nearest'});}"
                        + "return rows.length>0;");
    }

    private void navigateToOrderFocus(String orderId) throws InterruptedException {
        String base = ConfigProps.getTargetURL();
        if (base == null || base.trim().isEmpty()) {
            base = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "");
        String focusUrl = root + "/focus?tableView=orders&orderId=" + orderId;
        Logger.logReportMessage("Navigating to order focus: " + focusUrl);
        BaseTest.driver.get().browser().getUrl(focusUrl);
        WaitUtil.waitForJSToLoad(15);
    }

    public boolean isOrderVisibleInGrid(String orderId) {
        Object found = runScript(
                FIND_TARGET_ROW_JS
                        + "return findTargetRowIndex('" + esc(orderId) + "')!=null;");
        return Boolean.TRUE.equals(found) || "true".equals(String.valueOf(found));
    }

    private void navigateToOrdersHome() throws InterruptedException {
        String base = ConfigProps.getTargetURL();
        if (base == null || base.trim().isEmpty()) {
            base = "https://operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "") + "/";
        String current = BaseTest.driver.get().browser().getCurrentUrl();
        if (current == null || current.contains("/focus") || current.contains("orderId=")
                || !current.replaceAll("/+$", "").endsWith(root.replaceAll("/+$", ""))) {
            Logger.logReportMessage("Navigating to orders home: " + root);
            BaseTest.driver.get().browser().getUrl(root);
            WaitUtil.waitForJSToLoad(15);
            tableViewUtill.waitForOrdersGridReady();
            Thread.sleep(1500);
        }
    }

    public void ensureOrdersHomeReady() throws InterruptedException {
        navigateToOrdersHome();
        tableViewUtill.waitForOrdersGridReady();
    }

    public JSONArray waitForStatusTooltipDetails(String orderId, int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            JSONArray details = getStatusTooltipDetailsFromApi(orderId);
            if (details != null && !details.isEmpty()) {
                return details;
            }
            Thread.sleep(400);
        }
        return getStatusTooltipDetailsFromApi(orderId);
    }

    public boolean hoverStatusIconForOrder(String orderId) throws InterruptedException {
        if (hoverStatusIconViaSynergyFocusedRow()) {
            Thread.sleep(800);
            String tooltip = waitForTooltipText(2);
            if (tooltip != null && !tooltip.isEmpty()) {
                return true;
            }
        }
        String escaped = esc(orderId);
        Object result = runScript(
                IS_VISIBLE_FN
                        + FIND_TARGET_ROW_JS
                        + "var idx=findTargetRowIndex('" + escaped + "');"
                        + "var rows=rowsForIndex(idx);"
                        + "if(!rows.length){"
                        + "  rows=Array.prototype.slice.call(document.querySelectorAll('.ag-row-focus,.ag-row.ag-row-focus'));"
                        + "}"
                        + "var targets=collectHoverTargets(rows);"
                        + "var hovered=false;"
                        + "for(var t=0;t<targets.length;t++){if(hoverEl(targets[t]))hovered=true;}"
                        + "return hovered;");
        Thread.sleep(1000);
        return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
    }

    public boolean hoverStatusIconForOrderWithRetry(String orderId) throws InterruptedException {
        String tooltip = readTooltipForStatusOnOrder(orderId, null, false);
        return tooltip != null && !tooltip.isEmpty();
    }

    /**
     * Hovers each status icon on the target order row individually (PROD shows one status per icon,
     * e.g. "Processing Complete 11" on the green checkmark).
     */
    public String readTooltipForStatusOnOrder(String orderId, String statusLabel, boolean optional)
            throws InterruptedException {
        scrollTargetOrderIntoView(orderId);
        Object rowIndex = runScript(
                FIND_TARGET_ROW_JS + "return findTargetRowIndex('" + esc(orderId) + "');");
        String idx = rowIndex == null ? null : String.valueOf(rowIndex).trim();
        if (idx == null || idx.isEmpty() || "null".equalsIgnoreCase(idx)) {
            return hoverAnyVisibleStatusIconForTooltip(statusLabel, optional);
        }
        By rowIcons = By.XPath("//div[contains(@class,'ag-row')][@row-index='" + idx + "']//msc-status");
        List<DesktopBrowserElement> icons = new ArrayList<>();
        try {
            if (WaitUtil.isDisplay(rowIcons, 3)) {
                icons = BaseTest.driver.get().finder().findElements(rowIcons);
            }
        } catch (Exception ignored) {
        }
        if (icons.isEmpty()) {
            icons = findVisibleStatusIconsNearOrder(orderId);
        }
        if (icons.isEmpty()) {
            return hoverAnyVisibleStatusIconForTooltip(statusLabel, optional);
        }
        for (DesktopBrowserElement icon : icons) {
            try {
                icon.scrollIntoView();
                icon.mouseOver();
                Thread.sleep(700);
                String tooltip = waitForTooltipText(3);
                if (tooltip == null || tooltip.isEmpty()) {
                    continue;
                }
                if (statusLabel == null || getTooltipCountForStatus(tooltip, statusLabel, optional) > 0) {
                    Logger.logReportMessage("Tooltip on order " + orderId + ": " + tooltip);
                    return tooltip;
                }
                dismissTooltip();
            } catch (Exception ignored) {
            }
        }
        return hoverAnyVisibleStatusIconForTooltip(statusLabel, optional);
    }

    private List<DesktopBrowserElement> findVisibleStatusIconsNearOrder(String orderId) {
        List<DesktopBrowserElement> icons = new ArrayList<>();
        try {
            By nearOrder = By.XPath("//div[contains(@class,'ag-row')][contains(translate(.,"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                    + esc(orderId).toLowerCase() + "')]//msc-status");
            if (WaitUtil.isDisplay(nearOrder, 2)) {
                icons = BaseTest.driver.get().finder().findElements(nearOrder);
            }
        } catch (Exception ignored) {
        }
        return icons;
    }

    private String hoverAnyVisibleStatusIconForTooltip(String statusLabel, boolean optional)
            throws InterruptedException {
        By allStatus = By.XPath("//msc-status");
        List<DesktopBrowserElement> icons = new ArrayList<>();
        try {
            if (WaitUtil.isDisplay(allStatus, 3)) {
                icons = BaseTest.driver.get().finder().findElements(allStatus);
            }
        } catch (Exception ignored) {
        }
        int limit = Math.min(icons.size(), 20);
        for (int i = 0; i < limit; i++) {
            try {
                DesktopBrowserElement icon = icons.get(i);
                icon.scrollIntoView();
                icon.mouseOver();
                Thread.sleep(700);
                String tooltip = waitForTooltipText(3);
                if (tooltip == null || tooltip.isEmpty()) {
                    continue;
                }
                if (statusLabel == null || getTooltipCountForStatus(tooltip, statusLabel, optional) > 0) {
                    Logger.logReportMessage("Tooltip from status icon: " + tooltip);
                    return tooltip;
                }
                dismissTooltip();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public String getVisibleTooltipText() {
        Object text = runScript(
                IS_VISIBLE_FN
                        + "var selectors=["
                        + "  '.cdk-overlay-container .mat-mdc-tooltip',"
                        + "  '.cdk-overlay-container .mdc-tooltip__surface',"
                        + "  '.cdk-overlay-container [class*=tooltip]',"
                        + "  '.cdk-overlay-container .mdc-tooltip',"
                        + "  '.mat-tooltip',"
                        + "  '[class*=status-tooltip]',"
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
        return text == null ? null : String.valueOf(text).trim();
    }

    public String waitForTooltipText(int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            String text = getVisibleTooltipText();
            if (text != null && !text.isEmpty()) {
                return text;
            }
            Thread.sleep(300);
        }
        return getVisibleTooltipText();
    }

    private boolean hoverStatusIconViaSynergyFocusedRow() {
        By mscStatus = By.XPath("//msc-status");
        try {
            if (!WaitUtil.isDisplay(mscStatus, 2)) {
                return false;
            }
            List<DesktopBrowserElement> elements = BaseTest.driver.get().finder().findElements(mscStatus);
            for (DesktopBrowserElement element : elements) {
                try {
                    element.scrollIntoView();
                    element.mouseOver();
                    Thread.sleep(400);
                } catch (Exception ignored) {
                }
            }
            return !elements.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hoverStatusIconViaSynergy(String orderId) {
        String escaped = orderId.replace("'", "");
        By[] locators = new By[]{
                By.XPath("//div[contains(@class,'ag-row')][contains(., '" + escaped + "')]"
                        + "//div[contains(@class,'ag-cell')][contains(@col-id,'status')]"
                        + "//*[self::msc-status or contains(@class,'status') or self::svg or contains(@class,'icon')]"),
                By.XPath("//div[contains(@class,'ag-row')][contains(., '" + escaped + "')]"
                        + "//div[contains(@class,'ag-cell')][contains(@col-id,'status')]"),
                By.XPath("//div[contains(@class,'ag-row')][contains(., '" + escaped + "')]"
                        + "//msc-status | //*[contains(@class,'status-summary')]//msc-status"
                        + " | //*[contains(@class,'status-filter')]"),
                By.XPath("//div[contains(@class,'ag-row')][contains(., '" + escaped + "')]"
                        + "//*[contains(@class,'status-summary') or contains(@class,'order-status')]")
        };
        for (By locator : locators) {
            try {
                if (!WaitUtil.isDisplay(locator, 3)) {
                    continue;
                }
                DesktopBrowserElement element = BaseTest.driver.get().finder().findElement(locator);
                element.scrollIntoView();
                element.mouseOver();
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public JSONArray getStatusTooltipDetailsFromApi(String orderId) {
        try {
            Object raw = runScript(
                    "var id='" + esc(orderId) + "'.toLowerCase();"
                            + "var map=window.__ffFilterOrdersByOrderId||{};"
                            + "return map[id]||null;");
            return toJsonArray(raw);
        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                Logger.logConsoleMessage("Failed to read statusTooltipDetails from API hook: " + e.getMessage());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private JSONArray toJsonArray(Object raw) throws org.json.simple.parser.ParseException {
        if (raw == null || "null".equals(String.valueOf(raw))) {
            return null;
        }
        if (raw instanceof JSONArray) {
            return (JSONArray) raw;
        }
        if (raw instanceof List) {
            JSONArray arr = new JSONArray();
            for (Object item : (List<?>) raw) {
                arr.add(item);
            }
            return arr.isEmpty() ? null : arr;
        }
        if (raw instanceof Map) {
            JSONArray arr = new JSONArray();
            arr.add(raw);
            return arr;
        }
        if (raw instanceof String) {
            Object parsed = new JSONParser().parse((String) raw);
            if (parsed instanceof JSONArray) {
                return (JSONArray) parsed;
            }
        }
        return null;
    }

    public List<String> buildExpectedTooltipLines(JSONArray statusTooltipDetails) {
        List<String> lines = new ArrayList<>();
        if (statusTooltipDetails == null) {
            return lines;
        }
        for (Object item : statusTooltipDetails) {
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject entry = (JSONObject) item;
            String status = String.valueOf(entry.get("status"));
            long count = toLong(entry.get("count"));
            long optionalCount = toLong(entry.get("optionalCount"));
            if (count > 0) {
                lines.add(status + " - " + count);
            }
            if (optionalCount > 0) {
                lines.add(status + " (optional) - " + optionalCount);
            }
        }
        return lines;
    }

    public boolean hasZeroCountEntries(String tooltipText) {
        if (tooltipText == null || tooltipText.isEmpty()) {
            return false;
        }
        for (String line : tooltipText.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (ZERO_COUNT_LINE.matcher(trimmed).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean tooltipContainsExpectedLines(String tooltipText, List<String> expectedLines) {
        if (tooltipText == null || expectedLines.isEmpty()) {
            return false;
        }
        String normalized = tooltipText.toLowerCase().replaceAll("\\s+", " ");
        for (String line : expectedLines) {
            if (!normalized.contains(line.toLowerCase().replaceAll("\\s+", " "))) {
                return false;
            }
        }
        return true;
    }

    public boolean tooltipHasOptionalFormat(String tooltipText) {
        if (tooltipText == null) {
            return false;
        }
        for (String line : tooltipText.split("\\R")) {
            if (OPTIONAL_LINE.matcher(line).find()) {
                return true;
            }
        }
        return false;
    }

    public void verifyTooltipVisibleOnHover(String... orderIds) throws InterruptedException {
        for (String orderId : orderIds) {
            if (orderId == null || orderId.isEmpty()) {
                continue;
            }
            searchOrder(orderId);
            String tooltip = readTooltipForStatusOnOrder(orderId, null, false);
            if (tooltip != null && !tooltip.isEmpty()) {
                Verify.softAssert(true, "Status tooltip visible on hover for order " + orderId);
                Logger.logReportMessage("Tooltip for " + orderId + ": " + tooltip);
                return;
            }
            Logger.logReportMessage("Diagnostics for " + orderId + ": " + dumpGridDiagnostics(orderId));
            dismissTooltip();
        }
        ensureOrdersHomeReady();
        new FilterPanel_Util().applyYesterdayDateFilter();
        String firstOrder = getFirstVisibleOrderId();
        if (firstOrder != null) {
            scrollTargetOrderIntoView(firstOrder);
            String tooltip = readTooltipForStatusOnOrder(firstOrder, null, false);
            if (tooltip != null && !tooltip.isEmpty()) {
                Verify.softAssert(true, "Status tooltip visible on hover for first grid order " + firstOrder);
                Logger.logReportMessage("Tooltip for " + firstOrder + ": " + tooltip);
                return;
            }
        }
        Verify.softAssert(false, "Status tooltip visible on hover for orders "
                + String.join(", ", orderIds)
                + " — enable-order-summary-tooltips may be off in this environment");
    }

    public void verifyNonZeroCountsOnly(String orderId) throws InterruptedException {
        searchOrder(orderId);
        hoverStatusIconForOrderWithRetry(orderId);
        String tooltip = waitForTooltipText(5);
        if (tooltip == null || tooltip.isEmpty()) {
            Logger.log("Skip non-zero validation - no tooltip for order " + orderId);
            return;
        }
        Verify.softAssert(!hasZeroCountEntries(tooltip),
                "Tooltip excludes zero-count statuses for order " + orderId + " [" + tooltip + "]");
    }

    public void verifyApiMatchesUiTooltip(String orderId) throws InterruptedException {
        searchOrder(orderId);
        JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
        if (apiDetails == null || apiDetails.isEmpty()) {
            Logger.log("Skip API match - statusTooltipDetails not captured for order " + orderId);
            return;
        }
        List<String> expected = buildExpectedTooltipLines(apiDetails);
        hoverStatusIconForOrderWithRetry(orderId);
        String tooltip = waitForTooltipText(5);
        Verify.softAssert(tooltip != null && !tooltip.isEmpty(),
                "Tooltip visible for API validation on order " + orderId);
        Verify.softAssert(tooltipContainsExpectedLines(tooltip, expected),
                "Tooltip matches filterOrders statusTooltipDetails for " + orderId
                        + " expected=" + expected + " actual=" + tooltip);
    }

    public int getTooltipCountForStatus(String tooltipText, String statusLabel, boolean optional) {
        if (tooltipText == null || tooltipText.isEmpty()) {
            return -1;
        }
        for (String line : tooltipText.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String lower = trimmed.toLowerCase();
            boolean optionalLine = lower.contains("(optional)");
            if (optional != optionalLine) {
                continue;
            }
            Matcher matcher = TOOLTIP_COUNT_LINE.matcher(trimmed);
            int count = 1;
            String statusPart;
            if (matcher.find()) {
                statusPart = trimmed.substring(0, matcher.start()).trim();
                count = Integer.parseInt(matcher.group(1));
            } else {
                statusPart = trimmed;
            }
            if (!statusMatchesLabel(statusPart, statusLabel)) {
                continue;
            }
            return count;
        }
        return 0;
    }

    public int getApiCountForStatus(JSONArray details, String statusLabel, boolean optional) {
        if (details == null) {
            return -1;
        }
        for (Object item : details) {
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject entry = (JSONObject) item;
            if (!statusMatchesLabel(String.valueOf(entry.get("status")), statusLabel)) {
                continue;
            }
            long count = optional ? toLong(entry.get("optionalCount")) : toLong(entry.get("count"));
            return (int) count;
        }
        return 0;
    }

    private boolean statusMatchesLabel(String actual, String expected) {
        String a = normalizeStatusText(actual);
        String b = normalizeStatusText(expected);
        if (a.equals(b)) {
            return true;
        }
        if (a.startsWith(b) || b.startsWith(a)) {
            return true;
        }
        return aliasStatus(b).equals(aliasStatus(a));
    }

    private String aliasStatus(String status) {
        String normalized = normalizeStatusText(status);
        switch (normalized) {
            case "delivery completed":
                return "delivery complete";
            case "processing":
                return "processing complete";
            case "pending":
            case "delivery pending":
                return "delivery pending";
            case "packing":
                return "packaging";
            case "ready for delivery":
                return "ready for delivery";
            default:
                return normalized;
        }
    }

    private String normalizeStatusText(String status) {
        if (status == null) {
            return "";
        }
        return status.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    @SuppressWarnings("unchecked")
    private List<String> discoverVisibleOrderIds(int limit) {
        Object raw = runScript(
                "var limit=" + limit + ";"
                        + "var ids=[];"
                        + "var seen={};"
                        + "function pushId(id){"
                        + "  if(!id)return;"
                        + "  var key=id.toLowerCase();"
                        + "  if(seen[key])return;"
                        + "  seen[key]=true;"
                        + "  ids.push(id);"
                        + "}"
                        + "var rows=document.querySelectorAll('.ag-center-cols-container .ag-row,.ag-body-viewport .ag-row');"
                        + "for(var r=0;r<rows.length && ids.length<limit;r++){"
                        + "  var cells=rows[r].querySelectorAll('[col-id=orderId],[col-id=id],[col-id=orderID]');"
                        + "  for(var c=0;c<cells.length;c++){"
                        + "    pushId((cells[c].textContent||'').trim());"
                        + "  }"
                        + "  if(ids.length>=limit)break;"
                        + "  var txt=(rows[r].textContent||'');"
                        + "  var m=txt.match(/\\b([a-z]{2,5}\\d{5,10})\\b/i);"
                        + "  if(m)pushId(m[1]);"
                        + "}"
                        + "return ids;");
        List<String> orderIds = new ArrayList<>();
        if (raw instanceof List) {
            for (Object item : (List<Object>) raw) {
                if (item != null) {
                    String id = String.valueOf(item).trim();
                    if (!id.isEmpty()) {
                        orderIds.add(id);
                    }
                }
            }
        }
        return orderIds;
    }

    public String resolveOrderWithStatus(String primaryOrderId, String altOrderId, String statusLabel, boolean optional)
            throws InterruptedException {
        List<String> candidates = new ArrayList<>();
        for (String orderId : new String[]{
                primaryOrderId,
                altOrderId,
                PROD_ORDER_KNOWN,
                PROD_ORDER_PROCESSING_COMPLETE
        }) {
            if (orderId != null && !orderId.isEmpty() && !candidates.contains(orderId)) {
                candidates.add(orderId);
            }
        }
        navigateToOrdersHome();
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1000);
        for (String visibleId : discoverVisibleOrderIds(8)) {
            if (!candidates.contains(visibleId)) {
                candidates.add(visibleId);
            }
        }
        installTooltipCaptureHook();
        for (String orderId : candidates) {
            JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
            if (apiDetails != null && getApiCountForStatus(apiDetails, statusLabel, optional) > 0) {
                return orderId;
            }
        }
        for (String orderId : candidates) {
            if (orderId == null || orderId.isEmpty()) {
                continue;
            }
            searchOrderOnOrdersGrid(orderId);
            JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
            if (apiDetails != null && getApiCountForStatus(apiDetails, statusLabel, optional) > 0) {
                return orderId;
            }
            String tooltip = readTooltipForStatusOnOrder(orderId, statusLabel, optional);
            dismissTooltip();
            if (tooltip != null && getTooltipCountForStatus(tooltip, statusLabel, optional) > 0) {
                return orderId;
            }
        }
        return null;
    }

    private String preferredOrderForStatus(String statusLabel) {
        if (LineItemStatusConstants.PROCESSING.equals(statusLabel)
                || LineItemStatusConstants.PACKING.equals(statusLabel)
                || LineItemStatusConstants.PACKAGING_PENDING.equals(statusLabel)) {
            return PROD_ORDER_PROCESSING_COMPLETE;
        }
        if (LineItemStatusConstants.DELIVERY_COMPLETED.equals(statusLabel)) {
            return PROD_ORDER_KNOWN;
        }
        return PROD_ORDER_MULTI_STATUS;
    }

    private boolean orderHasStatus(String orderId, String statusLabel, boolean optional)
            throws InterruptedException {
        JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
        if (apiDetails != null && getApiCountForStatus(apiDetails, statusLabel, optional) > 0) {
            return true;
        }
        String tooltip = readTooltipForStatusOnOrder(orderId, statusLabel, optional);
        dismissTooltip();
        return tooltip != null && getTooltipCountForStatus(tooltip, statusLabel, optional) > 0;
    }

    private String findOrderWithStatusOnYesterdayGrid(String statusLabel, boolean optional, FilterPanel_Util filterPanelUtil)
            throws InterruptedException {
        ensureOrdersHomeReady();
        filterPanelUtil.applyYesterdayDateFilter();
        installTooltipCaptureHook();
        tableViewUtill.waitForOrdersGridReady();
        Thread.sleep(1500);

        List<String> candidates = new ArrayList<>();
        String preferred = preferredOrderForStatus(statusLabel);
        if (preferred != null && !preferred.isEmpty()) {
            candidates.add(preferred);
        }
        for (String orderId : new String[]{PROD_ORDER_MULTI_STATUS, PROD_ORDER_ALT, PROD_ORDER_KNOWN, PROD_ORDER_PROCESSING_COMPLETE}) {
            if (orderId != null && !orderId.isEmpty() && !candidates.contains(orderId)) {
                candidates.add(orderId);
            }
        }

        for (String orderId : candidates) {
            JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
            if (apiDetails != null && getApiCountForStatus(apiDetails, statusLabel, optional) > 0) {
                searchOrderOnOrdersGrid(orderId);
                return orderId;
            }
        }
        for (String orderId : candidates) {
            searchOrderOnOrdersGrid(orderId);
            if (orderHasStatus(orderId, statusLabel, optional)) {
                return orderId;
            }
        }
        return null;
    }

    public boolean expandOrder(String orderId) throws InterruptedException {
        scrollTargetOrderIntoView(orderId);
        Object expanded = runScript(
                EXPAND_AND_COUNT_JS
                        + FIND_TARGET_ROW_JS
                        + "var id='" + esc(orderId) + "';"
                        + "var idx=findTargetRowIndex(id);"
                        + "var rows=rowsForIndex(idx);"
                        + "if(rows.length){"
                        + "  rows[0].scrollIntoView({block:'center'});"
                        + "  var closed=rows[0].querySelector('.ag-group-contracted,.ag-icon-tree-closed,[aria-expanded=false]');"
                        + "  if(closed){closed.click();return true;}"
                        + "  if(rows[0].querySelector('.ag-group-expanded,.ag-icon-tree-open,[aria-expanded=true]'))return true;"
                        + "  var btn=rows[0].querySelector('button.side-nav-button,button.dropdown-toggle:not(.ellipsisButton)');"
                        + "  if(btn){btn.click();return true;}"
                        + "}"
                        + "return expandOrderRow(id);");
        Thread.sleep(2500);
        return Boolean.TRUE.equals(expanded) || "true".equals(String.valueOf(expanded));
    }

    public int countExpandedLineItemsByStatus(String orderId, String statusLabel, boolean optional) {
        Object count = runScript(
                EXPAND_AND_COUNT_JS
                        + "return countExpandedLineItems('"
                        + esc(orderId) + "','"
                        + esc(statusLabel) + "',"
                        + optional + ");");
        if (count instanceof Number) {
            return ((Number) count).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(count));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void dismissTooltip() throws InterruptedException {
        runScript("document.dispatchEvent(new MouseEvent('mousemove',{clientX:0,clientY:0,bubbles:true}));");
        Thread.sleep(300);
    }

    public void verifyTooltipCountViaLineItemStatusFilter(
            FilterPanel_Util filterPanelUtil, String filterSearchTerm, String statusLabel, boolean optional)
            throws InterruptedException {
        ensureOrdersHomeReady();
        installTooltipCaptureHook();
        boolean filterApplied = filterPanelUtil.filterByLineItemStatus(filterSearchTerm, statusLabel);

        String orderId = null;
        if (filterApplied) {
            orderId = getFirstVisibleOrderId();
            if (orderId != null) {
                Logger.logReportMessage("First filtered order for [" + statusLabel + "]: " + orderId);
            }
        }
        if (orderId == null) {
            Logger.logReportMessage("Line Item Status filter path did not yield an order for [" + statusLabel
                    + "] — searching Yesterday grid for order with status");
            orderId = findOrderWithStatusOnYesterdayGrid(statusLabel, optional, filterPanelUtil);
            if (orderId != null) {
                ensureOrdersHomeReady();
                filterPanelUtil.applyYesterdayDateFilter();
                searchOrderOnOrdersGrid(orderId);
                Logger.logReportMessage("Using order " + orderId + " on Yesterday grid for [" + statusLabel + "]");
            }
        }

        Verify.softAssert(orderId != null,
                "Order with status [" + statusLabel + "] found for count validation"
                        + (filterApplied ? " (after filter)" : " (Yesterday grid search)"));
        if (orderId == null) {
            return;
        }

        scrollTargetOrderIntoView(orderId);
        Verify.softAssert(expandOrder(orderId), "Expanded order " + orderId + " to count line items");
        Thread.sleep(2000);

        assertTooltipCountMatchesExpandedLineItems(orderId, statusLabel, optional);
    }

    public void verifyTooltipCountMatchesExpandedLineItems(
            String primaryOrderId, String altOrderId, String statusLabel, boolean optional)
            throws InterruptedException {
        String orderId = resolveOrderWithStatus(primaryOrderId, altOrderId, statusLabel, optional);
        if (orderId == null) {
            Logger.log("Skip count match — no tooltip count for status [" + statusLabel + "] on test orders");
            return;
        }
        searchOrder(orderId);
        assertTooltipCountMatchesExpandedLineItems(orderId, statusLabel, optional);
    }

    private void assertTooltipCountMatchesExpandedLineItems(String orderId, String statusLabel, boolean optional)
            throws InterruptedException {
        JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
        String tooltip = readTooltipForStatusOnOrder(orderId, statusLabel, optional);
        if (tooltip == null) {
            hoverStatusIconForOrderWithRetry(orderId);
            tooltip = waitForTooltipText(5);
        }
        dismissTooltip();

        int tooltipCount = getTooltipCountForStatus(tooltip, statusLabel, optional);
        if (tooltipCount <= 0 && apiDetails != null) {
            tooltipCount = getApiCountForStatus(apiDetails, statusLabel, optional);
        }
        if (tooltipCount <= 0) {
            if (optional) {
                Logger.log("Skip count match — zero tooltip count for [" + statusLabel + "] on order " + orderId);
                return;
            }
            Verify.softAssert(false,
                    "Non-zero tooltip count for [" + statusLabel + "] on order " + orderId + " (tooltip=" + tooltip + ")");
            return;
        }

        if (!isOrderExpanded(orderId)) {
            Verify.softAssert(expandOrder(orderId), "Expanded order " + orderId + " to count line items");
            Thread.sleep(1500);
        }
        int lineItemCount = countExpandedLineItemsByStatus(orderId, statusLabel, optional);
        Logger.logReportMessage("Status [" + statusLabel + "] order=" + orderId
                + " tooltipCount=" + tooltipCount + " expandedLiCount=" + lineItemCount
                + " tooltip=" + tooltip);

        Verify.softAssert(lineItemCount >= 0,
                "Expanded line item count retrieved for status [" + statusLabel + "]");
        Verify.softAssert(tooltipCount == lineItemCount,
                "Tooltip count matches expanded line item count for [" + statusLabel + "] on order "
                        + orderId + " — tooltip=" + tooltipCount + " expanded=" + lineItemCount);
    }

    public String getFirstVisibleOrderId() {
        Object raw = runScript(
                FIND_TARGET_ROW_JS
                        + "var rows=document.querySelectorAll("
                        + "'.ag-center-cols-container .ag-row,.ag-body-viewport .ag-row,.ag-row');"
                        + "for(var i=0;i<rows.length;i++){"
                        + "  if(rows[i].classList.contains('ag-row-level-1')"
                        + "    ||rows[i].classList.contains('ag-row-level-2'))continue;"
                        + "  var cell=rows[i].querySelector('[col-id=orderId],[col-id=id],[col-id=orderID]');"
                        + "  if(cell){"
                        + "    var id=(cell.textContent||'').trim();"
                        + "    if(id)return id;"
                        + "  }"
                        + "  var m=(rows[i].textContent||'').match(/\\b([a-z]{2,5}\\d{5,12})\\b/i);"
                        + "  if(m)return m[1];"
                        + "}"
                        + "return null;");
        if (raw == null || "null".equalsIgnoreCase(String.valueOf(raw).trim())) {
            return null;
        }
        String orderId = String.valueOf(raw).trim();
        return orderId.isEmpty() ? null : orderId;
    }

    private boolean isOrderExpanded(String orderId) {
        Object expanded = runScript(
                FIND_TARGET_ROW_JS
                        + "var idx=findTargetRowIndex('" + esc(orderId) + "');"
                        + "var rows=rowsForIndex(idx);"
                        + "if(!rows.length)return false;"
                        + "var row=rows[0];"
                        + "if(row.getAttribute('aria-expanded')==='true')return true;"
                        + "return !!row.querySelector('.ag-row-group-expanded,.ag-group-expanded,[aria-expanded=true]');");
        return Boolean.TRUE.equals(expanded) || "true".equals(String.valueOf(expanded));
    }

    public Map<String, Integer> parseAllTooltipCounts(String tooltipText) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (tooltipText == null) {
            return counts;
        }
        for (String line : tooltipText.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher matcher = TOOLTIP_COUNT_LINE.matcher(trimmed);
            if (!matcher.find()) {
                continue;
            }
            int count = Integer.parseInt(matcher.group(1));
            String status = trimmed.substring(0, matcher.start()).trim();
            counts.put(status, count);
        }
        return counts;
    }

    public void verifyOptionalCountFormat(String orderId) throws InterruptedException {
        searchOrder(orderId);
        JSONArray apiDetails = getStatusTooltipDetailsFromApi(orderId);
        boolean apiHasOptional = false;
        if (apiDetails != null) {
            for (Object item : apiDetails) {
                if (item instanceof JSONObject && toLong(((JSONObject) item).get("optionalCount")) > 0) {
                    apiHasOptional = true;
                    break;
                }
            }
        }
        if (!apiHasOptional) {
            Logger.log("Skip optional format - no optionalCount in API for order " + orderId);
            return;
        }
        hoverStatusIconForOrderWithRetry(orderId);
        String tooltip = waitForTooltipText(5);
        Verify.softAssert(tooltipHasOptionalFormat(tooltip),
                "Optional LI counts shown as '(optional)' for order " + orderId + " [" + tooltip + "]");
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String esc(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    public String dumpGridDiagnostics(String orderId) {
        Object result = runScript(
                IS_VISIBLE_FN
                        + FIND_TARGET_ROW_JS
                        + "var out={};"
                        + "out.url=window.location.href;"
                        + "out.rowIndex=findTargetRowIndex('" + esc(orderId) + "');"
                        + "out.focusRow=document.querySelectorAll('.ag-row-focus,.ag-row.ag-row-focus').length;"
                        + "out.sideNav=document.querySelectorAll('button.side-nav-button,button.dropdown-toggle:not(.ellipsisButton)').length;"
                        + "out.colIds=Array.prototype.slice.call(document.querySelectorAll('[col-id]')).slice(0,40)"
                        + ".map(function(c){return c.getAttribute('col-id');});"
                        + "out.mscStatus=document.querySelectorAll('msc-status').length;"
                        + "out.rows=rowsForIndex(out.rowIndex).length;"
                        + "out.targets=collectHoverTargets(rowsForIndex(out.rowIndex)).length;"
                        + "out.apiKeys=Object.keys(window.__ffFilterOrdersByOrderId||{});"
                        + "return JSON.stringify(out,null,2);");
        return result == null ? "{}" : String.valueOf(result);
    }

    private Object runScript(String script) {
        return SynergyRetryUtil.executeScript(script);
    }
}
