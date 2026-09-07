package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Locators for BSD-29441 — PTS Packaging ID column (Orders / Line Items).
 * PROD (Jul 2026): {@code th[@title='PTS Packaging ID']} with class {@code ops-console-id-cell}.
 * DEV may still use {@code pts-packaging-id-col}.
 */
public class PtsPackagingIdPage {

    public static final String COLUMN_LABEL = "PTS Packaging ID";
    /** Truncated header text shown in the Orders grid when column width is narrow. */
    public static final String COLUMN_HEADER_TRUNCATED = "PTS Packagin";
    /** PROD grid header/cell class (see th.title on #orderTable). */
    public static final String COLUMN_CSS_CLASS = "ops-console-id-cell";
    /** Legacy DEV class — kept as fallback in grid XPaths. */
    public static final String COLUMN_CSS_CLASS_LEGACY = "pts-packaging-id-col";
    public static final String DEMAND_SYSTEM_FILTER = "Demand system";
    public static final String DEMAND_SYSTEM_PTS = "PTS";
    /** Label text in Details panel Job Request section (PROD uses lowercase "packaging"). */
    public static final String DETAILS_PANEL_LABEL = "PTS packaging ID";
    public static final String EXPORT_FILE_PREFIX = "FulfillmentExport-";
    public static final String REFERENCE_ORDER_ID = "UFCAlchemy210703S";
    /** Saved table view with PTS Packaging ID enabled (Standard view has no Save changes). */
    public static final String AUTOMATION_TABLE_VIEW = "Automation";
    public static final String STANDARD_VIEW = "Standard view";

    private final TableView tableView = new TableView();

    public By tableViewDropdown() {
        return tableView.standardViewdropdownButton();
    }

    public By standardViewOption() {
        return tableView.selectStandardViewMode();
    }

    public By tableViewOption(String viewName) {
        return tableView.tableViewDropdownOption(viewName);
    }

    public By tableViewRowContextMenuButton(String viewName) {
        return tableView.tableViewRowContextMenuButton(viewName);
    }

    public By firstTableViewDropdownOptionRow() {
        return tableView.firstTableViewDropdownOptionRow();
    }

    public By firstTableViewRowContextMenuButton() {
        return tableView.firstTableViewRowContextMenuButton();
    }

    public By setTableViewAsDefaultMenuItem() {
        return tableView.setTableViewAsDefaultMenuItem();
    }

    public By saveNewViewButton() {
        return tableView.saveNewButtonTableView();
    }

    public By saveNewViewNameInput() {
        return tableView.inputTableViewName();
    }

    public By saveNewViewConfirmButton() {
        return tableView.saveButtonOnTableViewPopup();
    }

    public By tableViewCreatedToast() {
        return tableView.tableViewCreatedToast();
    }

    /** Broader toast match (same as TC_004 {@link TableView#SaveConfirmationMessage()}). */
    public By tableViewCreatedMessage() {
        return tableView.SaveConfirmationMessage();
    }

    public By selectedTableViewLabel(String viewName) {
        return tableView.tableViewDefaultSelected(viewName);
    }

    public By activeTableViewLabel(String viewName) {
        return tableView.activeTableViewInDropdown(viewName);
    }

    public By tableViewDefaultMarker(String viewName) {
        return tableView.tableViewDefaultMarker(viewName);
    }

    public By tableViewButton() {
        return tableView.getTableViewButton();
    }

    public By manageColumnsPanelHeading() {
        return By.XPath("//span[contains(normalize-space(),'Manage columns')"
                + " or contains(normalize-space(),'Manage Columns')]");
    }

    /** Line Items tab: flat list under "Manage Columns (Line item table)" — no Order/Package sections. */
    public By lineItemTableManagePanelRoot() {
        return By.XPath("//span[contains(translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'manage columns')"
                + " and contains(translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'line item')]"
                + "/ancestor::div[contains(@class,'table-view') or contains(@class,'manage-column')][1]");
    }

    public By orderColumnCheckboxLabel() {
        return tableView.OrderColumnNameOnTableView(COLUMN_LABEL);
    }

