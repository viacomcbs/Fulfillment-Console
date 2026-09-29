package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SynergyRetryUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public final class FulfillmentJsUtil {

    public static final int FAST_ELEMENT_TIMEOUT_MS = 20000;

    private static final String IS_VISIBLE_FN =
            "function isVisible(el){"
                    + "if(!el)return false;"
                    + "var r=el.getBoundingClientRect();"
                    + "if(r.width<=0||r.height<=0)return false;"
                    + "var s=window.getComputedStyle(el);"
                    + "return s.display!=='none'&&s.visibility!=='hidden'&&parseFloat(s.opacity||'1')>0;"
                    + "}";

    /** PROD/DEV may expose orders table via [role=grid] without .ag-root. */
    private static final String ORDERS_GRID_ROOT_FN =
            "function ordersGridRoot(){"
                    + "var roots=document.querySelectorAll('.ag-root-wrapper,.ag-root');"
                    + "var best=null,bestScore=0;"
                    + "for(var r=0;r<roots.length;r++){"
                    + "  var hdrs=roots[r].querySelectorAll('.ag-header-cell,[role=columnheader]').length;"
                    + "  var rows=roots[r].querySelectorAll('.ag-center-cols-container > .ag-row:not(.ag-row-level-1)').length;"
                    + "  var score=hdrs*100+rows;"
                    + "  if(score>bestScore){bestScore=score;best=roots[r];}"
                    + "}"
                    + "if(best&&bestScore>=300)return best;"
                    + "var grids=document.querySelectorAll('[role=grid]');"
                    + "best=null;bestScore=0;"
                    + "for(var g=0;g<grids.length;g++){"
                    + "  var rect=grids[g].getBoundingClientRect();"
                    + "  if(rect.width<220||rect.height<100)continue;"
                    + "  var hdrs=grids[g].querySelectorAll('[role=columnheader],.ag-header-cell,thead th,.label-ellipsis span').length;"
                    + "  var rows=grids[g].querySelectorAll('[role=row]').length;"
                    + "  var area=rect.width*rect.height;"
                    + "  var score=hdrs*80+Math.min(rows,50)+area/5000;"
                    + "  if(score>bestScore){bestScore=score;best=grids[g];}"
                    + "}"
                    + "if(best&&bestScore>=8)return best;"
                    + "return best||document;"
                    + "}";

    private static final String ORDERS_GRID_DETECT_FN = ORDERS_GRID_ROOT_FN
            + "function ordersGridHeaderCount(){"
                    + "var ag=document.querySelectorAll('.ag-root-wrapper .ag-header-cell,.ag-root .ag-header-cell');"
                    + "if(ag.length>=3)return ag.length;"
                    + "var root=ordersGridRoot();"
                    + "if(!root||root===document){"
                    + "  return document.querySelectorAll('[role=columnheader],.ag-header-cell,thead th,.label-ellipsis span').length;"
                    + "}"
                    + "return root.querySelectorAll('[role=columnheader],.ag-header-cell,thead th,.label-ellipsis span').length;"
                    + "}"
            + "function ordersGridHasHeaders(){return ordersGridHeaderCount()>=3;}"
                    + "function ordersGridResultsVisible(){"
                    + "  return /\\d[\\d,]*\\s+results/i.test((document.body&&document.body.innerText)||'');"
                    + "}";

    private FulfillmentJsUtil() {
    }

    public static void useFastElementTimeout() {
        try {
            BaseTest.driver.get().options().setElementTimeout(FAST_ELEMENT_TIMEOUT_MS);
        } catch (Exception ignored) {
        }
    }

    public static boolean isFulfillmentConsoleReady() {
        return truthy(run(
                IS_VISIBLE_FN
                        + ORDERS_GRID_ROOT_FN
                        + "function findGrid(){"
                        + "  var root=ordersGridRoot();"
                        + "  if(root&&root!==document)return root;"
                        + "  return document.querySelector('.ag-root')||document.querySelector('.ag-root-wrapper');"
                        + "}"
                        + "var href=(location.href||'').toLowerCase();"
                        + "if(href.indexOf('okta')>=0||href.indexOf('/login')>=0)return false;"
                        + "var btn=document.querySelector('#tableViewButton')"
                        + "||document.querySelector('button.table-view-button');"
                        + "var grid=findGrid();"
                        + "var headers=grid.querySelectorAll('.ag-header-cell,[role=columnheader]').length;"
                        + "if(isVisible(btn)&&grid&&grid!==document&&headers>=3)return true;"
                        + "if(isVisible(grid)&&headers>=3)return true;"
                        + "if(isVisible(btn)&&ordersGridResultsVisible())return true;"
                        + "if(isVisible(btn)&&(href.indexOf('fulfillment')>=0"
                        + "||href.indexOf('operationsconsole')>=0))return true;"
                        + "return false;"));
    }

    /**
     * Lighter post-login check for Synergy: fulfillment URL + app shell visible (no AG grid headers required).
     * Used to apply zoom and proceed to calendar/filter setup while grid data still loads.
     */
    public static boolean isFulfillmentLoginShellReady() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var href=(location.href||'').toLowerCase();"
                        + "if(href.indexOf('okta')>=0||href.indexOf('/login')>=0)return false;"
                        + "if(href.indexOf('fulfillment')<0&&href.indexOf('operationsconsole')<0)return false;"
                        + "var btn=document.querySelector('#tableViewButton')"
                        + "||document.querySelector('button.table-view-button');"
                        + "var panel=document.querySelector('msc-left-filter-panel');"
                        + "var table=document.querySelector('app-fulfillment-main-table-container');"
                        + "return isVisible(btn)||isVisible(panel)||isVisible(table);"));
    }

    /** Grid has rendered column headers — use after login before column prep. */
    public static boolean isOrdersGridHeadersReady() {
        return truthy(run(ORDERS_GRID_DETECT_FN + "return ordersGridHasHeaders();"));
    }

    public static boolean waitForOrdersGridHeadersReady(int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isOrdersGridHeadersReady()) {
                return true;
            }
            sleep(400);
        }
        return isOrdersGridHeadersReady();
    }

    public static boolean waitForFulfillmentConsoleReady(int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isFulfillmentConsoleReady()) {
                return true;
            }
            sleep(250);
        }
        return isFulfillmentConsoleReady();
    }

    /** Single loop: console ready + header row present (for column tests). */
    public static boolean waitForOrdersGridReadyFast(int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isFulfillmentConsoleReady() && isOrdersGridHeadersReady()) {
                return true;
            }
            sleep(250);
        }
        return isFulfillmentConsoleReady() && isOrdersGridHeadersReady();
    }

    /**
     * Combined export bootstrap wait: console shell, header row, and ag-grid root in one poll loop.
     */
    public static boolean waitForOrdersGridReadyForExport(int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        int attempt = 0;
        while (System.currentTimeMillis() < end) {
            if (isFulfillmentConsoleReady() && isOrdersGridHeadersReady()) {
                if (truthy(run(ORDERS_GRID_DETECT_FN + "return ordersGridHasHeaders();"))) {
                    return true;
                }
            }
            if (attempt == 0 || attempt % 20 == 0) {
                closeManageColumnsPanel();
            }
            attempt++;
            Thread.sleep(400);
        }
        Logger.logReportMessage("Orders grid export-ready wait timed out");
        return isOrdersGridRendered();
    }

    public static boolean isOrdersGridRendered() {
        return truthy(run(ORDERS_GRID_DETECT_FN
                + "if(ordersGridHasHeaders())return true;"
                + "return ordersGridResultsVisible()&&!!document.querySelector('#tableViewButton');"));
    }

    public static boolean isManageColumnsPanelOpen() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "function isOrderManagePanel(t){"
                        + "t=t||'';"
                        + "return /manage columns/i.test(t)&&(/order columns|order table/i.test(t));"
                        + "}"
                        + "function panelOpen(){"
                        + "var titles=document.querySelectorAll('span.wrapper-dropdown-title,h3,div,span');"
                        + "for(var i=0;i<titles.length;i++){"
                        + "  if(isOrderManagePanel(titles[i].textContent)&&isVisible(titles[i]))return true;"
                        + "}"
                        + "var menus=document.querySelectorAll('.wrapper-dropdown-container,.cdk-overlay-pane');"
                        + "for(var j=0;j<menus.length;j++){"
                        + "  if(isOrderManagePanel(menus[j].textContent)&&isVisible(menus[j]))return true;"
                        + "}"
                        + "var cols=document.querySelectorAll('.multi-table-column-container');"
                        + "for(var k=0;k<cols.length;k++){"
                        + "  var anc=cols[k];"
                        + "  for(var d=0;d<8&&anc;d++){"
                        + "    if(isOrderManagePanel(anc.textContent)&&isVisible(anc))return true;"
                        + "    anc=anc.parentElement;"
                        + "  }"
                        + "}"
                        + "return false;"
                        + "}"
                        + "return panelOpen();"));
    }

    public static boolean openManageColumnsPanel() {
        if (isManageColumnsPanelOpen()) {
            return true;
        }
        if (!truthy(run(
                "var btn = document.querySelector('#tableViewButton')"
                        + " || document.querySelector('button.table-view-button');"
                        + "if (!btn) return false; btn.click(); return true;"))) {
            return false;
        }
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            if (isManageColumnsPanelOpen()) {
                return true;
            }
            sleep(200);
        }
        return isManageColumnsPanelOpen();
    }

    public static void closeManageColumnsPanel() {
        run("document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));"
                + "var backdrop = document.querySelector('.cdk-overlay-backdrop,.modal-backdrop');"
                + "if (backdrop) backdrop.click();");
    }

    /** Click Apply in Manage Columns when present (PROD option-container layout). */
    public static boolean clickManageColumnsApplyIfPresent() {
        return truthy(run(
                "var pane = document.querySelector('.cdk-overlay-pane,.wrapper-dropdown-container');"
                        + "if (!pane) return false;"
                        + "var btns = pane.querySelectorAll('button');"
                        + "for (var i = 0; i < btns.length; i++) {"
                        + "  var t = (btns[i].textContent || '').replace(/\\s+/g, ' ').trim();"
                        + "  if (/^apply$/i.test(t)) { btns[i].click(); return true; }"
                        + "}"
                        + "return false;"));
    }

    public static boolean selectStandardViewIfNeeded() {
        if (truthy(run(
                "function hasStandard(t){return (t||'').toLowerCase().indexOf('standard view')>=0;}"
                        + "var btn = document.querySelector('#tableViewButton');"
                        + "if (btn && hasStandard(btn.textContent)) return true;"
                        + "var reset = document.querySelector('.dropdown-reset-container');"
                        + "if (reset && hasStandard(reset.textContent)) return true;"
                        + "var panel = document.querySelector('.wrapper-dropdown-container');"
                        + "if (panel && hasStandard(panel.textContent)) return true;"
                        + "return false;"))) {
            return true;
        }
        if (!truthy(run(
                "var btn = document.querySelector('#dropdownMenuButton');"
                        + "if (btn) { btn.click(); return true; }"
                        + "return false;"))) {
            run(
                    "var nodes = document.querySelectorAll('label.form-check-label, label, span, div.option');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = (nodes[i].textContent || '').trim().toLowerCase();"
                            + "  if (t.indexOf('standard view') >= 0) { nodes[i].click(); return true; }"
                            + "}"
                            + "return false;");
        }
        return truthy(run(
                "var nodes = document.querySelectorAll('label.form-check-label, label, span, div.option, a');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  var t = (nodes[i].textContent || '').trim().toLowerCase();"
                        + "  if (t.indexOf('standard view') >= 0) { nodes[i].click(); return true; }"
                        + "}"
                        + "return false;"));
    }

    /** Select a saved view from the Orders grid #tableViewButton dropdown (not Manage Columns). */
    public static boolean selectSavedViewOnOrdersGrid(String viewName) {
        if (viewName == null || viewName.trim().isEmpty()) {
            return false;
        }
        String escaped = esc(viewName);
        return truthy(run(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                        + "var target=norm('" + escaped + "');"
                        + "var btn=document.querySelector('#tableViewButton')"
                        + "||document.querySelector('button.table-view-button');"
                        + "if(!btn)return false;"
                        + "btn.click();"
                        + "var end=Date.now()+5000;"
                        + "while(Date.now()<end){"
                        + "  var nodes=document.querySelectorAll("
                        + "'span,label,div.option,div.team-filter,a,[role=option]');"
                        + "  for(var i=0;i<nodes.length;i++){"
                        + "    var t=norm(nodes[i].textContent);"
                        + "    if(t===target||t.indexOf(target)>=0){nodes[i].click();return true;}"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    public static boolean isGridHeaderVisible(String columnName) {
        return isColumnVisibleInGrid(columnName);
    }

    public static boolean isGridHeaderHidden(String columnName) {
        return !isColumnVisibleInGrid(columnName);
    }

    public static boolean waitForGridHeaderVisible(String columnName, int timeoutSec) {
        return waitForGridHeaderVisible(columnName, null, timeoutSec);
    }

    public static boolean waitForGridHeaderVisible(String columnName, String columnId, int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isColumnVisibleInGrid(columnName, columnId)) {
                return true;
            }
            sleep(400);
        }
        return isColumnVisibleInGrid(columnName, columnId);
    }

    public static boolean isColumnVisibleInGrid(String columnName) {
        return isColumnVisibleInGrid(columnName, null);
    }

    public static boolean isColumnVisibleInGrid(String columnName, String columnId) {
        return truthy(run(gridVisibilityScript(columnName, columnId)));
    }

    public static boolean hasGridColumnSearchControl(String columnName, String columnId) {
        return truthy(run(gridSearchControlScript(columnName, columnId)));
    }

    /** Scroll orders grid horizontally until the column header is in the viewport. */
    public static boolean scrollColumnHeaderIntoView(String columnName, String columnId) {
        return truthy(run(buildScrollHeaderIntoViewScript(columnName, columnId)));
    }

    /** Scroll Manage Columns panel so the column checkbox/label is visible (no XPath). */
    public static boolean scrollManagePanelToColumn(String columnName, String section) {
        String escaped = esc(columnName);
        String escapedSection = esc(section == null ? "order" : section);
        String columnId = esc(resolveListingColumnId(columnName, section));
        return truthy(run(
                IS_VISIBLE_FN
                        + columnCheckboxFinderJs()
                        + "var cb = findCheckbox('" + escaped + "','" + escapedSection + "', true, '" + columnId + "');"
                        + "if (!cb) return false;"
                        + "var row = cb.closest('.column-item,.form-check,label,div') || cb;"
                        + "row.scrollIntoView({block:'center'});"
                        + "return true;"));
    }

    public static boolean isColumnListed(String columnName, String section) {
        return findColumnCheckbox(columnName, section, null, resolveListingColumnId(columnName, section));
    }

    /** Exact UI label match in Manage Columns — no GraphQL or column-id fallback. */
    public static boolean isExactColumnLabelInManagePanel(String columnName, String section) {
        String escaped = esc(columnName);
        String escapedSection = esc(section == null ? "order" : section);
        return truthy(run(
                IS_VISIBLE_FN
                        + columnCheckboxFinderJs()
                        + "var panel = findPanel();"
                        + "if (!panel) return false;"
                        + "var scope = findSectionScope(panel, '" + escapedSection + "') || panel;"
                        + "return !!findColumnLabelInScope(scope, norm('" + escaped + "'), true);"));
    }

    public static boolean isColumnChecked(String columnName, String section) {
        return readColumnCheckboxChecked(columnName, section);
    }

    private static String resolveListingColumnId(String columnName, String section) {
        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column != null) {
            return column.id;
        }
        column = OrderTabColumnData.findByIdAndSection(columnName, section);
        return column != null ? column.id : "";
    }

    private static boolean readColumnCheckboxChecked(String columnName, String section) {
        String escaped = esc(columnName);
        String escapedSection = esc(section == null ? "order" : section);
        String columnId = esc(resolveListingColumnId(columnName, section));
        Object result = run(
                IS_VISIBLE_FN
                        + columnCheckboxFinderJs()
                        + "var cb = findCheckbox('" + escaped + "','" + escapedSection + "', true, '" + columnId + "');"
                        + "return isCheckboxChecked(cb);");
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return "true".equalsIgnoreCase(String.valueOf(result));
    }

    private static String columnCheckboxFinderJs() {
        return "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function normalizeCheckbox(el){"
                + "  if(!el)return null;"
                + "  if(el.type==='checkbox')return el;"
                + "  var inner=el.querySelector&&el.querySelector('input[type=checkbox]');"
                + "  if(inner)return inner;"
                + "  return el;"
                + "}"
                + "function isCheckboxChecked(el){"
                + "  el=normalizeCheckbox(el);"
                + "  if(!el)return false;"
                + "  if(el.type==='checkbox')return !!el.checked;"
                + "  var host=el.closest&&el.closest('mat-checkbox,.mat-mdc-checkbox,[role=checkbox]');"
                + "  if(host){"
                + "    if(host.classList&&(host.classList.contains('mat-mdc-checkbox-checked')"
                + "      ||host.classList.contains('mat-checkbox-checked')))return true;"
                + "    if(host.getAttribute('aria-checked')==='true')return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function clickCheckbox(el){"
                + "  el=normalizeCheckbox(el);"
                + "  if(!el)return;"
                + "  var clickTarget=el.closest('label,mat-checkbox,.mat-mdc-checkbox,[role=checkbox]')||el;"
                + "  try{clickTarget.click();}catch(e){try{el.click();}catch(e2){}}"
                + "}"
                + "function resolveCheckbox(labelEl){"
                + "  var cb = labelEl.previousElementSibling;"
                + "  if (!cb || cb.type !== 'checkbox') {"
                + "    cb = labelEl.parentElement && labelEl.parentElement.querySelector('input[type=checkbox]');"
                + "  }"
                + "  if (!cb || cb.type !== 'checkbox') {"
                + "    cb = labelEl.closest('.column-item,.form-check,.option-container,div') && "
                + "         labelEl.closest('.column-item,.form-check,.option-container,div').querySelector('input[type=checkbox]');"
                + "  }"
                + "  return cb && cb.type === 'checkbox' ? cb : null;"
                + "}"
                + "function isOrderManagePanel(t){"
                + "  t=t||'';"
                + "  return /manage columns/i.test(t)&&(/order columns|order table/i.test(t));"
                + "}"
                + "function findPanel(){"
                + "  var msc = document.querySelector('msc-custom-wrapper-dropdown');"
                + "  if (msc && isVisible(msc)) {"
                + "    var mscRoot = msc.closest('.cdk-overlay-pane') || msc.closest('.wrapper-dropdown-container') || msc;"
                + "    if (isOrderManagePanel(mscRoot.textContent)"
                + "      || mscRoot.querySelector('.multi-table-column-container,.table-column-name')) {"
                + "      return mscRoot;"
                + "    }"
                + "  }"
                + "  var candidates = document.querySelectorAll('.cdk-overlay-pane .wrapper-dropdown-container,.wrapper-dropdown-container');"
                + "  for (var i = 0; i < candidates.length; i++) {"
                + "    var p = candidates[i];"
                + "    if (isOrderManagePanel(p.textContent) && isVisible(p)) return p;"
                + "  }"
                + "  var panes = document.querySelectorAll('.cdk-overlay-pane');"
                + "  for (var j = 0; j < panes.length; j++) {"
                + "    var pane = panes[j];"
                + "    if (isOrderManagePanel(pane.textContent) && isVisible(pane)) return pane;"
                + "  }"
                + "  var titles = document.querySelectorAll('span.wrapper-dropdown-title,h3,div,span');"
                + "  for (var t = 0; t < titles.length; t++) {"
                + "    if (!isOrderManagePanel(titles[t].textContent) || !isVisible(titles[t])) continue;"
                + "    var root = titles[t].closest('.wrapper-dropdown-container,.cdk-overlay-pane');"
                + "    if (!root) root = titles[t].parentElement;"
                + "    while (root && root !== document.body) {"
                + "      if (root.querySelector('.multi-table-column-container,.column-item,[class*=column-item]')) return root;"
                + "      root = root.parentElement;"
                + "    }"
                + "  }"
                + "  var colContainers = document.querySelectorAll('.multi-table-column-container');"
                + "  for (var c = 0; c < colContainers.length; c++) {"
                + "    var anc = colContainers[c];"
                + "    for (var d = 0; d < 10 && anc; d++) {"
                + "      if (isOrderManagePanel(anc.textContent) && isVisible(anc)) return anc;"
                + "      anc = anc.parentElement;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function sectionNeedle(section){"
                + "  section = norm(section || 'order');"
                + "  if (section.indexOf('package') >= 0) return 'package columns';"
                + "  if (section.indexOf('line') >= 0) return 'line item columns';"
                + "  return 'order columns';"
                + "}"
                + "function sectionHeaderMatches(headerText, needle){"
                + "  var h = norm(headerText);"
                + "  if (h.indexOf(needle) >= 0) return true;"
                + "  return needle === 'order columns' && h.indexOf('order table') >= 0;"
                + "}"
                + "function findSectionScope(panel, section){"
                + "  var needle = sectionNeedle(section);"
                + "  var headers = panel.querySelectorAll('.table-column-name');"
                + "  for (var h = 0; h < headers.length; h++) {"
                + "    if (sectionHeaderMatches(headers[h].textContent, needle)) {"
                + "      var scoped = headers[h].closest('.multi-table-column-container');"
                + "      if (scoped) return scoped;"
                + "    }"
                + "  }"
                + "  var containers = panel.querySelectorAll('.multi-table-column-container');"
                + "  for (var i = 0; i < containers.length; i++) {"
                + "    var header = containers[i].querySelector('.table-column-name');"
                + "    if (header && sectionHeaderMatches(header.textContent, needle)) return containers[i];"
                + "  }"
                + "  if (needle === 'order columns') {"
                + "    var first = panel.querySelector('.multi-table-column-container');"
                + "    if (first) return first;"
                + "    return panel;"
                + "  }"
                + "  return null;"
                + "}"
                + "function findColumnLabelInScope(scope, target, exactOnly){"
                + "  if (!scope) return null;"
                + "  var nodes = scope.querySelectorAll('label,span,div,.column-item,[class*=column-item]');"
                + "  for (var i = 0; i < nodes.length; i++) {"
                + "    var node = nodes[i];"
                + "    if (node.querySelector('.multi-table-column-container,.table-column-name')) continue;"
                + "    var t = norm(node.textContent);"
                + "    if (exactOnly) {"
                + "      if (t !== target) continue;"
                + "    } else if (t !== target && t.indexOf(target) < 0 && target.indexOf(t) < 0) continue;"
                + "    if (!isVisible(node)) continue;"
                + "    node.scrollIntoView({block:'center'});"
                + "    return node;"
                + "  }"
                + "  return null;"
                + "}"
                + "function findByCheckboxRowText(scope, target, exactOnly){"
                + "  var inputs = scope.querySelectorAll('input.form-check-input.checkbox,input[type=checkbox],mat-checkbox,.mat-mdc-checkbox,.mat-checkbox,[role=checkbox]');"
                + "  for (var i = 0; i < inputs.length; i++) {"
                + "    var row = inputs[i].closest('.column-item,.form-check,.multi-table-column-item,label,div');"
                + "    if (!row) row = inputs[i].parentElement;"
                + "    if (!row) continue;"
                + "    var text = norm(row.textContent);"
                + "    if (exactOnly) {"
                + "      if (text !== target) continue;"
                + "    } else if (text !== target && text.indexOf(target) < 0 && target.indexOf(text) < 0) {"
                + "      continue;"
                + "    }"
                + "    inputs[i].scrollIntoView({block:'center'});"
                + "    var cb = inputs[i].type === 'checkbox' ? inputs[i] : inputs[i].querySelector('input[type=checkbox]');"
                + "    return cb || inputs[i];"
                + "  }"
                + "  return null;"
                + "}"
                + "function findInScope(scope, target, exactOnly){"
                + "  var labels = scope.querySelectorAll('label');"
                + "  for (var i = 0; i < labels.length; i++) {"
                + "    var t = norm(labels[i].textContent);"
                + "    if (exactOnly) {"
                + "      if (t !== target) continue;"
                + "    } else if (t !== target && t.indexOf(target) < 0 && target.indexOf(t) < 0) {"
                + "      continue;"
                + "    }"
                + "    labels[i].scrollIntoView({block:'center'});"
                + "    var cb = resolveCheckbox(labels[i]);"
                + "    if (cb) return cb;"
                + "  }"
                + "  var items = scope.querySelectorAll('.column-item,.form-check,[class*=column-item]');"
                + "  for (var j = 0; j < items.length; j++) {"
                + "    var itemText = norm(items[j].textContent);"
                + "    if (exactOnly) {"
                + "      if (itemText !== target) continue;"
                + "    } else if (itemText !== target && itemText.indexOf(target) < 0 && target.indexOf(itemText) < 0) {"
                + "      continue;"
                + "    }"
                + "    items[j].scrollIntoView({block:'center'});"
                + "    var itemCb = items[j].querySelector('input[type=checkbox]');"
                + "    if (itemCb) return itemCb;"
                + "    var itemLabel = items[j].querySelector('label');"
                + "    if (itemLabel) { itemCb = resolveCheckbox(itemLabel); if (itemCb) return itemCb; }"
                + "  }"
                + "  cb = findByCheckboxRowText(scope, target, exactOnly);"
                + "  if (cb) return cb;"
                + "  return findColumnLabelInScope(scope, target, exactOnly);"
                + "}"
                + "function tablePrefixForSection(section){"
                + "  var s=norm(section||'order');"
                + "  if(s.indexOf('package')>=0)return 'packageTable';"
                + "  if(s.indexOf('line')>=0)return 'lineItemTable';"
                + "  return 'orderTable';"
                + "}"
                + "function findByColumnId(scope, columnId, section){"
                + "  if (!columnId) return null;"
                + "  var prefix=tablePrefixForSection(section);"
                + "  var panel=findPanel();"
                + "  function resolveIn(root){"
                + "    if(!root)return null;"
                + "    var byId=root.querySelector('#'+prefix+columnId);"
                + "    if(byId){"
                + "      var cb=normalizeCheckbox(byId);"
                + "      if(cb){byId.scrollIntoView({block:'center'});return cb;}"
                + "    }"
                + "    return null;"
                + "  }"
                + "  var cb=resolveIn(scope);"
                + "  if(cb)return cb;"
                + "  cb=resolveIn(panel);"
                + "  if(cb)return cb;"
                + "  var needle = norm(columnId);"
                + "  var inputs = scope.querySelectorAll('input[type=checkbox],mat-checkbox,.mat-mdc-checkbox');"
                + "  for (var i = 0; i < inputs.length; i++) {"
                + "    var input = inputs[i];"
                + "    var attrs = norm((input.id || '') + ' ' + (input.name || '') + ' ' + (input.value || '')"
                + "      + ' ' + (input.getAttribute('formcontrolname') || '')"
                + "      + ' ' + (input.getAttribute('ng-reflect-name') || '')"
                + "      + ' ' + (input.getAttribute('data-testid') || ''));"
                + "    if (attrs.indexOf(needle) >= 0) {"
                + "      input.scrollIntoView({block:'center'});"
                + "      return normalizeCheckbox(input)||input;"
                + "    }"
                + "    if (input.id) {"
                + "      var forLabel = scope.querySelector('label[for=\"' + input.id.replace(/\"/g,'') + '\"]');"
                + "      if (forLabel && norm(forLabel.textContent).indexOf(needle) >= 0) {"
                + "        input.scrollIntoView({block:'center'});"
                + "        return normalizeCheckbox(input)||input;"
                + "      }"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function findManageColumnsScrollers(panel, scope){"
                + "  var scrollers=[], seen=new Set();"
                + "  function add(el){if(el&&!seen.has(el)){seen.add(el);scrollers.push(el);}}"
                + "  if(scope){scope.querySelectorAll('.multi-options-list,.multi-table-column-container')"
                + "    .forEach(function(el){add(el);});}"
                + "  if(panel){panel.querySelectorAll('.multi-options-list,.multi-table-column-container')"
                + "    .forEach(function(el){add(el);});}"
                + "  var msc=(panel&&panel.querySelector('msc-custom-wrapper-dropdown'))"
                + "    ||document.querySelector('msc-custom-wrapper-dropdown');"
                + "  if(msc){"
                + "    add(msc.querySelector(':scope > div > div:nth-child(3) > div:nth-child(2)'));"
                + "    msc.querySelectorAll('div').forEach(function(d){"
                + "      if(d.scrollHeight>d.clientHeight+8)add(d);"
                + "    });"
                + "  }"
                + "  if(scope)add(scope);"
                + "  if(panel)add(panel);"
                + "  return scrollers.length?scrollers:[scope||panel||document.body];"
                + "}"
                + "function findCheckbox(labelName, section, exactOnly, columnId){"
                + "  var panel = findPanel();"
                + "  if (!panel) return null;"
                + "  var target = norm(labelName);"
                + "  var scope = findSectionScope(panel, section) || panel;"
                + "  var cb = findByColumnId(scope, columnId, section);"
                + "  if (cb) return cb;"
                + "  cb = findByColumnId(panel, columnId, section);"
                + "  if (cb) return cb;"
                + "  cb = findInScope(scope, target, true);"
                + "  if (!cb && !exactOnly) cb = findInScope(scope, target, false);"
                + "  if (cb) return cb;"
                + "  var scrollers = findManageColumnsScrollers(panel, scope);"
                + "  for (var s = 0; s < scrollers.length; s++) {"
                + "    var scroller = scrollers[s];"
                + "    scroller.scrollTop = 0;"
                + "    for (var pass = 0; pass < 24; pass++) {"
                + "      cb = findByColumnId(scroller, columnId, section);"
                + "      if (cb) return cb;"
                + "      cb = findInScope(scroller, target, true);"
                + "      if (!cb && !exactOnly) cb = findInScope(scroller, target, false);"
                + "      if (!cb) cb = findByCheckboxRowText(scroller, target, true);"
                + "      if (!cb && !exactOnly) cb = findByCheckboxRowText(scroller, target, false);"
                + "      if (!cb) cb = findColumnLabelInScope(scroller, target, true);"
                + "      if (!cb && !exactOnly) cb = findColumnLabelInScope(scroller, target, false);"
                + "      if (cb) return cb;"
                + "      scroller.scrollTop = (scroller.scrollTop || 0) + 260;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}";
    }

    public static boolean setColumnChecked(String columnName, String section, boolean checked) {
        return findColumnCheckbox(columnName, section, checked, resolveListingColumnId(columnName, section));
    }

    public static void scrollSavedViewIntoList(String viewName) {
        String escaped = esc(viewName);
        run(CONTEXT_MENU_JS
                + "return scrollViewIntoList('" + escaped + "');");
    }

    /** Select a saved table view from the Manage Columns view dropdown (TC_006 / TC_007 parity). */
    public static boolean selectSavedViewViaScript(String viewName) {
        if (viewName == null || viewName.trim().isEmpty()) {
            return false;
        }
        if (!isManageColumnsPanelOpen()) {
            openManageColumnsPanel();
            sleep(400);
        }
        String escaped = esc(viewName);
        return truthy(run(
                IS_VISIBLE_FN
                        + CONTEXT_MENU_JS
                        + "var end = Date.now() + 8000;"
                        + "while (Date.now() < end) {"
                        + "  if (openViewDropdown()) break;"
                        + "}"
                        + "if (!openViewDropdown()) return false;"
                        + "scrollViewIntoList('" + escaped + "');"
                        + "var row = findViewRow('" + escaped + "');"
                        + "if (!row) return false;"
                        + "row.click();"
                        + "return true;"));
    }

    public static boolean isActiveTableView(String viewName) {
        String escaped = esc(viewName);
        return truthy(run(
                "var name = '" + escaped + "';"
                        + "var btn = document.querySelector('#tableViewButton');"
                        + "if (!btn) return false;"
                        + "return (btn.textContent || '').indexOf(name) >= 0;"));
    }

    public static boolean isViewNameVisible(String viewName) {
        String escaped = esc(viewName);
        if (isActiveTableView(viewName)) {
            return true;
        }
        if (truthy(run(
                "var name = '" + escaped + "';"
                        + "var btn = document.querySelector('#tableViewButton');"
                        + "if (btn && (btn.textContent || '').indexOf(name) >= 0) return true;"
                        + "var panel = document.querySelector('.wrapper-dropdown-container');"
                        + "if (panel && (panel.textContent || '').indexOf(name) >= 0) return true;"
                        + "return false;"))) {
            return true;
        }
        installNetworkCaptureHook();
        run("window.__ffViewLookupResult='pending';"
                + TABLE_VIEW_API_JS
                + "(async function(){"
                + "  try {"
                + "    var views=await fetchTableViews();"
                + "    window.__ffViewLookupResult=views.some(function(v){return normName(v.name)==='" + escaped + "';})?'true':'false';"
                + "  } catch(e){window.__ffViewLookupResult='false';}"
                + "})();");
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            Object result = run("return window.__ffViewLookupResult;");
            if ("true".equalsIgnoreCase(String.valueOf(result))) {
                return true;
            }
            if ("false".equalsIgnoreCase(String.valueOf(result))) {
                return false;
            }
            sleep(300);
        }
        return false;
    }

    public static boolean clickSaveNewViewButton() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var panel=document.querySelector('.wrapper-dropdown-container');"
                        + "if(!panel)return false;"
                        + "var scrollers=panel.querySelectorAll('.multi-table-column-container,.multi-options-list');"
                        + "for(var s=0;s<scrollers.length;s++){scrollers[s].scrollTop=scrollers[s].scrollHeight;}"
                        + "panel.scrollTop=panel.scrollHeight;"
                        + "var buttons=panel.querySelectorAll('button');"
                        + "for(var i=0;i<buttons.length;i++){"
                        + "  var t=(buttons[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(t==='Save new view'){buttons[i].scrollIntoView({block:'center'});buttons[i].click();return true;}"
                        + "}"
                        + "var nodes=panel.querySelectorAll('button,span,a');"
                        + "for(var j=0;j<nodes.length;j++){"
                        + "  var u=(nodes[j].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(u==='Save new view'&&isVisible(nodes[j])){"
                        + "    nodes[j].scrollIntoView({block:'center'});nodes[j].click();return true;"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    public static boolean isSaveModalVisible() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var modal=document.querySelector('ngb-modal-window.show,ngb-modal-window,[role=dialog].modal.show,[role=dialog]');"
                        + "if(modal&&isVisible(modal))return true;"
                        + "var inputs=document.querySelectorAll("
                        + "'input[placeholder*=\"table view\" i],input[placeholder*=\"view name\" i],"
                        + "ngb-modal-window input,[role=dialog] input,.modal input');"
                        + "for(var i=0;i<inputs.length;i++){if(isVisible(inputs[i]))return true;}"
                        + "return false;"));
    }

    public static boolean waitForSaveModalVisible(int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (isSaveModalVisible()) {
                return true;
            }
            sleep(300);
        }
        return isSaveModalVisible();
    }

    public static boolean fillSaveModalViewName(String viewName) {
        String escaped = esc(viewName);
        return truthy(run(
                IS_VISIBLE_FN
                        + "function findInput(){"
                        + "  var inputs=document.querySelectorAll("
                        + "  'input[placeholder*=\"table view\" i],input[placeholder*=\"view name\" i],"
                        + "  ngb-modal-window input,[role=dialog] input,.modal input');"
                        + "  for(var i=0;i<inputs.length;i++){if(isVisible(inputs[i]))return inputs[i];}"
                        + "  return null;"
                        + "}"
                        + "var input=findInput();"
                        + "if(!input)return false;"
                        + "input.focus();input.value='';input.value='" + escaped + "';"
                        + "input.dispatchEvent(new Event('input',{bubbles:true}));"
                        + "input.dispatchEvent(new Event('change',{bubbles:true}));"
                        + "return (input.value||'').indexOf('" + escaped + "')>=0;"));
    }

    public static boolean clickSaveNewOnModal() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "function findModalInput(){"
                        + "  var inputs=document.querySelectorAll("
                        + "  'input[placeholder*=\"table view\" i],input[placeholder*=\"view name\" i],"
                        + "  ngb-modal-window input,[role=dialog] input,.modal input');"
                        + "  for(var i=0;i<inputs.length;i++){if(isVisible(inputs[i]))return inputs[i];}"
                        + "  return null;"
                        + "}"
                        + "var input=findModalInput();"
                        + "if(!input||!(input.value||'').trim())return false;"
                        + "var root=input.closest('ngb-modal-window,[role=dialog],.modal,.cdk-overlay-pane')||document.body;"
                        + "var buttons=root.querySelectorAll('button,span,a');"
                        + "for(var i=0;i<buttons.length;i++){"
                        + "  var t=(buttons[i].textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();"
                        + "  if((t==='save new'||t==='save')&&isVisible(buttons[i])){buttons[i].click();return true;}"
                        + "}"
                        + "var all=document.querySelectorAll('ngb-modal-window button,[role=dialog] button,.modal button');"
                        + "for(var j=0;j<all.length;j++){"
                        + "  var u=(all[j].textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();"
                        + "  if((u==='save new'||u==='save')&&isVisible(all[j])){all[j].click();return true;}"
                        + "}"
                        + "return false;"));
    }

    public static boolean isTableViewCreatedToastVisible() {
        return truthy(run(
                "var nodes=document.querySelectorAll('.toast,.toast-body,.alert,[role=alert],.cdk-overlay-pane');"
                        + "for(var i=0;i<nodes.length;i++){"
                        + "  if(/table view created/i.test(nodes[i].textContent||''))return true;"
                        + "}"
                        + "return /table view created/i.test(document.body?(document.body.textContent||''):'');"));
    }

    public static boolean saveTableViewViaScript(String viewName) {
        installNetworkCaptureHook();
        if (!isSaveModalVisible()) {
            if (!clickSaveNewViewButton()) {
                return false;
            }
            if (!waitForSaveModalVisible(20000)) {
                return false;
            }
        }
        if (!fillSaveModalViewName(viewName)) {
            return false;
        }
        sleep(300);
        if (!clickSaveNewOnModal()) {
            return false;
        }
        long end = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < end) {
            if (isTableViewCreatedToastVisible() || isViewNameVisible(viewName)) {
                return true;
            }
            sleep(500);
        }
        return isViewNameVisible(viewName);
    }

    /**
     * Saves via in-browser GraphQL so network hooks capture the full column payload (matches TC_006 UI save).
     */
    public static boolean saveTableViewItemViaBrowserGraphql(org.json.simple.JSONObject payload) {
        if (payload == null || payload.get("name") == null) {
            return false;
        }
        installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        try {
            String payloadJson = payload.toJSONString()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\r", "")
                    .replace("\n", "");
            Object result = run(
                    NETWORK_HOOK_JS + TABLE_VIEW_API_JS
                            + "try {"
                            + "  var item = JSON.parse('" + payloadJson + "');"
                            + "  var json = gqlSync('SaveTableView', SAVE_TABLE_VIEW, {item: item});"
                            + "  var saved = json.data && json.data.saveTableView;"
                            + "  if (!saved || !saved.id) return null;"
                            + "  item.id = saved.id;"
                            + "  item.name = saved.name || item.name;"
                            + "  window.__ffLastSavedView = item;"
                            + "  window.__ffLastSaveTableViewResponse = item;"
                            + "  if (!window.__ffSavedViews) window.__ffSavedViews = {};"
                            + "  window.__ffSavedViews[item.name] = item;"
                            + "  return saved.id;"
                            + "} catch (e) { return 'ERR:' + (e.message || e); }");
            if (result == null || "null".equals(String.valueOf(result))
                    || String.valueOf(result).startsWith("ERR:")) {
                if (result != null && String.valueOf(result).startsWith("ERR:")) {
                    Logger.logConsoleMessage("GraphQL browser save failed: " + result);
                }
                return false;
            }
            TableViewGraphqlClient.cacheViewId(String.valueOf(payload.get("name")), String.valueOf(result));
            Logger.logConsoleMessage("GraphQL browser save: id=" + result + " name=" + payload.get("name"));
            return true;
        } catch (Exception e) {
            Logger.logConsoleMessage("GraphQL browser save exception: " + e.getMessage());
            return false;
        }
    }

    private static final String CONTEXT_MENU_JS =
            "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                    + "function findViewTextNode(name){"
                    + "  var nodes = document.querySelectorAll("
                    + "'.wrapper-dropdown-container span,.wrapper-dropdown-container label,"
                    + ".cdk-overlay-pane span,.cdk-overlay-pane label');"
                    + "  for (var j = 0; j < nodes.length; j++) {"
                    + "    if (!isVisible(nodes[j]) || norm(nodes[j].textContent) !== name) continue;"
                    + "    return nodes[j];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findViewRow(name){ return findViewTextNode(name); }"
                    + "function fireContextMenu(el){"
                    + "  el.scrollIntoView({block:'center'});"
                    + "  var r = el.getBoundingClientRect();"
                    + "  var cx = r.left + r.width / 2, cy = r.top + r.height / 2;"
                    + "  ['pointerover','mouseover','pointerdown','mousedown','contextmenu','pointerup','mouseup']"
                    + "    .forEach(function(type){"
                    + "      el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,view:window,"
                    + "button:2,buttons:type.indexOf('down')>=0?2:0,clientX:cx,clientY:cy}));"
                    + "    });"
                    + "}"
                    + "function findMenu(label){"
                    + "  var want = label.toLowerCase();"
                    + "  var roots = document.querySelectorAll("
                    + "'.cdk-overlay-container,.cdk-overlay-pane,[role=menu],.mat-mdc-menu-panel');"
                    + "  var items = document.querySelectorAll("
                    + "'[role=menuitem],.mat-mdc-menu-item,.dropdown-item,[class*=menu-item],"
                    + "[class*=context-menu],.cdk-menu-item,.cdk-overlay-pane button,.cdk-overlay-pane span,button,span');"
                    + "  for (var i = 0; i < items.length; i++) {"
                    + "    var t = norm(items[i].textContent).toLowerCase();"
                    + "    if (!isVisible(items[i])) continue;"
                    + "    if (t === want) return items[i];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function waitAndClickMenu(label){"
                    + "  var end = Date.now() + 5000;"
                    + "  while (Date.now() < end) {"
                    + "    var item = findMenu(label);"
                    + "    if (item) { item.click(); return true; }"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "function openViewDropdown(){"
                    + "  var panel = document.querySelector('.wrapper-dropdown-container');"
                    + "  if (!panel) return false;"
                    + "  var dropdown = panel.querySelector('.dropdown-reset-container');"
                    + "  if (dropdown) { dropdown.click(); return true; }"
                    + "  var team = panel.querySelector('.option.team-filter');"
                    + "  if (team) { team.click(); return true; }"
                    + "  return false;"
                    + "}"
                    + "function scrollViewIntoList(name){"
                    + "  var node = findViewTextNode(name);"
                    + "  if (node) node.scrollIntoView({block:'center'});"
                    + "  return !!node;"
                    + "}";

    private static final String NETWORK_HOOK_JS =
            "(function(){"
                    + "if(window.__ffApiHookInstalled)return true;"
                    + "window.__ffApiHookInstalled=true;"
                    + "window.__ffApiCalls=[];"
                    + "window.__ffGqlResponses=[];"
                    + "window.__ffSavedViews={};"
                    + "function pushCall(method,url,body){"
                    + "  window.__ffApiCalls.push({method:(method||'GET').toUpperCase(),url:String(url||''),body:body,t:Date.now()});"
                    + "}"
                    + "function pushGqlResponse(reqBody,json){"
                    + "  if(!window.__ffGqlResponses)window.__ffGqlResponses=[];"
                    + "  window.__ffGqlResponses.push({req:reqBody,res:json,t:Date.now()});"
                    + "  if(window.__ffGqlResponses.length>50)window.__ffGqlResponses.shift();"
                    + "}"
                    + "function rememberSavedView(json){"
                    + "  try{"
                    + "    var view=json&&json.data&&(json.data.saveTableView||json.data.updateTableView);"
                    + "    if(view&&view.id){"
                    + "      window.__ffSavedViews[view.name]=view;"
                    + "      window.__ffLastSavedView=view;"
                    + "      window.__ffLastSaveTableViewResponse=view;"
                    + "    }"
                    + "  }catch(e){}"
                    + "}"
                    + "function rememberTableViews(json){"
                    + "  try{"
                    + "    var views=json&&json.data&&json.data.tableViews;"
                    + "    if(!Array.isArray(views))return;"
                    + "    if(!window.__ffSavedViews)window.__ffSavedViews={};"
                    + "    for(var i=0;i<views.length;i++){"
                    + "      if(views[i]&&views[i].id&&views[i].name){window.__ffSavedViews[views[i].name]=views[i];}"
                    + "    }"
                    + "  }catch(e){}"
                    + "}"
                    + "function rememberFilterOrders(json){"
                    + "  try{"
                    + "    if(!window.__ffFilterOrdersByOrderId){window.__ffFilterOrdersByOrderId={};}"
                    + "    function rememberOrder(o){"
                    + "      if(!o||!o.statusTooltipDetails)return;"
                    + "      var id=String(o.orderId||o.id||'');"
                    + "      if(id){window.__ffFilterOrdersByOrderId[id.toLowerCase()]=o.statusTooltipDetails;}"
                    + "    }"
                    + "    var orders=(json&&json.data&&json.data.filterOrders&&json.data.filterOrders.orders)"
                    + "      ||(json&&json.data&&json.data.orders)||[];"
                    + "    if(Array.isArray(orders)){for(var i=0;i<orders.length;i++){rememberOrder(orders[i]);}}"
                    + "    function walk(node){"
                    + "      if(!node||typeof node!=='object')return;"
                    + "      if(Array.isArray(node.statusTooltipDetails)&&(node.orderId||node.id)){rememberOrder(node);}"
                    + "      if(Array.isArray(node)){for(var j=0;j<node.length;j++){walk(node[j]);}return;}"
                    + "      for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k)){walk(node[k]);}}"
                    + "    }"
                    + "    walk(json);"
                    + "  }catch(e){}"
                    + "}"
                    + "function rememberExportFromGql(json){"
                    + "  try{"
                    + "    if(!window.__ffExportUrls)window.__ffExportUrls=[];"
                    + "    var text=JSON.stringify(json||{});"
                    + "    var re=/https?:[^\"'\\\\s]*(?:cloudfront\\\\.net|ops-console-backend-export)[^\"'\\\\s]*(?:FulfillmentExport)?[^\"'\\\\s]*\\\\.xlsx[^\"'\\\\s]*/gi;"
                    + "    var m;while((m=re.exec(text))!==null){window.__ffExportUrls.push(m[0]);}"
                    + "  }catch(e){}"
                    + "}"
                    + "var origFetch=window.fetch;"
                    + "if(origFetch){window.__ffOrigFetch=origFetch;"
                    + "  window.fetch=function(input,init){"
                    + "    var url=typeof input==='string'?input:(input&&input.url?input.url:'');"
                    + "    var method=(init&&init.method)||'GET';"
                    + "    pushCall(method,url,init&&init.body);"
                    + "    return origFetch.apply(this,arguments).then(function(resp){"
                    + "      if(url.indexOf('graphql')>=0){"
                    + "        resp.clone().json().then(function(json){"
                    + "          rememberSavedView(json);rememberTableViews(json);rememberFilterOrders(json);"
                    + "          rememberExportFromGql(json);"
                    + "          pushGqlResponse(init&&init.body,json);"
                    + "        }).catch(function(){});"
                    + "        if(init&&init.body){"
                    + "          try{"
                    + "            var body=JSON.parse(init.body);"
                    + "            if(body.operationName==='TableViews'){window.__ffTableViewsQuery=body.query;window.__ffTableViewsVars=body.variables;}"
                    + "          }catch(e){}"
                    + "        }"
                    + "      }"
                    + "      return resp;"
                    + "    });"
                    + "  };"
                    + "}"
                    + "var xhrOpen=XMLHttpRequest.prototype.open;"
                    + "var xhrSend=XMLHttpRequest.prototype.send;"
                    + "var xhrSetHeader=XMLHttpRequest.prototype.setRequestHeader;"
                    + "XMLHttpRequest.prototype.setRequestHeader=function(h,v){"
                    + "  if(String(h||'').toLowerCase()==='authorization'&&v){window.__ffAuthHeader=String(v);}"
                    + "  return xhrSetHeader.apply(this,arguments);"
                    + "};"
                    + "XMLHttpRequest.prototype.open=function(method,url){this.__ffMethod=method;this.__ffUrl=url;return xhrOpen.apply(this,arguments);};"
                    + "XMLHttpRequest.prototype.send=function(body){"
                    + "  pushCall(this.__ffMethod,this.__ffUrl,body);"
                    + "  var xhr=this;"
                    + "  xhr.addEventListener('load',function(){"
                    + "    try{"
                    + "      if(!xhr.__ffUrl||xhr.__ffUrl.indexOf('graphql')<0||!xhr.responseText)return;"
                    + "      var json=JSON.parse(xhr.responseText);"
                    + "      rememberSavedView(json);"
                    + "      rememberTableViews(json);"
                    + "      rememberFilterOrders(json);"
                    + "      rememberExportFromGql(json);"
                    + "      pushGqlResponse(body,json);"
                    + "    }catch(e){}"
                    + "  });"
                    + "  try{"
                    + "    if(body&&xhr.__ffUrl&&xhr.__ffUrl.indexOf('graphql')>=0){"
                    + "      var reqBody=JSON.parse(body);"
                    + "      if(reqBody.operationName==='TableViews'){window.__ffTableViewsQuery=reqBody.query;window.__ffTableViewsVars=reqBody.variables;}"
                    + "    }"
                    + "  }catch(e){}"
                    + "  return xhrSend.apply(this,arguments);"
                    + "};"
                    + "return true;"
                    + "})();";

    private static final String TABLE_VIEW_API_JS =
            "function normName(v){return (v||'').replace(/\\s+/g,' ').trim();}"
                    + "function viewNameFromObj(v){"
                    + "  return normName(v.name||v.viewName||v.tableViewName||v.label||v.displayName||'');"
                    + "}"
                    + "function viewIdFromObj(v){"
                    + "  return v.id||v.viewId||v.tableViewId||v.tableViewID||v.uuid||null;"
                    + "}"
                    + "function findViewIdInDom(name){"
                    + "  var n=normName(name);"
                    + "  var roots=document.querySelectorAll('.wrapper-dropdown-container,.cdk-overlay-pane');"
                    + "  for(var r=0;r<roots.length;r++){"
                    + "    var nodes=roots[r].querySelectorAll('label,span,.option,input[type=radio],input[type=checkbox]');"
                    + "    for(var i=0;i<nodes.length;i++){"
                    + "      var text=normName(nodes[i].textContent||nodes[i].value||'');"
                    + "      if(text!==n&&normName(nodes[i].getAttribute&&nodes[i].getAttribute('aria-label'))!==n)continue;"
                    + "      if(nodes[i].tagName==='INPUT'&&(nodes[i].value||nodes[i].id))return nodes[i].value||nodes[i].id;"
                    + "      var row=nodes[i].closest?nodes[i].closest('.option,label,div[role=option]'):null;"
                    + "      var input=(row&&row.querySelector)?row.querySelector('input[type=radio],input[type=checkbox]'):null;"
                    + "      if(!input&&nodes[i].previousElementSibling&&nodes[i].previousElementSibling.tagName==='INPUT'){"
                    + "        input=nodes[i].previousElementSibling;"
                    + "      }"
                    + "      if(input&&(input.value||input.id))return input.value||input.id;"
                    + "      if(nodes[i].dataset&&(nodes[i].dataset.viewId||nodes[i].dataset.id))return nodes[i].dataset.viewId||nodes[i].dataset.id;"
                    + "      if(row&&row.dataset&&(row.dataset.viewId||row.dataset.id))return row.dataset.viewId||row.dataset.id;"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function collectViews(payload){"
                    + "  if(!payload)return [];"
                    + "  if(Array.isArray(payload))return payload;"
                    + "  return payload.items||payload.data||payload.tableViews||payload.views||payload.results||[];"
                    + "}"
                    + "function findCreateUrl(){"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    var c=calls[i];"
                    + "    if(c.method==='POST'&&/(table|view|column|grid|preference|layout|setting|config|graphql|fulfillment)/i.test(c.url))"
                    + "      return c.url.split('?')[0];"
                    + "  }"
                    + "  var perf=(window.performance&&performance.getEntriesByType)?performance.getEntriesByType('resource'):[];"
                    + "  for(var j=perf.length-1;j>=0;j--){"
                    + "    var u=perf[j].name||'';"
                    + "    if(/(table.?view|tableview|grid.?view|column.?layout|user.?view|saved.?view|table-view)/i.test(u))"
                    + "      return u.split('?')[0];"
                    + "  }"
                    + "  for(var k=calls.length-1;k>=0;k--){"
                    + "    if(/(table|view|column|grid|preference|layout|fulfillment)/i.test(calls[k].url))"
                    + "      return calls[k].url.split('?')[0];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findLastPostBody(){"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    if(calls[i].method==='POST'&&calls[i].body)return calls[i].body;"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "async function fetchJson(url,method,body){"
                    + "  var opts={method:method||'GET',credentials:'include',headers:{'Accept':'application/json','Content-Type':'application/json'}};"
                    + "  if(body!==undefined&&body!==null)opts.body=typeof body==='string'?body:JSON.stringify(body);"
                    + "  var resp=await fetch(url,opts);"
                    + "  if(!resp.ok)throw new Error('HTTP '+resp.status+' '+url);"
                    + "  var text=await resp.text();"
                    + "  try{return text?JSON.parse(text):{};}catch(e){return {raw:text};}"
                    + "}"
                    + "var GQL_URL='https://fc-api.paramountmsc.com/graphql/';"
                    + "function resolveGqlUrl(){"
                    + "  if(window.__ffGqlUrl)return window.__ffGqlUrl;"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    if(calls[i].url&&calls[i].url.indexOf('graphql')>=0){"
                    + "      window.__ffGqlUrl=calls[i].url.split('?')[0];return window.__ffGqlUrl;"
                    + "    }"
                    + "  }"
                    + "  var perf=(window.performance&&performance.getEntriesByType)?performance.getEntriesByType('resource'):[];"
                    + "  for(var j=perf.length-1;j>=0;j--){"
                    + "    var u=perf[j].name||'';"
                    + "    if(u.indexOf('graphql')>=0){window.__ffGqlUrl=u.split('?')[0];return window.__ffGqlUrl;}"
                    + "  }"
                    + "  window.__ffGqlUrl=GQL_URL;return window.__ffGqlUrl;"
                    + "}"
                    + "var TABLE_VIEWS_QUERY='query TableViews($user: String!, $tableType: String!) { tableViews(user: $user, tableType: $tableType) { id tableType name columns { orderTable { label selected __typename } packageTable { label selected __typename } lineItemTable { label selected __typename } __typename } teamView default access createdBy updatedBy updatedAt __typename } }';"
                    + "var UPDATE_TABLE_VIEW='mutation UpdateTableView($item: TableViewInput!) { updateTableView(item: $item) { id name __typename } }';"
                    + "var SAVE_TABLE_VIEW='mutation SaveTableView($item: TableViewInput!) { saveTableView(item: $item) { id name __typename } }';"
                    + "var DELETE_TABLE_VIEW='mutation DeleteTableView($id: String!) { deleteTableView(id: $id) { id name __typename } }';"
                    + "function resolveAuthHeader(){"
                    + "  if(window.__ffAuthHeader){"
                    + "    var h=String(window.__ffAuthHeader);"
                    + "    return h.indexOf('Bearer ')===0?h:'Bearer '+h;"
                    + "  }"
                    + "  function walk(o){"
                    + "    if(!o||typeof o!=='object')return null;"
                    + "    if(typeof o.accessToken==='string')return o.accessToken;"
                    + "    if(o.accessToken&&o.accessToken.accessToken)return o.accessToken.accessToken;"
                    + "    if(typeof o.token==='string')return o.token;"
                    + "    if(typeof o.idToken==='string')return o.idToken;"
                    + "    for(var k in o){if(Object.prototype.hasOwnProperty.call(o,k)){var v=walk(o[k]);if(v)return v;}}"
                    + "    return null;"
                    + "  }"
                    + "  try{"
                    + "    var stores=[localStorage,sessionStorage];"
                    + "    for(var s=0;s<stores.length;s++){"
                    + "      var store=stores[s];"
                    + "      for(var i=0;i<store.length;i++){"
                    + "        var v=store.getItem(store.key(i));"
                    + "        if(!v)continue;"
                    + "        try{var t=walk(JSON.parse(v));if(t)return t.indexOf('Bearer')===0?t:'Bearer '+t;}catch(e){}"
                    + "        if(/^ey[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+/.test(v))return 'Bearer '+v;"
                    + "      }"
                    + "    }"
                    + "  }catch(e){}"
                    + "  return null;"
                    + "}"
                    + "function gqlViaXhr(operationName,query,variables){"
                    + "  return new Promise(function(resolve,reject){"
                    + "    var url=resolveGqlUrl();"
                    + "    var xhr=new XMLHttpRequest();"
                    + "    xhr.open('POST',url,true);"
                    + "    xhr.withCredentials=true;"
                    + "    xhr.setRequestHeader('Content-Type','application/json');"
                    + "    xhr.setRequestHeader('Accept','application/json');"
                    + "    var auth=resolveAuthHeader();"
                    + "    if(auth)xhr.setRequestHeader('Authorization',auth);"
                    + "    xhr.onload=function(){"
                    + "      try{"
                    + "        var json=JSON.parse(xhr.responseText||'{}');"
                    + "        if(xhr.status<200||xhr.status>=300)reject(new Error('HTTP '+xhr.status));"
                    + "        else if(json.errors&&json.errors.length)reject(new Error(json.errors[0].message||'graphql error'));"
                    + "        else resolve(json);"
                    + "      }catch(e){reject(e);}"
                    + "    };"
                    + "    xhr.onerror=function(){reject(new Error('XHR network error'));};"
                    + "    xhr.send(JSON.stringify({operationName:operationName,variables:variables,query:query,extensions:{clientLibrary:{name:'@apollo/client',version:'4.1.0'}}}));"
                    + "  });"
                    + "}"
                    + "async function gql(operationName,query,variables){"
                    + "  try{return await gqlViaXhr(operationName,query,variables);}"
                    + "  catch(xhrErr){"
                    + "    var fetchFn=window.__ffOrigFetch||window.fetch;"
                    + "    if(!fetchFn)throw xhrErr;"
                    + "    var headers={'Content-Type':'application/json','Accept':'application/json'};"
                    + "    var auth=resolveAuthHeader();"
                    + "    if(auth)headers.Authorization=auth;"
                    + "    var resp=await fetchFn(resolveGqlUrl(),{method:'POST',credentials:'include',headers:headers,"
                    + "body:JSON.stringify({operationName:operationName,variables:variables,query:query,extensions:{clientLibrary:{name:'@apollo/client',version:'4.1.0'}}})});"
                    + "    var json=await resp.json();"
                    + "    if(!resp.ok)throw new Error('HTTP '+resp.status);"
                    + "    if(json.errors&&json.errors.length)throw new Error(json.errors[0].message||'graphql error');"
                    + "    return json;"
                    + "  }"
                    + "}"
                    + "async function fetchTableViews(){"
                    + "  function decodeUserFromAuth(){"
                    + "    var auth=resolveAuthHeader();"
                    + "    if(!auth)return null;"
                    + "    var token=String(auth).replace(/^Bearer\\s+/i,'').trim();"
                    + "    try{"
                    + "      var parts=token.split('.');"
                    + "      if(parts.length<2)return null;"
                    + "      var b64=parts[1].replace(/-/g,'+').replace(/_/g,'/');"
                    + "      while(b64.length%4)b64+='=';"
                    + "      var payload=JSON.parse(atob(b64));"
                    + "      var keys=['preferred_username','email','upn','unique_name','sub'];"
                    + "      for(var i=0;i<keys.length;i++){if(payload[keys[i]])return payload[keys[i]];}"
                    + "    }catch(e){}"
                    + "    return null;"
                    + "  }"
                    + "  var user=(window.__ffTableViewsVars&&window.__ffTableViewsVars.user)||decodeUserFromAuth();"
                    + "  if(!user)throw new Error('Cannot resolve user for TableViews');"
                    + "  var tableType=(window.__ffTableViewsVars&&window.__ffTableViewsVars.tableType)||'ORDER';"
                    + "  var res=await gql('TableViews',TABLE_VIEWS_QUERY,{user:user,tableType:tableType});"
                    + "  return (res.data&&res.data.tableViews)||[];"
                    + "}"
                    + "function findSavedViewPayload(oldName){"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    var c=calls[i];"
                    + "    if(!c.url||c.url.indexOf('graphql')<0||!c.body)continue;"
                    + "    try{"
                    + "      var body=JSON.parse(c.body);"
                    + "      if(body.operationName==='SaveTableView'&&body.variables&&body.variables.item&&normName(body.variables.item.name)===oldName){"
                    + "        return body.variables.item;"
                    + "      }"
                    + "    }catch(e){}"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findInSavedViews(name){"
                    + "  if(!window.__ffSavedViews)return null;"
                    + "  var keys=Object.keys(window.__ffSavedViews);"
                    + "  for(var i=0;i<keys.length;i++){if(normName(keys[i])===name)return window.__ffSavedViews[keys[i]];}"
                    + "  return null;"
                    + "}"
                    + "function stripTypename(obj){"
                    + "  if(!obj||typeof obj!=='object')return obj;"
                    + "  if(Array.isArray(obj))return obj.map(stripTypename);"
                    + "  var out={};"
                    + "  for(var k in obj){if(Object.prototype.hasOwnProperty.call(obj,k)&&k!=='__typename'){out[k]=stripTypename(obj[k]);}}"
                    + "  return out;"
                    + "}"
                    + "async function renameViaGraphql(oldName,newName){"
                    + "  var item=null;"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===oldName){item=window.__ffLastSavedView;}"
                    + "  if(!item){item=findInSavedViews(oldName);}"
                    + "  if(!item){item=findSavedViewPayload(oldName);}"
                    + "  if(item&&!item.id&&window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===oldName){item.id=window.__ffLastSavedView.id;}"
                    + "  if(!item||!item.id){"
                    + "    var views=await fetchTableViews();"
                    + "    item=views.find(function(v){return normName(v.name)===oldName;});"
                    + "  }"
                    + "  if(!item||!item.id)throw new Error('view-not-found');"
                    + "  var payload=stripTypename(JSON.parse(JSON.stringify(item)));"
                    + "  payload.name=newName;"
                    + "  if(!payload.tableType)payload.tableType='ORDER';"
                    + "  try{"
                    + "    await gql('UpdateTableView',UPDATE_TABLE_VIEW,{item:payload});"
                    + "  }catch(e1){"
                    + "    await gql('SaveTableView',SAVE_TABLE_VIEW,{item:payload});"
                    + "  }"
                    + "  var renamed=Object.assign({},payload,{name:newName});"
                    + "  window.__ffLastSavedView=renamed;"
                    + "  if(!window.__ffSavedViews)window.__ffSavedViews={};"
                    + "  delete window.__ffSavedViews[oldName];"
                    + "  window.__ffSavedViews[newName]=renamed;"
                    + "}"
                    + "async function deleteViaGraphql(name){"
                    + "  var view=null;"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===name){view=window.__ffLastSavedView;}"
                    + "  if(!view){view=findInSavedViews(name);}"
                    + "  if(!view||!view.id){"
                    + "    var views=await fetchTableViews();"
                    + "    view=views.find(function(v){return normName(v.name)===name;});"
                    + "  }"
                    + "  if(!view||!view.id)throw new Error('view-not-found');"
                    + "  try{await gql('DeleteTableView',DELETE_TABLE_VIEW,{id:view.id});}"
                    + "  catch(e1){await gql('DeleteTableView','mutation DeleteTableView($item: TableViewInput!) { deleteTableView(item: $item) }',{item:{id:view.id,name:view.name,tableType:view.tableType||'ORDER'}});}"
                    + "  if(window.__ffSavedViews)delete window.__ffSavedViews[name];"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===name){window.__ffLastSavedView=null;}"
                    + "}"
                    + "function gqlSync(operationName,query,variables){"
                    + "  var url=resolveGqlUrl();"
                    + "  var xhr=new XMLHttpRequest();"
                    + "  xhr.open('POST',url,false);"
                    + "  xhr.withCredentials=true;"
                    + "  xhr.setRequestHeader('Content-Type','application/json');"
                    + "  xhr.setRequestHeader('Accept','application/json');"
                    + "  var auth=resolveAuthHeader();"
                    + "  if(auth)xhr.setRequestHeader('Authorization',auth);"
                    + "  xhr.send(JSON.stringify({operationName:operationName,variables:variables,query:query,extensions:{clientLibrary:{name:'@apollo/client',version:'4.1.0'}}}));"
                    + "  if(xhr.status<200||xhr.status>=300)throw new Error('HTTP '+xhr.status);"
                    + "  var json=JSON.parse(xhr.responseText||'{}');"
                    + "  if(json.errors&&json.errors.length)throw new Error(json.errors[0].message||'graphql error');"
                    + "  return json;"
                    + "}"
                    + "function fetchTableViewsSync(){"
                    + "  var res=gqlSync('TableViews',TABLE_VIEWS_QUERY,{tableType:'ORDER'});"
                    + "  return (res.data&&res.data.tableViews)||[];"
                    + "}"
                    + "function renameViaGraphqlSync(oldName,newName){"
                    + "  var item=null;"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===oldName){item=window.__ffLastSavedView;}"
                    + "  if(!item){item=findInSavedViews(oldName);}"
                    + "  if(!item){item=findSavedViewPayload(oldName);}"
                    + "  if(item&&!item.id&&window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===oldName){item.id=window.__ffLastSavedView.id;}"
                    + "  if(!item||!item.id){"
                    + "    var views=fetchTableViewsSync();"
                    + "    item=views.find(function(v){return normName(v.name)===oldName;});"
                    + "  }"
                    + "  if(!item||!item.id)throw new Error('view-not-found');"
                    + "  var payload=stripTypename(JSON.parse(JSON.stringify(item)));"
                    + "  payload.name=newName;"
                    + "  if(!payload.tableType)payload.tableType='ORDER';"
                    + "  try{gqlSync('UpdateTableView',UPDATE_TABLE_VIEW,{item:payload});}"
                    + "  catch(e1){gqlSync('SaveTableView',SAVE_TABLE_VIEW,{item:payload});}"
                    + "  var renamed=Object.assign({},payload,{name:newName});"
                    + "  window.__ffLastSavedView=renamed;"
                    + "  if(!window.__ffSavedViews)window.__ffSavedViews={};"
                    + "  delete window.__ffSavedViews[oldName];"
                    + "  window.__ffSavedViews[newName]=renamed;"
                    + "}"
                    + "function deleteViaGraphqlSync(name){"
                    + "  var view=null;"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===name){view=window.__ffLastSavedView;}"
                    + "  if(!view){view=findInSavedViews(name);}"
                    + "  if(!view||!view.id){"
                    + "    var views=fetchTableViewsSync();"
                    + "    view=views.find(function(v){return normName(v.name)===name;});"
                    + "  }"
                    + "  if(!view||!view.id)throw new Error('view-not-found');"
                    + "  try{gqlSync('DeleteTableView',DELETE_TABLE_VIEW,{id:view.id});}"
                    + "  catch(e1){gqlSync('DeleteTableView','mutation DeleteTableView($item: TableViewInput!) { deleteTableView(item: $item) }',{item:{id:view.id,name:view.name,tableType:view.tableType||'ORDER'}});}"
                    + "  if(window.__ffSavedViews)delete window.__ffSavedViews[name];"
                    + "  if(window.__ffLastSavedView&&normName(window.__ffLastSavedView.name)===name){window.__ffLastSavedView=null;}"
                    + "}";

    private static final String FIND_VIEW_FROM_CACHE_INLINE =
            "function norm(v){return (v||'').replace(/\\s+/g,' ').trim();}"
                    + "function viewFromGqlResponses(name){"
                    + "  var n=norm(name);"
                    + "  var responses=window.__ffGqlResponses||[];"
                    + "  for(var i=responses.length-1;i>=0;i--){"
                    + "    var entry=responses[i];"
                    + "    var json=entry&&entry.res;"
                    + "    if(!json||!json.data)continue;"
                    + "    var saved=json.data.saveTableView||json.data.updateTableView;"
                    + "    if(saved&&saved.id&&norm(saved.name)===n)return saved;"
                    + "    var views=json.data.tableViews;"
                    + "    if(Array.isArray(views)){"
                    + "      for(var j=0;j<views.length;j++){"
                    + "        if(views[j]&&views[j].id&&norm(views[j].name)===n)return views[j];"
                    + "      }"
                    + "    }"
                    + "    if(entry.req){"
                    + "      try{"
                    + "        var body=typeof entry.req==='string'?JSON.parse(entry.req):entry.req;"
                    + "        if(body.variables&&body.variables.item&&norm(body.variables.item.name)===n){"
                    + "          var item=Object.assign({},body.variables.item);"
                    + "          if(saved&&saved.id)item.id=saved.id;"
                    + "          if(item.id)return item;"
                    + "        }"
                    + "      }catch(e){}"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findView(name){"
                    + "  var n=norm(name);"
                    + "  if(window.__ffLastSavedView&&norm(window.__ffLastSavedView.name)===n){"
                    + "    return window.__ffLastSavedView;"
                    + "  }"
                    + "  if(window.__ffLastSaveTableViewResponse&&norm(window.__ffLastSaveTableViewResponse.name)===n){"
                    + "    return window.__ffLastSaveTableViewResponse;"
                    + "  }"
                    + "  if(window.__ffSavedViews){"
                    + "    for(var k in window.__ffSavedViews){"
                    + "      if(norm(k)===n)return window.__ffSavedViews[k];"
                    + "    }"
                    + "  }"
                    + "  var fromGql=viewFromGqlResponses(n);"
                    + "  if(fromGql)return fromGql;"
                    + "  var calls=window.__ffApiCalls||[];"
                    + "  for(var i=calls.length-1;i>=0;i--){"
                    + "    var c=calls[i];"
                    + "    if(!c.body)continue;"
                    + "    try{"
                    + "      var b=JSON.parse(c.body);"
                    + "      if(b.variables&&b.variables.item&&norm(b.variables.item.name)===n){"
                    + "        return b.variables.item;"
                    + "      }"
                    + "    }catch(e){}"
                    + "  }"
                    + "  return null;"
                    + "}";

    public static void installNetworkCaptureHook() {
        run(NETWORK_HOOK_JS);
    }

    public static void refreshGraphqlAuth() {
        installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        run("try{"
                + "var d=document.querySelector('.dropdown-reset-container');"
                + "if(d){d.click();return true;}"
                + "var btn=document.querySelector('#tableViewButton');"
                + "if(btn){btn.click();return true;}"
                + "}catch(e){}"
                + "return false;");
        long end = System.currentTimeMillis() + 12000;
        while (System.currentTimeMillis() < end) {
            Object auth = run("return window.__ffAuthHeader||null;");
            if (auth != null && !String.valueOf(auth).trim().isEmpty()
                    && !"null".equals(String.valueOf(auth))) {
                return;
            }
            sleep(400);
        }
    }

    private static boolean runBrowserGraphqlSync(String operation) {
        refreshGraphqlAuth();
        Object result = run(NETWORK_HOOK_JS + TABLE_VIEW_API_JS + operation);
        if (result == null) {
            return false;
        }
        String value = String.valueOf(result);
        if ("ok".equals(value)) {
            return true;
        }
        Logger.logConsoleMessage("Browser GraphQL sync result: " + value);
        return false;
    }

    public static String getApiCaptureDebugJson() {
        Object result = run(
                "try {"
                        + "return JSON.stringify({"
                        + "calls:(window.__ffApiCalls||[]).slice(-25),"
                        + "gql:(window.__ffGqlResponses||[]).slice(-10),"
                        + "saved:Object.keys(window.__ffSavedViews||{}),"
                        + "perf:(window.performance&&performance.getEntriesByType"
                        + "?performance.getEntriesByType('resource').map(function(e){return e.name;}).filter(function(u){"
                        + "return /(table|view|column|grid|preference|layout|fulfillment|graphql)/i.test(u);}).slice(-25)"
                        + ":[])"
                        + "});"
                        + "} catch(e){return JSON.stringify({error:e.message});}");
        return result == null ? "{}" : String.valueOf(result);
    }

    public static String viewIdLookupJs(String viewName) {
        return TABLE_VIEW_API_JS + "return findViewIdInDom('" + esc(viewName) + "');";
    }

    public static void cacheSavedViewFromApi(String viewName) {
        String escaped = esc(viewName);
        installNetworkCaptureHook();
        refreshGraphqlAuth();
        Object cached = run(NETWORK_HOOK_JS + TABLE_VIEW_API_JS
                + FIND_VIEW_FROM_CACHE_INLINE
                + "return findView('" + escaped + "')?'true':'false';");
        if ("true".equalsIgnoreCase(String.valueOf(cached))) {
            run(NETWORK_HOOK_JS + TABLE_VIEW_API_JS + FIND_VIEW_FROM_CACHE_INLINE
                    + "var v=findView('" + escaped + "');"
                    + "if(v){window.__ffLastSavedView=v;if(!window.__ffSavedViews)window.__ffSavedViews={};"
                    + "window.__ffSavedViews[v.name||'" + escaped + "']=v;}");
            return;
        }
        run("window.__ffViewLookupResult='pending';"
                + TABLE_VIEW_API_JS
                + "(async function(){"
                + "  try {"
                + "    var views=await fetchTableViews();"
                + "    var v=views.find(function(x){return normName(x.name)==='" + escaped + "';});"
                + "    if(v){window.__ffLastSavedView=v;if(!window.__ffSavedViews)window.__ffSavedViews={};window.__ffSavedViews[v.name]=v;window.__ffViewLookupResult='true';}"
                + "    else window.__ffViewLookupResult='false';"
                + "  } catch(e){window.__ffViewLookupResult='false';}"
                + "})();");
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            Object asyncResult = run("return window.__ffViewLookupResult;");
            if ("true".equalsIgnoreCase(String.valueOf(asyncResult))
                    || "false".equalsIgnoreCase(String.valueOf(asyncResult))) {
                return;
            }
            sleep(200);
        }
    }

    public static boolean waitForSavedViewWithId(String viewName, int timeoutSec) {
        installNetworkCaptureHook();
        String escaped = esc(viewName);
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            Object hasId = run(NETWORK_HOOK_JS + TABLE_VIEW_API_JS + FIND_VIEW_FROM_CACHE_INLINE
                    + "var v=findView('" + escaped + "');"
                    + "return !!(v && v.id);");
            if (truthy(hasId)) {
                cacheSavedViewFromApi(viewName);
                return true;
            }
            cacheSavedViewFromApi(viewName);
            if (TableViewGraphqlClient.syncViewIdAfterUiSave(viewName)) {
                return true;
            }
            if (TableViewGraphqlClient.cacheViewFromGraphql(viewName)) {
                return true;
            }
            sleep(400);
        }
        Object hasId = run(NETWORK_HOOK_JS + TABLE_VIEW_API_JS + FIND_VIEW_FROM_CACHE_INLINE
                + "var v=findView('" + escaped + "');"
                + "return !!(v && v.id);");
        if (truthy(hasId)) {
            return true;
        }
        return TableViewGraphqlClient.syncViewIdAfterUiSave(viewName)
                || TableViewGraphqlClient.cacheViewFromGraphql(viewName);
    }

    public static boolean isColumnSelectionCounterVisible() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var nodes=document.querySelectorAll("
                        + "'.wrapper-dropdown-container span,.wrapper-dropdown-container div,"
                        + "'.wrapper-dropdown-container p,span.wrapper-dropdown-title,"
                        + "'.cdk-overlay-pane span,.cdk-overlay-pane div,.multi-options-list *');"
                        + "for(var i=0;i<nodes.length;i++){"
                        + "  if(!isVisible(nodes[i]))continue;"
                        + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(/\\d+\\s*(of|\\/)\\s*\\d+/i.test(t))return true;"
                        + "}"
                        + "var panel=document.querySelector('.wrapper-dropdown-container');"
                        + "if(panel&&isVisible(panel)){"
                        + "  var checked=panel.querySelectorAll('input[type=checkbox]:checked').length;"
                        + "  var total=panel.querySelectorAll('input[type=checkbox]').length;"
                        + "  if(checked>0&&total>=2)return true;"
                        + "}"
                        + "return false;"));
    }

    /** Returns JSON {@code {"columns":{"orderTable":[...],...}}} from the open Manage Columns panel. */
    public static String captureManageColumnsSelectionJson() {
        Object result = run(
                IS_VISIBLE_FN
                        + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                        + "function findPanel(){"
                        + "  var candidates=document.querySelectorAll('.cdk-overlay-pane .wrapper-dropdown-container,.wrapper-dropdown-container');"
                        + "  for(var i=0;i<candidates.length;i++){"
                        + "    var p=candidates[i];"
                        + "    var t=p.textContent||'';"
                        + "    if(/manage columns/i.test(t)&&/order columns/i.test(t)&&isVisible(p))return p;"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "function resolveCheckbox(labelEl){"
                        + "  var cb=labelEl.previousElementSibling;"
                        + "  if(!cb||cb.type!=='checkbox'){"
                        + "    cb=labelEl.parentElement&&labelEl.parentElement.querySelector('input[type=checkbox]');"
                        + "  }"
                        + "  if(!cb||cb.type!=='checkbox'){"
                        + "    cb=labelEl.closest('.column-item,.form-check,div')"
                        + "      &&labelEl.closest('.column-item,.form-check,div').querySelector('input[type=checkbox]');"
                        + "  }"
                        + "  return cb&&cb.type==='checkbox'?cb:null;"
                        + "}"
                        + "function sectionKey(headerText){"
                        + "  var t=norm(headerText).toLowerCase();"
                        + "  if(t.indexOf('package')>=0)return 'packageTable';"
                        + "  if(t.indexOf('line item')>=0)return 'lineItemTable';"
                        + "  return 'orderTable';"
                        + "}"
                        + "var panel=findPanel();"
                        + "if(!panel)return null;"
                        + "var out={orderTable:[],packageTable:[],lineItemTable:[]};"
                        + "var containers=panel.querySelectorAll('.multi-table-column-container');"
                        + "for(var i=0;i<containers.length;i++){"
                        + "  var c=containers[i];"
                        + "  var header=c.querySelector('.table-column-name');"
                        + "  var key=sectionKey(header?header.textContent:'');"
                        + "  var labels=c.querySelectorAll('label');"
                        + "  for(var j=0;j<labels.length;j++){"
                        + "    var lbl=norm(labels[j].textContent);"
                        + "    if(!lbl)continue;"
                        + "    var cb=resolveCheckbox(labels[j]);"
                        + "    out[key].push({label:lbl,selected:!!(cb&&cb.checked)});"
                        + "  }"
                        + "}"
                        + "return JSON.stringify({columns:out});");
        if (result == null || "null".equals(String.valueOf(result))) {
            return null;
        }
        return String.valueOf(result);
    }

    public static boolean clickRefreshOrdersTable() {
        return truthy(run(
                "var buttons = document.querySelectorAll('button,[role=button],a');"
                        + "for (var i = 0; i < buttons.length; i++) {"
                        + "  var t = (buttons[i].innerText || buttons[i].textContent || '').trim().toLowerCase();"
                        + "  if (t.indexOf('refresh table') >= 0) { buttons[i].click(); return true; }"
                        + "}"
                        + "return false;"));
    }

    /** Logs visible orders-grid header labels (diagnostic when column prep fails). */
    public static void logOrdersGridHeaderLabels() {
        Object labels = run(
                IS_VISIBLE_FN
                        + ORDERS_GRID_ROOT_FN
                        + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                        + "var root=ordersGridRoot();"
                        + "if(!root||root===document)return 'no-grid';"
                        + "var headers=root.querySelectorAll('.ag-header-cell,[role=columnheader],thead th');"
                        + "var out=[];"
                        + "for(var i=0;i<headers.length;i++){"
                        + "  if(!isVisible(headers[i]))continue;"
                        + "  var lbl=norm(headers[i].innerText||headers[i].textContent);"
                        + "  var cid=headers[i].getAttribute('col-id')||headers[i].getAttribute('aria-colindex')||'';"
                        + "  if(lbl||cid)out.push(lbl+(cid?'['+cid+']':''));"
                        + "}"
                        + "return out.length?out.join(', '):('role-grid-headers='+headers.length);");
        Logger.logConsoleMessage("Orders grid headers visible: " + String.valueOf(labels));
    }

    public static boolean clickExportOrdersMenu() {
        return truthy(run(
                "function clickText(txt){"
                        + "  var nodes=document.querySelectorAll('button,span,div,a,[role=menuitem]');"
                        + "  for(var i=0;i<nodes.length;i++){"
                        + "    var t=(nodes[i].innerText||nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "    if(t===txt){try{nodes[i].click();return true;}catch(e){}}"
                        + "  }"
                        + "  return false;"
                        + "}"
                        + "var exportBtn=null;"
                        + "var buttons=document.querySelectorAll('button,[role=button]');"
                        + "for(var b=0;b<buttons.length;b++){"
                        + "  var bt=(buttons[b].innerText||buttons[b].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(/^export$/i.test(bt)){exportBtn=buttons[b];break;}"
                        + "}"
                        + "if(!exportBtn)return false;"
                        + "try{exportBtn.click();}catch(e){return false;}"
                        + "var end=Date.now()+5000;"
                        + "while(Date.now()<end){if(clickText('Orders'))return true;}"
                        + "return clickText('Order');"));
    }

    /** Logs first-row action button candidates (diagnostic when export menu fails). */
    public static void logFirstRowActionButtonDiag() {
        Object diag = run(
                firstTopOrderRowScript()
                        + "var row=firstTopOrderRow();"
                        + "if(!row)return 'no-first-row(topRows='+"
                        + "document.querySelectorAll('.ag-center-cols-container > .ag-row').length+')';"
                        + "var idx=row.getAttribute('row-index')||'?';"
                        + "var out=['row-index='+idx];"
                        + "function describe(btn,where){"
                        + "  if(!btn)return;"
                        + "  var cls=(btn.className||'').toString();"
                        + "  var aria=btn.getAttribute('aria-label')||'';"
                        + "  out.push(where+':'+cls+(aria?('[aria='+aria+']'):''));"
                        + "}"
                        + "var pinned=document.querySelector("
                        + "'.ag-pinned-right-cols-container .ag-row[row-index=\"'+idx+'\"]');"
                        + "if(pinned){"
                        + "  pinned.querySelectorAll('button').forEach(function(b,i){"
                        + "    describe(b,'pinned-btn'+i);"
                        + "  });"
                        + "}"
                        + "row.querySelectorAll('button').forEach(function(b,i){"
                        + "  describe(b,'center-btn'+i);"
                        + "});"
                        + "return out.join(' | ');");
        Logger.logReportMessage("First row action buttons: " + String.valueOf(diag));
    }

    /** Opens the first Orders grid row action menu (pinned-right ellipsis preferred). */
    public static boolean openFirstRowActionMenu() {
        Object result = run(
                firstTopOrderRowScript()
                        + "function isVisible(el){"
                        + "  if(!el)return false;"
                        + "  var s=window.getComputedStyle(el);"
                        + "  if(s.display==='none'||s.visibility==='hidden'||parseFloat(s.opacity||'1')<=0)return false;"
                        + "  var r=el.getBoundingClientRect();"
                        + "  return r.width>0&&r.height>0;"
                        + "}"
                        + "function hoverRow(row){"
                        + "  if(!row)return;"
                        + "  row.dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                        + "  row.dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));"
                        + "}"
                        + "function findEllipsisButton(row){"
                        + "  if(!row)return null;"
                        + "  var selectors=["
                        + "'button.ellipsisButton','button[class*=ellipsisButton]',"
                        + "'button[class*=ellipsis]','button.dropdown-toggle.ellipsisButton'];"
                        + "  for(var s=0;s<selectors.length;s++){"
                        + "    var b=row.querySelector(selectors[s]);"
                        + "    if(b&&isVisible(b))return b;"
                        + "  }"
                        + "  var buttons=row.querySelectorAll('button');"
                        + "  for(var i=buttons.length-1;i>=0;i--){"
                        + "    var cls=(buttons[i].className||'').toString();"
                        + "    if(cls.indexOf('side-nav')>=0)continue;"
                        + "    if(isVisible(buttons[i]))return buttons[i];"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "try{document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}));}"
                        + "catch(e){}"
                        + "var row=firstTopOrderRow();"
                        + "if(!row)return false;"
                        + "hoverRow(row);"
                        + "var idx=row.getAttribute('row-index')||'0';"
                        + "var btn=null;"
                        + "var pinned=document.querySelector("
                        + "'.ag-pinned-right-cols-container .ag-row[row-index=\"'+idx+'\"]');"
                        + "if(pinned){"
                        + "  hoverRow(pinned);"
                        + "  btn=findEllipsisButton(pinned);"
                        + "}"
                        + "if(!btn){"
                        + "  var pr=document.querySelector('.ag-pinned-right-cols-container .ag-row');"
                        + "  if(pr){hoverRow(pr);btn=findEllipsisButton(pr);}"
                        + "}"
                        + "if(!btn)return false;"
                        + "try{"
                        + "  btn.scrollIntoView({block:'center',inline:'nearest'});"
                        + "  btn.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));"
                        + "  btn.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));"
                        + "  btn.click();"
                        + "  return true;"
                        + "}catch(e){return false;}");
        return truthy(result);
    }

    public static boolean isRowActionMenuOpen() {
        return truthy(run("return !!document.querySelector('.dropdown-menu.show,.cdk-overlay-pane .dropdown-menu');"));
    }

    /** Waits until the orders ag-grid header row is rendered (or timeout). */
    public static boolean waitForOrdersGridRendered(int timeoutSec) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSec * 1000L);
        int attempt = 0;
        while (System.currentTimeMillis() < deadline) {
            if (isOrdersGridRendered()) {
                return true;
            }
            if (attempt == 0 || attempt % 15 == 0) {
                closeManageColumnsPanel();
            }
            attempt++;
            Thread.sleep(400);
        }
        Logger.logReportMessage("Orders grid render wait timed out");
        logOrdersGridDiagnostics();
        return isOrdersGridRendered();
    }

    /** Logs page state when orders grid fails to render (Synergy diagnostics). */
    public static void logOrdersGridDiagnostics() {
        Object diag = run(
                "try{"
                        + "var body=(document.body&&document.body.innerText)||'';"
                        + "return JSON.stringify({"
                        + "url:location.href,"
                        + "tableViewBtn:!!document.querySelector('#tableViewButton'),"
                        + "agRoots:document.querySelectorAll('.ag-root-wrapper,.ag-root').length,"
                        + "roleGrids:document.querySelectorAll('[role=grid]').length,"
                        + "headers:document.querySelectorAll('.ag-header-cell,[role=columnheader]').length,"
                        + "rows:document.querySelectorAll('.ag-center-cols-container > .ag-row,[role=grid] [role=row]').length,"
                        + "loading:!!document.querySelector('.ag-overlay-loading-center,.spinner,[class*=loading]'),"
                        + "auth:!!(window.__ffAuthHeader||''),"
                        + "bodySample:body.substring(0,200).replace(/\\s+/g,' ').trim()"
                        + "});"
                        + "}catch(e){return e.message;}");
        Logger.logReportMessage("Orders grid diagnostics: " + String.valueOf(diag));
        Logger.logReportMessage("Orders grid API capture: " + getApiCaptureDebugJson());
    }

    /** Waits until a FulfillmentExport CloudFront download URL appears in the page or network hooks. */
    public static String waitForFulfillmentExportDownloadUrl(int timeoutSec) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            String url = readFulfillmentExportDownloadUrl();
            if (url != null && !url.isEmpty()) {
                return url;
            }
            Thread.sleep(500);
        }
        return null;
    }

    /** Returns count of top-level order rows in the orders grid. */
    public static int countOrdersGridTopRows() {
        Object result = run(
                ORDERS_GRID_DETECT_FN
                        + "function isTopOrderRow(row){"
                        + "  if(!row||!row.classList)return true;"
                        + "  if(row.classList.contains('ag-row-level-1'))return false;"
                        + "  if(row.classList.contains('ag-row-level-2'))return false;"
                        + "  var parent=row.parentElement;"
                        + "  if(parent&&parent.getAttribute&&parent.getAttribute('role')==='rowgroup'){"
                        + "    var rowgroup=parent;"
                        + "    if(rowgroup.parentElement&&rowgroup.parentElement.getAttribute('role')!=='grid')return false;"
                        + "  }"
                        + "  return true;"
                        + "}"
                        + "var root=ordersGridRoot();"
                        + "var rows=root.querySelectorAll('.ag-center-cols-container > .ag-row,[role=grid] > [role=rowgroup] > [role=row],[role=row]');"
                        + "var count=0;"
                        + "for(var i=0;i<rows.length;i++){if(isTopOrderRow(rows[i]))count++;}"
                        + "return Math.min(count,200);");
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(result));
        } catch (Exception ex) {
            return 0;
        }
    }

    /** Waits until at least one top-level order row is rendered (or timeout). */
    public static boolean waitForOrdersGridTopRows(int minRows, int timeoutSec) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (countOrdersGridTopRows() >= minRows) {
                return true;
            }
            Thread.sleep(500);
        }
        Logger.logReportMessage("Orders grid row wait timed out — top rows found: " + countOrdersGridTopRows());
        return countOrdersGridTopRows() >= minRows;
    }

    /** Forces global search for an order id without navigating away from the current view. */
    public static boolean forceGlobalOrderSearch(String orderId) throws InterruptedException {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        String escaped = esc(orderId.trim());
        run(
                "var id='" + escaped + "';"
                        + "var icon=document.querySelector('[class*=search-icon],.search-icon');"
                        + "if(icon){try{icon.click();}catch(e){}}"
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
        return waitForOrderVisibleInGrid(orderId.trim(), 20);
    }

    /** Returns true when the order id appears in a rendered grid cell (not URL-only). */
    public static boolean isOrderInGridCells(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        String escaped = esc(orderId.trim());
        Object found = run(
                "var needle='" + escaped + "'.toLowerCase();"
                        + "var cells=document.querySelectorAll('.ag-center-cols-container .ag-cell,[col-id]');"
                        + "for(var i=0;i<cells.length;i++){"
                        + "  var t=(cells[i].innerText||cells[i].textContent||'').toLowerCase();"
                        + "  if(t.indexOf(needle)>=0)return true;"
                        + "}"
                        + "return false;");
        return truthy(found);
    }

    /** Waits until the given order id appears in the orders grid (global search result). */
    public static boolean waitForOrderVisibleInGrid(String orderId, int timeoutSec) throws InterruptedException {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        String escaped = esc(orderId.trim());
        long deadline = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (isOrderInGridCells(orderId)) {
                return true;
            }
            Object found = run(
                    "var needle='" + escaped + "'.toLowerCase();"
                            + "var url=(window.location.href||'').toLowerCase();"
                            + "return url.indexOf(needle)>=0;");
            if (truthy(found) && countOrdersGridTopRows() > 0) {
                return true;
            }
            Thread.sleep(500);
        }
        Logger.logReportMessage("Order not visible in grid after " + timeoutSec + "s: " + orderId);
        return false;
    }

    /** Navigates to orders focus URL for the given order (tries order= and orderId=). */
    public static void navigateToOrderFocus(String orderId) throws InterruptedException {
        if (orderId == null || orderId.trim().isEmpty()) {
            return;
        }
        String base = com.paramount.test.ff.common.util.Config.getString("TargetUrl");
        if (base == null || base.trim().isEmpty()) {
            base = com.paramount.test.ff.common.util.Config.getString("TargetUrlDEV");
        }
        if (base == null || base.trim().isEmpty()) {
            base = "https://dev-operationsconsole.paramountmsc.com/fulfillment/";
        }
        String root = base.replaceAll("/+$", "");
        String trimmed = orderId.trim();
        String[] focusUrls = new String[] {
                root + "/focus?tableView=orders&order=" + trimmed,
                root + "/focus?tableView=orders&orderId=" + trimmed
        };
        for (String focusUrl : focusUrls) {
            Logger.logReportMessage("Navigating to order focus: " + focusUrl);
            BaseTest.driver.get().browser().getUrl(focusUrl);
            Thread.sleep(3000);
            if (waitForOrderVisibleInGrid(trimmed, 15)) {
                return;
            }
        }
    }

    /** Scrolls the grid row containing the order id into view. */
    public static void scrollOrderIntoView(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return;
        }
        String escaped = esc(orderId.trim());
        run(
                "var needle='" + escaped + "'.toLowerCase();"
                        + "var rows=document.querySelectorAll("
                        + "'.ag-center-cols-container .ag-row,.ag-body-viewport .ag-row,.ag-row-focus');"
                        + "for(var i=0;i<rows.length;i++){"
                        + "  var t=(rows[i].innerText||rows[i].textContent||'').toLowerCase();"
                        + "  if(t.indexOf(needle)>=0){"
                        + "    rows[i].scrollIntoView({block:'center',inline:'nearest'});"
                        + "    return true;"
                        + "  }"
                        + "}"
                        + "return false;");
    }

    private static String firstTopOrderRowScript() {
        return "function isTopOrderRow(row){"
                + "  if(!row||!row.classList)return false;"
                + "  if(row.classList.contains('ag-row-level-1'))return false;"
                + "  if(row.classList.contains('ag-row-level-2'))return false;"
                + "  return true;"
                + "}"
                + "function firstTopOrderRow(){"
                + "  var rows=document.querySelectorAll('.ag-center-cols-container > .ag-row');"
                + "  for(var i=0;i<rows.length;i++){"
                + "    if(isTopOrderRow(rows[i]))return rows[i];"
                + "  }"
                + "  rows=document.querySelectorAll('.ag-body-viewport .ag-row');"
                + "  for(var j=0;j<rows.length;j++){"
                + "    if(isTopOrderRow(rows[j]))return rows[j];"
                + "  }"
                + "  return null;"
                + "}";
    }

    /** Hovers a row menu submenu (e.g. Export) to reveal nested items. */
    public static boolean hoverRowMenuSubmenu(String needle) {
        String escaped = needle.replace("\\", "\\\\").replace("'", "\\'");
        Object result = run(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "var needle='" + escaped.toLowerCase() + "';"
                        + "var subs=document.querySelectorAll("
                        + "'.dropdown-menu.show .dropdown-submenu,.cdk-overlay-pane .dropdown-submenu');"
                        + "for(var s=0;s<subs.length;s++){"
                        + "  var st=norm(subs[s].innerText||subs[s].textContent);"
                        + "  if(st.indexOf(needle)<0)continue;"
                        + "  subs[s].dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                        + "  subs[s].dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));"
                        + "  var toggle=subs[s].querySelector('a,.dropdown-item,button');"
                        + "  if(toggle){"
                        + "    toggle.dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                        + "    try{toggle.click();}catch(e){}"
                        + "  }"
                        + "  return true;"
                        + "}"
                        + "return false;");
        return truthy(result);
    }

    /** Reads first visible orders-grid row cell text by header label. */
    public static String readFirstRowCellByColumnLabel(String columnLabel) {
        if (columnLabel == null || columnLabel.trim().isEmpty()) {
            return null;
        }
        String escaped = esc(columnLabel);
        Object result = run(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                        + "function cellValue(cell){"
                        + "  if(!cell)return '';"
                        + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                        + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                        + "  return norm(cell.innerText||cell.textContent);"
                        + "}"
                        + "var label='" + escaped + "';"
                        + "var colId=null;"
                        + "var headers=document.querySelectorAll('.ag-header-cell');"
                        + "for(var i=0;i<headers.length;i++){"
                        + "  var ht=norm(headers[i].innerText||headers[i].textContent);"
                        + "  if(ht===label||ht.indexOf(label)>=0||label.indexOf(ht)>=0){"
                        + "    colId=headers[i].getAttribute('col-id');"
                        + "    if(colId)break;"
                        + "  }"
                        + "}"
                        + "if(!colId){"
                        + "  var spans=document.querySelectorAll("
                        + "'.label-ellipsis span,.ag-header-cell-text');"
                        + "  for(var j=0;j<spans.length;j++){"
                        + "    var st=norm(spans[j].innerText||spans[j].textContent);"
                        + "    if(st===label){"
                        + "      var hc=spans[j].closest('.ag-header-cell');"
                        + "      if(hc){colId=hc.getAttribute('col-id');break;}"
                        + "    }"
                        + "  }"
                        + "}"
                        + "if(!colId)return '';"
                        + "var row=document.querySelector("
                        + "'.ag-center-cols-container>.ag-row:not(.ag-row-level-1):not(.ag-row-level-2)');"
                        + "if(!row)return '';"
                        + "var idx=row.getAttribute('row-index')||'0';"
                        + "var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                        + "'.ag-pinned-right-cols-container'];"
                        + "for(var c=0;c<containers.length;c++){"
                        + "  var dataRow=document.querySelector(containers[c]+' .ag-row[row-index=\"'+idx+'\"]');"
                        + "  if(!dataRow)continue;"
                        + "  var cell=dataRow.querySelector('[col-id=\"'+colId+'\"]');"
                        + "  if(cell){var v=cellValue(cell);if(v)return v;}"
                        + "}"
                        + "return '';");
        if (result == null) {
            return null;
        }
        String value = String.valueOf(result).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    /** Clicks a visible row menu item whose text contains needle (case-insensitive). */
    public static boolean clickVisibleRowMenuItem(String needle) {
        String escaped = needle.replace("\\", "\\\\").replace("'", "\\'");
        Object result = run(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "var needle='" + escaped.toLowerCase() + "';"
                        + "var nodes=document.querySelectorAll("
                        + "'.dropdown-menu.show .dropdown-item,.dropdown-menu.show a,"
                        + "'.dropdown-menu.show [role=menuitem],.dropdown-menu.show li,"
                        + "'.dropdown-submenu .dropdown-menu .dropdown-item,.dropdown-submenu .dropdown-menu a,"
                        + "'.cdk-overlay-pane .dropdown-item,.cdk-overlay-pane [role=menuitem]');"
                        + "for(var i=0;i<nodes.length;i++){"
                        + "  var t=norm(nodes[i].innerText||nodes[i].textContent);"
                        + "  if(!t)continue;"
                        + "  if(t===needle||t.indexOf(needle)>=0){"
                        + "    try{nodes[i].dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));}"
                        + "    catch(e){}"
                        + "    try{nodes[i].click();return true;}catch(e){}"
                        + "  }"
                        + "}"
                        + "var subs=document.querySelectorAll('.dropdown-menu.show .dropdown-submenu');"
                        + "for(var s=0;s<subs.length;s++){"
                        + "  var st=norm(subs[s].innerText||subs[s].textContent);"
                        + "  if(st.indexOf(needle)<0)continue;"
                        + "  subs[s].dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                        + "  var toggle=subs[s].querySelector('a,.dropdown-item,button');"
                        + "  if(toggle)try{toggle.click();}catch(e){}"
                        + "  return true;"
                        + "}"
                        + "return false;");
        return truthy(result);
    }

    /** First Orders grid row: action menu -> Export -> Excel Document (.xlsx). */
    public static boolean clickFirstRowExportExcelDocument() {
        if (!openFirstRowActionMenu()) {
            Logger.logReportMessage("First row export: failed to open row action menu");
            return false;
        }
        if (!clickVisibleRowMenuItem("export")) {
            Logger.logReportMessage("First row export: Export menu item not found");
            return false;
        }
        if (!clickVisibleRowMenuItem("excel")) {
            Logger.logReportMessage("First row export: Excel Document menu item not found");
            return false;
        }
        return true;
    }

    public static void installExportCaptureHook() {
        installNetworkCaptureHook();
        run(
                "if(window.__ffExportHookInstalled)return true;"
                        + "window.__ffExportHookInstalled=true;"
                        + "window.__ffExportFileName=null;"
                        + "window.__ffExportUrls=[];"
                        + "window.__ffLastExportB64=null;"
                        + "window.__ffLastExportName=null;"
                        + "function pushExportUrl(u){"
                        + "  if(!u||typeof u!=='string')return;"
                        + "  if(/FulfillmentExport|cloudfront\\.net\\/[^?]*\\.xlsx|ops-console-backend-export-data[^?]*\\.xlsx/i.test(u)){"
                        + "    if(!window.__ffExportUrls)window.__ffExportUrls=[];"
                        + "    window.__ffExportUrls.push(u);"
                        + "  }"
                        + "}"
                        + "function scanTextForExportUrl(t){"
                        + "  if(!t||typeof t!=='string')return;"
                        + "  var m=t.match(/https?:[^\"'\\s]*(?:cloudfront\\.net|ops-console-backend-export)[^\"'\\s]*\\.xlsx[^\"'\\s]*/i);"
                        + "  if(m&&m[0])pushExportUrl(m[0]);"
                        + "}"
                        + "function toB64(buf){"
                        + "  var bytes=new Uint8Array(buf);"
                        + "  var binary='';"
                        + "  var chunk=0x8000;"
                        + "  for(var i=0;i<bytes.length;i+=chunk){"
                        + "    binary+=String.fromCharCode.apply(null,bytes.subarray(i,i+chunk));"
                        + "  }"
                        + "  return btoa(binary);"
                        + "}"
                        + "function storeExportBody(buf,cd,url){"
                        + "  if(!buf||!buf.byteLength)return;"
                        + "  var name=window.__ffExportFileName||'FulfillmentExport.xlsx';"
                        + "  var m=(cd||'').match(/filename[^;=\\n]*=(UTF-8''|\"?)([^\";\\n]+)/i);"
                        + "  if(m&&m[2])name=decodeURIComponent(m[2].replace(/\"/g,''));"
                        + "  else if(url){var parts=url.split('/');var tail=parts[parts.length-1].split('?')[0];if(tail)name=tail;}"
                        + "  window.__ffLastExportB64=toB64(buf);"
                        + "  window.__ffLastExportName=name;"
                        + "  window.__ffExportFileName=name;"
                        + "}"
                        + "function captureName(cd){"
                        + "  if(!cd)return;"
                        + "  var m=cd.match(/filename[^;=\\n]*=(UTF-8''|\"?)([^\";\\n]+)/i);"
                        + "  if(m&&m[2]){window.__ffExportFileName=decodeURIComponent(m[2].replace(/\"/g,''));}"
                        + "}"
                        + "if(window.fetch&&!window.__ffExportFetchWrapped){"
                        + "  window.__ffExportFetchWrapped=true;"
                        + "  var origFetch=window.fetch;"
                        + "  window.fetch=function(){"
                        + "    return origFetch.apply(this,arguments).then(function(res){"
                        + "      try{"
                        + "        var cd=res.headers&&res.headers.get?res.headers.get('content-disposition'):'';"
                        + "        var ct=res.headers&&res.headers.get?res.headers.get('content-type'):'';"
                        + "        captureName(cd||'');"
                        + "        pushExportUrl(res.url||'');"
                        + "        if((res.url||'').indexOf('graphql')>=0){"
                        + "          res.clone().text().then(function(t){scanTextForExportUrl(t);}).catch(function(){});"
                        + "        }"
                        + "        if(/sheet|excel|octet-stream|zip/i.test(ct||'')||/filename/i.test(cd||'')||/FulfillmentExport|cloudfront\\.net.*\\.xlsx|ops-console-backend-export-data.*\\.xlsx/i.test(res.url||'')){"
                        + "          res.clone().arrayBuffer().then(function(buf){storeExportBody(buf,cd,res.url);}).catch(function(){});"
                        + "        }"
                        + "      }catch(e){}"
                        + "      return res;"
                        + "    });"
                        + "  };"
                        + "}"
                        + "if(!window.__ffExportXhrWrapped){"
                        + "  window.__ffExportXhrWrapped=true;"
                        + "  var origOpen=XMLHttpRequest.prototype.open;"
                        + "  var origSend=XMLHttpRequest.prototype.send;"
                        + "  XMLHttpRequest.prototype.open=function(m,u){this.__ffUrl=u;return origOpen.apply(this,arguments);};"
                        + "  XMLHttpRequest.prototype.send=function(){"
                        + "    this.addEventListener('load',function(){"
                        + "      try{"
                        + "        var cd=this.getResponseHeader&&this.getResponseHeader('content-disposition');"
                        + "        var ct=this.getResponseHeader&&this.getResponseHeader('content-type');"
                        + "        captureName(cd||'');"
                        + "        pushExportUrl(this.__ffUrl||'');"
                        + "        if(this.responseType==='text'||typeof this.responseText==='string'){scanTextForExportUrl(this.responseText||'');}"
                        + "        if(this.response&&this.response.byteLength>0&&(/sheet|excel|octet-stream|zip/i.test(ct||'')||/filename/i.test(cd||'')||/FulfillmentExport|cloudfront\\.net.*\\.xlsx|ops-console-backend-export-data.*\\.xlsx/i.test(this.__ffUrl||''))){"
                        + "          storeExportBody(this.response,cd,this.__ffUrl);"
                        + "        }"
                        + "      }catch(e){}"
                        + "    });"
                        + "    return origSend.apply(this,arguments);"
                        + "  };"
                        + "}"
                        + "if(!window.__ffExportClickHook){"
                        + "  window.__ffExportClickHook=true;"
                        + "  document.addEventListener('click',function(e){"
                        + "    try{"
                        + "      var a=e.target&&e.target.closest?e.target.closest('a[download],a[href*=\".xlsx\"]'):null;"
                        + "      if(a&&a.href){pushExportUrl(a.href);"
                        + "        var dn=a.getAttribute('download');"
                        + "        if(dn)window.__ffExportFileName=dn;}"
                        + "    }catch(ex){}"
                        + "  },true);"
                        + "}"
                        + "if(window.PerformanceObserver&&!window.__ffExportPerfObs){"
                        + "  window.__ffExportPerfObs=true;"
                        + "  try{"
                        + "    new PerformanceObserver(function(list){"
                        + "      list.getEntries().forEach(function(e){"
                        + "        if(e.name&&/FulfillmentExport|cloudfront\\.net\\/[^?]*\\.xlsx|ops-console-backend-export-data[^?]*\\.xlsx/i.test(e.name)){"
                        + "          pushExportUrl(e.name);"
                        + "        }"
                        + "      });"
                        + "    }).observe({entryTypes:['resource']});"
                        + "  }catch(ex){}"
                        + "}"
                        + "if(!window.__ffExportMutObs&&window.MutationObserver){"
                        + "  window.__ffExportMutObs=true;"
                        + "  try{"
                        + "    new MutationObserver(function(muts){"
                        + "      muts.forEach(function(m){"
                        + "        m.addedNodes.forEach(function(n){"
                        + "          if(!n||!n.getAttribute)return;"
                        + "          if(n.tagName==='A'){"
                        + "            var href=n.href||n.getAttribute('href')||'';"
                        + "            if(/FulfillmentExport|cloudfront|ops-console-backend-export-data/i.test(href)){"
                        + "              pushExportUrl(href);"
                        + "              window.__ffExportFileName=(href.split('/').pop()||'').split('?')[0];"
                        + "            }"
                        + "          }"
                        + "        });"
                        + "      });"
                        + "    }).observe(document.body,{childList:true,subtree:true});"
                        + "  }catch(ex){}"
                        + "}"
                        + "return true;");
    }

    public static String readCapturedExportFileName() {
        Object result = run(
                "try{"
                        + "if(window.__ffExportFileName)return window.__ffExportFileName;"
                        + "var entries=(window.performance&&performance.getEntriesByType)?performance.getEntriesByType('resource'):[];"
                        + "for(var i=entries.length-1;i>=0;i--){"
                        + "  var n=entries[i].name||'';"
                        + "  if(/\\.xlsx(\\?|$)|cloudfront\\.net.*FulfillmentExport|ops-console-backend-export-data/i.test(n)){"
                        + "    var parts=n.split('/');"
                        + "    return parts[parts.length-1].split('?')[0];"
                        + "  }"
                        + "}"
                        + "return '';"
                        + "}catch(e){return '';}");
        if (result == null) {
            return null;
        }
        String value = String.valueOf(result).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    /**
     * Fetch the most recent captured export URL response as a local temp file (Synergy remote browser).
     */
    public static File fetchLatestExportFileToTemp() {
        Object fetched = run(
                "try{"
                        + "if(window.__ffLastExportB64&&window.__ffLastExportName)return true;"
                        + "window.__ffLastExportB64=null;"
                        + "window.__ffLastExportName=null;"
                        + "function toB64(buf){"
                        + "  var bytes=new Uint8Array(buf);"
                        + "  var binary='';"
                        + "  var chunk=0x8000;"
                        + "  for(var i=0;i<bytes.length;i+=chunk){"
                        + "    binary+=String.fromCharCode.apply(null,bytes.subarray(i,i+chunk));"
                        + "  }"
                        + "  return btoa(binary);"
                        + "}"
                        + "function captureFromUrl(u){"
                        + "  if(!u)return false;"
                        + "  var xhr=new XMLHttpRequest();"
                        + "  xhr.open('GET',u,false);"
                        + "  xhr.withCredentials=true;"
                        + "  xhr.responseType='arraybuffer';"
                        + "  try{xhr.send();}catch(e){return false;}"
                        + "  if(xhr.status<200||xhr.status>=300||!xhr.response||xhr.response.byteLength<=0)return false;"
                        + "  var cd=xhr.getResponseHeader('content-disposition')||'';"
                        + "  var name='Orders.xlsx';"
                        + "  var m=cd.match(/filename[^;=\\n]*=((['\"]).*?\\2|[^;\\n]*)/i);"
                        + "  if(m&&m[1])name=m[1].replace(/['\"]/g,'').trim();"
                        + "  else{var parts=u.split('/');name=parts[parts.length-1].split('?')[0]||name;}"
                        + "  window.__ffLastExportB64=toB64(xhr.response);"
                        + "  window.__ffLastExportName=name;"
                        + "  return true;"
                        + "}"
                        + "var urls=window.__ffExportUrls||[];"
                        + "for(var i=urls.length-1;i>=0;i--){"
                        + "  if(urls[i]&&/xlsx|export|download|orders|cloudfront|FulfillmentExport|ops-console-backend-export-data/i.test(urls[i])&&captureFromUrl(urls[i]))return true;"
                        + "}"
                        + "var domUrl=null;"
                        + "var domLinks=document.querySelectorAll('a[href]');"
                        + "for(var d=domLinks.length-1;d>=0;d--){"
                        + "  var href=domLinks[d].href||'';"
                        + "  if(/FulfillmentExport|cloudfront\\.net|ops-console-backend-export-data/i.test(href)){domUrl=href;break;}"
                        + "}"
                        + "if(domUrl&&captureFromUrl(domUrl))return true;"
                        + "var entries=(window.performance&&performance.getEntriesByType)?performance.getEntriesByType('resource'):[];"
                        + "for(var j=entries.length-1;j>=0;j--){"
                        + "  var url=entries[j].name||'';"
                        + "  if(/\\.xlsx(\\?|$)|cloudfront\\.net|ops-console-backend-export-data|FulfillmentExport/i.test(url)&&captureFromUrl(url))return true;"
                        + "}"
                        + "return false;"
                        + "}catch(e){return false;}");
        if (!truthy(fetched)) {
            return null;
        }
        Object nameObj = run("try{return window.__ffLastExportName||'';}catch(e){return '';}");
        Object b64Obj = run("try{return window.__ffLastExportB64||'';}catch(e){return '';}");
        if (nameObj == null || b64Obj == null) {
            return null;
        }
        String fileName = String.valueOf(nameObj).trim();
        String base64 = String.valueOf(b64Obj).trim();
        if (fileName.isEmpty() || base64.isEmpty() || "null".equalsIgnoreCase(base64)) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes.length == 0) {
                return null;
            }
            File temp = File.createTempFile("ff-export-", "-" + fileName.replaceAll("[^A-Za-z0-9._-]", "_"));
            Files.write(temp.toPath(), bytes);
            Logger.logReportMessage("Fetched export via captured URL: " + temp.getAbsolutePath());
            return temp;
        } catch (Exception ex) {
            Logger.log("fetchLatestExportFileToTemp failed: " + ex.getMessage());
            return null;
        }
    }

    public static String readFulfillmentExportDownloadUrl() {
        Object result = run(
                "try{"
                        + "function pick(href){"
                        + "  if(!href)return null;"
                        + "  href=String(href);"
                        + "  if(/FulfillmentExport|cloudfront\\.net|ops-console-backend-export-data|\\.xlsx(\\?|$)/i.test(href))return href;"
                        + "  return null;"
                        + "}"
                        + "var links=document.querySelectorAll('a[href],a[download],[role=link]');"
                        + "for(var i=links.length-1;i>=0;i--){"
                        + "  var href=links[i].href||links[i].getAttribute('href')||'';"
                        + "  var picked=pick(href);"
                        + "  if(picked)return picked;"
                        + "}"
                        + "var snack=document.querySelectorAll('.mat-mdc-snack-bar-container,.snackbar,[class*=toast],[class*=notification] a');"
                        + "for(var s=snack.length-1;s>=0;s--){"
                        + "  var sh=snack[s].href||snack[s].getAttribute('href')||'';"
                        + "  var sp=pick(sh);"
                        + "  if(sp)return sp;"
                        + "}"
                        + "var urls=window.__ffExportUrls||[];"
                        + "for(var j=urls.length-1;j>=0;j--){"
                        + "  var up=pick(urls[j]);"
                        + "  if(up)return up;"
                        + "}"
                        + "var entries=(window.performance&&performance.getEntriesByType)?performance.getEntriesByType('resource'):[];"
                        + "for(var k=entries.length-1;k>=0;k--){"
                        + "  var np=pick(entries[k].name||'');"
                        + "  if(np)return np;"
                        + "}"
                        + "var html=(document.body&&document.body.innerHTML)||'';"
                        + "var hm=html.match(/https?:\\/\\/[^\"'\\s]+cloudfront[^\"'\\s]*FulfillmentExport[^\"'\\s]*\\.xlsx[^\"'\\s]*/i);"
                        + "if(hm&&hm[0])return hm[0];"
                        + "hm=html.match(/https?:\\/\\/[^\"'\\s]*ops-console-backend-export-data[^\"'\\s]*\\.xlsx[^\"'\\s]*/i);"
                        + "if(hm&&hm[0])return hm[0];"
                        + "return '';"
                        + "}catch(e){return '';}");
        if (result == null) {
            return null;
        }
        String value = String.valueOf(result).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    public static void logExportCaptureDiagnostics() {
        Object diag = run(
                "try{return JSON.stringify({"
                        + "urls:(window.__ffExportUrls||[]).slice(-10),"
                        + "fileName:window.__ffExportFileName||null,"
                        + "hasB64:!!window.__ffLastExportB64,"
                        + "anchors:Array.from(document.querySelectorAll('a[href]')).slice(-20).map(function(a){return a.href||'';}).filter(function(h){return /xlsx|cloudfront|export/i.test(h);}),"
                        + "apiCalls:(window.__ffApiCalls||[]).slice(-15).map(function(c){return c.url;}).filter(function(u){return /export|cloudfront|xlsx/i.test(u);}),"
                        + "snackbars:Array.from(document.querySelectorAll('.mat-mdc-snack-bar-container,[class*=snackbar],[class*=toast]')).slice(-3).map(function(n){return (n.innerText||'').replace(/\\s+/g,' ').trim().slice(0,200);}),"
                        + "envCount:(function(){var m=(document.body.innerText||'').match(/environment\\s*(\\d+)\\s*\\/\\s*(\\d+)/i);return m?m[1]+'/'+m[2]:null;})()"
                        + "});}catch(e){return e.message;}");
        Logger.logReportMessage("Export capture diagnostics: " + String.valueOf(diag));
    }

    public static boolean clickFulfillmentExportDownloadLink() {
        return truthy(run(
                "var links=document.querySelectorAll('a[href]');"
                        + "for(var i=links.length-1;i>=0;i--){"
                        + "  var href=links[i].href||'';"
                        + "  if(/FulfillmentExport|cloudfront\\.net|ops-console-backend-export-data/i.test(href)){"
                        + "    if(!window.__ffExportUrls)window.__ffExportUrls=[];"
                        + "    window.__ffExportUrls.push(href);"
                        + "    try{links[i].click();return true;}catch(e){}"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    public static void applyPageZoom(String zoom) {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "document.documentElement.style.zoom = '" + zoom + "';"
                            + "if (document.body) { document.body.style.zoom = '" + zoom + "'; }");
        } catch (Exception ignored) {
        }
    }

    public static void resetPageZoom() {
        applyPageZoom("1");
    }

    public static void dismissBlockingOverlays() {
        dismissReleaseNotesModal();
        run(
                "try{document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}));}catch(e){}"
                        + "var b=document.querySelector('.cdk-overlay-backdrop,.modal-backdrop');"
                        + "if(b){try{b.click();}catch(e){}}");
    }

    /** Dismisses the Fulfillment Console "We've got an update for you" release-notes modal. */
    public static boolean dismissReleaseNotesModal() {
        Object result = run(
                IS_VISIBLE_FN
                        + "function clickEl(el){"
                        + "if(!el)return false;"
                        + "try{el.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));}catch(e){}"
                        + "try{el.click();}catch(e){}"
                        + "return true;"
                        + "}"
                        + "function findReleaseNotesModal(){"
                        + "var dialogs=document.querySelectorAll("
                        + "'ngb-modal-window,[role=dialog],.modal,.cdk-overlay-pane');"
                        + "for(var i=0;i<dialogs.length;i++){"
                        + "  var dlg=dialogs[i];"
                        + "  if(!isVisible(dlg))continue;"
                        + "  var text=(dlg.innerText||dlg.textContent||'').toLowerCase();"
                        + "  if(text.indexOf('update for you')>=0"
                        + "     || text.indexOf('got an update')>=0"
                        + "     || text.indexOf('release notes')>=0){"
                        + "    return dlg;"
                        + "  }"
                        + "}"
                        + "return null;"
                        + "}"
                        + "var modal=findReleaseNotesModal();"
                        + "if(!modal)return false;"
                        + "var buttons=modal.querySelectorAll('button,a,[role=button]');"
                        + "for(var b=0;b<buttons.length;b++){"
                        + "  var label=(buttons[b].innerText||buttons[b].textContent||'').trim().toLowerCase();"
                        + "  if(label==='ok'||label==='not now'||label==='close'||label==='dismiss'){"
                        + "    return clickEl(buttons[b]);"
                        + "  }"
                        + "}"
                        + "return clickEl(buttons[buttons.length-1]);");
        return truthy(result);
    }

    public static void refreshTableViewsUiState() {
        installNetworkCaptureHook();
        run("try{"
                + "if(window.__APOLLO_CLIENT__&&window.__APOLLO_CLIENT__.cache){"
                + "  window.__APOLLO_CLIENT__.cache.evict({fieldName:'tableViews'});"
                + "  window.__APOLLO_CLIENT__.cache.gc();"
                + "}"
                + "}catch(e){}"
                + "try{"
                + "  var backdrop=document.querySelector('.cdk-overlay-backdrop.cdk-overlay-backdrop-showing');"
                + "  if(backdrop)backdrop.click();"
                + "}catch(e){}");
        refreshGraphqlAuth();
    }

    public static void reloadFulfillmentPage() {
        try {
            String url = com.paramount.test.ff.common.util.Config.getString("TargetUrl");
            if (url == null || url.trim().isEmpty()) {
                url = com.paramount.test.ff.common.util.Config.getString("TargetUrlDEV");
            }
            if (url == null || url.trim().isEmpty()) {
                url = "https://dev-operationsconsole.paramountmsc.com/fulfillment/";
            }
            BaseTest.driver.get().browser().getUrl(url);
            sleep(8000);
            try {
                BaseTest.driver.get().browser().executeScript(
                        "document.documentElement.style.zoom = '1';"
                                + "if (document.body) { document.body.style.zoom = '1'; }");
            } catch (Exception ignored) {
            }
            installNetworkCaptureHook();
            installExportCaptureHook();
            TableViewGraphqlClient.installAuthCaptureHook();
        } catch (Exception e) {
            Logger.logConsoleMessage("Reload fulfillment page failed: " + e.getMessage());
        }
    }

    /** Reloads orders home when the ag-grid is missing after filter changes. */
    public static void recoverOrdersGridIfMissing() throws InterruptedException {
        if (waitForOrdersGridRendered(5)) {
            return;
        }
        Logger.logReportMessage("Orders grid missing - reloading fulfillment home");
        reloadFulfillmentPage();
        sleep(3000);
        closeManageColumnsPanel();
        dismissBlockingOverlays();
        waitForOrdersGridRendered(30);
        if (!waitForOrdersGridRendered(3)) {
            Logger.logReportMessage("Orders grid still missing after reload");
        }
    }

    public static boolean waitForViewListedInApi(String viewName, boolean shouldExist, int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isViewNameVisible(viewName) == shouldExist) {
                return shouldExist;
            }
            sleep(250);
        }
        return isViewNameVisible(viewName) == shouldExist;
    }

    public static boolean renameViewViaApi(String currentName, String newName) {
        installNetworkCaptureHook();
        cacheSavedViewFromApi(currentName);
        if (TableViewGraphqlClient.renameTableView(currentName, newName)) {
            return waitForViewListedInApi(newName, true, 8)
                    && waitForViewListedInApi(currentName, false, 8);
        }
        String current = esc(currentName);
        String updated = esc(newName);
        if (runBrowserGraphqlSync(
                "try{renameViaGraphqlSync('" + current + "','" + updated + "');return 'ok';}"
                        + "catch(e){return 'fail:'+e.message;}")) {
            return waitForViewListedInApi(newName, true, 8)
                    && waitForViewListedInApi(currentName, false, 8);
        }
        run("window.__ffTableViewOpResult='pending';"
                + NETWORK_HOOK_JS
                + TABLE_VIEW_API_JS
                + "(async function(){"
                + "  try {"
                + "    await renameViaGraphql('" + current + "','" + updated + "');"
                + "    window.__ffTableViewOpResult='ok';"
                + "  } catch(e){window.__ffTableViewOpResult='fail:'+e.message;}"
                + "})();");
        if (!waitForTableViewOpResult("ok", 15000)) {
            Logger.logConsoleMessage("GraphQL rename result: " + getTableViewOpResult());
            return false;
        }
        return waitForViewListedInApi(newName, true, 8)
                && waitForViewListedInApi(currentName, false, 8);
    }

    public static String getTableViewOpResult() {
        Object result = run("return window.__ffTableViewOpResult;");
        return result == null ? "" : String.valueOf(result);
    }

    public static boolean deleteViewViaApi(String viewName) {
        installNetworkCaptureHook();
        cacheSavedViewFromApi(viewName);
        if (TableViewGraphqlClient.deleteTableView(viewName)) {
            return waitForViewListedInApi(viewName, false, 8);
        }
        String escaped = esc(viewName);
        if (runBrowserGraphqlSync(
                "try{deleteViaGraphqlSync('" + escaped + "');return 'ok';}"
                        + "catch(e){return 'fail:'+e.message;}")) {
            return waitForViewListedInApi(viewName, false, 8);
        }
        run("window.__ffTableViewOpResult='pending';"
                + NETWORK_HOOK_JS
                + TABLE_VIEW_API_JS
                + "(async function(){"
                + "  try {"
                + "    await deleteViaGraphql('" + escaped + "');"
                + "    window.__ffTableViewOpResult='ok';"
                + "  } catch(e){window.__ffTableViewOpResult='fail:'+e.message;}"
                + "})();");
        if (!waitForTableViewOpResult("ok", 15000)) {
            Logger.logConsoleMessage("GraphQL delete result: " + getTableViewOpResult());
            return false;
        }
        return waitForViewListedInApi(viewName, false, 8);
    }

    private static boolean waitForTableViewOpResult(String successToken, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            Object result = run("return window.__ffTableViewOpResult;");
            if (result == null) {
                sleep(400);
                continue;
            }
            String value = String.valueOf(result);
            if (successToken.equals(value)) {
                return true;
            }
            if (value.startsWith("fail")) {
                return false;
            }
            sleep(400);
        }
        Object result = run("return window.__ffTableViewOpResult;");
        return result != null && successToken.equals(String.valueOf(result));
    }

    public static boolean renameViewViaScript(String currentName, String newName) {
        String current = esc(currentName);
        String updated = esc(newName);
        return truthy(run(
                IS_VISIBLE_FN
                        + CONTEXT_MENU_JS
                        + "if (!openViewDropdown()) return false;"
                        + "scrollViewIntoList('" + current + "');"
                        + "var row = findViewRow('" + current + "');"
                        + "if (!row) return false;"
                        + "fireContextMenu(row);"
                        + "if (!waitAndClickMenu('rename')) return false;"
                        + "var inputs = document.querySelectorAll("
                        + "'ngb-modal-window input,[role=dialog] input,.cdk-overlay-pane input');"
                        + "var input = null;"
                        + "for (var k = 0; k < inputs.length; k++) { if (isVisible(inputs[k])) { input = inputs[k]; break; } }"
                        + "if (!input) return false;"
                        + "input.focus(); input.value = '" + updated + "';"
                        + "input.dispatchEvent(new Event('input', {bubbles:true}));"
                        + "input.dispatchEvent(new Event('change', {bubbles:true}));"
                        + "var buttons = document.querySelectorAll("
                        + "'ngb-modal-window button,[role=dialog] button,.cdk-overlay-pane button');"
                        + "for (var m = 0; m < buttons.length; m++) {"
                        + "  var t = norm(buttons[m].textContent).toLowerCase();"
                        + "  if (isVisible(buttons[m]) && (t === 'save' || t.indexOf('save') >= 0)) {"
                        + "    buttons[m].click(); return true;"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    public static boolean deleteViewViaScript(String viewName) {
        String escaped = esc(viewName);
        return truthy(run(
                IS_VISIBLE_FN
                        + CONTEXT_MENU_JS
                        + "function confirmDelete(){"
                        + "  var buttons = document.querySelectorAll("
                        + "'ngb-modal-window button,[role=dialog] button,.cdk-overlay-pane button,button');"
                        + "  for (var i = 0; i < buttons.length; i++) {"
                        + "    var t = norm(buttons[i].textContent).toLowerCase();"
                        + "    if (isVisible(buttons[i]) && (t.indexOf('delete') >= 0 || t.indexOf('confirm') >= 0 || t === 'yes')) {"
                        + "      buttons[i].click(); return true;"
                        + "    }"
                        + "  }"
                        + "  return false;"
                        + "}"
                        + "if (!openViewDropdown()) return false;"
                        + "scrollViewIntoList('" + escaped + "');"
                        + "var row = findViewRow('" + escaped + "');"
                        + "if (!row) return false;"
                        + "fireContextMenu(row);"
                        + "if (!waitAndClickMenu('delete')) return false;"
                        + "return confirmDelete();"));
    }

    private static String gridVisibilityScript(String columnName, String columnId) {
        String escapedId = esc(columnId == null ? "" : columnId);
        return gridVisibilityScriptSharedPrefix()
                + "var candidates=" + buildCandidateArray(columnName) + ".map(norm);"
                + "var colId = '" + escapedId + "';"
                + "return !!scrollUntilVisible(candidates, colId);";
    }

    private static String buildScrollHeaderIntoViewScript(String columnName, String columnId) {
        String escapedId = esc(columnId == null ? "" : columnId);
        return gridVisibilityScriptSharedPrefix()
                + "var candidates=" + buildCandidateArray(columnName) + ".map(norm);"
                + "var colId = '" + escapedId + "';"
                + "return !!scrollUntilVisible(candidates, colId);";
    }

    private static String buildCandidateArray(String columnName) {
        String[] candidates = gridSearchHeaderCandidates(columnName);
        StringBuilder candidateArray = new StringBuilder("[");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                candidateArray.append(',');
            }
            candidateArray.append('\'').append(esc(candidates[i])).append('\'');
        }
        candidateArray.append(']');
        return candidateArray.toString();
    }

    private static String gridVisibilityScriptSharedPrefix() {
        return IS_VISIBLE_FN
                + "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function headerCell(el){return el && (el.closest('.ag-header-cell,[role=columnheader]') || el);}"
                + "function headerUsable(el){"
                + "  var cell = headerCell(el);"
                + "  if (!cell) return false;"
                + "  var s = window.getComputedStyle(cell);"
                + "  return s.display !== 'none' && s.visibility !== 'hidden';"
                + "}"
                + "function headerText(el){"
                + "  var cell = headerCell(el);"
                + "  if (!cell) return '';"
                + "  var ellipsis = cell.querySelector('.label-ellipsis span');"
                + "  if (ellipsis) {"
                + "    var et = norm(ellipsis.innerText || ellipsis.textContent);"
                + "    if (et) return et;"
                + "  }"
                + "  var label = cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  var raw = label ? (label.innerText || label.textContent) : (cell.innerText || cell.textContent);"
                + "  return norm(raw || cell.getAttribute('aria-label') || cell.getAttribute('title'));"
                + "}"
                + "function labelMatches(text, cand){"
                + "  if (!text || !cand) return false;"
                + "  if (text === cand || text.indexOf(cand) >= 0 || cand.indexOf(text) >= 0) {"
                + "    if (cand === 'partner' && text !== 'partner') return false;"
                + "    if (cand === 'order id' && text !== 'order id') return false;"
                + "    if (cand === 'type' && text !== 'type') return false;"
                + "    return true;"
                + "  }"
                + "  if (text.indexOf('...') >= 0 || text.indexOf('\\u2026') >= 0) {"
                + "    var prefix = text.replace(/\\.{3,}$/, '').replace(/\\u2026$/, '').trim();"
                + "    if (prefix.length >= 4 && (cand.indexOf(prefix) === 0 || prefix.indexOf(cand) === 0)) return true;"
                + "  }"
                + "  return false;"
                + "}"
                + ORDERS_GRID_ROOT_FN
                + "function headerCellsInOrders(){"
                + "  var root=ordersGridRoot();"
                + "  return root.querySelectorAll("
                + "'.ag-header-cell,.ag-pinned-left-header .ag-header-cell,.ag-pinned-right-header .ag-header-cell,[role=columnheader],thead th');"
                + "}"
                + "function inViewport(cell){"
                + "  if (!cell) return false;"
                + "  var r = cell.getBoundingClientRect();"
                + "  if (r.width <= 0 || r.height <= 0) return false;"
                + "  var pad = Math.max(80, window.innerWidth * 0.12);"
                + "  return r.right > -pad && r.left < window.innerWidth + pad;"
                + "}"
                + "function findHeader(candidates){"
                + "  var cells = headerCellsInOrders();"
                + "  for (var i = 0; i < cells.length; i++) {"
                + "    if (!headerUsable(cells[i])) continue;"
                + "    var t = headerText(cells[i]);"
                + "    if (!t) continue;"
                + "    for (var c = 0; c < candidates.length; c++) {"
                + "      if (labelMatches(t, candidates[c]) && inViewport(cells[i])) return cells[i];"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function findHeaderAny(candidates){"
                + "  var cells = headerCellsInOrders();"
                + "  for (var i = 0; i < cells.length; i++) {"
                + "    if (!headerUsable(cells[i])) continue;"
                + "    var t = headerText(cells[i]);"
                + "    if (!t) continue;"
                + "    for (var c = 0; c < candidates.length; c++) {"
                + "      if (labelMatches(t, candidates[c])) return cells[i];"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function colIdVariants(id){"
                + "  var out = [];"
                + "  if (!id) return out;"
                + "  out.push(id);"
                + "  var snake = id.replace(/([A-Z])/g, '_$1').toLowerCase().replace(/^_/, '');"
                + "  if (snake && out.indexOf(snake) < 0) out.push(snake);"
                + "  if (id.indexOf('Order') >= 0) out.push(id.replace(/Order$/,''));"
                + "  if (id.indexOf('Package') >= 0) out.push(id.replace(/Package$/,''));"
                + "  if (id.indexOf('LineItem') >= 0) out.push(id.replace(/LineItem$/,''));"
                + "  var low = id.toLowerCase();"
                + "  if (low.indexOf('orderstart') >= 0 || low === 'startdate') {"
                + "    out.push('startdate','orderstart','order_start_date','orderstartdate');"
                + "  }"
                + "  if (low.indexOf('orderend') >= 0 || low === 'enddate') {"
                + "    out.push('enddate','orderend','order_end_date','orderenddate');"
                + "  }"
                + "  return out;"
                + "}"
                + "function findByColIdAny(id){"
                + "  var variants = colIdVariants(id);"
                + "  if (!variants.length) return null;"
                + "  var headers = headerCellsInOrders();"
                + "  for (var i = 0; i < headers.length; i++) {"
                + "    var colIdAttr = (headers[i].getAttribute('col-id') || '').toLowerCase();"
                + "    if (!colIdAttr) continue;"
                + "    for (var v = 0; v < variants.length; v++) {"
                + "      var want = variants[v].toLowerCase();"
                + "      if (id && String(id).toLowerCase() === 'partner' && colIdAttr !== 'partner') continue;"
                + "      if (id && String(id).toLowerCase() === 'orderid' && colIdAttr !== 'orderid') continue;"
                + "      if (colIdAttr === want || colIdAttr.indexOf(want) >= 0 || want.indexOf(colIdAttr) >= 0) {"
                + "        if (headerUsable(headers[i])) return headers[i];"
                + "      }"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function findByColId(id){"
                + "  var variants = colIdVariants(id);"
                + "  if (!variants.length) return null;"
                + "  var headers = headerCellsInOrders();"
                + "  for (var i = 0; i < headers.length; i++) {"
                + "    var colIdAttr = (headers[i].getAttribute('col-id') || '').toLowerCase();"
                + "    if (!colIdAttr) continue;"
                + "    for (var v = 0; v < variants.length; v++) {"
                + "      var want = variants[v].toLowerCase();"
                + "      if (id && String(id).toLowerCase() === 'partner' && colIdAttr !== 'partner') continue;"
                + "      if (id && String(id).toLowerCase() === 'orderid' && colIdAttr !== 'orderid') continue;"
                + "      if (colIdAttr === want || colIdAttr.indexOf(want) >= 0 || want.indexOf(colIdAttr) >= 0) {"
                + "        try { headers[i].scrollIntoView({inline:'center', block:'nearest'}); } catch(e) {}"
                + "        if (headerUsable(headers[i]) && inViewport(headers[i])) return headers[i];"
                + "      }"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function resetScroll(){"
                + "  var viewports = ordersGridRoot().querySelectorAll("
                + "'.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport,.ag-body-viewport');"
                + "  for (var i = 0; i < viewports.length; i++) viewports[i].scrollLeft = 0;"
                + "}"
                + "function scrollHeaders(delta){"
                + "  var viewports = ordersGridRoot().querySelectorAll("
                + "'.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport,.ag-body-viewport');"
                + "  for (var i = 0; i < viewports.length; i++) {"
                + "    viewports[i].scrollLeft = Math.max(0, (viewports[i].scrollLeft || 0) + delta);"
                + "  }"
                + "}"
                + "function scrollUntilVisible(candidates, colId){"
                + "  resetScroll();"
                + "  for (var pass = 0; pass < 40; pass++) {"
                + "    var hit = findHeader(candidates) || findByColId(colId);"
                + "    if (hit) { try { hit.scrollIntoView({inline:'center', block:'nearest'}); } catch(e) {} return hit; }"
                + "    scrollHeaders(450);"
                + "  }"
                + "  resetScroll();"
                + "  for (var back = 0; back < 8; back++) {"
                + "    var hit2 = findHeader(candidates) || findByColId(colId);"
                + "    if (hit2) { try { hit2.scrollIntoView({inline:'center', block:'nearest'}); } catch(e) {} return hit2; }"
                + "    scrollHeaders(-450);"
                + "  }"
                + "  return findHeaderAny(candidates) || findByColIdAny(colId);"
                + "}";
    }

    private static String[] gridSearchHeaderCandidates(String columnName) {
        if (columnName == null) {
            return new String[] { "" };
        }
        if ("Title".equalsIgnoreCase(columnName)
                || "Season".equalsIgnoreCase(columnName)
                || "Episode".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Title, Season, Episode", "Title/Season/Episode" };
        }
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Title", "Season", "Episode", "Title/Season/Episode" };
        }
        if ("Delivery date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Delivery Date" };
        }
        if ("Offset Delivery date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Offset Delivery Date" };
        }
        if ("Order start date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order Start Date", "Start date", "Start Date" };
        }
        if ("Order end date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order End Date", "End date", "End Date" };
        }
        return new String[] { columnName };
    }

    private static String gridSearchControlScript(String columnName, String columnId) {
        String escapedId = esc(columnId == null ? "" : columnId);
        String[] candidates = gridSearchHeaderCandidates(columnName);
        StringBuilder candidateArray = new StringBuilder("[");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                candidateArray.append(',');
            }
            candidateArray.append('\'').append(esc(candidates[i])).append('\'');
        }
        candidateArray.append(']');
        return IS_VISIBLE_FN
                + "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                + ORDERS_GRID_ROOT_FN
                + "var candidates=" + candidateArray + ".map(norm);"
                + "var columnId=normId('" + escapedId + "');"
                + "var idAliases=[];"
                + "if(columnId){idAliases.push(columnId);}"
                + "if(columnId.indexOf('titleseason')>=0){idAliases.push('title','titleseasonepisode');}"
                + "if(columnId.indexOf('assetid')>=0){idAliases.push('assetid','asset');}"
                + "if(columnId.indexOf('submittedby')>=0){idAliases.push('submittedby','submitted');}"
                + "if(columnId.indexOf('assignedto')>=0){idAliases.push('assignedto','assigned');}"
                + "function scrollHeaders(delta){"
                + "  var viewports=ordersGridRoot().querySelectorAll("
                + "'.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport');"
                + "  for(var i=0;i<viewports.length;i++){"
                + "    if(delta===0)viewports[i].scrollLeft=0;"
                + "    else viewports[i].scrollLeft=Math.max(0,(viewports[i].scrollLeft||0)+delta);"
                + "  }"
                + "}"
                + "function cellText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent)"
                + "    ||cell.getAttribute('aria-label')||cell.getAttribute('title'));"
                + "}"
                + "function matches(cell){"
                + "  var text=cellText(cell);"
                + "  var colId=normId(cell.getAttribute('col-id'));"
                + "  for(var a=0;a<idAliases.length;a++){"
                + "    var alias=idAliases[a];"
                + "    if(!alias||!colId)continue;"
                + "    if(columnId==='partner'&&colId!=='partner')continue;"
                + "    if(columnId==='partnerprofile'&&colId!=='partnerprofile')continue;"
                + "    if(columnId==='type'&&colId!=='type')continue;"
                + "    if(colId===alias||colId.indexOf(alias)>=0||alias.indexOf(colId)>=0)return true;"
                + "  }"
                + "  for(var c=0;c<candidates.length;c++){"
                + "    var cand=candidates[c];"
                + "    if(!text||!cand)continue;"
                + "    if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + "      if(cand==='partner'&&text!=='partner')continue;"
                + "      if(cand==='order id'&&text!=='order id')continue;"
                + "      if(cand==='type'&&text!=='type')continue;"
                + "      return true;"
                + "    }"
                + "    if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "      var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "      if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "    }"
                + "  }"
                + "  return false;"
                + "}"
                + "function headerHasSearch(cell){"
                + "  if(!cell)return false;"
                + "  if(cell.querySelector('msc-custom-table-column-filter,msc-custom-table-column-filter-themed'))return true;"
                + "  var html=(cell.innerHTML||'').toLowerCase();"
                + "  if(html.indexOf('search-icon')>=0||html.indexOf('search')>=0)return true;"
                + "  var sel='input,.search-icon,[class*=search],[class*=Search],[class*=filter],[class*=Filter],"
                + "    .ag-icon-filter,.ag-header-cell-filter-button,mat-icon,svg,[aria-label*=search],[aria-label*=filter]';"
                + "  if(cell.querySelector(sel))return true;"
                + "  var sort=cell.querySelector('.ag-sort-indicator-container,.ag-sort-indicator-icon');"
                + "  var menu=cell.querySelector('.ag-header-cell-menu-button');"
                + "  var icons=cell.querySelectorAll('svg,img,button,i,span[class*=icon],mat-icon');"
                + "  for(var k=0;k<icons.length;k++){"
                + "    var el=icons[k];"
                + "    if(sort&&sort.contains(el))continue;"
                + "    if(menu&&menu.contains(el))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-text'))continue;"
                + "    return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function headerCells(){"
                + "  var cells=[];"
                + "  ordersGridRoot().querySelectorAll('.ag-header-cell,[role=columnheader]').forEach(function(c){cells.push(c);});"
                + "  return cells;"
                + "}"
                + "function columnHasSearchUi(cell){"
                + "  if(!cell)return false;"
                + "  if(headerHasSearch(cell))return true;"
                + "  var colId=cell.getAttribute('col-id');"
                + "  if(colId){"
                + "    if(document.getElementById('orderTable'+colId))return true;"
                + "    for(var a2=0;a2<idAliases.length;a2++){"
                + "      var aid=idAliases[a2];"
                + "      if(aid&&document.getElementById('orderTable'+aid))return true;"
                + "    }"
                + "    var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+colId+'\"]');"
                + "    if(floatCell&&floatCell.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                + "  }"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  if(idx){"
                + "    var floatByIdx=document.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                + "    if(floatByIdx&&floatByIdx.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                + "  }"
                + "  var html=(cell.innerHTML||'').toLowerCase();"
                + "  if(html.indexOf('search-icon')>=0||html.indexOf('search')>=0)return true;"
                + "  var icons=cell.querySelectorAll('svg,img,.search-icon,[class*=icon],button,i');"
                + "  return icons.length>=2;"
                + "}"
                + "for(var pass=0;pass<8;pass++){"
                + "  scrollHeaders(pass===0?0:450);"
                + "  var cells=headerCells();"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(matches(cells[i])&&columnHasSearchUi(cells[i]))return true;"
                + "  }"
                + "}"
                + "var icons=ordersGridRoot().querySelectorAll('.search-icon,[class*=search-icon]');"
                + "if(icons.length){"
                + "  for(var pass2=0;pass2<12;pass2++){"
                + "    scrollHeaders(pass2===0?0:450);"
                + "    var cells2=headerCells();"
                + "    for(var h=0;h<cells2.length;h++){"
                + "      if(!matches(cells2[h]))continue;"
                + "      var rect=cells2[h].getBoundingClientRect();"
                + "      for(var k=0;k<icons.length;k++){"
                + "        var ir=icons[k].getBoundingClientRect();"
                + "        if(Math.abs(ir.top-rect.top)<40&&ir.left>=rect.left-20&&ir.left<=rect.right+80)return true;"
                + "      }"
                + "    }"
                + "  }"
                + "}"
                + "return false;";
    }

    private static boolean findColumnCheckbox(String columnName, String section, Boolean checked, String columnId) {
        String escaped = esc(columnName);
        String escapedSection = esc(section == null ? "order" : section);
        String escapedColumnId = esc(columnId == null ? "" : columnId);
        String toggle = checked == null ? "null" : String.valueOf(checked);
        return truthy(run(
                IS_VISIBLE_FN
                        + columnCheckboxFinderJs()
                        + "var exactOnly = true;"
                        + "var cb = findCheckbox('" + escaped + "','" + escapedSection + "', exactOnly, '" + escapedColumnId + "');"
                        + "if (!cb) return false;"
                        + "var want = " + toggle + ";"
                        + "if (want === null) return true;"
                        + "if (isCheckboxChecked(cb) === want) return true;"
                        + "clickCheckbox(cb);"
                        + "return isCheckboxChecked(cb) === want;"));
    }

    private static Object run(String script) {
        try {
            return SynergyRetryUtil.executeScript(script);
        } catch (Exception e) {
            if (SynergyRetryUtil.isConnectionError(e)) {
                Logger.logConsoleMessage("JS execute skipped after Synergy retry: " + e.getMessage());
            }
            return null;
        }
    }

    private static boolean truthy(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return "true".equalsIgnoreCase(String.valueOf(result));
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- Line Items tab (top-level tab, separate from Orders nested line-item columns) ---

    private static final String LINE_ITEM_TAB_JS = IS_VISIBLE_FN
            + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
            + "function isLineItemManagePanel(t){"
            + "  t=norm(t);"
            + "  return /manage columns/i.test(t)&&/line item table/i.test(t);"
            + "}"
            + "function findLineItemManagePanel(){"
            + "  var panes=document.querySelectorAll('.cdk-overlay-pane,.wrapper-dropdown-container');"
            + "  for(var i=0;i<panes.length;i++){"
            + "    if(isLineItemManagePanel(panes[i].textContent)&&isVisible(panes[i]))return panes[i];"
            + "  }"
            + "  var titles=document.querySelectorAll('span.wrapper-dropdown-title,h3,div,span');"
            + "  for(var j=0;j<titles.length;j++){"
            + "    if(!isLineItemManagePanel(titles[j].textContent)||!isVisible(titles[j]))continue;"
            + "    var root=titles[j].closest('.wrapper-dropdown-container,.cdk-overlay-pane');"
            + "    if(root)return root;"
            + "  }"
            + "  return null;"
            + "}";

    public static boolean clickLineItemsTab() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var nodes=document.querySelectorAll('button,a,[role=tab],span,div,label');"
                        + "for(var i=0;i<nodes.length;i++){"
                        + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(/^line items?$/i.test(t)&&isVisible(nodes[i])){"
                        + "    try{nodes[i].click();return true;}catch(e){}"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    public static boolean isLineItemsTabActive() {
        return truthy(run(
                IS_VISIBLE_FN
                        + "var nodes=document.querySelectorAll('button,a,[role=tab],span,div');"
                        + "for(var i=0;i<nodes.length;i++){"
                        + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(!/^line items?$/i.test(t)||!isVisible(nodes[i]))continue;"
                        + "  var cls=(nodes[i].className||'')+' '+(nodes[i].getAttribute&&nodes[i].getAttribute('aria-selected')||'');"
                        + "  if(/active|selected|current|aria-selected/.test(cls)||nodes[i].getAttribute('aria-selected')==='true')return true;"
                        + "  var parent=nodes[i].parentElement;"
                        + "  if(parent){"
                        + "    var pcls=parent.className||'';"
                        + "    if(/active|selected|current/.test(pcls))return true;"
                        + "  }"
                        + "}"
                        + "var body=(document.body&&document.body.innerText)||'';"
                        + "return /line items?\\s+\\d[\\d,]*\\s+results/i.test(body);"));
    }

    public static boolean waitForLineItemsGridReady(int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isLineItemsGridReady()) {
                return true;
            }
            sleep(250);
        }
        return isLineItemsGridReady();
    }

    public static boolean isLineItemsGridReady() {
        return truthy(run(
                IS_VISIBLE_FN
                        + ORDERS_GRID_ROOT_FN
                        + "function headerHasLineItemId(root){"
                        + "  var cells=root.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "  for(var i=0;i<cells.length;i++){"
                        + "    if(!isVisible(cells[i]))continue;"
                        + "    var t=(cells[i].innerText||cells[i].textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();"
                        + "    if(t.indexOf('lineitem id')>=0||t.indexOf('line item id')>=0)return true;"
                        + "  }"
                        + "  return false;"
                        + "}"
                        + "var root=ordersGridRoot();"
                        + "if(!root||root===document)return false;"
                        + "var headers=root.querySelectorAll('.ag-header-cell,[role=columnheader]').length;"
                        + "if(headers<3)return false;"
                        + "return headerHasLineItemId(root)||isLineItemsTabActive();"));
    }

    public static boolean isLineItemTabManageColumnsPanelOpen() {
        return truthy(run(LINE_ITEM_TAB_JS + "return !!findLineItemManagePanel();"));
    }

    public static boolean openLineItemTabManageColumnsPanel() {
        if (isLineItemTabManageColumnsPanelOpen()) {
            return true;
        }
        if (!truthy(run(
                "var btn=document.querySelector('#tableViewButton')||document.querySelector('button.table-view-button');"
                        + "if(!btn)return false;btn.click();return true;"))) {
            return false;
        }
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            if (isLineItemTabManageColumnsPanelOpen()) {
                return true;
            }
            sleep(200);
        }
        return isLineItemTabManageColumnsPanelOpen();
    }

    public static boolean scrollLineItemTabManagePanelToColumn(String columnName) {
        String esc = esc(columnName);
        return truthy(run(LINE_ITEM_TAB_JS
                + "function findLabel(panel,target){"
                + "  var nodes=panel.querySelectorAll('label,span,div,.column-item');"
                + "  for(var i=0;i<nodes.length;i++){"
                + "    var t=norm(nodes[i].textContent);"
                + "    if(t!==target&&t.indexOf(target)<0&&target.indexOf(t)<0)continue;"
                + "    if(!isVisible(nodes[i]))continue;"
                + "    nodes[i].scrollIntoView({block:'center'});"
                + "    return nodes[i];"
                + "  }"
                + "  return null;"
                + "}"
                + "var panel=findLineItemManagePanel();"
                + "if(!panel)return false;"
                + "var target=norm('" + esc + "');"
                + "var scroll=panel.querySelector('.multi-options-list,[style*=overflow],.scroll');"
                + "if(!scroll)scroll=panel;"
                + "for(var a=0;a<25;a++){"
                + "  if(findLabel(panel,target))return true;"
                + "  scroll.scrollTop+=220;"
                + "}"
                + "return !!findLabel(panel,target);"));
    }

    public static boolean isLineItemTabColumnListed(String columnName) {
        String esc = esc(columnName);
        return truthy(run(LINE_ITEM_TAB_JS
                + "var panel=findLineItemManagePanel();"
                + "if(!panel)return false;"
                + "var target=norm('" + esc + "');"
                + "var nodes=panel.querySelectorAll('label,span,div,.column-item');"
                + "for(var i=0;i<nodes.length;i++){"
                + "  var t=norm(nodes[i].textContent);"
                + "  if(t===target||t.indexOf(target)>=0||target.indexOf(t)>=0)return true;"
                + "}"
                + "return false;"));
    }

    public static boolean isLineItemTabColumnChecked(String columnName) {
        String esc = esc(columnName);
        return truthy(run(LINE_ITEM_TAB_JS
                + "function resolveCheckbox(labelEl){"
                + "  var cb=labelEl.previousElementSibling;"
                + "  if(!cb||cb.type!=='checkbox'){"
                + "    cb=labelEl.parentElement&&labelEl.parentElement.querySelector('input[type=checkbox]');"
                + "  }"
                + "  if(!cb||cb.type!=='checkbox'){"
                + "    cb=labelEl.closest('.column-item,.form-check,div')"
                + "      &&labelEl.closest('.column-item,.form-check,div').querySelector('input[type=checkbox]');"
                + "  }"
                + "  return cb&&cb.type==='checkbox'?cb:null;"
                + "}"
                + "var panel=findLineItemManagePanel();"
                + "if(!panel)return false;"
                + "var target=norm('" + esc + "');"
                + "var labels=panel.querySelectorAll('label');"
                + "for(var i=0;i<labels.length;i++){"
                + "  var t=norm(labels[i].textContent);"
                + "  if(t!==target&&t.indexOf(target)<0&&target.indexOf(t)<0)continue;"
                + "  var cb=resolveCheckbox(labels[i]);"
                + "  return !!(cb&&cb.checked);"
                + "}"
                + "return false;"));
    }

    public static boolean setLineItemTabColumnChecked(String columnName, boolean checked) {
        String esc = esc(columnName);
        return truthy(run(LINE_ITEM_TAB_JS
                + "function resolveCheckbox(labelEl){"
                + "  var cb=labelEl.previousElementSibling;"
                + "  if(!cb||cb.type!=='checkbox'){"
                + "    cb=labelEl.parentElement&&labelEl.parentElement.querySelector('input[type=checkbox]');"
                + "  }"
                + "  if(!cb||cb.type!=='checkbox'){"
                + "    cb=labelEl.closest('.column-item,.form-check,div')"
                + "      &&labelEl.closest('.column-item,.form-check,div').querySelector('input[type=checkbox]');"
                + "  }"
                + "  return cb&&cb.type==='checkbox'?cb:null;"
                + "}"
                + "var panel=findLineItemManagePanel();"
                + "if(!panel)return false;"
                + "var target=norm('" + esc + "');"
                + "var want=" + checked + ";"
                + "var labels=panel.querySelectorAll('label');"
                + "for(var i=0;i<labels.length;i++){"
                + "  var t=norm(labels[i].textContent);"
                + "  if(t!==target&&t.indexOf(target)<0&&target.indexOf(t)<0)continue;"
                + "  labels[i].scrollIntoView({block:'center'});"
                + "  var cb=resolveCheckbox(labels[i]);"
                + "  if(!cb){labels[i].click();return true;}"
                + "  if(!!cb.checked===want)return true;"
                + "  cb.click();"
                + "  return !!cb.checked===want;"
                + "}"
                + "return false;"));
    }

    public static boolean isLineItemTabGridColumnVisible(String columnName, String columnId) {
        String nameEsc = esc(columnName);
        String idEsc = esc(columnId == null ? "" : columnId);
        return truthy(run(
                IS_VISIBLE_FN
                        + ORDERS_GRID_ROOT_FN
                        + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "var root=ordersGridRoot();"
                        + "if(!root||root===document)return false;"
                        + "var target=norm('" + nameEsc + "');"
                        + "var id=norm('" + idEsc + "');"
                        + "var cells=root.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "for(var i=0;i<cells.length;i++){"
                        + "  if(!isVisible(cells[i]))continue;"
                        + "  var t=norm(cells[i].innerText||cells[i].textContent);"
                        + "  var cid=norm(cells[i].getAttribute('col-id')||'');"
                        + "  if(t===target||t.indexOf(target)>=0||target.indexOf(t)>=0)return true;"
                        + "  if(id&&cid&&(cid===id||cid.indexOf(id)>=0||id.indexOf(cid)>=0))return true;"
                        + "}"
                        + "return false;"));
    }

    public static boolean clickRefreshLineItemsTable() {
        return clickRefreshOrdersTable();
    }
}
