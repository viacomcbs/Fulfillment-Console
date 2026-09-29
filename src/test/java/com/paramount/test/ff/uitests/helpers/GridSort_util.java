package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.TableView;
import com.synergy.core.driver.By;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * AG Grid column sort helpers for Orders tab (ascending / descending verification).
 */
public class GridSort_util {

    private static final int DEFAULT_MAX_ROWS = 15;
    private static final int MAX_SORT_CLICKS = 4;

    private final TableView tableView = new TableView();

    public String resolveColumnId(String columnName, String section) {
        if ("table".equalsIgnoreCase(section)) {
            LineItemTabColumnData.ColumnDef lineItemColumn = LineItemTabColumnData.findByLabel(columnName);
            if (lineItemColumn == null) {
                if ("Title".equalsIgnoreCase(columnName)
                        || "Season".equalsIgnoreCase(columnName)
                        || "Episode".equalsIgnoreCase(columnName)) {
                    lineItemColumn = LineItemTabColumnData.findByLabel("Title, Season, Episode");
                }
            }
            if (lineItemColumn != null) {
                String actual = resolveHeaderColId(lineItemColumn.id, columnName);
                return actual != null && !actual.isEmpty() ? actual : lineItemColumn.id;
            }
        }
        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column == null && "order".equalsIgnoreCase(section)) {
            if ("Title".equalsIgnoreCase(columnName)
                    || "Season".equalsIgnoreCase(columnName)
                    || "Episode".equalsIgnoreCase(columnName)) {
                column = OrderTabColumnData.findByLabelAndSection("Title, Season, Episode", section);
            }
        }
        String resolved = column != null ? column.id : columnName;
        String actual = resolveHeaderColId(resolved, columnName);
        return actual != null && !actual.isEmpty() ? actual : resolved;
    }

    public String resolveHeaderColId(String columnId, String columnName) {
        String escapedId = esc(columnId == null ? "" : columnId);
        String[] labels = headerLabelCandidates(columnName);
        StringBuilder labelArray = new StringBuilder("[");
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                labelArray.append(',');
            }
            labelArray.append('\'').append(esc(labels[i])).append('\'');
        }
        labelArray.append(']');
        Object result = runScript(
                "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function headerLabelText(cell){"
                        + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                        + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                        + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                        + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                        + "}"
                        + "var colId='" + escapedId + "';"
                        + "var labels=" + labelArray + ";"
                        + "function findHeader(){"
                        + "  var cell=document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                        + "  if(cell)return cell;"
                        + "  var headers=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "  for(var i=0;i<headers.length;i++){"
                        + "    var t=headerLabelText(headers[i]);"
                        + "    for(var j=0;j<labels.length;j++){"
                        + "      var n=norm(labels[j]);"
                        + "      if(t===n||t.indexOf(n)>=0||n.indexOf(t)>=0)return headers[i];"
                        + "    }"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "var h=findHeader();"
                        + "return h?h.getAttribute('col-id'):'';");
        return result == null ? "" : String.valueOf(result);
    }

    public String[] headerLabelCandidates(String columnName) {
        if ("Title".equalsIgnoreCase(columnName)
                || "Season".equalsIgnoreCase(columnName)
                || "Episode".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Title, Season, Episode", "Title/Season/Episode" };
        }
        if ("Order start date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order Start Date" };
        }
        if ("Order end date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Order End Date" };
        }
        if ("Partner Profile".equalsIgnoreCase(columnName)) {
            return new String[] { columnName };
        }
        if ("Package name".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Package Name" };
        }
        if ("Delivery date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Delivery Date" };
        }
        if ("Offset Delivery date".equalsIgnoreCase(columnName)) {
            return new String[] { columnName, "Offset Delivery Date" };
        }
        return new String[] { columnName };
    }

    public void scrollColumnHeaderIntoView(String columnId, String columnName) {
        scrollHeaderIntoView(columnId, headerLabelCandidates(columnName));
    }

    public List<String> readVisibleColumnValues(String columnId, int maxRows) {
        return getVisibleColumnValues(columnId, maxRows);
    }

    public boolean isSortableHeaderVisible(String columnId, String columnName) {
        scrollHeaderIntoView(columnId, headerLabelCandidates(columnName));
        if (FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.gridColumnHeader(columnName), 1)) {
            return true;
        }
        if (columnId != null && !columnId.isEmpty()
                && WaitUtil.isDisplay(tableView.gridColumnHeaderByColId(columnId), 1)) {
            return true;
        }
        Object found = runScript(
                "var colId='" + esc(columnId) + "';"
                        + "return !!document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');");
        return Boolean.TRUE.equals(found) || "true".equalsIgnoreCase(String.valueOf(found));
    }

    public void sortAscending(String columnId, String columnName) throws InterruptedException {
        applySortDirection(columnId, columnName, "asc");
    }

    public void sortDescending(String columnId, String columnName) throws InterruptedException {
        applySortDirection(columnId, columnName, "desc");
    }

    public void applySortDirection(String columnId, String columnName, String targetDirection)
            throws InterruptedException {
        scrollHeaderIntoView(columnId, headerLabelCandidates(columnName));
        String resolvedColId = resolveHeaderColId(columnId, columnName);
        if (resolvedColId != null && !resolvedColId.isEmpty()) {
            columnId = resolvedColId;
        }
        String current = getSortDirection(columnId, columnName);
        for (int click = 0; click < MAX_SORT_CLICKS && !targetDirection.equals(current); click++) {
            clickColumnHeader(columnId, columnName);
            for (int poll = 0; poll < 10 && !targetDirection.equals(current); poll++) {
                Thread.sleep(150);
                resolvedColId = resolveHeaderColId(columnId, columnName);
                if (resolvedColId != null && !resolvedColId.isEmpty()) {
                    columnId = resolvedColId;
                }
                current = getSortDirection(columnId, columnName);
            }
        }
    }

    public String getSortDirection(String columnId) {
        return getSortDirection(columnId, null);
    }

    public String getSortDirection(String columnId, String columnName) {
        String resolvedColId = resolveHeaderColId(columnId, columnName);
        if (resolvedColId != null && !resolvedColId.isEmpty()) {
            columnId = resolvedColId;
        }
        String escaped = esc(columnId);
        String[] labels = headerLabelCandidates(columnName == null ? "" : columnName);
        StringBuilder labelArray = new StringBuilder("[");
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                labelArray.append(',');
            }
            labelArray.append('\'').append(esc(labels[i])).append('\'');
        }
        labelArray.append(']');
        Object result = runScript(
                "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function headerLabelText(cell){"
                        + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                        + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                        + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                        + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                        + "}"
                        + "var colId='" + escaped + "';"
                        + "var labels=" + labelArray + ";"
                        + "function findHeader(){"
                        + "  var cell=document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                        + "  if(cell)return cell;"
                        + "  var headers=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "  for(var i=0;i<headers.length;i++){"
                        + "    var t=headerLabelText(headers[i]);"
                        + "    for(var j=0;j<labels.length;j++){"
                        + "      var n=norm(labels[j]);"
                        + "      if(t===n||t.indexOf(n)>=0||n.indexOf(t)>=0)return headers[i];"
                        + "    }"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "var cell=findHeader();"
                        + "if (!cell) return 'none';"
                        + "var aria = (cell.getAttribute('aria-sort') || '').toLowerCase();"
                        + "if (aria === 'ascending' || cell.classList.contains('ag-sort-ascending')) return 'asc';"
                        + "if (aria === 'descending' || cell.classList.contains('ag-sort-descending')) return 'desc';"
                        + "return 'none';");
        return result == null ? "none" : String.valueOf(result);
    }

    public void clickColumnHeader(String columnId, String columnName) {
        String escapedId = esc(columnId);
        String[] labels = headerLabelCandidates(columnName);
        StringBuilder labelArray = new StringBuilder("[");
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                labelArray.append(',');
            }
            labelArray.append('\'').append(esc(labels[i])).append('\'');
        }
        labelArray.append(']');
        runScript(
                "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function headerLabelText(cell){"
                        + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                        + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                        + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                        + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                        + "}"
                        + "var colId='" + escapedId + "';"
                        + "var labels=" + labelArray + ";"
                        + "function findHeader(){"
                        + "  var cell=document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                        + "  if(cell)return cell;"
                        + "  var headers=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "  for(var i=0;i<headers.length;i++){"
                        + "    var t=headerLabelText(headers[i]);"
                        + "    for(var j=0;j<labels.length;j++){"
                        + "      var n=norm(labels[j]);"
                        + "      if(t===n||t.indexOf(n)>=0||n.indexOf(t)>=0)return headers[i];"
                        + "    }"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "var h=findHeader();"
                        + "if(!h)return false;"
                        + "h.scrollIntoView({block:'nearest',inline:'center'});"
                        + "var clickTarget=h.querySelector('.label-ellipsis span,.ag-header-cell-label,.ag-header-cell-text,.ag-sort-indicator-container')||h;"
                        + "clickTarget.click();"
                        + "return true;");
        By header = tableView.gridColumnHeaderByColId(columnId);
        if (WaitUtil.isDisplay(header, 1)) {
            DriverUtil.clickOnElementSafely(header, 3);
        } else if (WaitUtil.isDisplay(tableView.gridColumnHeader(columnName), 1)) {
            DriverUtil.clickOnElementSafely(tableView.gridColumnHeader(columnName), 3);
        }
    }

    public void scrollHeaderIntoView(String columnId, String[] labelCandidates) {
        resetGridHorizontalScroll();
        String escapedId = esc(columnId);
        StringBuilder labelArray = new StringBuilder("[");
        for (int i = 0; i < labelCandidates.length; i++) {
            if (i > 0) {
                labelArray.append(',');
            }
            labelArray.append('\'').append(esc(labelCandidates[i])).append('\'');
        }
        labelArray.append(']');
        runScript(
                "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function headerLabelText(cell){"
                        + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                        + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                        + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                        + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                        + "}"
                        + "var colId='" + escapedId + "';"
                        + "var labels=" + labelArray + ";"
                        + "var viewport=document.querySelector('.ag-header-viewport,.ag-center-cols-viewport');"
                        + "function findHeader(){"
                        + "  var cell=document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                        + "  if(cell)return cell;"
                        + "  var headers=document.querySelectorAll('.ag-header-cell');"
                        + "  for(var i=0;i<headers.length;i++){"
                        + "    var t=headerLabelText(headers[i]);"
                        + "    for(var j=0;j<labels.length;j++){"
                        + "      var n=norm(labels[j]);"
                        + "      if(t===n||t.indexOf(n)>=0||n.indexOf(t)>=0)return headers[i];"
                        + "    }"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "for(var pass=0;pass<12;pass++){"
                        + "  var h=findHeader();"
                        + "  if(h){h.scrollIntoView({block:'nearest',inline:'center'});return true;}"
                        + "  if(viewport){viewport.scrollLeft=(viewport.scrollLeft||0)+320;}"
                        + "}"
                        + "return false;");
    }

    @SuppressWarnings("unchecked")
    public List<String> getVisibleColumnValues(String columnId, int maxRows) {
        String escaped = esc(columnId);
        Object result = runScript(
                "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                        + "function colIdVariants(id){"
                        + "  var out=[];"
                        + "  if(!id)return out;"
                        + "  out.push(id);"
                        + "  var snake=id.replace(/([A-Z])/g,'_$1').toLowerCase().replace(/^_/,'');"
                        + "  if(snake&&out.indexOf(snake)<0)out.push(snake);"
                        + "  if(id.indexOf('Order')>=0)out.push(id.replace(/Order$/,''));"
                        + "  if(id.indexOf('titleSeason')>=0){out.push('title','titleseasonEpisode','title_season_episode');}"
                        + "  if(id.indexOf('assetId')>=0)out.push('assetid','asset_id');"
                        + "  if(id.indexOf('xytechId')>=0)out.push('xytechid','xytech_id');"
                        + "  if(id.indexOf('orderId')>=0)out.push('orderid','order_id');"
                        + "  if(id.indexOf('dsid')>=0)out.push('dsid','d_s_i_d');"
                        + "  return out;"
                        + "}"
                        + "function scrollBody(delta){"
                        + "  var viewports=document.querySelectorAll("
                        + "'.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport,.ag-body-viewport');"
                        + "  for(var i=0;i<viewports.length;i++){"
                        + "    if(delta===0)viewports[i].scrollLeft=0;"
                        + "    else viewports[i].scrollLeft=Math.max(0,(viewports[i].scrollLeft||0)+delta);"
                        + "  }"
                        + "}"
                        + "function findCellForRow(rowIndex,target,variants){"
                        + "  var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                        + "    '.ag-pinned-right-cols-container'];"
                        + "  for(var ci=0;ci<containers.length;ci++){"
                        + "    var row=document.querySelector(containers[ci]+' > .ag-row[row-index=\"'+rowIndex+'\"]');"
                        + "    if(!row)continue;"
                        + "    for(var vi=0;vi<variants.length;vi++){"
                        + "      var cell=row.querySelector('[col-id=\"'+variants[vi]+'\"]');"
                        + "      if(cell)return cell;"
                        + "    }"
                        + "    var cells=row.querySelectorAll('[col-id]');"
                        + "    for(var c=0;c<cells.length;c++){"
                        + "      var cid=normId(cells[c].getAttribute('col-id'));"
                        + "      for(var v=0;v<variants.length;v++){"
                        + "        var alias=normId(variants[v]);"
                        + "        if(cid===alias||cid.indexOf(alias)>=0||alias.indexOf(cid)>=0)return cells[c];"
                        + "      }"
                        + "    }"
                        + "  }"
                        + "  return null;"
                        + "}"
                        + "function collectValues(){"
                        + "  var colId='" + escaped + "';"
                        + "  var target=normId(colId);"
                        + "  var variants=colIdVariants(colId);"
                        + "  var maxRows=" + maxRows + ";"
                        + "  var out=[];"
                        + "  var seen={};"
                        + "  var rows=document.querySelectorAll('.ag-center-cols-container > .ag-row');"
                        + "  for(var i=0;i<rows.length && out.length<maxRows;i++){"
                        + "    var row=rows[i];"
                        + "    if(row.classList.contains('ag-row-level-1')) continue;"
                        + "    var idx=row.getAttribute('row-index');"
                        + "    if(idx===null||seen[idx]) continue;"
                        + "    seen[idx]=true;"
                        + "    var cell=findCellForRow(idx,colId,target,variants);"
                        + "    if(!cell) continue;"
                        + "    var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                        + "    if(v) out.push(v);"
                        + "  }"
                        + "  return out;"
                        + "}"
                        + "scrollBody(0);"
                        + "var best=collectValues();"
                        + "for(var pass=0;pass<10 && best.length<2;pass++){"
                        + "  scrollBody(280);"
                        + "  var next=collectValues();"
                        + "  if(next.length>best.length)best=next;"
                        + "}"
                        + "return best;");
        if (result instanceof List) {
            return (List<String>) result;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<String> readVisibleColumnValuesByColIndex(String colIndex, int maxRows) {
        String escaped = esc(colIndex);
        Object result = runScript(
                "var colIndex='" + escaped + "';"
                        + "var maxRows=" + maxRows + ";"
                        + "var out=[];"
                        + "var rows=document.querySelectorAll('.ag-center-cols-container > .ag-row');"
                        + "for(var i=0;i<rows.length && out.length<maxRows;i++){"
                        + "  var row=rows[i];"
                        + "  if(row.classList.contains('ag-row-level-1'))continue;"
                        + "  var cell=row.querySelector('[aria-colindex=\"'+colIndex+'\"]');"
                        + "  if(!cell)continue;"
                        + "  var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if(v)out.push(v);"
                        + "}"
                        + "return out;");
        if (result instanceof List) {
            return (List<String>) result;
        }
        return Collections.emptyList();
    }

    public void verifySortedAscending(String columnName, String columnId, String valueType, int minValues) {
        scrollColumnHeaderIntoView(columnId, columnName);
        List<String> values = getVisibleColumnValues(columnId, DEFAULT_MAX_ROWS);
        if (values.size() < minValues) {
            Logger.log("Skip sort value verify — only " + values.size() + " values for " + columnName);
            return;
        }
        Verify.softAssert(isMonotonic(values, valueType, true),
                columnName + " column values are sorted ascending (" + values.size() + " rows checked)");
        Verify.softAssert("asc".equals(getSortDirection(columnId, columnName)),
                columnName + " header shows ascending sort indicator");
    }

    public void verifySortedDescending(String columnName, String columnId, String valueType, int minValues) {
        scrollColumnHeaderIntoView(columnId, columnName);
        List<String> values = getVisibleColumnValues(columnId, DEFAULT_MAX_ROWS);
        if (values.size() < minValues) {
            Logger.log("Skip sort value verify — only " + values.size() + " values for " + columnName);
            return;
        }
        Verify.softAssert(isMonotonic(values, valueType, false),
                columnName + " column values are sorted descending (" + values.size() + " rows checked)");
        Verify.softAssert("desc".equals(getSortDirection(columnId, columnName)),
                columnName + " header shows descending sort indicator");
    }

    boolean isMonotonic(List<String> values, String valueType, boolean ascending) {
        if (values.size() < 2) {
            return true;
        }
        Comparator<String> cmp = comparatorFor(valueType);
        for (int i = 1; i < values.size(); i++) {
            int order = cmp.compare(values.get(i - 1), values.get(i));
            if (ascending && order > 0) {
                return false;
            }
            if (!ascending && order < 0) {
                return false;
            }
        }
        return true;
    }

    private Comparator<String> comparatorFor(String valueType) {
        if ("DATE".equalsIgnoreCase(valueType)) {
            return (a, b) -> Long.compare(parseDateMillis(a), parseDateMillis(b));
        }
        if ("NUMBER".equalsIgnoreCase(valueType)) {
            return (a, b) -> Double.compare(parseNumber(a), parseNumber(b));
        }
        return String.CASE_INSENSITIVE_ORDER;
    }

    private long parseDateMillis(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Long.MIN_VALUE;
        }
        String[] patterns = {
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm",
                "MM/dd/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(value.trim()).getTime();
            } catch (ParseException ignored) {
            }
        }
        return value.toLowerCase(Locale.US).hashCode();
    }

    private double parseNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private void resetGridHorizontalScroll() {
        runScript(
                "var v=document.querySelector('.ag-header-viewport,.ag-center-cols-viewport');"
                        + "if(v)v.scrollLeft=0;");
    }

    private static String esc(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static Object runScript(String script) {
        try {
            return BaseTest.driver.get().browser().executeScript(script);
        } catch (Exception e) {
            Logger.logConsoleMessage("GridSort script failed: " + e.getMessage());
            return null;
        }
    }
}