    /**
     * Line Items console tab — single flat column list (PROD: label.option-label in draggable-item).
     * Title: "Manage Columns (Line item table)" — no Order/Package/Line item sections.
     */
    public By lineItemColumnCheckboxLabel() {
        return By.XPath("(" + lineItemTableManagePanelRootXPath()
                + "//label[contains(@class,'option-label') and normalize-space()='" + COLUMN_LABEL + "'])[1]"
                + " | (" + lineItemTableManagePanelRootXPath()
                + "//label[normalize-space()='" + COLUMN_LABEL + "'])[1]"
                + " | //div[contains(@class,'options-list-scrollbar')]"
                + "//label[normalize-space()='" + COLUMN_LABEL + "']"
                + " | //div[contains(@class, 'table-column')]//div[contains(text(), 'Line item columns')]"
                + "//following::label[contains(text(), '" + COLUMN_LABEL + "')]");
    }

    /** Scoped to Order columns section — PTS Packaging ID is last in the list. */
    public By orderColumnCheckbox() {
        return By.XPath("//div[contains(@class, 'table-column')]//div[contains(text(), 'Order columns')]"
                + "//following::label[normalize-space()='" + COLUMN_LABEL + "']"
                + "/preceding-sibling::input[@type='checkbox']");
    }

    /** Line Items tab flat list, or Line item columns section when Manage columns opened from Orders tab. */
    public By lineItemColumnCheckbox() {
        return By.XPath("(" + lineItemTableManagePanelRootXPath()
                + "//label[normalize-space()='" + COLUMN_LABEL + "']"
                + "/preceding-sibling::input[@type='checkbox'])[1]"
                + " | //div[contains(@class,'options-list-scrollbar')]"
                + "//label[normalize-space()='" + COLUMN_LABEL + "']"
                + "/preceding-sibling::input[@type='checkbox']"
                + " | //div[contains(@class, 'table-column')]//div[contains(text(), 'Line item columns')]"
                + "//following::label[normalize-space()='" + COLUMN_LABEL + "']"
                + "/preceding-sibling::input[@type='checkbox']");
    }

    public By lineItemManageColumnsOptionsList() {
        return By.XPath(lineItemTableManagePanelRootXPath()
                + "//div[contains(@class,'options-list-scrollbar') or contains(@class,'cdk-drop-list')]"
                + " | //div[contains(@class,'options-list-scrollbar')]");
    }

    /** Scrollable CDK drop list for Order columns (PROD: cdk-drop-list options-list-scrollbar). */
    public By orderColumnsListContainer() {
        return orderColumnsDropList();
    }

    public By saveChangesButtonInPanel() {
        String saveChangesBtn = "//button[contains(@class,'save-button') and not(@disabled)]"
                + "[.//span[normalize-space()='Save changes']]";
        return By.XPath("//msc-custom-wrapper-dropdown[contains(@class,'lineitem-column-selector')]"
                + saveChangesBtn
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//msc-custom-wrapper-dropdown[contains(@class,'lineitem-column-selector')]"
                + saveChangesBtn
                + " | //div[contains(@class,'btn-container')]" + saveChangesBtn
                + " | //span[contains(normalize-space(),'Manage columns')]"
                + "/ancestor::div[contains(@class,'table-view') or contains(@class,'manage-column')][1]"
                + saveChangesBtn
                + " | //div[contains(@class,'manage-column') or contains(@class,'table-view')]"
                + saveChangesBtn);
    }

    public By saveChangesConfirmDialog() {
        return saveChangesConfirmDialogAny();
    }

    public By saveChangesConfirmDialogAny() {
        return By.XPath("//div[contains(@class,'modal-content')]"
                + "[.//*[contains(normalize-space(),'Added Columns')"
                + " or contains(normalize-space(),'Removed Columns')"
                + " or contains(normalize-space(),'save the following changes')]]");
    }

    public By saveChangesConfirmButton() {
        return By.XPath("//div[contains(@class,'modal-content')]"
                + "[.//*[contains(normalize-space(),'Added Columns')"
                + " or contains(normalize-space(),'Removed Columns')"
                + " or contains(normalize-space(),'save the following changes')]]"
                + "//button[normalize-space()='Save changes']");
    }

