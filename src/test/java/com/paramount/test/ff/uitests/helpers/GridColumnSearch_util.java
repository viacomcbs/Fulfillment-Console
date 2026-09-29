package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.TableView;
import com.synergy.core.driver.By;
import com.synergy.core.enums.WebKey;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AG Grid per-column header search / filter helpers for Orders tab.
 */
public class GridColumnSearch_util {

    private static final int SAMPLE_ROWS = 10;
    private static final String NO_MATCH_PREFIX = "ZZZ__FF_NO_MATCH__";
    private static final String EXACT_LABEL_MATCH_GUARD_JS =
            "if(cand==='partner'&&text!=='partner')return false;"
                    + "if(cand==='order id'&&text!=='order id')return false;"
                    + "if(cand==='type'&&text!=='type')return false;";
    private static final String HEADER_PROBE_BODY_JS =
            "function hasSearchUi(c){"
                    + "if(!c)return false;"
                    + "var html=(c.innerHTML||'').toLowerCase();"
                    + "if(html.indexOf('search')>=0||html.indexOf('filter')>=0)return true;"
                    + "if(c.querySelector('input,.search-icon,[class*=search],[class*=filter],mat-icon'))return true;"
                    + "var cid=c.getAttribute('col-id');"
                    + "var idx=c.getAttribute('aria-colindex');"
                    + "var root=c.closest('.ag-root-wrapper,.ag-root')||document;"
                    + "if(cid){"
                    + "  var fl=root.querySelector('.ag-floating-filter-body[col-id=\"'+cid+'\"]');"
                    + "  if(fl&&fl.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                    + "}"
                    + "if(idx){"
                    + "  var fl2=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                    + "  if(fl2&&fl2.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                    + "}"
                    + "var sort=c.querySelector('.ag-sort-indicator-container');"
                    + "var icons=c.querySelectorAll('svg,mat-icon,button,[class*=icon],[role=button]');"
                    + "for(var i=0;i<icons.length;i++){"
                    + "  if(sort&&sort.contains(icons[i]))continue;"
                    + "  if(icons[i].classList&&icons[i].classList.contains('ag-header-cell-menu-button'))continue;"
                    + "  return true;"
                    + "}"
                    + "return false;}"
                    + "function probeMenuSearch(c){"
                    + "var menu=c.querySelector('.ag-header-cell-menu-button');"
                    + "if(!menu)return false;"
                    + "try{menu.click();}catch(e){return false;}"
                    + "if(document.querySelector('.ag-popup input,.ag-popup-child input,.cdk-overlay-pane input'))return true;"
                    + "var opts=document.querySelectorAll('.ag-menu-option,.ag-menu .ag-menu-option');"
                    + "for(var j=0;j<opts.length;j++){"
                    + "  var t=(opts[j].innerText||opts[j].textContent||'').toLowerCase();"
                    + "  if(t.indexOf('search')>=0||t.indexOf('filter')>=0)return true;"
                    + "}"
                    + "return false;}"
                    + "function readSampleForCol(colId,headerCell){"
                    + "if(!colId)return '';"
                    + "var idx=headerCell?headerCell.getAttribute('aria-colindex'):null;"
                    + "var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                    + "  '.ag-pinned-right-cols-container'];"
                    + "for(var ci=0;ci<containers.length;ci++){"
                    + "  var rows=document.querySelectorAll(containers[ci]+' > .ag-row');"
                    + "  for(var r=0;r<rows.length&&r<12;r++){"
                    + "    if(rows[r].classList.contains('ag-row-level-1'))continue;"
                    + "    var dataCell=rows[r].querySelector('[col-id=\"'+colId+'\"]');"
                    + "    if(!dataCell&&idx)dataCell=rows[r].querySelector('[aria-colindex=\"'+idx+'\"]');"
                    + "    if(!dataCell)continue;"
                    + "    var v=(dataCell.innerText||dataCell.textContent||'').replace(/\\s+/g,' ').trim();"
                    + "    if(v)return v;"
                    + "  }"
                    + "}"
                    + "return '';}"
                    + "for(var pass=0;pass<12;pass++){"
                    + "  scrollHeaders(pass===0?0:450);"
                    + "  var cells=headerCells();"
                    + "  for(var h=0;h<cells.length;h++){"
                    + "    if(!matches(cells[h]))continue;"
                    + "    var resolvedColId=cells[h].getAttribute('col-id')||colId;"
                    + "    var hasSearch=hasSearchUi(cells[h])||probeMenuSearch(cells[h]);"
                    + "    try{document.body.click();}catch(e){}"
                    + "    return [resolvedColId,hasSearch,readSampleForCol(resolvedColId,cells[h])];"
                    + "  }"
                    + "}"
                    + "return [colId,false,''];";

    private final GridSort_util gridSortUtil = new GridSort_util();
    private final TableView_utill tableViewUtil = new TableView_utill();
    private final TableView tableView = new TableView();

    public String resolveColumnId(String columnName, String section) {
        return gridSortUtil.resolveColumnId(columnName, section);
    }

    public boolean isHeaderVisible(String columnId, String columnName) {
        try {
            FulfillmentJsUtil.useFastElementTimeout();
            if ("Title, Season, Episode".equalsIgnoreCase(columnName)
                    && tableViewUtil.isTitleSeasonEpisodeGridVisible()) {
                return true;
            }
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            if ("Partner".equalsIgnoreCase(columnName)) {
                return tableViewUtil.isExactOrderGridHeaderVisible(columnName)
                        || tableViewUtil.isGridColumnHeaderVisibleForSearch(columnName, "order");
            }
            return tableViewUtil.isGridHeaderVisibleForPrep(columnName, "order")
                    || tableViewUtil.isGridColumnHeaderVisibleForSearch(columnName, "order");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId);
        }
    }

    public boolean hasColumnSearchControl(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (isTc022DateSearchColumn(columnId, columnName)) {
            return hasTc022DateFilterStructure(columnId, columnName);
        }
        if (FulfillmentJsUtil.hasGridColumnSearchControl(columnName, columnId)) {
            return true;
        }
        if (detectColumnFilterControl(columnName)) {
            return true;
        }
        hoverColumnHeaderForFilterUi(columnName, columnId);
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        if (WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByLabel(columnName), 1)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(effectiveId), 1)) {
            return true;
        }
        if (hasCustomColumnFilterControl(effectiveId, columnName)) {
            return true;
        }
        if (hasColumnSearchControlViaSynergyProbe(columnName)) {
            return true;
        }
        DomColumnRef domRef = resolveDomColumnRef(columnName);
        String resolvedId = domRef != null && domRef.hasColId() ? domRef.colId : columnId;
        if (hasColumnSearchViaBulkScript(resolvedId, columnName)) {
            return true;
        }
        return truthy(runScript(buildHasSearchScript(resolvedId, columnName)));
    }

    /** Lineitem/package nested grid only — avoids order-grid false positives for labels like "Type". */
    public boolean hasLineItemColumnSearchControl(String columnId, String columnName) {
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        return truthy(runScript(buildLineItemSearchControlScript(effectiveId, columnName)));
    }

    private String buildLineItemSearchControlScript(String columnId, String columnName) {
        String columnIdEsc = esc(columnId == null ? "" : columnId);
        String columnNameEsc = esc(columnName == null ? "" : columnName);
        return HEADER_PROBE_BODY_JS
                + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "  return text===cand;"
                + "}"
                + "var columnId=normId('" + columnIdEsc + "');"
                + "var labels=[norm('" + columnNameEsc + "')];"
                + "var roots=[document];"
                + "document.querySelectorAll("
                + "'.package-group,.package-details,[class*=inner-package],.ag-details-row,.ag-full-width-container'"
                + ").forEach(function(r){roots.push(r);});"
                + "for(var r=0;r<roots.length;r++){"
                + "  var cells=roots[r].querySelectorAll('.ag-header-cell,[role=columnheader]');"
                + "  for(var i=0;i<cells.length;i++){"
                + "    var cid=normId(cells[i].getAttribute('col-id'));"
                + "    if(columnId&&cid){"
                + "      if(columnId==='type'&&cid!=='type')continue;"
                + "      if(cid===columnId||cid.indexOf(columnId)>=0||columnId.indexOf(cid)>=0){"
                + "        if(hasSearchUi(cells[i]))return true;"
                + "        continue;"
                + "      }"
                + "    }"
                + "    var text=norm(cells[i].innerText||cells[i].textContent);"
                + "    for(var c=0;c<labels.length;c++){"
                + "      if(!labelMatches(text,labels[c]))continue;"
                + "      if(hasSearchUi(cells[i]))return true;"
                + "    }"
                + "  }"
                + "}"
                + "return false;";
    }

    /** Durable MSC filter in header — excludes transient popup/probe false positives during prep. */
    public boolean hasPersistentColumnSearchControl(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (isTc022DateSearchColumn(columnId, columnName)) {
            return hasTc022DateFilterStructure(columnId, columnName);
        }
        if (FulfillmentJsUtil.hasGridColumnSearchControl(columnName, columnId)) {
            return true;
        }
        hoverColumnHeaderForFilterUi(columnName, columnId);
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        if (WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByLabel(columnName), 1)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(effectiveId), 1)) {
            return true;
        }
        return WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByColId(effectiveId), 1);
    }

    /** Scriptless TC045: msc-custom-table-column-filter + #orderTable{columnId} search popup. */
    private boolean hasCustomColumnFilterControl(String columnId, String columnName) {
        try {
            if (columnName != null && WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByLabel(columnName), 1)) {
                return true;
            }
            if (WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByColId(columnId), 1)) {
                return true;
            }
            if (columnName != null && WaitUtil.isDisplay(tableView.customColumnFilterTriggerByLabel(columnName), 1)) {
                return true;
            }
            if (openCustomColumnFilterPopup(columnId, columnName)) {
                Thread.sleep(300);
                boolean hasInput = WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(columnId), 1);
                dismissCustomFilterPopup();
                return hasInput;
            }
        } catch (RuntimeException e) {
            Logger.log("Custom column filter probe skipped: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private boolean openCustomColumnFilterPopup(String columnId, String columnName) throws InterruptedException {
        if (isOffsetDeliveryColumn(columnId, columnName)) {
            return clickTc022FilterTrigger(columnId, columnName, true);
        }
        if (isDeliveryDateColumn(columnId, columnName)) {
            return clickTc022FilterTrigger(columnId, columnName, false);
        }
        By trigger = resolveTc022FilterTrigger(columnId, columnName);
        if (!WaitUtil.isDisplay(trigger, 1)) {
            return false;
        }
        return DriverUtil.clickOnElementSafely(trigger, 5);
    }

    private By resolveTc022FilterTrigger(String columnId, String columnName) {
        if (isDeliveryDateColumn(columnId, columnName)) {
            By calendar = tableView.customColumnFilterCalendarTriggerByLabel(columnName);
            if (WaitUtil.isDisplay(calendar, 1)) {
                return calendar;
            }
            return tableView.customColumnFilterCalendarTriggerByColId(columnId);
        }
        if (isOffsetDeliveryColumn(columnId, columnName)) {
            By search = tableView.customColumnFilterSearchTriggerByLabel(columnName);
            if (WaitUtil.isDisplay(search, 1)) {
                return search;
            }
            return tableView.customColumnFilterTriggerByColId(columnId);
        }
        By trigger = tableView.customColumnFilterTriggerByLabel(columnName);
        if (!WaitUtil.isDisplay(trigger, 1) && columnName != null) {
            trigger = tableView.customColumnFilterTriggerByColId(columnId);
        }
        return trigger;
    }

    private boolean isTc022DateSearchColumn(String columnId, String columnName) {
        return isDeliveryDateColumn(columnId, columnName) || isOffsetDeliveryColumn(columnId, columnName);
    }

    private boolean isOffsetDeliveryColumn(String columnId, String columnName) {
        return "deliveryOffset".equalsIgnoreCase(columnId)
                || (columnName != null && columnName.toLowerCase(Locale.US).contains("offset delivery"));
    }

    private boolean isDeliveryDateColumn(String columnId, String columnName) {
        return "deliveryDate".equalsIgnoreCase(columnId)
                || (columnName != null && "delivery date".equalsIgnoreCase(columnName.trim()));
    }

    private boolean hasTc022DateFilterStructure(String columnId, String columnName) {
        prepareTc022DateFilterHeader(columnId, columnName);
        ensureTc022ColumnScrolledIntoView(columnId, columnName);
        if (hasTc022FilterViaHorizontalAlignment(columnName)) {
            return true;
        }
        if (hasTc022FilterViaSynergyXPath(columnName)) {
            return true;
        }
        if (hasTc022FilterViaLabelElement(columnName)) {
            return true;
        }
        if (isOffsetDeliveryColumn(columnId, columnName)) {
            return truthy(runScript(buildTc022FilterPresenceScript(columnId, columnName, true)));
        }
        if (isDeliveryDateColumn(columnId, columnName)) {
            return truthy(runScript(buildTc022FilterPresenceScript(columnId, columnName, false)));
        }
        return false;
    }

    private void prepareTc022DateFilterHeader(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        hoverColumnHeaderForFilterUi(columnName, columnId);
    }

    private void ensureTc022ColumnScrolledIntoView(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        DriverUtil.scrollToElement(tableView.gridHeaderLabel(columnName));
    }

    private boolean verifyTc022DateFilterControl(String columnId, String columnName) throws InterruptedException {
        prepareTc022DateFilterHeader(columnId, columnName);
        Thread.sleep(200);
        ensureTc022ColumnScrolledIntoView(columnId, columnName);
        if (hasTc022FilterViaHorizontalAlignment(columnName)
                || hasTc022FilterViaSynergyXPath(columnName)
                || hasTc022FilterViaLabelElement(columnName)) {
            if (isOffsetDeliveryColumn(columnId, columnName)) {
                return verifyOffsetDeliveryDateSearchFilter(columnId, columnName);
            }
            if (isDeliveryDateColumn(columnId, columnName)) {
                return verifyDeliveryDateCalendarFilter(columnId, columnName);
            }
        }
        if (isOffsetDeliveryColumn(columnId, columnName)) {
            if (verifyOffsetDeliveryDateSearchFilter(columnId, columnName)) {
                return true;
            }
        } else if (isDeliveryDateColumn(columnId, columnName)) {
            if (verifyDeliveryDateCalendarFilter(columnId, columnName)) {
                return true;
            }
        }
        if (hasTc022DateFilterStructure(columnId, columnName)) {
            if (isOffsetDeliveryColumn(columnId, columnName)) {
                return verifyOffsetDeliveryDateSearchFilter(columnId, columnName);
            }
            if (isDeliveryDateColumn(columnId, columnName)) {
                return verifyDeliveryDateCalendarFilter(columnId, columnName);
            }
        }
        if (detectColumnFilterControl(columnName)) {
            Logger.log(columnName + " filter control detected via cross-row header probe");
            return verifyFilterOpensAfterClick(columnId, columnName);
        }
        if (hasOrderTableFilterInputInDom(columnId)) {
            Logger.log(columnName + " filter input present in DOM (#" + TableView.orderTableSearchInputId(columnId) + ")");
            return verifyFilterOpensAfterClick(columnId, columnName);
        }
        Logger.logReportMessage("TC022 filter probe failed for [" + columnName + "]: "
                + diagnoseTc022FilterState(columnId, columnName));
        return false;
    }

    private boolean verifyFilterOpensAfterClick(String columnId, String columnName) throws InterruptedException {
        if (isOffsetDeliveryColumn(columnId, columnName)) {
            return verifyOffsetDeliveryDateSearchFilter(columnId, columnName);
        }
        if (isDeliveryDateColumn(columnId, columnName)) {
            return verifyDeliveryDateCalendarFilter(columnId, columnName);
        }
        return false;
    }

    private boolean hasOrderTableFilterInputInDom(String columnId) {
        if (columnId == null || columnId.trim().isEmpty()) {
            return false;
        }
        return truthy(runScript(
                "var el=document.getElementById('" + esc(TableView.orderTableSearchInputId(columnId)) + "');"
                        + "return !!el;"));
    }

    private String diagnoseTc022FilterState(String columnId, String columnName) {
        String inputId = esc(TableView.orderTableSearchInputId(columnId));
        Object result = runScript(tc022FilterCellFinderJs(columnId, columnName)
                + "var label=findLabelCell();"
                + "var filter=findFilterComponent();"
                + "var filters=document.querySelectorAll('msc-custom-table-column-filter,"
                + "msc-custom-table-column-filter-themed').length;"
                + "var idx=label?label.getAttribute('aria-colindex'):'';"
                + "var cid=label?label.getAttribute('col-id'):'';"
                + "var hasInput=!!document.getElementById('" + inputId + "');"
                + "return 'label='+(!!label)+',filter='+(!!filter)+',filterCount='+filters"
                + "+',ariaColindex='+(idx||'')+',colId='+(cid||'')+',input='+hasInput;");
        return (result == null ? "no-script-result" : String.valueOf(result))
                + ",labelSpan=" + WaitUtil.isDisplay(tableView.gridHeaderLabel(columnName), 1)
                + ",alignFilter=" + hasTc022FilterViaHorizontalAlignment(columnName)
                + ",synergyFilter=" + hasTc022FilterViaSynergyXPath(columnName)
                + ",labelElementFilter=" + hasTc022FilterViaLabelElement(columnName);
    }

    private boolean hasTc022FilterViaSynergyXPath(String columnName) {
        if (WaitUtil.isDisplay(tableView.customColumnFilterInFilterRowByLabel(columnName), 1)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.customColumnFilterSearchTriggerInFilterRowByLabel(columnName), 1)) {
            return true;
        }
        return WaitUtil.isDisplay(tableView.customColumnFilterCalendarTriggerInFilterRowByLabel(columnName), 1);
    }

    private String tc022ResolveHeaderCellJs() {
        return "function resolveHeaderCell(el){"
                + "if(!el)return null;"
                + "var cell=el.closest('.ag-header-cell,[role=columnheader],.ag-header-group-cell');"
                + "if(cell)return cell;"
                + "var row=el.closest('.ag-header-row');"
                + "if(row){"
                + "  var cells=row.querySelectorAll('.ag-header-cell,[role=columnheader],.ag-header-group-cell');"
                + "  for(var i=0;i<cells.length;i++){if(cells[i].contains(el))return cells[i];}"
                + "}"
                + "return null;"
                + "}";
    }

    private String tc022HeaderCellSelector() {
        return ".ag-header-cell,[role=columnheader],.ag-header-group-cell";
    }

    private boolean hasTc022FilterViaHorizontalAlignment(String columnName) {
        try {
            By labelSpan = tableView.gridHeaderLabel(columnName);
            if (!WaitUtil.isDisplay(labelSpan, 3)) {
                return false;
            }
            Object labelCenter = BaseTest.driver.get().finder().findElement(labelSpan).executeScript(
                    "var r=arguments[0].getBoundingClientRect();"
                            + "if(!r||r.width<1)return null;"
                            + "return (r.left+r.right)/2;");
            if (labelCenter == null) {
                return false;
            }
            Object result = runScript(
                    "var center=" + labelCenter + ";"
                            + "var filters=document.querySelectorAll('msc-custom-table-column-filter,"
                            + "msc-custom-table-column-filter-themed');"
                            + "for(var i=0;i<filters.length;i++){"
                            + "  var r=filters[i].getBoundingClientRect();"
                            + "  if(r.width<1)continue;"
                            + "  if(Math.abs((r.left+r.right)/2-center)<400)return true;"
                            + "}"
                            + "return false;");
            return truthy(result);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean clickTc022FilterViaHorizontalAlignment(String columnName, boolean offsetSearch) {
        try {
            By labelSpan = tableView.gridHeaderLabel(columnName);
            if (!WaitUtil.isDisplay(labelSpan, 3)) {
                return false;
            }
            Object labelCenter = BaseTest.driver.get().finder().findElement(labelSpan).executeScript(
                    "var r=arguments[0].getBoundingClientRect();"
                            + "if(!r||r.width<1)return null;"
                            + "return (r.left+r.right)/2;");
            if (labelCenter == null) {
                return false;
            }
            String triggerPick = offsetSearch
                    ? "var t=best.querySelector('div>div>div:nth-of-type(1)>div>span')"
                    + "||best.querySelector('div>div>div:nth-of-type(1)>div');"
                    : "var t=best.querySelector('div>div>div:nth-of-type(1)>div:nth-of-type(2)>i')"
                    + "||best.querySelector('i.fa-calendar,mat-icon,[class*=calendar]');";
            Object result = runScript(
                    "var center=" + labelCenter + ";"
                            + "var filters=document.querySelectorAll('msc-custom-table-column-filter,"
                            + "msc-custom-table-column-filter-themed');"
                            + "var best=null,bestDist=1e9;"
                            + "for(var i=0;i<filters.length;i++){"
                            + "  var r=filters[i].getBoundingClientRect();"
                            + "  if(r.width<1)continue;"
                            + "  var dist=Math.abs((r.left+r.right)/2-center);"
                            + "  if(dist<bestDist){bestDist=dist;best=filters[i];}"
                            + "}"
                            + "if(!best||bestDist>=400)return false;"
                            + triggerPick
                            + "if(t){t.click();return true;}best.click();return true;");
            return truthy(result);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasTc022FilterViaLabelElement(String columnName) {
        try {
            By labelSpan = tableView.gridHeaderLabel(columnName);
            if (!WaitUtil.isDisplay(labelSpan, 3)) {
                return false;
            }
            String script = tc022ResolveHeaderCellJs()
                    + buildFindFilterFromLabelElementScript()
                    + "var span=arguments[0];"
                    + "var label=resolveHeaderCell(span);"
                    + "return !!findFilterFromLabel(label);";
            return truthy(BaseTest.driver.get().finder().findElement(labelSpan).executeScript(script));
        } catch (Exception ignored) {
            return false;
        }
    }

    private String buildFindFilterFromLabelElementScript() {
        String headerCell = tc022HeaderCellSelector();
        return "function cellHasFilter(cell){"
                + "return !!(cell&&cell.querySelector('msc-custom-table-column-filter,"
                + "msc-custom-table-column-filter-themed,input,[class*=calendar],[class*=search]'));"
                + "}"
                + "function findFilterFromLabel(label){"
                + "if(!label)return null;"
                + "var root=label.closest('.ag-root-wrapper,.ag-root')||document;"
                + "var idx=label.getAttribute('aria-colindex');"
                + "if(idx){"
                + "  var cells=root.querySelectorAll('" + headerCell + "[aria-colindex=\"'+idx+'\"]');"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(cells[i]!==label&&cellHasFilter(cells[i])){"
                + "      return cells[i].querySelector('msc-custom-table-column-filter,"
                + "msc-custom-table-column-filter-themed')||cells[i];"
                + "    }"
                + "  }"
                + "}"
                + "var labelRow=label.closest('.ag-header-row');"
                + "if(labelRow){"
                + "  var labelCells=labelRow.querySelectorAll('" + headerCell + "');"
                + "  var pos=-1;"
                + "  for(var p=0;p<labelCells.length;p++){if(labelCells[p]===label){pos=p;break;}}"
                + "  if(pos>=0){"
                + "    var rows=root.querySelectorAll('.ag-header-row');"
                + "    for(var r=0;r<rows.length;r++){"
                + "      if(rows[r]===labelRow)continue;"
                + "      var fcells=rows[r].querySelectorAll('" + headerCell + "');"
                + "      if(pos<fcells.length&&cellHasFilter(fcells[pos])){"
                + "        return fcells[pos].querySelector('msc-custom-table-column-filter,"
                + "msc-custom-table-column-filter-themed')||fcells[pos];"
                + "      }"
                + "    }"
                + "  }"
                + "}"
                + "var lr=label.getBoundingClientRect();"
                + "var center=(lr.left+lr.right)/2;"
                + "var all=root.querySelectorAll('msc-custom-table-column-filter,msc-custom-table-column-filter-themed');"
                + "var best=null,bestDist=1e9;"
                + "for(var f=0;f<all.length;f++){"
                + "  var fr=all[f].getBoundingClientRect();"
                + "  if(fr.width<1)continue;"
                + "  var dist=Math.abs((fr.left+fr.right)/2-center);"
                + "  if(dist<bestDist){bestDist=dist;best=all[f];}"
                + "}"
                + "return bestDist<400?best:null;"
                + "}";
    }

    private boolean clickTc022FilterViaLabelElement(String columnName, boolean offsetSearch) {
        try {
            By labelSpan = tableView.gridHeaderLabel(columnName);
            if (!WaitUtil.isDisplay(labelSpan, 3)) {
                return false;
            }
            String triggerPick = offsetSearch
                    ? "var t=filter.querySelector('div>div>div:nth-of-type(1)>div>span')"
                    + "||filter.querySelector('div>div>div:nth-of-type(1)>div');"
                    : "var t=filter.querySelector('div>div>div:nth-of-type(1)>div:nth-of-type(2)>i')"
                    + "||filter.querySelector('i.fa-calendar,mat-icon,[class*=calendar]');";
            String script = tc022ResolveHeaderCellJs()
                    + buildFindFilterFromLabelElementScript()
                    + "var span=arguments[0];"
                    + "var label=resolveHeaderCell(span);"
                    + "var filter=findFilterFromLabel(label);"
                    + "if(!filter)return false;"
                    + triggerPick
                    + "if(t){t.click();return true;}filter.click();return true;";
            Object result = BaseTest.driver.get().finder().findElement(labelSpan).executeScript(script);
            return truthy(result);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean verifyOffsetDeliveryDateSearchFilter(String columnId, String columnName)
            throws InterruptedException {
        ensureTc022ColumnScrolledIntoView(columnId, columnName);
        if (!clickTc022FilterTrigger(columnId, columnName, true)) {
            return false;
        }
        Thread.sleep(400);
        String inputId = TableView.orderTableSearchInputId(columnId);
        boolean hasInput = truthy(runScript(
                "var el=document.getElementById('" + esc(inputId) + "');"
                        + "return !!(el&&(el.offsetParent!==null||el.getBoundingClientRect().width>0));"))
                || WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(columnId), 3);
        dismissCustomFilterPopup();
        return hasInput;
    }

    private boolean verifyDeliveryDateCalendarFilter(String columnId, String columnName)
            throws InterruptedException {
        ensureTc022ColumnScrolledIntoView(columnId, columnName);
        if (!clickTc022FilterTrigger(columnId, columnName, false)) {
            return false;
        }
        Thread.sleep(400);
        boolean hasCalendar = truthy(runScript(
                "var dd=document.getElementById('calendar-dropdown');"
                        + "return !!(dd&&(dd.offsetParent!==null||dd.querySelector('div')));"))
                || WaitUtil.isDisplay(tableView.calendarDropdown(), 3)
                || WaitUtil.isDisplay(By.XPath("//*[contains(@class,'mat-calendar')]"), 2);
        dismissCustomFilterPopup();
        return hasCalendar;
    }

    private boolean clickTc022FilterTrigger(String columnId, String columnName, boolean offsetSearch) {
        ensureTc022ColumnScrolledIntoView(columnId, columnName);
        if (clickTc022FilterViaHorizontalAlignment(columnName, offsetSearch)) {
            return true;
        }
        By synergyTrigger = offsetSearch
                ? tableView.customColumnFilterSearchTriggerInFilterRowByLabel(columnName)
                : tableView.customColumnFilterCalendarTriggerInFilterRowByLabel(columnName);
        if (DriverUtil.clickOnElementSafely(synergyTrigger, 3)) {
            return true;
        }
        if (DriverUtil.clickOnElementSafely(tableView.customColumnFilterInFilterRowByLabel(columnName), 2)) {
            return true;
        }
        if (clickTc022FilterViaLabelElement(columnName, offsetSearch)) {
            return true;
        }
        if (truthy(runScript(buildTc022ClickFilterScript(columnId, columnName, offsetSearch)))) {
            return true;
        }
        By trigger = offsetSearch
                ? tableView.customColumnFilterSearchTriggerByLabel(columnName)
                : tableView.customColumnFilterCalendarTriggerByLabel(columnName);
        return DriverUtil.clickOnElementSafely(trigger, 2);
    }

    private String tc022FilterCellFinderJs(String columnId, String columnName) {
        String escapedName = esc(columnName == null ? "" : columnName);
        String inputId = esc(TableView.orderTableSearchInputId(columnId));
        String headerCell = tc022HeaderCellSelector();
        return tc022ResolveHeaderCellJs()
                + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "var colId='" + esc(columnId) + "';"
                + "var needle=norm('" + escapedName + "');"
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
                + "function headerCells(root){"
                + "  return root.querySelectorAll('.ag-header-cell,.ag-pinned-left-header .ag-header-cell,"
                + ".ag-pinned-right-header .ag-header-cell,[role=columnheader]');"
                + "}"
                + "function scrollHeaders(root,delta){"
                + "  root.querySelectorAll('.ag-header-viewport,.ag-center-cols-viewport,"
                + ".ag-body-horizontal-scroll-viewport,.ag-body-viewport')"
                + "    .forEach(function(v){"
                + "      if(delta===0)v.scrollLeft=0;"
                + "      else v.scrollLeft=Math.max(0,(v.scrollLeft||0)+delta);"
                + "    });"
                + "}"
                + "function headerText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent)"
                + "    :(cell.innerText||cell.textContent||cell.getAttribute('aria-label')||''));"
                + "}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function findLabelCell(){"
                + "  var globalSpans=document.querySelectorAll('.label-ellipsis span');"
                + "  for(var g=0;g<globalSpans.length;g++){"
                + "    var gt=norm(globalSpans[g].innerText||globalSpans[g].textContent);"
                + "    if(gt&&labelMatches(gt,needle)){"
                + "      var gcell=resolveHeaderCell(globalSpans[g]);"
                + "      if(gcell){"
                + "        try{gcell.scrollIntoView({inline:'center',block:'nearest'});}catch(e){}"
                + "        return gcell;"
                + "      }"
                + "    }"
                + "  }"
                + "  var root=ordersGridRoot();"
                + "  for(var pass=0;pass<14;pass++){"
                + "    scrollHeaders(root,pass===0?0:400);"
                + "    var cells=headerCells(root);"
                + "    for(var i=0;i<cells.length;i++){"
                + "      var text=headerText(cells[i]);"
                + "      if(text&&labelMatches(text,needle)){"
                + "        try{cells[i].scrollIntoView({inline:'center',block:'nearest'});}catch(e){}"
                + "        return cells[i];"
                + "      }"
                + "    }"
                + "    if(colId){"
                + "      for(var j=0;j<cells.length;j++){"
                + "        var cid=(cells[j].getAttribute('col-id')||'').toLowerCase();"
                + "        if(cid&&cid===colId.toLowerCase())return cells[j];"
                + "      }"
                + "    }"
                + "  }"
                + "  scrollHeaders(root,0);"
                + "  var all=document.querySelectorAll('.label-ellipsis span');"
                + "  for(var k=0;k<all.length;k++){"
                + "    var t=norm(all[k].innerText||all[k].textContent);"
                + "    if(t&&labelMatches(t,needle)){"
                + "      var cell=resolveHeaderCell(all[k]);"
                + "      if(cell)return cell;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function cellHasFilter(cell){"
                + "  return !!(cell&&cell.querySelector('msc-custom-table-column-filter,"
                + "msc-custom-table-column-filter-themed,input,[class*=calendar],[class*=search]'));"
                + "}"
                + "function findFilterCellByRowPosition(){"
                + "  var label=findLabelCell();"
                + "  if(!label)return null;"
                + "  var labelRow=label.closest('.ag-header-row');"
                + "  if(!labelRow)return null;"
                + "  var labelCells=labelRow.querySelectorAll('" + headerCell + "');"
                + "  var pos=-1;"
                + "  for(var i=0;i<labelCells.length;i++){if(labelCells[i]===label){pos=i;break;}}"
                + "  if(pos<0)return null;"
                + "  var root=label.closest('.ag-root-wrapper,.ag-root')||ordersGridRoot();"
                + "  var rows=root.querySelectorAll('.ag-header-row');"
                + "  for(var r=0;r<rows.length;r++){"
                + "    if(rows[r]===labelRow)continue;"
                + "    var fcells=rows[r].querySelectorAll('" + headerCell + "');"
                + "    if(pos<fcells.length&&cellHasFilter(fcells[pos]))return fcells[pos];"
                + "  }"
                + "  return null;"
                + "}"
                + "function findFilterCell(){"
                + "  var label=findLabelCell();"
                + "  if(!label)return null;"
                + "  var root=label.closest('.ag-root-wrapper,.ag-root')||ordersGridRoot();"
                + "  var cid=label.getAttribute('col-id')||colId;"
                + "  var idx=label.getAttribute('aria-colindex');"
                + "  if(cid){"
                + "    var cells=root.querySelectorAll('" + headerCell + "[col-id=\"'+cid+'\"]');"
                + "    for(var j=0;j<cells.length;j++){"
                + "      if(cells[j]!==label&&cellHasFilter(cells[j]))return cells[j];"
                + "    }"
                + "  }"
                + "  if(idx){"
                + "    var cells2=root.querySelectorAll('" + headerCell + "[aria-colindex=\"'+idx+'\"]');"
                + "    for(var k=0;k<cells2.length;k++){"
                + "      if(cells2[k]!==label&&cellHasFilter(cells2[k]))return cells2[k];"
                + "    }"
                + "  }"
                + "  var byPos=findFilterCellByRowPosition();"
                + "  if(byPos)return byPos;"
                + "  if(cellHasFilter(label))return label;"
                + "  return null;"
                + "}"
                + "function findFilterByLabelAlignment(){"
                + "  var label=findLabelCell();"
                + "  if(!label)return null;"
                + "  var lr=label.getBoundingClientRect();"
                + "  var center=(lr.left+lr.right)/2;"
                + "  var root=label.closest('.ag-root-wrapper,.ag-root')||ordersGridRoot();"
                + "  var all=root.querySelectorAll('msc-custom-table-column-filter,msc-custom-table-column-filter-themed');"
                + "  var best=null,bestDist=1e9;"
                + "  for(var i=0;i<all.length;i++){"
                + "    var r=all[i].getBoundingClientRect();"
                + "    if(r.width<1||r.height<1)continue;"
                + "    var dist=Math.abs((r.left+r.right)/2-center);"
                + "    if(dist<bestDist){bestDist=dist;best=all[i];}"
                + "  }"
                + "  return bestDist<400?best:null;"
                + "}"
                + "function findFilterByInputId(){"
                + "  if(!colId)return null;"
                + "  var el=document.getElementById('" + inputId + "');"
                + "  return el||null;"
                + "}"
                + "function findFilterComponent(){"
                + "  var cell=findFilterCell();"
                + "  if(cell){"
                + "    var filter=cell.querySelector('msc-custom-table-column-filter,msc-custom-table-column-filter-themed');"
                + "    if(filter)return filter;"
                + "    if(cell.querySelector('input,[class*=calendar],[class*=search]'))return cell;"
                + "  }"
                + "  var aligned=findFilterByLabelAlignment();"
                + "  if(aligned)return aligned;"
                + "  return findFilterByInputId();"
                + "}";
    }

    private String buildTc022ClickFilterScript(String columnId, String columnName, boolean offsetSearch) {
        String triggerSelector = offsetSearch
                ? "filter.querySelector('div>div>div:nth-of-type(1)>div>span')"
                + "||filter.querySelector('div>div>div:nth-of-type(1)>div')"
                : "filter.querySelector('div>div>div:nth-of-type(1)>div:nth-of-type(2)>i')"
                + "||filter.querySelector('i.fa-calendar,mat-icon,[class*=calendar]')";
        return tc022FilterCellFinderJs(columnId, columnName)
                + "var label=findLabelCell();"
                + "if(!label)return false;"
                + "label.scrollIntoView({block:'nearest',inline:'center'});"
                + "label.dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                + "label.dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));"
                + "var filter=findFilterComponent();"
                + "if(!filter)return false;"
                + "var trigger=" + triggerSelector + ";"
                + "if(!trigger){filter.click();return true;}"
                + "trigger.click();return true;";
    }

    private String buildTc022FilterPresenceScript(String columnId, String columnName, boolean offsetSearch) {
        return tc022FilterCellFinderJs(columnId, columnName)
                + "var label=findLabelCell();"
                + "if(!label)return false;"
                + "label.dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                + "return !!findFilterComponent();";
    }

    private void dismissCustomFilterPopup() {
        try {
            BaseTest.driver.get().browser().executeScript(
                    "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));"
                            + "try{document.body.click();}catch(e){}");
        } catch (Exception ignored) {
        }
    }

    private boolean applyColumnSearchViaCustomFilter(String columnId, String columnName, String searchTerm)
            throws InterruptedException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return false;
        }
        if (!openCustomColumnFilterPopup(columnId, columnName)) {
            return false;
        }
        Thread.sleep(500);
        if (typeIntoColumnSearchInput(tableView.orderTableColumnSearchInput(columnId), searchTerm)) {
            Logger.logReportMessage("Applied column search via MSC custom filter for [" + columnName + "] id=#"
                    + TableView.orderTableSearchInputId(columnId));
            return true;
        }
        dismissCustomFilterPopup();
        return false;
    }

    private void hoverColumnHeaderForFilterUi(String columnName, String columnId) {
        runScript("function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "var needle=norm('" + esc(columnName) + "');"
                + "var cells=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                + "for(var i=0;i<cells.length;i++){"
                + "  var ellipsis=cells[i].querySelector('.label-ellipsis span');"
                + "  var label=cells[i].querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  var text=norm(ellipsis?(ellipsis.innerText||ellipsis.textContent):"
                + "(label?(label.innerText||label.textContent):(cells[i].innerText||cells[i].textContent)));"
                + "  if(needle==='partner'&&text!=='partner')continue;"
                + "  if(needle==='type'&&text!=='type')continue;"
                + "  if(needle==='order id'&&text!=='order id')continue;"
                + "  if(text!==needle&&text.indexOf(needle)<0&&needle.indexOf(text)<0)continue;"
                + "  cells[i].dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                + "  cells[i].dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));"
                + "  break;"
                + "}");
    }

    private boolean hasColumnSearchControlViaSynergyProbe(String columnName) {
        try {
            for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
                if (clickColumnSearchTrigger(label)) {
                    Thread.sleep(500);
                    By popupInput = By.XPath("//div[contains(@class,'ag-popup-child')]//input"
                            + " | //div[contains(@class,'ag-filter')]//input"
                            + " | //div[contains(@class,'cdk-overlay-pane')]//input");
                    if (WaitUtil.isDisplay(popupInput, 2)) {
                        BaseTest.driver.get().browser().executeScript(
                                "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));"
                                        + "try{document.body.click();}catch(e){}");
                        return true;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /** Single JS pass: scroll headers, match label, detect AG Grid filter/search icon. */
    private boolean detectColumnFilterControl(String columnName) {
        gridSortUtil.scrollColumnHeaderIntoView("", columnName);
        Object result = runScript(buildDetectFilterControlScript(columnName));
        return truthy(result);
    }

    private String buildDetectFilterControlScript(String columnName) {
        String labels = labelsArray(columnName);
        String headerCell = tc022HeaderCellSelector();
        return tc022ResolveHeaderCellJs()
                + "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function headerLabelText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                + "}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function cellHasFilterUi(cell){"
                + "  if(!cell)return false;"
                + "  if(cell.querySelector('msc-custom-table-column-filter,msc-custom-table-column-filter-themed'))return true;"
                + "  if(cell.querySelector('.ag-icon-filter,[data-ref=eFilterButton],.filter-icon,.search-icon,[class*=filter],input'))return true;"
                + "  var sort=cell.querySelector('.ag-sort-indicator-container,.ag-sort-indicator-icon');"
                + "  var menu=cell.querySelector('.ag-header-cell-menu-button');"
                + "  var svgs=cell.querySelectorAll('svg');"
                + "  for(var s=0;s<svgs.length;s++){"
                + "    if(sort&&sort.contains(svgs[s]))continue;"
                + "    if(menu&&menu.contains(svgs[s]))continue;"
                + "    return true;"
                + "  }"
                + "  var icons=cell.querySelectorAll('span[class*=icon],button,i,[role=button]');"
                + "  for(var k=0;k<icons.length;k++){"
                + "    var el=icons[k];"
                + "    if(sort&&sort.contains(el))continue;"
                + "    if(menu&&menu.contains(el))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-text'))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-label'))continue;"
                + "    return true;"
                + "  }"
                + "  var cid=cell.getAttribute('col-id');"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  var root=cell.closest('.ag-root-wrapper,.ag-root')||ordersGridRoot();"
                + "  if(cid){"
                + "    var fl=root.querySelector('.ag-floating-filter-body[col-id=\"'+cid+'\"]');"
                + "    if(fl&&fl.querySelector('input,[class*=filter]'))return true;"
                + "  }"
                + "  if(idx){"
                + "    var fl2=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                + "    if(fl2&&fl2.querySelector('input,[class*=filter]'))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function columnHasFilterUi(labelCell){"
                + "  if(cellHasFilterUi(labelCell))return true;"
                + "  var root=labelCell.closest('.ag-root-wrapper,.ag-root')||ordersGridRoot();"
                + "  var idx=labelCell.getAttribute('aria-colindex');"
                + "  if(idx){"
                + "    var siblings=root.querySelectorAll('" + headerCell + "[aria-colindex=\"'+idx+'\"]');"
                + "    for(var i=0;i<siblings.length;i++){"
                + "      if(siblings[i]!==labelCell&&cellHasFilterUi(siblings[i]))return true;"
                + "    }"
                + "  }"
                + "  var labelRow=labelCell.closest('.ag-header-row');"
                + "  if(labelRow){"
                + "    var labelCells=labelRow.querySelectorAll('" + headerCell + "');"
                + "    var pos=-1;"
                + "    for(var p=0;p<labelCells.length;p++){if(labelCells[p]===labelCell){pos=p;break;}}"
                + "    if(pos>=0){"
                + "      var rows=root.querySelectorAll('.ag-header-row');"
                + "      for(var r=0;r<rows.length;r++){"
                + "        if(rows[r]===labelRow)continue;"
                + "        var fcells=rows[r].querySelectorAll('" + headerCell + "');"
                + "        if(pos<fcells.length&&cellHasFilterUi(fcells[pos]))return true;"
                + "      }"
                + "    }"
                + "  }"
                + "  var lr=labelCell.getBoundingClientRect();"
                + "  var center=(lr.left+lr.right)/2;"
                + "  var filters=root.querySelectorAll('msc-custom-table-column-filter,msc-custom-table-column-filter-themed');"
                + "  for(var f=0;f<filters.length;f++){"
                + "    var fr=filters[f].getBoundingClientRect();"
                + "    if(fr.width<1)continue;"
                + "    if(Math.abs((fr.left+fr.right)/2-center)<400)return true;"
                + "  }"
                + "  return false;"
                + "}"
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
                + "var labels=" + labels + ".map(norm);"
                + "var globalSpans=document.querySelectorAll('.label-ellipsis span');"
                + "for(var gi=0;gi<globalSpans.length;gi++){"
                + "  var gcell=resolveHeaderCell(globalSpans[gi]);"
                + "  if(!gcell)continue;"
                + "  var gtext=headerLabelText(gcell);"
                + "  for(var gc=0;gc<labels.length;gc++){"
                + "    if(labelMatches(gtext,labels[gc])&&columnHasFilterUi(gcell))return true;"
                + "  }"
                + "}"
                + "var root=ordersGridRoot();"
                + "function scrollHeaders(delta){"
                + "  root.querySelectorAll('.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport')"
                + "    .forEach(function(v){"
                + "      if(delta===0)v.scrollLeft=0;"
                + "      else v.scrollLeft=Math.max(0,(v.scrollLeft||0)+delta);"
                + "    });"
                + "}"
                + "for(var pass=0;pass<8;pass++){"
                + "  scrollHeaders(pass===0?0:350);"
                + "  var cells=root.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                + "  for(var i=0;i<cells.length;i++){"
                + "    var text=headerLabelText(cells[i]);"
                + "    for(var c=0;c<labels.length;c++){"
                + "      if(!labelMatches(text,labels[c]))continue;"
                + "      if(columnHasFilterUi(cells[i]))return true;"
                + "    }"
                + "  }"
                + "}"
                + "return false;";
    }

    private String probeResolvedColId(String columnId, String columnName) {
        Object result = runScript(buildResolveColIdScript(columnId, columnName));
        if (result == null) {
            return columnId;
        }
        String resolved = String.valueOf(result).trim();
        return resolved.isEmpty() || "null".equalsIgnoreCase(resolved) ? columnId : resolved;
    }

    private boolean probeColumnSearchInput(String columnId, String columnName) {
        gridSortUtil.scrollColumnHeaderIntoView(columnId, columnName);
        return truthy(runScript(buildProbeSearchInputScript(columnId, columnName)));
    }

    private boolean hasColumnSearchControlViaSynergy(String columnId, String columnName, DomColumnRef domRef) {
        gridSortUtil.scrollColumnHeaderIntoView(columnId, columnName);
        if (domRef != null && hasSearchControlForDomRef(domRef, columnName)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.gridColumnSearchInput(columnId), 3)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.gridFloatingFilterInput(columnId), 3)) {
            return true;
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (WaitUtil.isDisplay(tableView.gridColumnSearchInputByLabel(label), 3)) {
                return true;
            }
            if (WaitUtil.isDisplay(tableView.gridFloatingFilterInputByLabel(label), 3)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSearchControlForDomRef(DomColumnRef domRef, String columnName) {
        if (domRef == null || domRef.colId.contains("{") || domRef.colId.contains("=")) {
            return false;
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (hasHeaderFilterIcon(label)) {
                return true;
            }
        }
        if (domRef.hasColId()) {
            if (WaitUtil.isDisplay(tableView.gridFloatingFilterInput(domRef.colId), 2)) {
                return true;
            }
            if (WaitUtil.isDisplay(tableView.gridColumnSearchInput(domRef.colId), 2)) {
                return true;
            }
            if (WaitUtil.isDisplay(tableView.gridColumnFilterIconByColId(domRef.colId), 2)) {
                return true;
            }
        }
        if (domRef.hasColIndex()) {
            if (WaitUtil.isDisplay(tableView.gridFloatingFilterInputByColIndex(domRef.ariaColIndex), 2)) {
                return true;
            }
        }
        return truthy(runScript(buildDomRefSearchProbeScript(domRef, columnName)));
    }

    private boolean hasHeaderFilterIcon(String columnLabel) {
        String litLower = escXPath(columnLabel.toLowerCase(Locale.US));
        By filterIcon = By.XPath("//div[contains(@class,'ag-header-cell')][contains("
                + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + litLower + "')]"
                + "//*[contains(@class,'ag-icon-filter') or contains(@class,'filter-icon')"
                + " or @data-ref='eFilterButton' or contains(@class,'search-icon')"
                + " or contains(@class,'search')][not(ancestor::*[contains(@class,'ag-sort-indicator')])]");
        if (WaitUtil.isDisplay(filterIcon, 2)) {
            return true;
        }
        By headerScope = By.XPath("//div[contains(@class,'ag-header-cell')][contains("
                + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + litLower + "')]");
        return WaitUtil.isDisplay(headerScope, 2)
                && truthy(runScript(
                "var cells=document.querySelectorAll('.ag-header-cell');"
                        + "var needle='" + esc(columnLabel.toLowerCase(Locale.US)) + "';"
                        + "for(var i=0;i<cells.length;i++){"
                        + "  var t=(cells[i].innerText||cells[i].textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();"
                        + "  if(t.indexOf(needle)<0&&needle.indexOf(t)<0)continue;"
                        + "  if(cells[i].querySelector('.ag-icon-filter,[data-ref=eFilterButton],.filter-icon,.search-icon,[class*=filter]'))return true;"
                        + "  var icons=cells[i].querySelectorAll('svg,span[role=button],button');"
                        + "  var sort=cells[i].querySelector('.ag-sort-indicator-container');"
                        + "  for(var k=0;k<icons.length;k++){"
                        + "    if(sort&&sort.contains(icons[k]))continue;"
                        + "    if(icons[k].classList&&icons[k].classList.contains('ag-header-cell-menu-button'))continue;"
                        + "    return true;"
                        + "  }"
                        + "}"
                        + "return false;"));
    }

    private DomColumnRef resolveDomColumnRef(String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, "");
        Object result = runScript(buildResolveDomColumnRefScript(columnName));
        return DomColumnRef.fromScriptResult(result);
    }

    private String buildResolveDomColumnRefScript(String columnName) {
        return headerMatcherJsPrefix("", columnName)
                + "function headerLabelText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                + "}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function matchesLabel(cell){"
                + "  var text=headerLabelText(cell);"
                + "  for(var c=0;c<labels.length;c++){if(labelMatches(text,labels[c]))return true;}"
                + "  return false;"
                + "}"
                + "for(var pass=0;pass<12;pass++){"
                + "  scrollHeaders(pass===0?0:450);"
                + "  var cells=headerCells();"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(!matchesLabel(cells[i]))continue;"
                + "    return (cells[i].getAttribute('col-id')||'')+'|'+(cells[i].getAttribute('aria-colindex')||'');"
                + "  }"
                + "}"
                + "return '|';";
    }

    private String buildDomRefSearchProbeScript(DomColumnRef domRef, String columnName) {
        String colId = esc(domRef.colId == null ? "" : domRef.colId);
        String colIndex = esc(domRef.ariaColIndex == null ? "" : domRef.ariaColIndex);
        return "function ordersGridRoot(){"
                + "  var roots=document.querySelectorAll('.ag-root-wrapper,.ag-root');"
                + "  var best=null,bestRows=0;"
                + "  for(var r=0;r<roots.length;r++){"
                + "    var rows=roots[r].querySelectorAll("
                + "'.ag-center-cols-container > .ag-row:not(.ag-row-level-1)').length;"
                + "    if(rows>bestRows){bestRows=rows;best=roots[r];}"
                + "  }"
                + "  return best||document;"
                + "}"
                + "var colId='" + colId + "';"
                + "var colIndex='" + colIndex + "';"
                + "function hasInput(scope){"
                + "  return !!(scope&&scope.querySelector('input,.search-icon,[class*=search],[class*=filter],button[class*=filter]'));"
                + "}"
                + "var root=ordersGridRoot();"
                + "if(colId){"
                + "  var fl=root.querySelector('.ag-floating-filter-body[col-id=\"'+colId+'\"]');"
                + "  if(hasInput(fl))return true;"
                + "  var hdr=root.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                + "  if(hdr&&hdr.querySelector('.search-icon,[class*=search-icon],input,[class*=filter],svg'))return true;"
                + "}"
                + "if(colIndex){"
                + "  var fl2=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+colIndex+'\"]');"
                + "  if(hasInput(fl2))return true;"
                + "}"
                + "return false;";
    }

    private static final class DomColumnRef {
        final String colId;
        final String ariaColIndex;

        DomColumnRef(String colId, String ariaColIndex) {
            this.colId = colId == null ? "" : colId.trim();
            this.ariaColIndex = ariaColIndex == null ? "" : ariaColIndex.trim();
        }

        boolean hasColId() {
            return !colId.isEmpty();
        }

        boolean hasColIndex() {
            return !ariaColIndex.isEmpty();
        }

        @SuppressWarnings("unchecked")
        static DomColumnRef fromScriptResult(Object result) {
            if (result instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result;
                return new DomColumnRef(stringValue(map.get("colId")), stringValue(map.get("ariaColIndex")));
            }
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                String colId = list.size() > 0 ? stringValue(list.get(0)) : "";
                String colIndex = list.size() > 1 ? stringValue(list.get(1)) : "";
                return new DomColumnRef(colId, colIndex);
            }
            if (result != null) {
                String raw = String.valueOf(result).trim();
                if (raw.startsWith("{") && raw.contains("=")) {
                    String colId = extractMapField(raw, "colId");
                    String colIndex = extractMapField(raw, "ariaColIndex");
                    if (!colId.isEmpty() || !colIndex.isEmpty()) {
                        return new DomColumnRef(colId, colIndex);
                    }
                    if (raw.contains("colId=") || raw.contains("ariaColIndex=")) {
                        return new DomColumnRef("", "");
                    }
                }
            }
            return fromPipeValue(result);
        }

        private static String extractMapField(String raw, String field) {
            String needle = field + "=";
            int start = raw.indexOf(needle);
            if (start < 0) {
                return "";
            }
            start += needle.length();
            int end = raw.indexOf(',', start);
            if (end < 0) {
                end = raw.indexOf('}', start);
            }
            if (end < 0) {
                end = raw.length();
            }
            String value = raw.substring(start, end).trim();
            return "null".equalsIgnoreCase(value) ? "" : value;
        }

        static DomColumnRef fromPipeValue(Object result) {
            if (result == null) {
                return new DomColumnRef("", "");
            }
            String raw = String.valueOf(result).trim();
            if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) {
                return new DomColumnRef("", "");
            }
            int pipe = raw.indexOf('|');
            if (pipe >= 0) {
                return new DomColumnRef(raw.substring(0, pipe).trim(), raw.substring(pipe + 1).trim());
            }
            return new DomColumnRef(raw, "");
        }

        private static String stringValue(Object value) {
            return value == null ? "" : String.valueOf(value).trim();
        }
    }

    private ColumnProbe probeColumn(String columnId, String columnName) {
        gridSortUtil.scrollColumnHeaderIntoView(columnId, columnName);
        ColumnProbe headerProbe = probeColumnViaHeaderElement(columnId, columnName);
        if (headerProbe != null && (headerProbe.hasSearch || headerProbe.sample != null)) {
            enrichSample(headerProbe, columnId, columnName);
            return headerProbe;
        }
        Object result = runScript(buildColumnProbeScript(columnId, columnName));
        ColumnProbe probe = ColumnProbe.fromScriptResult(result, columnId);
        if (headerProbe != null) {
            if (!probe.hasSearch && headerProbe.hasSearch) {
                probe = new ColumnProbe(
                        headerProbe.colId.isEmpty() ? probe.colId : headerProbe.colId,
                        true,
                        probe.sample != null ? probe.sample : headerProbe.sample);
            } else if ((probe.colId.equals(columnId) || probe.sample == null) && headerProbe.sample != null) {
                probe = new ColumnProbe(
                        headerProbe.colId.isEmpty() ? probe.colId : headerProbe.colId,
                        probe.hasSearch || headerProbe.hasSearch,
                        headerProbe.sample);
            }
        }
        enrichSample(probe, columnId, columnName);
        return probe;
    }

    private void enrichSample(ColumnProbe probe, String columnId, String columnName) {
        if (probe.sample != null) {
            return;
        }
        probe.sample = pickSampleTerm(gridSortUtil.readVisibleColumnValues(probe.colId, SAMPLE_ROWS));
        if (probe.sample == null && !probe.colId.equals(columnId)) {
            probe.sample = pickSampleTerm(gridSortUtil.readVisibleColumnValues(columnId, SAMPLE_ROWS));
        }
        if (probe.sample == null) {
            probe.sample = sampleFromGridByLabel(columnName);
        }
    }

    private ColumnProbe probeColumnViaHeaderElement(String columnId, String columnName) {
        boolean headerVisible = false;
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (WaitUtil.isDisplay(tableView.gridColumnHeader(label), 2)) {
                headerVisible = true;
                break;
            }
        }
        if (!headerVisible && !WaitUtil.isDisplay(tableView.gridColumnHeaderByColId(columnId), 2)) {
            return null;
        }
        Object result = runScript(buildHeaderElementProbeScript(columnId, columnName));
        ColumnProbe probe = ColumnProbe.fromScriptResult(result, columnId);
        if (probe.colId.equals(columnId) && !probe.hasSearch && probe.sample == null) {
            return null;
        }
        return probe;
    }

    private String buildHeaderElementProbeScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function matches(cell){"
                + "  var text=cellText(cell);"
                + "  var cid=normId(cell.getAttribute('col-id'));"
                + "  for(var a=0;a<idAliases.length;a++){"
                + "    var alias=idAliases[a];"
                + "    if(!alias||!cid)continue;"
                + "    if(cid===alias||cid.indexOf(alias)>=0||alias.indexOf(cid)>=0)return true;"
                + "  }"
                + "  for(var c=0;c<labels.length;c++){if(labelMatches(text,labels[c]))return true;}"
                + "  return false;"
                + "}"
                + HEADER_PROBE_BODY_JS;
    }

    private String sampleFromGridByLabel(String columnName) {
        Object result = runScript(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function labelMatches(text,label){"
                        + "  if(!text||!label)return false;"
                        + "  if(text===label||text.indexOf(label)>=0||label.indexOf(text)>=0)return true;"
                        + "  if(label.length>=4&&text.indexOf(label.substring(0,Math.min(label.length,10)))===0)return true;"
                        + "  if((text.indexOf('...')>=0||text.indexOf('\\u2026')>=0)){"
                        + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                        + "    if(prefix.length>=4&&label.indexOf(prefix)===0)return true;"
                        + "  }"
                        + "  return false;"
                        + "}"
                        + "var label=norm('" + esc(columnName) + "');"
                        + "var roots=document.querySelectorAll('.ag-root-wrapper,.ag-root');"
                        + "var colId=null,idx=null;"
                        + "for(var ri=0;ri<roots.length;ri++){"
                        + "  var headers=roots[ri].querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "  for(var i=0;i<headers.length;i++){"
                        + "    var t=norm(headers[i].innerText||headers[i].textContent);"
                        + "    if(labelMatches(t,label)){"
                        + "      colId=headers[i].getAttribute('col-id');"
                        + "      idx=headers[i].getAttribute('aria-colindex');"
                        + "      break;"
                        + "    }"
                        + "  }"
                        + "  if(colId||idx)break;"
                        + "}"
                        + "if(!colId&&!idx)return null;"
                        + "var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                        + "  '.ag-pinned-right-cols-container'];"
                        + "for(var ci=0;ci<containers.length;ci++){"
                        + "  var rows=document.querySelectorAll(containers[ci]+' > .ag-row');"
                        + "  for(var r=0;r<rows.length&&r<12;r++){"
                        + "    if(rows[r].classList.contains('ag-row-level-1'))continue;"
                        + "    var cell=colId?rows[r].querySelector('[col-id=\"'+colId+'\"]'):null;"
                        + "    if(!cell&&idx)cell=rows[r].querySelector('[aria-colindex=\"'+idx+'\"]');"
                        + "    if(!cell)continue;"
                        + "    var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                        + "    if(v)return v;"
                        + "  }"
                        + "}"
                        + "return null;");
        return result == null ? null : trimToNull(String.valueOf(result));
    }

    private static String trimToNull(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private static final class ColumnProbe {
        final String colId;
        final boolean hasSearch;
        String sample;

        ColumnProbe(String colId, boolean hasSearch, String sample) {
            this.colId = colId == null ? "" : colId;
            this.hasSearch = hasSearch;
            this.sample = sample;
        }

        @SuppressWarnings("unchecked")
        static ColumnProbe fromScriptResult(Object result, String fallbackColId) {
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                if (list.size() >= 2) {
                    String resolvedColId = stringOrDefault(list.get(0), fallbackColId);
                    boolean search = truthyValue(list.get(1));
                    String sampleText = list.size() > 2 ? trimToNull(stringOrDefault(list.get(2), null)) : null;
                    return new ColumnProbe(resolvedColId, search, sampleText);
                }
            }
            if (result instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result;
                Object colId = map.get("colId");
                Object hasSearch = map.get("hasSearch");
                Object sample = map.get("sample");
                String resolvedColId = stringOrDefault(colId, fallbackColId);
                boolean search = truthyValue(hasSearch);
                String sampleText = trimToNull(stringOrDefault(sample, null));
                return new ColumnProbe(resolvedColId, search, sampleText);
            }
            if (result instanceof String) {
                String raw = ((String) result).trim();
                if (raw.startsWith("[") && raw.endsWith("]")) {
                    String inner = raw.substring(1, raw.length() - 1).trim();
                    int firstComma = inner.indexOf(',');
                    if (firstComma > 0) {
                        String parsedColId = inner.substring(0, firstComma).trim();
                        String rest = inner.substring(firstComma + 1).trim();
                        int secondComma = rest.indexOf(',');
                        if (secondComma >= 0) {
                            String searchStr = rest.substring(0, secondComma).trim();
                            String sample = rest.substring(secondComma + 1).trim();
                            return new ColumnProbe(
                                    stringOrDefault(parsedColId, fallbackColId),
                                    truthyValue(searchStr),
                                    trimToNull(sample));
                        }
                    }
                }
                if (raw.startsWith("{") && raw.contains("colId")) {
                    boolean search = raw.contains("\"hasSearch\":true") || raw.contains("'hasSearch':true");
                    String colId = fallbackColId;
                    int colIdx = raw.indexOf("\"colId\"");
                    if (colIdx >= 0) {
                        int start = raw.indexOf(':', colIdx) + 1;
                        int end = raw.indexOf(',', start);
                        if (end < 0) {
                            end = raw.indexOf('}', start);
                        }
                        if (start > 0 && end > start) {
                            colId = raw.substring(start, end).replace("\"", "").replace("'", "").trim();
                        }
                    }
                    return new ColumnProbe(colId, search, null);
                }
                if (raw.contains("|")) {
                    String[] parts = raw.split("\\|", 3);
                    if (parts.length >= 2) {
                        return new ColumnProbe(
                                stringOrDefault(parts[0], fallbackColId),
                                truthyValue(parts[1]),
                                parts.length > 2 ? trimToNull(parts[2]) : null);
                    }
                }
            }
            Logger.log("Column probe parse fallback for colId=" + fallbackColId
                    + " resultType=" + (result == null ? "null" : result.getClass().getName())
                    + " value=" + result);
            return new ColumnProbe(fallbackColId, false, null);
        }

        private static String stringOrDefault(Object value, String fallback) {
            if (value == null) {
                return fallback;
            }
            String text = String.valueOf(value).trim();
            return text.isEmpty() ? fallback : text;
        }

        private static String trimToNull(String value) {
            if (value == null || "null".equalsIgnoreCase(String.valueOf(value).trim())) {
                return null;
            }
            String text = String.valueOf(value).trim();
            return text.isEmpty() ? null : text;
        }

        private static boolean truthyValue(Object value) {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value == null) {
                return false;
            }
            String text = String.valueOf(value).trim();
            return "true".equalsIgnoreCase(text) || "1".equals(text);
        }
    }

    public boolean hasDateFilterControl(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (isTc022DateSearchColumn(columnId, columnName)) {
            return hasTc022DateFilterStructure(columnId, columnName);
        }
        if (detectColumnFilterControl(columnName)) {
            return true;
        }
        if (FulfillmentJsUtil.hasGridColumnSearchControl(columnName, columnId)) {
            return true;
        }
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        if (WaitUtil.isDisplay(tableView.customColumnFilterInHeaderByLabel(columnName), 1)) {
            return true;
        }
        if (WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(effectiveId), 1)) {
            return true;
        }
        if (truthy(runScript(
                "var colId='" + esc(effectiveId) + "';"
                        + "var cell=document.querySelector('.ag-header-cell[col-id=\"'+colId+'\"]');"
                        + "if(!cell)return false;"
                        + "if(cell.querySelector('.ag-date-filter,[class*=date-filter],.fa-calendar,.calendar,"
                        + "msc-custom-table-column-filter,msc-custom-table-column-filter-themed'))return true;"
                        + "var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+colId+'\"]');"
                        + "if(floatCell&&floatCell.querySelector('input,.ag-date-filter,[class*=date],button'))return true;"
                        + "return false;"))) {
            return true;
        }
        return hasCustomColumnFilterControl(effectiveId, columnName);
    }

    public String sampleSearchTerm(String columnId, String columnName) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if ("orderId".equalsIgnoreCase(columnId) || "Order ID".equalsIgnoreCase(columnName)) {
            String orderSample = sampleViaBulkScript(columnId, columnName);
            if (orderSample != null) {
                return orderSample;
            }
            return "fdy";
        }
        Object quickSample = runScript(buildQuickSampleScript(columnName));
        String quick = quickSample == null ? null : trimToNull(String.valueOf(quickSample));
        if (quick != null) {
            return pickSampleTermFromValue(quick);
        }
        DomColumnRef domRef = resolveDomColumnRef(columnName);
        if (domRef != null && domRef.hasColId()) {
            String domSample = pickSampleTerm(gridSortUtil.readVisibleColumnValues(domRef.colId, SAMPLE_ROWS));
            if (domSample != null) {
                return domSample;
            }
        }
        if (domRef != null && domRef.hasColIndex()) {
            String idxSample = pickSampleTerm(
                    gridSortUtil.readVisibleColumnValuesByColIndex(domRef.ariaColIndex, SAMPLE_ROWS));
            if (idxSample != null) {
                return idxSample;
            }
        }
        String bulkSample = sampleViaBulkScript(columnId, columnName);
        if (bulkSample != null) {
            return bulkSample;
        }
        try {
            String synergySample = sampleSearchTermViaSynergy(columnName);
            if (synergySample != null) {
                return synergySample;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ColumnProbe probe = probeColumn(columnId, columnName);
        if (probe.sample != null) {
            return probe.sample;
        }
        String effectiveId = probe.colId;
        String term = pickSampleTerm(gridSortUtil.readVisibleColumnValues(effectiveId, SAMPLE_ROWS));
        if (term != null) {
            return term;
        }
        if (!effectiveId.equals(columnId)) {
            term = pickSampleTerm(gridSortUtil.readVisibleColumnValues(columnId, SAMPLE_ROWS));
            if (term != null) {
                return term;
            }
        }
        for (String altId : alternateColumnIds(columnId, columnName)) {
            if (altId.equals(columnId) || altId.equals(effectiveId)) {
                continue;
            }
            term = pickSampleTerm(gridSortUtil.readVisibleColumnValues(altId, SAMPLE_ROWS));
            if (term != null) {
                return term;
            }
        }
        return null;
    }

    private static String pickSampleTermFromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 4) {
            return trimmed.substring(0, Math.min(trimmed.length(), Math.max(4, trimmed.length() / 2)));
        }
        return trimmed.length() >= 2 ? trimmed : null;
    }

    private String buildQuickSampleScript(String columnName) {
        String labels = labelsArray(columnName);
        return "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
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
                + "function headerLabelText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                + "}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "var labels=" + labels + ".map(norm);"
                + "var colId=null,idx=null;"
                + "var root=ordersGridRoot();"
                + "function scrollHeaders(delta){"
                + "  root.querySelectorAll('.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport')"
                + "    .forEach(function(v){"
                + "      if(delta===0)v.scrollLeft=0;"
                + "      else v.scrollLeft=Math.max(0,(v.scrollLeft||0)+delta);"
                + "    });"
                + "}"
                + "for(var pass=0;pass<8;pass++){"
                + "  scrollHeaders(pass===0?0:350);"
                + "  var cells=root.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                + "  for(var i=0;i<cells.length;i++){"
                + "    var text=headerLabelText(cells[i]);"
                + "    for(var c=0;c<labels.length;c++){"
                + "      if(!labelMatches(text,labels[c]))continue;"
                + "      colId=cells[i].getAttribute('col-id');"
                + "      idx=cells[i].getAttribute('aria-colindex');"
                + "      break;"
                + "    }"
                + "    if(colId||idx)break;"
                + "  }"
                + "  if(colId||idx)break;"
                + "}"
                + "if(!colId&&!idx)return null;"
                + "var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                + "  '.ag-pinned-right-cols-container'];"
                + "for(var ci=0;ci<containers.length;ci++){"
                + "  var rows=root.querySelectorAll(containers[ci]+' > .ag-row');"
                + "  for(var r=0;r<rows.length&&r<15;r++){"
                + "    if(rows[r].classList.contains('ag-row-level-1'))continue;"
                + "    var cell=colId?rows[r].querySelector('[col-id=\"'+colId+'\"]'):null;"
                + "    if(!cell&&idx)cell=rows[r].querySelector('[aria-colindex=\"'+idx+'\"]');"
                + "    if(!cell)continue;"
                + "    var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                + "    if(v)return v;"
                + "  }"
                + "}"
                + "return null;";
    }

    private static String pickSampleTerm(List<String> values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.length() >= 4) {
                return trimmed.substring(0, Math.min(trimmed.length(), Math.max(4, trimmed.length() / 2)));
            }
            if (trimmed.length() >= 2) {
                return trimmed;
            }
        }
        return null;
    }

    private static String[] alternateColumnIds(String columnId, String columnName) {
        if (columnId != null && columnId.toLowerCase(Locale.US).contains("titleseason")) {
            return new String[] { "title", "titleSeasonEpisode" };
        }
        if ("Episode".equalsIgnoreCase(columnName)) {
            return new String[] { "episode", "title" };
        }
        if ("Assigned to".equalsIgnoreCase(columnName)) {
            return new String[] { "assignedTo", "assignedToOrder", "assigned_to" };
        }
        if ("Submitted by".equalsIgnoreCase(columnName)) {
            return new String[] { "submittedBy", "submitted_by" };
        }
        if ("Asset ID".equalsIgnoreCase(columnName)) {
            return new String[] { "assetId", "asset_id" };
        }
        return new String[] { columnId };
    }

    public String resolveEffectiveColumnId(String columnId, String columnName) {
        DomColumnRef domRef = resolveDomColumnRef(columnName);
        if (domRef != null && domRef.hasColId()) {
            return domRef.colId;
        }
        return probeColumn(columnId, columnName).colId;
    }

    public int countVisibleOrderRows() {
        Object count = runScript(
                "var rows=document.querySelectorAll('.ag-center-cols-container > .ag-row');"
                        + "var n=0;"
                        + "for(var i=0;i<rows.length;i++){"
                        + "  if(rows[i].classList.contains('ag-row-level-1'))continue;"
                        + "  if(rows[i].getAttribute('row-index')!=null)n++;"
                        + "}"
                        + "return n;");
        if (count instanceof Number) {
            return ((Number) count).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(count));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean applyColumnSearch(String columnId, String columnName, String searchTerm) {
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        try {
            if (searchColumn(searchTerm, columnName, columnId)) {
                return true;
            }
            if (applyColumnSearchViaCustomFilter(effectiveId, columnName, searchTerm)) {
                return true;
            }
            if (applyColumnSearchViaJsFilter(columnName, searchTerm)) {
                return true;
            }
            if (applyColumnSearchViaSynergy(columnName, searchTerm)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return truthy(runScript(buildApplySearchScript(effectiveId, columnName, searchTerm)));
    }

    /**
     * Task-list style column search: focus grid, click header, type in filter input, press Enter, wait for rows.
     */
    public boolean searchColumn(String searchWord, String columnName, String columnId) throws InterruptedException {
        if (searchWord == null || searchWord.trim().isEmpty()) {
            return false;
        }
        tableViewUtil.waitForOrdersGridReadyFast(3);
        String resolvedColumn = resolveColumnDisplayName(columnName);
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        Logger.logReportMessage("Searching Orders grid column '" + resolvedColumn + "' with keyword: " + searchWord);

        By tableViewBtn = tableView.getTableViewButton();
        if (WaitUtil.isDisplay(tableViewBtn, 3)) {
            try {
                BaseTest.driver.get().finder().findElement(tableViewBtn)
                        .executeScript("arguments[0].scrollIntoView({block:'center',inline:'nearest'});");
            } catch (Exception ignored) {
            }
        }

        gridSortUtil.scrollColumnHeaderIntoView(effectiveId, resolvedColumn);
        By headerLocator = resolveColumnHeaderLocator(resolvedColumn, columnName);
        if (WaitUtil.isDisplay(headerLocator, 5)) {
            BaseTest.driver.get().finder().findElement(headerLocator)
                    .executeScript("arguments[0].scrollIntoView({block:'nearest',inline:'center'});");
            DriverUtil.clickOnElementSafely(headerLocator, 5);
            Thread.sleep(500);
        }

        if (!WaitUtil.isDisplay(tableView.orderTableColumnSearchInput(effectiveId), 2)) {
            openCustomColumnFilterPopup(effectiveId, columnName);
            clickColumnSearchTrigger(resolvedColumn);
            Thread.sleep(800);
        }

        By[] inputCandidates = {
                tableView.orderTableColumnSearchInput(effectiveId),
                columnSearchInputByLabel(resolvedColumn),
                tableView.gridFloatingFilterInput(effectiveId),
                tableView.gridColumnSearchInput(effectiveId)
        };
        for (By input : inputCandidates) {
            if (typeIntoColumnSearchInput(input, searchWord)) {
                Thread.sleep(5000);
                gridSortUtil.scrollColumnHeaderIntoView(effectiveId, resolvedColumn);
                Logger.logReportMessage("Scrolled horizontally to " + resolvedColumn + " column to verify search results");
                return true;
            }
        }
        return false;
    }

    /** Validates visible grid rows contain the search keyword in the target column (Task-list parity). */
    public void verifyColumnSearchResultsVisible(String keyword, String columnName, String columnId) {
        String resolvedColumn = resolveColumnDisplayName(columnName);
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        Verify.softAssert(isHeaderVisible(columnId, columnName),
                resolvedColumn + " column header is visible after search");

        List<String> values = gridSortUtil.readVisibleColumnValues(effectiveId, SAMPLE_ROWS);
        Verify.softAssert(!values.isEmpty(), resolvedColumn + " column search returned at least one visible row");

        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        String keywordLower = keyword.trim().toLowerCase(Locale.US);
        for (String cellText : values) {
            String visible = cellText == null ? "" : cellText.trim();
            Logger.log("Validating visible " + resolvedColumn + " cell: " + visible);
            Verify.softAssert(!visible.isEmpty(),
                    resolvedColumn + " column value '" + visible + "' is visible in table");
            Verify.softAssert(visible.toLowerCase(Locale.US).contains(keywordLower),
                    resolvedColumn + " column value '" + visible + "' contains search keyword '" + keyword + "'");
        }
    }

    private String resolveColumnDisplayName(String columnName) {
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            if (tableViewUtil.isExactOrderGridHeaderVisible(columnName)) {
                return columnName;
            }
            for (String label : new String[] {"Title", "Season", "Episode"}) {
                try {
                    if (tableViewUtil.isExactOrderGridHeaderVisible(label)
                            || tableViewUtil.isGridColumnHeaderVisibleForSearch(label, "order")) {
                        return "Title";
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            try {
                if (tableViewUtil.isExactOrderGridHeaderVisible(label)
                        || tableViewUtil.isGridColumnHeaderVisibleForSearch(label, "order")) {
                    return label;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return columnName;
    }

    private By resolveColumnHeaderLocator(String resolvedColumn, String columnName) {
        By header = tableView.gridColumnHeader(resolvedColumn);
        if (WaitUtil.isDisplay(header, 1)) {
            return header;
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            header = tableView.gridColumnHeader(label);
            if (WaitUtil.isDisplay(header, 1)) {
                return header;
            }
        }
        return tableView.gridColumnHeader(resolvedColumn);
    }

    public void clearColumnSearch(String columnId, String columnName) {
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        gridSortUtil.scrollColumnHeaderIntoView(columnId, columnName);
        try {
            if (clearColumnSearchViaSynergy(columnName)) {
                return;
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        runScript(buildClearSearchScript(effectiveId, columnName));
    }

    private boolean applyColumnSearchViaSynergy(String columnName, String searchTerm) throws InterruptedException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return false;
        }
        gridSortUtil.scrollColumnHeaderIntoView("", columnName);
        Thread.sleep(500);
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (clickColumnSearchTrigger(label)) {
                Thread.sleep(800);
                By input = columnSearchInputByLabel(label);
                if (typeIntoColumnSearchInput(input, searchTerm)) {
                    return true;
                }
                By popupInput = By.XPath("//div[contains(@class,'ag-popup-child')]//input"
                        + " | //div[contains(@class,'ag-filter')]//input"
                        + " | //div[contains(@class,'cdk-overlay-pane')]//input");
                if (typeIntoColumnSearchInput(popupInput, searchTerm)) {
                    return true;
                }
            }
        }
        DomColumnRef domRef = resolveDomColumnRef(columnName);
        if (domRef != null) {
            if (domRef.hasColId() && typeIntoColumnSearchInput(tableView.gridFloatingFilterInput(domRef.colId), searchTerm)) {
                return true;
            }
            if (domRef.hasColId() && typeIntoColumnSearchInput(tableView.gridColumnSearchInput(domRef.colId), searchTerm)) {
                return true;
            }
            if (domRef.hasColIndex()
                    && typeIntoColumnSearchInput(tableView.gridFloatingFilterInputByColIndex(domRef.ariaColIndex), searchTerm)) {
                return true;
            }
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (typeIntoColumnSearchInput(tableView.gridFloatingFilterInputByLabel(label), searchTerm)) {
                return true;
            }
        }
        return false;
    }

    private boolean applyColumnSearchViaJsFilter(String columnName, String searchTerm) throws InterruptedException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return false;
        }
        Object applied = runScript(buildApplyFilterSearchScript(columnName, searchTerm));
        if (truthy(applied)) {
            Logger.logReportMessage("Applied column search via JS filter for [" + columnName + "] term=\""
                    + searchTerm + "\"");
            Thread.sleep(500);
            return true;
        }
        return false;
    }

    private String buildApplyFilterSearchScript(String columnName, String searchTerm) {
        String labels = labelsArray(columnName);
        String term = esc(searchTerm);
        return "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function headerLabelText(cell){"
                + "  var ellipsis=cell.querySelector('.label-ellipsis span');"
                + "  if(ellipsis){var et=norm(ellipsis.innerText||ellipsis.textContent);if(et)return et;}"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  return norm(label?(label.innerText||label.textContent):(cell.innerText||cell.textContent));"
                + "}"
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function scrollHeaders(delta){"
                + "  document.querySelectorAll('.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport')"
                + "    .forEach(function(v){"
                + "      if(delta===0)v.scrollLeft=0;"
                + "      else v.scrollLeft=Math.max(0,(v.scrollLeft||0)+delta);"
                + "    });"
                + "}"
                + "var labels=" + labels + ".map(norm);"
                + "function findHeaderCell(){"
                + "  for(var pass=0;pass<14;pass++){"
                + "    scrollHeaders(pass===0?0:350);"
                + "    var cells=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                + "    for(var i=0;i<cells.length;i++){"
                + "      var text=headerLabelText(cells[i]);"
                + "      for(var c=0;c<labels.length;c++){if(labelMatches(text,labels[c]))return cells[i];}"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "function openFilterInput(cell){"
                + "  if(!cell)return null;"
                + "  var trigger=cell.querySelector('.ag-icon-filter,[data-ref=eFilterButton],.filter-icon,.search-icon,[class*=filter]');"
                + "  if(!trigger){"
                + "    var sort=cell.querySelector('.ag-sort-indicator-container');"
                + "    var icons=cell.querySelectorAll('span,svg,button,i,[role=button]');"
                + "    for(var k=0;k<icons.length;k++){"
                + "      if(sort&&sort.contains(icons[k]))continue;"
                + "      if(icons[k].classList&&icons[k].classList.contains('ag-header-cell-menu-button'))continue;"
                + "      if(icons[k].classList&&icons[k].classList.contains('ag-header-cell-text'))continue;"
                + "      trigger=icons[k];break;"
                + "    }"
                + "  }"
                + "  if(trigger){try{trigger.click();}catch(e){}}"
                + "  var cid=cell.getAttribute('col-id');"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  var root=cell.closest('.ag-root-wrapper,.ag-root')||document;"
                + "  if(cid){"
                + "    var fl=root.querySelector('.ag-floating-filter-body[col-id=\"'+cid+'\"] input');"
                + "    if(fl)return fl;"
                + "  }"
                + "  if(idx){"
                + "    var fl2=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"] input');"
                + "    if(fl2)return fl2;"
                + "  }"
                + "  var popup=document.querySelector('.ag-popup-child input,.ag-filter input,.cdk-overlay-pane input');"
                + "  return popup||cell.querySelector('input');"
                + "}"
                + "var header=findHeaderCell();"
                + "if(!header)return false;"
                + "header.scrollIntoView({block:'nearest',inline:'center'});"
                + "var input=openFilterInput(header);"
                + "if(!input)return false;"
                + "input.focus();"
                + "input.value='" + term + "';"
                + "input.dispatchEvent(new Event('input',{bubbles:true}));"
                + "input.dispatchEvent(new Event('change',{bubbles:true}));"
                + "input.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));"
                + "return true;";
    }

    private boolean typeIntoColumnSearchInput(By input, String searchTerm) throws InterruptedException {
        if (!WaitUtil.isDisplay(input, 3)) {
            return false;
        }
        DriverUtil.clickOnElementSafely(input, 3);
        BaseTest.driver.get().finder().findElement(input).executeScript(
                "arguments[0].focus();arguments[0].value='';"
                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));");
        BaseTest.driver.get().finder().findElement(input).sendKeys(searchTerm);
        BaseTest.driver.get().browser().sendKeys(WebKey.ENTER);
        Logger.logReportMessage("Applied column search via Synergy input, term=\"" + searchTerm + "\"");
        Thread.sleep(500);
        return true;
    }

    private boolean clearColumnSearchViaSynergy(String columnName) throws InterruptedException {
        gridSortUtil.scrollColumnHeaderIntoView("", columnName);
        DomColumnRef domRef = resolveDomColumnRef(columnName);
        if (domRef != null) {
            if (domRef.hasColId()) {
                if (openCustomColumnFilterPopup(domRef.colId, columnName)
                        && clearSearchInput(tableView.orderTableColumnSearchInput(domRef.colId))) {
                    return true;
                }
            }
            if (domRef.hasColId() && clearSearchInput(tableView.gridFloatingFilterInput(domRef.colId))) {
                return true;
            }
            if (domRef.hasColId() && clearSearchInput(tableView.gridColumnSearchInput(domRef.colId))) {
                return true;
            }
            if (domRef.hasColIndex()
                    && clearSearchInput(tableView.gridFloatingFilterInputByColIndex(domRef.ariaColIndex))) {
                return true;
            }
        }
        for (String label : gridSortUtil.headerLabelCandidates(columnName)) {
            if (clearSearchInput(tableView.gridFloatingFilterInputByLabel(label))) {
                return true;
            }
            if (!clickColumnSearchTrigger(label)) {
                continue;
            }
            Thread.sleep(600);
            if (clearSearchInput(columnSearchInputByLabel(label))) {
                Logger.logReportMessage("Cleared column search via Synergy click for [" + columnName + "]");
                return true;
            }
        }
        return false;
    }

    private boolean clearSearchInput(By input) throws InterruptedException {
        if (!WaitUtil.isDisplay(input, 3)) {
            return false;
        }
        DriverUtil.clickOnElementSafely(input, 3);
        BaseTest.driver.get().finder().findElement(input).executeScript(
                "arguments[0].focus();arguments[0].value='';"
                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));");
        BaseTest.driver.get().browser().sendKeys(WebKey.ENTER);
        return true;
    }

    private boolean clickColumnSearchTrigger(String columnLabel) {
        By customTrigger = tableView.customColumnFilterTriggerByLabel(columnLabel);
        if (WaitUtil.isDisplay(customTrigger, 1)) {
            return DriverUtil.clickOnElementSafely(customTrigger, 5);
        }
        String lit = TableView.escapeXPathLiteral(columnLabel);
        String litLower = columnLabel.toLowerCase(Locale.US);
        By headerScope = By.XPath(tableView.gridHeaderCellXPathByLabel(columnLabel));
        if (!WaitUtil.isDisplay(headerScope, 1) && !"Partner".equalsIgnoreCase(columnLabel)) {
            headerScope = tableView.gridColumnHeader(columnLabel);
        }
        if (!WaitUtil.isDisplay(headerScope, 2) && !"Partner".equalsIgnoreCase(columnLabel)) {
            headerScope = By.XPath("//div[contains(@class,'ag-header-cell')][contains("
                    + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + litLower + "')]");
        }
        if (!WaitUtil.isDisplay(headerScope, 2)) {
            return false;
        }
        try {
            BaseTest.driver.get().finder().findElement(headerScope).executeScript(
                    "arguments[0].dispatchEvent(new MouseEvent('mouseenter',{bubbles:true}));"
                            + "arguments[0].dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));");
        } catch (Exception ignored) {
        }
        String headerPred = "//div[contains(@class,'label-ellipsis')]//span[normalize-space()=" + lit
                + "]/ancestor::div[contains(@class,'ag-header-cell')][1]";
        if (!WaitUtil.isDisplay(By.XPath(headerPred), 1)) {
            headerPred = "//div[contains(@class,'ag-header-cell')]"
                    + "[.//*[contains(@class,'ag-header-cell-text')][normalize-space()=" + lit + "]]";
        }
        if (!"Partner".equalsIgnoreCase(columnLabel) && !WaitUtil.isDisplay(By.XPath(headerPred), 1)) {
            headerPred = "//div[contains(@class,'ag-header-cell')]"
                    + "[.//*[contains(@class,'ag-header-cell-text')][normalize-space()=" + lit
                    + " or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                    + " 'abcdefghijklmnopqrstuvwxyz'), '" + litLower + "')]]";
        } else if (!"Partner".equalsIgnoreCase(columnLabel) && !WaitUtil.isDisplay(By.XPath(headerPred), 1)) {
            headerPred = "//div[contains(@class,'ag-header-cell')][contains("
                    + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + litLower + "')]";
        }
        By filterIcon = By.XPath(headerPred
                + "//*[contains(@class,'ag-icon-filter') or contains(@class,'filter-icon')"
                + " or @data-ref='eFilterButton' or contains(@class,'search-icon') or contains(@class,'search')]"
                + "[not(ancestor::*[contains(@class,'ag-sort-indicator')])]");
        if (WaitUtil.isDisplay(filterIcon, 2)) {
            return DriverUtil.clickOnElementSafely(filterIcon, 5);
        }
        By searchIcon = By.XPath(headerPred
                + "//*[contains(@class,'search-icon') or contains(@class,'search')]"
                + "[not(ancestor::*[contains(@class,'ag-sort-indicator')])]");
        if (WaitUtil.isDisplay(searchIcon, 2)) {
            return DriverUtil.clickOnElementSafely(searchIcon, 5);
        }
        By extraIcon = By.XPath(headerPred
                + "//span[@data-ref='eMenu']//preceding-sibling::*[self::span or self::button]"
                + " | " + headerPred
                + "//svg[not(ancestor::*[contains(@class,'ag-sort-indicator')])"
                + " and not(ancestor::*[contains(@class,'ag-header-cell-menu-button')])]");
        if (WaitUtil.isDisplay(extraIcon, 2)) {
            return DriverUtil.clickOnElementSafely(extraIcon, 5);
        }
        By inlineInput = columnSearchInputByLabel(columnLabel);
        return WaitUtil.isDisplay(inlineInput, 2) && DriverUtil.clickOnElementSafely(inlineInput, 3);
    }

    private By columnSearchInputByLabel(String columnLabel) {
        String litLower = escXPath(columnLabel.toLowerCase(Locale.US));
        return By.XPath("//div[contains(@class,'ag-header-cell')][contains("
                + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + litLower + "')]//input"
                + " | //div[contains(@class,'ag-floating-filter-body')][contains("
                + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + litLower + "')]//input"
                + " | //div[contains(@class,'ag-popup-child')]//input"
                + " | //div[contains(@class,'ag-filter')]//input"
                + " | //div[contains(@class,'cdk-overlay-pane')]//input"
                + " | //div[contains(@class,'ag-popup-child')]//input");
    }

    private String sampleSearchTermViaSynergy(String columnName) throws InterruptedException {
        gridSortUtil.scrollColumnHeaderIntoView("", columnName);
        Thread.sleep(500);
        Object result = runScript(
                "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"
                        + "function labelMatches(text,label){"
                        + "  if(!text||!label)return false;"
                        + "  if(label==='partner'&&text!=='partner')return false;"
                        + "  if(text===label||text.indexOf(label)>=0||label.indexOf(text)>=0)return true;"
                        + "  if(label.length>=4&&text.indexOf(label.substring(0,Math.min(label.length,10)))===0)return true;"
                        + "  return false;"
                        + "}"
                        + "var label=norm('" + esc(columnName) + "');"
                        + "var headers=document.querySelectorAll('.ag-header-cell,[role=columnheader]');"
                        + "var colId=null,idx=null;"
                        + "for(var i=0;i<headers.length;i++){"
                        + "  var t=norm(headers[i].innerText||headers[i].textContent);"
                        + "  if(labelMatches(t,label)){"
                        + "    colId=headers[i].getAttribute('col-id');"
                        + "    idx=headers[i].getAttribute('aria-colindex');"
                        + "    break;"
                        + "  }"
                        + "}"
                        + "if(!colId&&!idx)return null;"
                        + "var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                        + "  '.ag-pinned-right-cols-container'];"
                        + "for(var ci=0;ci<containers.length;ci++){"
                        + "  var rows=document.querySelectorAll(containers[ci]+' > .ag-row');"
                        + "  for(var r=0;r<rows.length&&r<15;r++){"
                        + "    if(rows[r].classList.contains('ag-row-level-1'))continue;"
                        + "    var cell=colId?rows[r].querySelector('[col-id=\"'+colId+'\"]'):null;"
                        + "    if(!cell&&idx)cell=rows[r].querySelector('[aria-colindex=\"'+idx+'\"]');"
                        + "    if(!cell)continue;"
                        + "    var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                        + "    if(v)return v;"
                        + "  }"
                        + "}"
                        + "return null;");
        return result == null ? null : trimToNull(String.valueOf(result));
    }

    private static String escXPath(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "\\'");
    }

    public boolean allVisibleValuesContain(String columnId, String columnName, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        List<String> values = gridSortUtil.readVisibleColumnValues(effectiveId, SAMPLE_ROWS);
        if (values.isEmpty()) {
            return true;
        }
        String needle = searchTerm.trim().toLowerCase(Locale.US);
        for (String value : values) {
            if (value == null || !value.toLowerCase(Locale.US).contains(needle)) {
                return false;
            }
        }
        return true;
    }

    public void verifySearchableTextColumn(String columnName, String columnId) throws InterruptedException {
        Verify.softAssert(isHeaderVisible(columnId, columnName),
                columnName + " header is visible on Orders grid");
        Verify.softAssert(hasColumnSearchControl(columnId, columnName),
                columnName + " header has column search control");

        String term = sampleSearchTerm(columnId, columnName);
        if (term == null) {
            if (searchColumn("a", columnName, columnId)) {
                Logger.log(columnName + " column search control verified via probe term \"a\"");
                clearColumnSearchViaSynergy(columnName);
                clearColumnSearch(columnId, columnName);
                Thread.sleep(1000);
            } else {
                Logger.log("Skip search apply — no sample value for " + columnName);
            }
            return;
        }

        int before = countVisibleOrderRows();
        Verify.softAssert(searchColumn(term, columnName, columnId),
                columnName + " column search accepts input");
        verifyColumnSearchResultsVisible(term, columnName, columnId);

        clearColumnSearchViaSynergy(columnName);
        clearColumnSearch(columnId, columnName);
        Thread.sleep(1500);
        int after = countVisibleOrderRows();
        Verify.softAssert(after >= before || before <= 1,
                columnName + " column search clears and grid restores rows");
    }

    public void verifySearchableDateColumn(String columnName, String columnId) throws InterruptedException {
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        boolean headerVisible = FulfillmentJsUtil.isColumnVisibleInGrid(columnName, effectiveId)
                || tableViewUtil.isExactOrderGridHeaderVisible(columnName);
        Verify.softAssert(headerVisible, columnName + " header is visible on Orders grid");
        boolean hasFilter = isTc022DateSearchColumn(effectiveId, columnName)
                ? verifyTc022DateFilterControl(effectiveId, columnName)
                : hasDateFilterControl(effectiveId, columnName);
        Verify.softAssert(hasFilter, columnName + " header has date/search filter control");
    }

    public void verifyNonSearchableColumn(String columnName, String columnId) {
        Verify.softAssert(isHeaderVisible(columnId, columnName),
                columnName + " header is visible on Orders grid");
        Verify.softAssert(!hasColumnSearchControl(columnId, columnName),
                columnName + " header does not expose column search control");
    }

    public void verifyNonSearchableLineItemColumn(String columnName, String columnId) throws InterruptedException {
        Verify.softAssert(!hasLineItemColumnSearchControl(columnId, columnName),
                columnName + " lineitem header does not expose column search control");
    }

    public String noMatchSearchTerm(String columnId, String columnName) {
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        List<String> values = gridSortUtil.readVisibleColumnValues(effectiveId, SAMPLE_ROWS);
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return NO_MATCH_PREFIX + value.trim();
            }
        }
        return NO_MATCH_PREFIX + effectiveId;
    }

    public boolean noVisibleValuesContain(String columnId, String columnName, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        String effectiveId = resolveEffectiveColumnId(columnId, columnName);
        List<String> values = gridSortUtil.readVisibleColumnValues(effectiveId, SAMPLE_ROWS);
        if (values.isEmpty()) {
            return true;
        }
        String needle = searchTerm.trim().toLowerCase(Locale.US);
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.US).contains(needle)) {
                return false;
            }
        }
        return true;
    }

    public void verifyNegativeTextColumnSearch(String columnName, String columnId) throws InterruptedException {
        Verify.softAssert(hasColumnSearchControl(columnId, columnName),
                columnName + " header has column search control");
        int before = countVisibleOrderRows();
        if (before == 0) {
            Logger.log("Skip negative search — no order rows on grid for " + columnName);
            return;
        }
        String term = noMatchSearchTerm(columnId, columnName);
        Verify.softAssert(applyColumnSearch(columnId, columnName, term),
                columnName + " column search accepts no-match input");
        Thread.sleep(2000);
        int after = countVisibleOrderRows();
        Verify.softAssert(after == 0 || after < before,
                columnName + " no-match search \"" + term + "\" filters grid (before="
                        + before + ", after=" + after + ")");
        Verify.softAssert(noVisibleValuesContain(columnId, columnName, term) || after == 0,
                columnName + " no visible row contains no-match term");
        clearColumnSearch(columnId, columnName);
        Thread.sleep(1500);
        int restored = countVisibleOrderRows();
        Verify.softAssert(restored >= before || before <= 1,
                columnName + " grid restores rows after clearing no-match search");
    }

    public void verifyNegativeDateColumnSearch(String columnName, String columnId) throws InterruptedException {
        Verify.softAssert(hasDateFilterControl(columnId, columnName)
                        || hasColumnSearchControl(columnId, columnName),
                columnName + " header has date/search filter control");
        int before = countVisibleOrderRows();
        if (before == 0) {
            Logger.log("Skip negative date search — no order rows on grid for " + columnName);
            return;
        }
        String invalidDate = "99/99/2099";
        Verify.softAssert(applyColumnSearch(columnId, columnName, invalidDate),
                columnName + " date filter accepts invalid/no-match date input");
        Thread.sleep(2000);
        int after = countVisibleOrderRows();
        Verify.softAssert(after == 0 || after < before,
                columnName + " invalid/no-match date filter reduces or clears visible rows (before="
                        + before + ", after=" + after + ")");
        clearColumnSearch(columnId, columnName);
        Thread.sleep(1500);
        int restored = countVisibleOrderRows();
        Verify.softAssert(restored >= before || before <= 1,
                columnName + " grid restores rows after clearing invalid date filter");
    }

    private String buildColumnProbeScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function labelMatches(text,cand){"
                + "  if(!text||!cand)return false;"
                + "  if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + EXACT_LABEL_MATCH_GUARD_JS
                + "    return true;"
                + "  }"
                + "  if(text.indexOf('...')>=0||text.indexOf('\\u2026')>=0){"
                + "    var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "    if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function matches(cell){"
                + "  var text=cellText(cell);"
                + "  var cid=normId(cell.getAttribute('col-id'));"
                + "  for(var a=0;a<idAliases.length;a++){"
                + "    var alias=idAliases[a];"
                + "    if(!alias||!cid)continue;"
                + "    if(cid===alias||cid.indexOf(alias)>=0||alias.indexOf(cid)>=0)return true;"
                + "  }"
                + "  for(var c=0;c<labels.length;c++){"
                + "    if(labelMatches(text,labels[c]))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function headerHasSearchUi(cell){"
                + "  if(!cell)return false;"
                + "  var html=(cell.innerHTML||'').toLowerCase();"
                + "  if(html.indexOf('search-icon')>=0||html.indexOf('search')>=0)return true;"
                + "  if(html.indexOf('filter')>=0&&!html.match(/ag-sort|sort-indicator/))return true;"
                + "  var sel='input,.search-icon,[class*=search],[class*=Search],[class*=filter],[class*=Filter],"
                + "    mat-icon,[role=button],[aria-label*=search],[aria-label*=filter]';"
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
                + "  var colIdAttr=cell.getAttribute('col-id');"
                + "  if(colIdAttr){"
                + "    var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+colIdAttr+'\"]');"
                + "    if(floatCell&&floatCell.querySelector(sel))return true;"
                + "  }"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  if(idx){"
                + "    var floatByIdx=document.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                + "    if(floatByIdx&&floatByIdx.querySelector(sel))return true;"
                + "  }"
                + "  var iconsAll=ordersGridRoot().querySelectorAll("
                + "'.search-icon,[class*=search-icon],mat-icon,svg');"
                + "  if(iconsAll.length){"
                + "    var rect=cell.getBoundingClientRect();"
                + "    for(var q=0;q<iconsAll.length;q++){"
                + "      var ir=iconsAll[q].getBoundingClientRect();"
                + "      if(Math.abs(ir.top-rect.top)<45&&ir.left>=rect.left-25&&ir.left<=rect.right+90)return true;"
                + "    }"
                + "  }"
                + "  return false;"
                + "}"
                + "function readSample(colIdAttr,headerCell){"
                + "  if(!colIdAttr&&!headerCell)return null;"
                + "  var idx=headerCell?headerCell.getAttribute('aria-colindex'):null;"
                + "  var root=ordersGridRoot();"
                + "  var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                + "    '.ag-pinned-right-cols-container'];"
                + "  for(var ci=0;ci<containers.length;ci++){"
                + "    var rows=root.querySelectorAll(containers[ci]+' > .ag-row');"
                + "    for(var ri=0;ri<rows.length&&ri<12;ri++){"
                + "      var row=rows[ri];"
                + "      if(row.classList.contains('ag-row-level-1'))continue;"
                + "      var cell=colIdAttr?row.querySelector('[col-id=\"'+colIdAttr+'\"]'):null;"
                + "      if(!cell&&idx)cell=row.querySelector('[aria-colindex=\"'+idx+'\"]');"
                + "      if(!cell)continue;"
                + "      var v=(cell.innerText||cell.textContent||'').replace(/\\s+/g,' ').trim();"
                + "      if(v)return v;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "for(var pass=0;pass<12;pass++){"
                + "  scrollHeaders(pass===0?0:450);"
                + "  var cells=headerCells();"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(!matches(cells[i]))continue;"
                + "    var resolved=cells[i].getAttribute('col-id')||colId;"
                + "    var sample=readSample(resolved,cells[i]);"
                + "    return [resolved,headerHasSearchUi(cells[i]),sample||''];"
                + "  }"
                + "}"
                + "return [colId,false,''];";
    }

    private String buildResolveColIdScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "for(var pass=0;pass<12;pass++){"
                + "  scrollHeaders(pass===0?0:450);"
                + "  var cells=headerCells();"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(matches(cells[i])){"
                + "      return cells[i].getAttribute('col-id')||colId;"
                + "    }"
                + "  }"
                + "}"
                + "return colId;";
    }

    private String buildProbeSearchInputScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function headerHasInput(header){"
                + "  if(!header)return false;"
                + "  var headerColId=header.getAttribute('col-id')||colId;"
                + "  var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+headerColId+'\"]');"
                + "  if(floatCell&&floatCell.querySelector('input'))return true;"
                + "  if(header.querySelector('input'))return true;"
                + "  var searchIcon=header.querySelector('.search-icon,[class*=search-icon]');"
                + "  if(searchIcon){"
                + "    try{searchIcon.click();}catch(e){}"
                + "    if(header.querySelector('input'))return true;"
                + "    if(document.querySelector('.ag-popup-child input,.cdk-overlay-pane input,[role=dialog] input'))return true;"
                + "  }"
                + "  var icons=header.querySelectorAll('svg,img,button,i,span,mat-icon,[role=button],[class*=icon]');"
                + "  for(var k=0;k<icons.length;k++){"
                + "    var el=icons[k];"
                + "    if(el.closest('.ag-sort-indicator-container'))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-menu-button'))continue;"
                + "    if(el.classList&&(el.classList.contains('ag-header-cell-text')||el.classList.contains('ag-header-cell-label')))continue;"
                + "    try{el.click();}catch(e){continue;}"
                + "    if(header.querySelector('input'))return true;"
                + "    if(document.querySelector('.ag-popup-child input,.cdk-overlay-pane input,[role=dialog] input'))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "for(var pass2=0;pass2<12;pass2++){"
                + "  scrollHeaders(pass2===0?0:450);"
                + "  var cells2=headerCells();"
                + "  for(var h=0;h<cells2.length;h++){"
                + "    if(matches(cells2[h])&&headerHasInput(cells2[h]))return true;"
                + "  }"
                + "}"
                + "return false;";
    }

    private String headerMatcherJsPrefix(String columnId, String columnName) {
        String labels = labelsArray(columnName);
        return "function norm(t){return (t||'').replace(/\\//g,', ').replace(/\\s+/g,' ').trim().toLowerCase();}"
                + "function normId(id){return (id||'').toLowerCase().replace(/[_-]/g,'');}"
                + "var colId='" + esc(columnId) + "';"
                + "var labels=" + labels + ".map(norm);"
                + "var columnId=normId(colId);"
                + "var idAliases=[];"
                + "if(columnId){idAliases.push(columnId);}"
                + "if(columnId.indexOf('titleseason')>=0){idAliases.push('title','titleseasonepisode');}"
                + "if(columnId.indexOf('assetid')>=0){idAliases.push('assetid','asset');}"
                + "if(columnId.indexOf('submittedby')>=0){idAliases.push('submittedby','submitted');}"
                + "if(columnId.indexOf('assignedto')>=0){idAliases.push('assignedto','assigned');}"
                + "function scrollHeaders(delta){"
                + "  ordersGridRoot().querySelectorAll("
                + "'.ag-header-viewport,.ag-center-cols-viewport,.ag-body-horizontal-scroll-viewport')"
                + "  .forEach(function(v){"
                + "    if(delta===0)v.scrollLeft=0;"
                + "    else v.scrollLeft=Math.max(0,(v.scrollLeft||0)+delta);"
                + "  });"
                + "}"
                + "function cellText(cell){"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  var raw=label?(label.innerText||label.textContent):(cell.innerText||cell.textContent);"
                + "  return norm(raw||cell.getAttribute('aria-label')||cell.getAttribute('title'));"
                + "}"
                + "function matches(cell){"
                + "  var text=cellText(cell);"
                + "  var cid=normId(cell.getAttribute('col-id'));"
                + "  for(var a=0;a<idAliases.length;a++){"
                + "    var alias=idAliases[a];"
                + "    if(!alias||!cid)continue;"
                + "    if(cid===alias||cid.indexOf(alias)>=0||alias.indexOf(cid)>=0)return true;"
                + "  }"
                + "  for(var c=0;c<labels.length;c++){"
                + "    var cand=labels[c];"
                + "    if(!text||!cand)continue;"
                + "    if(text===cand||text.indexOf(cand)>=0||cand.indexOf(text)>=0){"
                + "      if(cand==='partner'&&text!=='partner')continue;"
                + "      if(cand==='order id'&&text!=='order id')continue;"
                + "      if(cand==='type'&&text!=='type')continue;"
                + "      return true;"
                + "    }"
                + "    if((text.indexOf('...')>=0||text.indexOf('\\u2026')>=0)){"
                + "      var prefix=text.replace(/\\.{3,}$/,'').replace(/\\u2026$/,'').trim();"
                + "      if(prefix.length>=4&&(cand.indexOf(prefix)===0||prefix.indexOf(cand)===0))return true;"
                + "    }"
                + "  }"
                + "  return false;"
                + "}"
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
                + "function headerCells(){"
                + "  var cells=[];"
                + "  ordersGridRoot().querySelectorAll('.ag-header-cell,[role=columnheader]').forEach(function(c){"
                + "    cells.push(c);"
                + "  });"
                + "  return cells;"
                + "}";
    }

    private boolean hasColumnSearchViaBulkScript(String columnId, String columnName) {
        return truthy(runScript(buildBulkHeaderScanScript(columnId, columnName, true)));
    }

    private String sampleViaBulkScript(String columnId, String columnName) {
        Object result = runScript(buildBulkHeaderScanScript(columnId, columnName, false));
        return result == null ? null : trimToNull(String.valueOf(result));
    }

    private String buildBulkHeaderScanScript(String columnId, String columnName, boolean searchOnly) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function hasSearchUi(cell){"
                + "  if(!cell)return false;"
                + "  var html=(cell.innerHTML||'').toLowerCase();"
                + "  if(html.indexOf('search-icon')>=0||html.indexOf('search')>=0)return true;"
                + "  if(cell.querySelector('input,.search-icon,[class*=search],[class*=filter],mat-icon'))return true;"
                + "  var sort=cell.querySelector('.ag-sort-indicator-container');"
                + "  var menu=cell.querySelector('.ag-header-cell-menu-button');"
                + "  var icons=cell.querySelectorAll('svg,img,button,i,mat-icon,[role=button]');"
                + "  for(var k=0;k<icons.length;k++){"
                + "    var el=icons[k];"
                + "    if(sort&&sort.contains(el))continue;"
                + "    if(menu&&menu.contains(el))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-text'))continue;"
                + "    return true;"
                + "  }"
                + "  var root=cell.closest('.ag-root-wrapper,.ag-root')||document;"
                + "  var cid=cell.getAttribute('col-id');"
                + "  if(cid){"
                + "    var fl=root.querySelector('.ag-floating-filter-body[col-id=\"'+cid+'\"]');"
                + "    if(fl&&fl.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                + "  }"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  if(idx){"
                + "    var fl2=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                + "    if(fl2&&fl2.querySelector('input,.search-icon,[class*=search],[class*=filter]'))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function readSample(cell){"
                + "  if(!cell)return '';"
                + "  var colIdAttr=cell.getAttribute('col-id');"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  var root=cell.closest('.ag-root-wrapper,.ag-root')||document;"
                + "  var containers=['.ag-center-cols-container','.ag-pinned-left-cols-container',"
                + "    '.ag-pinned-right-cols-container'];"
                + "  for(var ci=0;ci<containers.length;ci++){"
                + "    var rows=root.querySelectorAll(containers[ci]+' > .ag-row');"
                + "    for(var ri=0;ri<rows.length&&ri<12;ri++){"
                + "      if(rows[ri].classList.contains('ag-row-level-1'))continue;"
                + "      var dataCell=colIdAttr?rows[ri].querySelector('[col-id=\"'+colIdAttr+'\"]'):null;"
                + "      if(!dataCell&&idx)dataCell=rows[ri].querySelector('[aria-colindex=\"'+idx+'\"]');"
                + "      if(!dataCell)continue;"
                + "      var v=(dataCell.innerText||dataCell.textContent||'').replace(/\\s+/g,' ').trim();"
                + "      if(v)return v;"
                + "    }"
                + "  }"
                + "  return '';"
                + "}"
                + "for(var pass=0;pass<12;pass++){"
                + "  scrollHeaders(pass===0?0:450);"
                + "  var cells=headerCells();"
                + "  for(var i=0;i<cells.length;i++){"
                + "    if(!matches(cells[i]))continue;"
                + (searchOnly
                ? "    if(hasSearchUi(cells[i]))return true;"
                : "    var sample=readSample(cells[i]);"
                        + "    if(sample)return sample;")
                + "  }"
                + "}"
                + (searchOnly ? "return false;" : "return null;");
    }

    private String buildHasSearchScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function searchSelectors(){"
                + "  return '.search-icon,[class*=search-icon],input,[class*=filter],button[class*=filter],"
                + "    .fa-search,mat-icon,svg,[aria-label*=search],[aria-label*=filter]';"
                + "}"
                + "function hasSearchInHeader(cell){"
                + "  if(!cell)return false;"
                + "  var sel=searchSelectors();"
                + "  if(cell.querySelector(sel))return true;"
                + "  var sortContainer=cell.querySelector('.ag-sort-indicator-container');"
                + "  var label=cell.querySelector('.ag-header-cell-text,.ag-header-cell-label');"
                + "  var icons=cell.querySelectorAll('svg,img,button,i,span,mat-icon,[role=button],[class*=icon]');"
                + "  var extra=0;"
                + "  for(var k=0;k<icons.length;k++){"
                + "    var el=icons[k];"
                + "    if(sortContainer&&sortContainer.contains(el))continue;"
                + "    if(label&&label.contains(el))continue;"
                + "    if(el.classList&&el.classList.contains('ag-header-cell-menu-button'))continue;"
                + "    if(el.classList&&(el.classList.contains('ag-header-cell-text')||el.classList.contains('ag-header-cell-label')))continue;"
                + "    extra++;"
                + "  }"
                + "  if(extra>=1)return true;"
                + "  var cid=cell.getAttribute('col-id')||colId;"
                + "  var root=cell.closest('.ag-root-wrapper,.ag-root')||document;"
                + "  var floatCell=root.querySelector('.ag-floating-filter-body[col-id=\"'+cid+'\"]');"
                + "  if(floatCell&&floatCell.querySelector(sel))return true;"
                + "  var idx=cell.getAttribute('aria-colindex');"
                + "  if(idx){"
                + "    var floatByIdx=root.querySelector('.ag-floating-filter-body[aria-colindex=\"'+idx+'\"]');"
                + "    if(floatByIdx&&floatByIdx.querySelector(sel))return true;"
                + "  }"
                + "  return false;"
                + "}"
                + "function findHeader(){"
                + "  for(var pass=0;pass<12;pass++){"
                + "    scrollHeaders(pass===0?0:450);"
                + "    var cells=headerCells();"
                + "    for(var i=0;i<cells.length;i++){if(matches(cells[i]))return cells[i];}"
                + "  }"
                + "  return null;"
                + "}"
                + "var header=findHeader();"
                + "if(hasSearchInHeader(header))return true;"
                + "return false;";
    }

    private String buildApplySearchScript(String columnId, String columnName, String searchTerm) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function findHeader(){"
                + "  for(var pass=0;pass<12;pass++){"
                + "    scrollHeaders(pass===0?0:450);"
                + "    var cells=headerCells();"
                + "    for(var i=0;i<cells.length;i++){if(matches(cells[i]))return cells[i];}"
                + "  }"
                + "  return null;"
                + "}"
                + "var term='" + esc(searchTerm) + "';"
                + "function findInput(){"
                + "  var header=findHeader();"
                + "  var headerColId=header?header.getAttribute('col-id'):colId;"
                + "  var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+(headerColId||colId)+'\"]');"
                + "  if(floatCell){"
                + "    var fi=floatCell.querySelector('input');"
                + "    if(fi)return fi;"
                + "  }"
                + "  var cell=header;"
                + "  if(cell){"
                + "    var ci=cell.querySelector('input');"
                + "    if(ci)return ci;"
                + "    var searchIcon=cell.querySelector('.search-icon,[class*=search-icon]');"
                + "    if(searchIcon){try{searchIcon.click();}catch(e){}"
                + "      ci=cell.querySelector('input');"
                + "      if(ci)return ci;"
                + "      var popup=document.querySelector('.ag-popup-child input,.cdk-overlay-pane input,[role=dialog] input');"
                + "      if(popup)return popup;"
                + "    }"
                + "    var icons=cell.querySelectorAll('svg,img,button,i,span,mat-icon,[role=button],[class*=icon]');"
                + "    for(var k=0;k<icons.length;k++){"
                + "      var el=icons[k];"
                + "      if(el.closest('.ag-sort-indicator-container'))continue;"
                + "      if(el.classList&&el.classList.contains('ag-header-cell-menu-button'))continue;"
                + "      if(el.classList&&(el.classList.contains('ag-header-cell-text')||el.classList.contains('ag-header-cell-label')))continue;"
                + "      try{el.click();}catch(e){continue;}"
                + "      ci=cell.querySelector('input');"
                + "      if(ci)return ci;"
                + "      var popup2=document.querySelector('.ag-popup-child input,.cdk-overlay-pane input,[role=dialog] input');"
                + "      if(popup2)return popup2;"
                + "    }"
                + "  }"
                + "  return null;"
                + "}"
                + "scrollHeaders(0);"
                + "var input=findInput();"
                + "if(!input)return false;"
                + "input.focus();"
                + "input.value=term;"
                + "input.dispatchEvent(new Event('input',{bubbles:true}));"
                + "input.dispatchEvent(new Event('change',{bubbles:true}));"
                + "input.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));"
                + "return true;";
    }

    private String buildClearSearchScript(String columnId, String columnName) {
        return headerMatcherJsPrefix(columnId, columnName)
                + "function findHeader(){"
                + "  for(var pass=0;pass<12;pass++){"
                + "    scrollHeaders(pass===0?0:450);"
                + "    var cells=headerCells();"
                + "    for(var i=0;i<cells.length;i++){if(matches(cells[i]))return cells[i];}"
                + "  }"
                + "  return null;"
                + "}"
                + "function findInput(){"
                + "  var header=findHeader();"
                + "  var headerColId=header?header.getAttribute('col-id'):colId;"
                + "  var floatCell=document.querySelector('.ag-floating-filter-body[col-id=\"'+(headerColId||colId)+'\"]');"
                + "  if(floatCell){var fi=floatCell.querySelector('input');if(fi)return fi;}"
                + "  var cell=header;"
                + "  if(cell){var ci=cell.querySelector('input');if(ci)return ci;}"
                + "  return null;"
                + "}"
                + "scrollHeaders(0);"
                + "var input=findInput();"
                + "if(!input)return false;"
                + "input.focus();"
                + "input.value='';"
                + "input.dispatchEvent(new Event('input',{bubbles:true}));"
                + "input.dispatchEvent(new Event('change',{bubbles:true}));"
                + "input.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));"
                + "return true;";
    }

    private String labelsArray(String columnName) {
        String[] labels = gridSortUtil.headerLabelCandidates(columnName);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('\'').append(esc(labels[i])).append('\'');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String esc(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static Object runScript(String script) {
        try {
            return BaseTest.driver.get().browser().executeScript(script);
        } catch (Exception e) {
            Logger.logConsoleMessage("GridColumnSearch script failed: " + e.getMessage());
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
        if (result instanceof List) {
            return !((List<?>) result).isEmpty();
        }
        return "true".equalsIgnoreCase(String.valueOf(result));
    }
}