    /** Save-changes modal bullet listing a newly added column (e.g. Added Columns: "Brand"). */
    public By saveChangesConfirmAddedColumn(String columnLabel) {
        return By.XPath("//div[contains(@class,'modal-content')]"
                + "[.//*[contains(normalize-space(),'Added Columns')]]"
                + "//*[contains(normalize-space(),'Added Columns')]"
                + "/following::*[contains(normalize-space(),'" + columnLabel + "')][1]"
                + " | //div[contains(@class,'modal-content')]"
                + "[.//*[contains(normalize-space(),'Added Columns')]]"
                + "//*[contains(normalize-space(),'Added Columns') and contains(normalize-space(),'"
                + columnLabel + "')]"
                + " | //div[contains(@class,'modal-content')]"
                + "[.//*[contains(normalize-space(),'Added Columns')]]"
                + "//li[contains(normalize-space(),'" + columnLabel + "')]"
                + " | //ngb-modal-window//ul//li[contains(normalize-space(),'" + columnLabel + "')]");
    }

    public By firstGridRow() {
        return By.XPath("//tbody//tr[contains(@class,'row')][1]");
    }

    /** PROD DOM: div.draggable-item > a[cdkdrag].option-item > i[cdkdraghandle].cdk-drag-handle */
    public By orderColumnDragHandle(String columnLabel) {
        return By.XPath(orderColumnsSectionRoot()
                + "//label[contains(normalize-space(),'" + columnLabel + "')]"
                + "/ancestor::a[@cdkdrag][1]"
                + "//i[@cdkdraghandle or contains(@class,'cdk-drag-handle')]");
    }

    public By lineItemColumnDragHandle(String columnLabel) {
        return By.XPath(lineItemColumnsSectionRoot()
                + "//label[contains(normalize-space(),'" + columnLabel + "')]"
                + "/ancestor::a[@cdkdrag][1]"
                + "//i[@cdkdraghandle or contains(@class,'cdk-drag-handle')]");
    }

    /** First draggable row anchor in the Order columns drop list. */
    public By firstOrderColumnDragRow() {
        return By.XPath(orderColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')][1]//a[@cdkdrag]");
    }

    public By firstLineItemColumnDragRow() {
        return By.XPath(lineItemColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')][1]//a[@cdkdrag]");
    }

    /** Draggable row immediately above the named column (for step-up CDK drag). */
    public By orderColumnRowAbove(String columnLabel) {
        return By.XPath(orderColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')]"
                + "[.//label[contains(normalize-space(),'" + columnLabel + "')]]"
                + "/preceding-sibling::div[contains(@class,'draggable-item')][1]");
    }

    public By lineItemColumnRowAbove(String columnLabel) {
        return By.XPath(lineItemColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')]"
                + "[.//label[contains(normalize-space(),'" + columnLabel + "')]]"
                + "/preceding-sibling::div[contains(@class,'draggable-item')][1]");
    }

    /** Drag handle on the row immediately above PTS (fallback drop target). */
    public By orderColumnRowAboveDragHandle(String columnLabel) {
        return By.XPath(orderColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')]"
                + "[.//label[contains(normalize-space(),'" + columnLabel + "')]]"
                + "/preceding-sibling::div[contains(@class,'draggable-item')][1]"
                + "//i[@cdkdraghandle or contains(@class,'cdk-drag-handle')]");
    }

    public By lineItemColumnRowAboveDragHandle(String columnLabel) {
        return By.XPath(lineItemColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]"
                + "//div[contains(@class,'draggable-item')]"
                + "[.//label[contains(normalize-space(),'" + columnLabel + "')]]"
                + "/preceding-sibling::div[contains(@class,'draggable-item')][1]"
                + "//i[@cdkdraghandle or contains(@class,'cdk-drag-handle')]");
    }

    /** CDK drop list container for Order columns (vertical drag, Y-axis locked). */
    public By orderColumnsDropList() {
        return By.XPath(orderColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]");
    }

    public By lineItemColumnsDropList() {
        return By.XPath(lineItemColumnsSectionRoot()
                + "//div[@cdkdroplist or contains(@class,'cdk-drop-list')]");
    }

    private String orderColumnsSectionRoot() {
        return "//div[contains(@class, 'table-column')][.//div[contains(text(), 'Order columns')]]";
    }

    private String lineItemColumnsSectionRoot() {
        return lineItemTableManagePanelRootXPath()
                + " | //div[contains(@class, 'table-column')][.//div[contains(text(), 'Line item columns')]]";
    }

    private String lineItemTableManagePanelRootXPath() {
        return "//span[contains(translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'manage columns')"
                + " and contains(translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'line item')]"
                + "/ancestor::div[contains(@class,'table-view') or contains(@class,'manage-column')][1]";
    }

    /**
     * PROD DOM (DevTools): {@code th[@title='PTS Packaging ID']} with class {@code ops-console-id-cell}.
     * Never match {@code ops-console-id-cell} alone — Asset ID uses the same class.
     */
    private String ptsGridHeaderXPath() {
        return "//th[@title='" + COLUMN_LABEL + "']"
                + " | //table[@id='orderTable']//th[@title='" + COLUMN_LABEL + "']"
                + " | //th[contains(@title,'PTS') and contains(@title,'Packag')]"
                + " | //th[contains(@class,'" + COLUMN_CSS_CLASS_LEGACY + "')]";
    }

    /** TD cells in PTS column — index from document-wide PTS header (sticky thead may sit outside #orderTable). */
    private String ptsGridCellXPath() {
        return "//table[@id='orderTable']//tbody//tr//td[count((//th[@title='" + COLUMN_LABEL + "'])[1]"
                + "/preceding-sibling::th)+1]"
                + " | //tbody//tr[contains(@class,'row')]//td[count((//th[@title='" + COLUMN_LABEL + "'])[1]"
                + "/preceding-sibling::th)+1]"
                + " | //tbody//tr[contains(@class,'row')]//td[contains(@class,'" + COLUMN_CSS_CLASS_LEGACY + "')]";
    }

    public By gridColumnHeader() {
        return By.XPath(ptsGridHeaderXPath());
    }

    public By gridColumnHeaderLabel() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//span[normalize-space()='" + COLUMN_LABEL + "']"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'label-ellipsis')]//span"
                + " | //th[contains(@title,'PTS') and contains(@title,'Packag')]"
                + "//span[contains(normalize-space(),'Packag')]"
                + " | (" + ptsGridHeaderXPath() + ")[1]");
    }

    /** Truncated header label visible after horizontal scroll (PROD shows "PTS Packagin..."). */
    public By gridColumnHeaderTruncated() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']"
                + " | //th[contains(@title,'PTS') and contains(@title,'Packag')]"
                + " | //th//span[contains(normalize-space(),'" + COLUMN_HEADER_TRUNCATED + "')]/ancestor::th[1]");
    }

    public By gridColumnCells() {
        return By.XPath(ptsGridCellXPath());
    }

    /** Copy-to-clipboard control beside each PTS Packaging ID cell (PROD: msc-copy-text-to-clipboard-chip). */
    public By gridColumnCopyIcons() {
        String ptsTd = ptsGridCellXPath();
        return By.XPath(ptsTd + "//button[contains(@class,'copy-button-wrapper')]"
                + " | " + ptsTd + "//msc-copy-text-to-clipboard-chip//button"
                + " | " + ptsTd + "//i[contains(@class,'bi-files')]"
                + " | " + ptsTd + "//*[contains(@class,'copy') or @cdkcopytoclipboard]");
    }

    public By gridColumnFirstCopyIcon() {
        return By.XPath("(" + ptsGridCellXPath()
                + "//button[contains(@class,'copy-button-wrapper')]"
                + " | " + ptsGridCellXPath() + "//msc-copy-text-to-clipboard-chip//button"
                + " | " + ptsGridCellXPath() + "//i[contains(@class,'bi-files')])[1]");
    }

    /** Magnifying glass on PTS column header — reveals in-column search input. */
    public By gridColumnSearchIcon() {
        return By.XPath("(" + ptsGridHeaderXPath() + ")//i[contains(@class,'bi-search')]"
                + " | (" + ptsGridHeaderXPath() + ")//*[contains(@class,'search-icon')]"
                + " | //th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//i[contains(@class,'search')]");
    }

    public By gridColumnSearchInput() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//input[@placeholder='Search...' or @type='search' or @type='text']"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'input-box')]//input"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'label-wrapper')]//input");
    }

    /** Click label area on PTS header to reveal in-column search (PROD: div.label-wrapper.input-focus). */
    public By gridColumnHeaderLabelWrapper() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'label-wrapper')]"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'label-ellipsis')]");
    }

    public By gridColumnSortableHeader() {
        return By.XPath(ptsGridHeaderXPath());
    }

    /** PROD: div.sort-icon-up / div.sort-icon-down inside msc-custom-table-column-filter on PTS header. */
    public By gridColumnSortAscIcon() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//div[contains(@class,'sort-icon-up')]"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'sort-icon-up')]"
                + "//i[contains(@class,'bi-caret-up-fill')]");
    }

    public By gridColumnSortDescIcon() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//div[contains(@class,'sort-icon-down')]"
                + " | //th[@title='" + COLUMN_LABEL + "']//div[contains(@class,'sort-icon-down')]"
                + "//i[contains(@class,'bi-caret-down-fill')]");
    }

    /** Fallback when sort icons are not clickable — label area in PTS column filter. */
    public By gridColumnSortTrigger() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//div[contains(@class,'label-and-sort-container')]"
                + " | //th[@title='" + COLUMN_LABEL + "']//msc-custom-table-column-filter"
                + "//div[contains(@class,'sort-icon-container')]"
                + " | //th[@title='" + COLUMN_LABEL + "'][1]");
    }

    /** PROD: #orderTable inside msc-custom-table / .custom-table-wrapper (horizontal scrollbar at bottom). */
    public By ordersGridTable() {
        return By.XPath("//table[@id='orderTable'] | //table[contains(@class,'fixed-header')][.//th]");
    }

    public By ordersGridHorizontalScrollContainer() {
        return By.XPath("//app-fulfillment-main-table-container//div[contains(@class,'custom-table-wrapper')]"
                + " | //app-fulfillment-main-table-container//div[contains(@class,'table-container')]"
                + " | //msc-custom-table//div[contains(@class,'custom-table-wrapper')]"
                + " | //table[@id='orderTable']/ancestor::div[contains(@class,'wrapper') or contains(@class,'container')][1]");
    }

    /** Horizontal scroll viewport for Orders / Line Items grid when columns extend past viewport. */
    public By gridHorizontalScrollViewport() {
        return By.XPath("//app-fulfillment-main-table-container//div[contains(@class,'custom-table-wrapper')]"
                + " | //app-fulfillment-main-table-container//div[contains(@class,'table-container')]"
                + " | //div[contains(@class,'bs-custom-table-viewport')]"
                + " | //table[@id='orderTable']/ancestor::div[contains(@class,'custom-table') or contains(@class,'table')][1]"
                + " | //app-fulfillment-main-table-container//cdk-virtual-scroll-viewport");
    }

    public By globalSearchInput() {
        return By.XPath("//input[@placeholder='Search']");
    }

    public By detailsButton() {
        return By.XPath("//button[normalize-space()='Details']");
    }

    /** Click first grid row — opens Details side panel on PROD. */
    public By gridFirstRowClickTarget() {
        return By.XPath("//tbody//tr[contains(@class,'row')][1]"
                + " | //table[@id='orderTable']//tbody//tr[1]");
    }

    public By detailsPanelJobRequestSection() {
        return By.XPath("//div[contains(@class,'section-label') and normalize-space()='Job Request']");
    }

    public By detailsPanelPtsPackagingIdLabel() {
        return By.XPath("//div[contains(@class,'label') and (normalize-space()='" + DETAILS_PANEL_LABEL + "'"
                + " or normalize-space()='" + COLUMN_LABEL + "')]");
    }

    public By detailsPanelPtsPackagingIdValue() {
        return By.XPath("//div[contains(@class,'label') and (normalize-space()='" + DETAILS_PANEL_LABEL + "'"
                + " or normalize-space()='" + COLUMN_LABEL + "')]"
                + "/following-sibling::div[contains(@class,'value')]"
                + "//span[contains(@class,'demandSystemId-value')]//span"
                + " | //div[contains(@class,'label') and (normalize-space()='" + DETAILS_PANEL_LABEL + "'"
                + " or normalize-space()='" + COLUMN_LABEL + "')]"
                + "/following-sibling::div[contains(@class,'value')]"
                + "//span[contains(@class,'demandSystemId-value')]");
    }

    public By detailsPanelPtsCopyButton() {
        return By.XPath("//div[contains(@class,'label') and contains(normalize-space(),'PTS packaging')]"
                + "/following-sibling::div[contains(@class,'value')]"
                + "//button[contains(@class,'copy-button-wrapper')]");
    }

    /** All data rows in Orders grid. */
    public By gridDataRows() {
        return By.XPath("//tbody//tr[contains(@class,'row')]"
                + " | //table[@id='orderTable']//tbody//tr");
    }

    public By orderIdCell(String orderId) {
        return By.XPath("//tbody//tr[contains(@class,'row')]"
                + "[.//td[contains(@class,'order-id-col')]//*[contains(normalize-space(),'" + orderId + "')]]");
    }
}
