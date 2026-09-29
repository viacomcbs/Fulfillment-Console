package com.paramount.test.ff.pageobjects;

import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.synergy.core.driver.By;

/**
 * Manage Columns / Table View locators for Fulfillment Console Orders tab.
 * XPath patterns aligned with PROD UI ({@code https://operationsconsole.paramountmsc.com/fulfillment/})
 * and Playwright reference page objects in fulfillment-console-tests.
 */
public class TableView {

    private static String ciContains(String text) {
        return "contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + text.toLowerCase() + "')";
    }

    private static String standardViewTextMatch() {
        return ciContains("standard view");
    }

    private static String manageColumnsTextMatch() {
        return ciContains("manage columns");
    }

    /**
     * Manage Columns panel — PROD uses cdk-overlay-pane + wrapper-dropdown-container.
     */
    private static String manageColumnsContext() {
        return "(//msc-custom-wrapper-dropdown/ancestor::div[contains(@class,'cdk-overlay-pane')][1]"
                + " | //div[contains(@class,'cdk-overlay-pane')][.//msc-custom-wrapper-dropdown]"
                + " | //div[contains(@class,'wrapper-dropdown-container')][.//*[" + manageColumnsTextMatch() + "]]"
                + " | //span[contains(@class,'wrapper-dropdown-title')][" + manageColumnsTextMatch() + "]"
                + "/ancestor::div[contains(@class,'wrapper-dropdown-container') or contains(@class,'cdk-overlay-pane')][1]"
                + " | //*[" + manageColumnsTextMatch() + "]/ancestor::div[contains(@class,'cdk-overlay-pane')][1]"
                + " | //*[" + manageColumnsTextMatch() + " and " + ciContains("order") + "]"
                + "/ancestor::div[contains(@class,'wrapper') or contains(@class,'dropdown') or contains(@class,'overlay')][1]"
                + " | //div[contains(@class,'multi-table-column-container')]/ancestor::div[.//*[" + manageColumnsTextMatch() + "]][1]"
                + " | //*[" + manageColumnsTextMatch() + "]/ancestor::div[.//div[contains(@class,'multi-table-column-container')]][1])";
    }

    /** Escape dynamic text for XPath string literals (view names, column labels). */
    public static String escapeXPathLiteral(String value) {
        if (value == null || value.isEmpty()) {
            return "''";
        }
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        String[] parts = value.split("'", -1);
        StringBuilder concat = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                concat.append(", \"'\", ");
            }
            concat.append("'").append(parts[i].replace("\\", "\\\\")).append("'");
        }
        concat.append(")");
        return concat.toString();
    }

    public By getTableViewButton() {
        return By.XPath("//button[@id='tableViewButton']");
    }

    /** View selector inside Manage Columns panel (Scriptless TC044: #dropdownMenuButton). */
    public By dropdownMenuButton() {
        return By.ID("dropdownMenuButton");
    }

    /** View selector / team view dropdown at top of Manage Columns panel (PROD). */
    public By viewSelectorInPanel() {
        return viewDropdownResetInPanel();
    }

    /** Playwright parity: current-view dropdown trigger inside Manage Columns panel. */
    public By viewDropdownResetInPanel() {
        return By.XPath(manageColumnsContext()
                + "//div[contains(@class,'dropdown-reset-container')]");
    }

    /** Team View row — opens nested saved-view list when needed. */
    public By teamViewFilterInPanel() {
        return By.XPath(manageColumnsContext()
                + "//div[contains(@class,'option') and contains(@class,'team-filter')]");
    }

    public By standardViewdropdownButton() {
        return viewSelectorInPanel();
    }

    public By manageColumnHeader() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane') or contains(@class,'wrapper-dropdown-container')]"
                + "//*[" + manageColumnsTextMatch() + "]"
                + " | //span[contains(@class,'wrapper-dropdown-title')][" + manageColumnsTextMatch() + "]");
    }

    public By orderTableHeader() {
        return By.XPath("//span[contains(@class,'wrapper-dropdown-title')][contains(normalize-space(),'Order table')]"
                + " | //*[" + ciContains("order table") + "]"
                + " | //div[contains(@class,'table-column-name')][" + ciContains("order columns") + "]");
    }

    /** Line Items tab trigger above the main grid. */
    public By lineItemsTab() {
        return By.XPath("//button[normalize-space()='Line items' or normalize-space()='Line Items']"
                + " | //a[normalize-space()='Line items' or normalize-space()='Line Items']"
                + " | //*[@role='tab'][normalize-space()='Line items' or normalize-space()='Line Items']"
                + " | //span[normalize-space()='Line items' or normalize-space()='Line Items']"
                + "/ancestor::button[1] | //span[normalize-space()='Line items' or normalize-space()='Line Items']"
                + "/ancestor::a[1]");
    }

    public By lineItemTableManageColumnsHeader() {
        return By.XPath("//span[contains(@class,'wrapper-dropdown-title')][contains(normalize-space(),'Line item table')]"
                + " | //*[" + manageColumnsTextMatch() + " and " + ciContains("line item table") + "]");
    }

    /** @deprecated Prefer {@link #manageColumnHeader()} plus {@link #orderTableHeader()} */
    public By manageColumnOpenOrderTab() {
        return manageColumnHeader();
    }

    public By manageColumnsOrderTablePanel() {
        return manageColumnHeader();
    }

    public By manageColumnsPanel() {
        return By.XPath(manageColumnsContext());
    }

    public By manageColumnsScrollContainer() {
        return By.XPath(manageColumnsContext()
                + "//msc-custom-wrapper-dropdown//div[contains(@class,'multi-table-column-container')]"
                + "//div[contains(@class,'multi-options-list') or contains(@style,'overflow')]"
                + " | " + manageColumnsContext()
                + "//div[contains(@class,'multi-table-column-container') or contains(@class,'multi-options-list')]"
                + " | " + manageColumnsContext()
                + "//div[contains(@class,'scroll') or contains(@class,'column-list')"
                + " or contains(@class,'column-item') or contains(@style,'overflow')]");
    }

    public By orderTableColumnLabel(String columnLabel) {
        return manageColumnLabel(columnLabel);
    }

    public By orderTableColumnLabelExact(String columnLabel) {
        return manageColumnLabel(columnLabel);
    }

    /** Manage Columns label — Scriptless: //label[contains(text(),'Column name')] */
    public By manageColumnLabel(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        return By.XPath("//label[contains(text()," + lit + ")]");
    }

    public By orderTableColumnCheckbox(String columnLabel) {
        return manageColumnCheckbox(columnLabel);
    }

    /** Manage Columns checkbox — supports label/preceding-sibling and option-container layouts on PROD. */
    public By manageColumnCheckbox(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        String ctx = manageColumnsContext();
        return By.XPath(ctx + "//label[normalize-space()=" + lit + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[contains(text()," + lit + ")]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + lit
                + "]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']"
                + " | " + ctx + "//label[contains(text()," + lit
                + ")]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']"
                + " | //label[normalize-space()=" + lit
                + "]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']");
    }

    /** Combined {@code Title, Season, Episode} Manage columns label (Automation view layout A). */
    public By titleSeasonEpisodeCombinedManageColumnLabel() {
        return manageColumnLabel(ManageColumnOptions.TITLE_SEASON_EPISODE);
    }

    /** Combined {@code Title, Season, Episode} Manage columns checkbox (Automation view layout A). */
    public By titleSeasonEpisodeCombinedManageColumnCheckbox() {
        return manageColumnCheckbox(ManageColumnOptions.TITLE_SEASON_EPISODE);
    }

    /**
     * Union label for Title/Season/Episode Manage columns — matches combined label or split Season/Episode
     * rows so batch setup works on either PROD Automation view layout.
     */
    public By titleSeasonEpisodeManageColumnLabel() {
        String ctx = manageColumnsContext();
        String combined = escapeXPathLiteral(ManageColumnOptions.TITLE_SEASON_EPISODE);
        String season = escapeXPathLiteral(ManageColumnOptions.SEASON);
        String episode = escapeXPathLiteral(ManageColumnOptions.EPISODE);
        return By.XPath(ctx + "//label[normalize-space()=" + combined + "]"
                + " | " + ctx + "//label[contains(normalize-space(.)," + combined + ")]"
                + " | " + ctx + "//label[normalize-space()=" + season + "]"
                + " | " + ctx + "//label[normalize-space()=" + episode + "]");
    }

    /** Union checkbox for {@link #titleSeasonEpisodeManageColumnLabel()}. */
    public By titleSeasonEpisodeManageColumnCheckbox() {
        String ctx = manageColumnsContext();
        String combined = escapeXPathLiteral(ManageColumnOptions.TITLE_SEASON_EPISODE);
        String season = escapeXPathLiteral(ManageColumnOptions.SEASON);
        String episode = escapeXPathLiteral(ManageColumnOptions.EPISODE);
        return By.XPath(ctx + "//label[normalize-space()=" + combined + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[contains(normalize-space(.)," + combined
                + ")]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + combined
                + "]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + season
                + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + season
                + "]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + episode
                + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + ctx + "//label[normalize-space()=" + episode
                + "]/ancestor::div[contains(@class,'option-container')][1]//input[@type='checkbox']");
    }

    /** Manage Columns checkbox by column id (e.g. orderTableorderStartDate from Scriptless TC044). */
    public By orderTableColumnCheckboxById(String columnId) {
        String id = columnId == null ? "" : columnId;
        String fullId = "orderTable" + id;
        String idLit = escapeXPathLiteral(fullId);
        String containsLit = escapeXPathLiteral(id);
        return By.XPath(manageColumnsContext()
                + "//input[@type='checkbox' and @id=" + idLit + "]"
                + " | " + manageColumnsContext()
                + "//input[@type='checkbox' and contains(@id," + containsLit + ")]"
                + " | //input[@type='checkbox' and @id=" + idLit + "]");
    }

    /** Column filter/search input in grid header (e.g. #orderTabledeliveryOffset). */
    public By orderGridColumnFilterInput(String columnId) {
        return By.ID("orderTable" + columnId);
    }

    private static String standardViewLabelXPath() {
        return "//div[contains(@class,'cdk-overlay-pane') or contains(@class,'wrapper-dropdown-container')]"
                + "//label[contains(@class,'form-check-label')][" + standardViewTextMatch() + "]"
                + " | " + manageColumnsContext()
                + "//label[contains(@class,'form-check-label')][" + standardViewTextMatch() + "]"
                + " | " + manageColumnsContext()
                + "//label[" + standardViewTextMatch() + "]";
    }

    /** PROD: Standard view label with (Default) suffix. */
    public By standardViewLabelOption() {
        return By.XPath(standardViewLabelXPath());
    }

    /** Standard View option in the open view-selector dropdown. */
    public By selectStandardViewMode() {
        return By.XPath(standardViewLabelXPath()
                + " | " + manageColumnsContext()
                + "//div[contains(@class,'option')][" + standardViewTextMatch() + "]"
                + " | //div[contains(@class,'dropdown-menu')]//label[" + standardViewTextMatch() + "]"
                + " | //span[" + standardViewTextMatch() + "]");
    }

    /** Standard View row — requires visible Standard view text (avoids wrong team-filter option). */
    public By standardViewTeamFilterOption() {
        return By.XPath(manageColumnsContext()
                + "//div[contains(@class,'option') and contains(@class,'team-filter')]"
                + "[.//*[" + standardViewTextMatch() + "] or " + standardViewTextMatch() + "]"
                + " | //div[contains(@class,'dropdown-menu')]//div[contains(@class,'team-filter')]"
                + "[.//*[" + standardViewTextMatch() + "] or " + standardViewTextMatch() + "]"
                + " | //div[contains(@class,'cdk-overlay-pane')]//div[contains(@class,'option')]"
                + "[.//*[" + standardViewTextMatch() + "] or " + standardViewTextMatch() + "]"
                + " | //div[contains(@class,'option') and contains(@class,'team-filter')]"
                + "[.//*[" + standardViewTextMatch() + "] or " + standardViewTextMatch() + "]");
    }

    public By StandardViewMode() {
        return By.XPath(standardViewLabelXPath()
                + " | " + manageColumnsContext()
                + "//div[contains(@class,'dropdown-reset-container')]//*[" + standardViewTextMatch() + "]"
                + " | " + manageColumnsContext()
                + "//span[normalize-space()='Standard view']");
    }

    public By standardViewSelectedInToolbar() {
        return StandardViewMode();
    }

    /** Selected view label on the main Orders toolbar (outside Manage Columns panel). */
    public By tableViewButtonSelectedLabel() {
        return By.XPath("//button[@id='tableViewButton']//*[self::span or self::div][normalize-space()!='']"
                + " | //button[@id='tableViewButton']");
    }

    public By viewNameInPanelSelector(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath(manageColumnsContext()
                + "//div[contains(@class,'dropdown-reset-container')]//span[normalize-space()=" + lit + "]"
                + " | " + manageColumnsContext()
                + "//span[normalize-space()=" + lit + "]");
    }

    public By ngbModalWindow() {
        return By.XPath("//ngb-modal-window[contains(@class,'show')]"
                + " | //div[@role='dialog'][contains(@class,'modal')]");
    }

    public By modalDismissButton() {
        return By.XPath("//ngb-modal-window//button[contains(normalize-space(),'Cancel')]"
                + " | //ngb-modal-window//button[contains(@class,'close') or @aria-label='Close']"
                + " | //div[@role='dialog']//button[contains(normalize-space(),'Cancel')]");
    }

    public By SaveConfirmationMessage() {
        return By.XPath("//*[" + ciContains("table view created") + "]"
                + " | //*[contains(@class,'toast')][" + ciContains("created") + "]");
    }

    public By tableViewRenamedMessage() {
        return By.XPath("//*[contains(normalize-space(),'Table view renamed') or contains(normalize-space(),'View renamed')]");
    }

    public By tableViewDeletedMessage() {
        return By.XPath("//*[contains(normalize-space(),'Table view deleted') or contains(normalize-space(),'View deleted')]");
    }

    public By OrderColumnNameOnTableView(String columnLabel) {
        return manageColumnLabel(columnLabel);
    }

    public By PackageColumnNameOnTableView(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        return By.XPath(sectionColumnScope("PACKAGE COLUMNS")
                + "//label[contains(text()," + lit + ")]"
                + " | " + sectionColumnScope("PACKAGE COLUMNS")
                + "//label[normalize-space()=" + lit + "][1]");
    }

    private static String sectionColumnScope(String sectionKeyword) {
        return manageColumnsContext()
                + "//div[contains(@class,'multi-table-column-container')]"
                + "[.//div[contains(@class,'table-column-name')]"
                + "[contains(translate(normalize-space(), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), '"
                + sectionKeyword + "')]]";
    }

    public By LineitemColumnNameOnTableView(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        return By.XPath(sectionColumnScope("LINE ITEM COLUMNS")
                + "//label[contains(text()," + lit + ")]"
                + " | " + sectionColumnScope("LINE ITEM COLUMNS")
                + "//label[normalize-space()=" + lit + "][1]");
    }

    public By OrderCheckboxOnTableView(String columnLabel) {
        return orderTableColumnCheckbox(columnLabel);
    }

    public By PackageCheckboxOnTableView(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        String scope = sectionColumnScope("PACKAGE COLUMNS");
        return By.XPath(scope + "//label[normalize-space()=" + lit + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + scope + "//label[contains(text()," + lit + ")]/preceding-sibling::input[@type='checkbox']");
    }

    public By LineitemCheckboxOnTableView(String columnLabel) {
        String lit = escapeXPathLiteral(columnLabel);
        String scope = sectionColumnScope("LINE ITEM COLUMNS");
        return By.XPath(scope + "//label[normalize-space()=" + lit + "]/preceding-sibling::input[@type='checkbox']"
                + " | " + scope + "//label[contains(text()," + lit + ")]/preceding-sibling::input[@type='checkbox']");
    }

    public By packageSectionHeader() {
        return By.XPath(manageColumnsContext()
                + "//*[contains(translate(normalize-space(), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'PACKAGE COLUMNS')]");
    }

    public By lineItemSectionHeader() {
        return By.XPath(manageColumnsContext()
                + "//*[contains(translate(normalize-space(), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'LINE ITEM COLUMNS')]");
    }

    public By orderSectionHeader() {
        return By.XPath(manageColumnsContext()
                + "//div[contains(@class,'table-column-name')][" + ciContains("order columns") + " or " + ciContains("order table") + "]"
                + " | " + manageColumnsContext()
                + "//*[" + ciContains("order columns") + " or " + ciContains("order table") + "]");
    }

    /** @deprecated Use section-specific checkbox methods via TableView_utill.columnCheckbox() */
    public By CheckboxOnTableView(String columnLabel) {
        return OrderCheckboxOnTableView(columnLabel);
    }

    public By saveNewButtonTableView() {
        return By.XPath(manageColumnsContext()
                + "//button[normalize-space()='Save new view']"
                + " | " + manageColumnsContext()
                + "//*[self::button or self::span][normalize-space()='Save new view']"
                + " | " + manageColumnsContext()
                + "//button[" + ciContains("save new view") + "]");
    }

    public By saveButtonOnTableViewPopup() {
        return By.XPath("//ngb-modal-window//button[normalize-space()='Save new']"
                + " | //ngb-modal-window//button[normalize-space()='Save']"
                + " | //div[@role='dialog']//button[normalize-space()='Save new']"
                + " | //div[contains(@class,'modal')]//button[normalize-space()='Save new']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//button[.//span[normalize-space()='Save new']]"
                + " | //div[contains(@class,'cdk-overlay-pane')]//button[.//span[normalize-space()='Save']]"
                + " | //div[contains(@class,'cdk-overlay-pane')]//span[normalize-space()='Save new']"
                + " | //span[normalize-space()='Save new']");
    }

    public By saveTableViewModal() {
        return By.XPath("//ngb-modal-window[.//input[@placeholder='Enter table view name']]"
                + " | //div[@role='dialog'][.//input[@placeholder='Enter table view name']]"
                + " | //div[contains(@class,'cdk-overlay-pane')][.//input[@placeholder='Enter table view name']]");
    }

    public By inputTableViewName() {
        return By.XPath("//ngb-modal-window//input[@placeholder='Enter table view name']"
                + " | //div[@role='dialog']//input[@placeholder='Enter table view name']"
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//input[@placeholder='Enter table view name']"
                + " | //input[@placeholder='Enter table view name']");
    }

    public By renameTableViewInput() {
        return By.XPath("//ngb-modal-window//input[@placeholder='Enter table view name' or contains(@placeholder,'name')]"
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//input[@placeholder='Enter table view name' or contains(@placeholder,'name')]"
                + " | //input[@placeholder='Enter table view name' or contains(@placeholder,'name')]");
    }

    public By saveRenameButton() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//button[normalize-space()='Save' or normalize-space()='Save new']"
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//span[normalize-space()='Save' or normalize-space()='Save new']"
                + " | //span[normalize-space()='Save']");
    }

    public By tableViewDefaultSelected(String viewName) {
        return tableViewInManageColumnsPanel(viewName);
    }

    public By tableViewInManageColumnsPanel(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath(manageColumnsContext()
                + "//span[normalize-space()=" + lit + "]");
    }

    public By tableViewInDropdown(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//label[normalize-space()=" + lit + "]"
                + " | " + manageColumnsContext()
                + "//label[normalize-space()=" + lit + "]"
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//span[normalize-space()=" + lit + "]"
                + " | //div[contains(@class,'dropdown-menu')]"
                + "//span[normalize-space()=" + lit + "]"
                + " | " + manageColumnsContext()
                + "//span[normalize-space()=" + lit + "]");
    }

    public By confirmtiontableviewPopup() {
        return SaveConfirmationMessage();
    }

    public By gridColumnHeader(String columnName) {
        return gridHeaderLabel(columnName);
    }

    /** Orders grid header — scoped to main orders AG Grid (excludes Manage Columns / other panels). */
    public By gridHeaderLabel(String columnName) {
        String lit = escapeXPathLiteral(columnName);
        return By.XPath(
                "//div[contains(@class,'ag-root-wrapper') or contains(@class,'ag-root')]"
                        + "[.//div[contains(@class,'ag-center-cols-container')]]"
                        + "//div[contains(@class,'label-ellipsis')]//span[normalize-space()=" + lit + "]"
                        + " | //*[@role='grid']//*[@role='columnheader']//span[normalize-space()=" + lit + "]"
                        + " | //*[@role='grid']//*[@role='columnheader'][normalize-space()=" + lit + "]"
                        + " | //div[contains(@class,'ag-header-cell')][.//span[normalize-space()=" + lit + "]]");
    }

    /** AG Grid header cell scoped from label-ellipsis span (PROD column headers). */
    public String gridHeaderCellXPathByLabel(String columnName) {
        String lit = escapeXPathLiteral(columnName);
        String litLower = escapeXPathLiteral(columnName.toLowerCase(java.util.Locale.US));
        String label = "//div[contains(@class,'label-ellipsis')]//span[normalize-space()=" + lit
                + " or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'), " + litLower + ")]"
                + " | //*[@role='columnheader']//span[normalize-space()=" + lit
                + " or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'), " + litLower + ")]"
                + " | //*[@role='columnheader'][normalize-space()=" + lit + "]";
        return ORDERS_GRID_SCOPE + label
                + "/ancestor::*[contains(@class,'ag-header-cell') or @role='columnheader'"
                + " or contains(@class,'ag-header-group-cell')][1]"
                + " | " + ORDERS_GRID_SCOPE
                + "//div[@role='columnheader'][.//div[contains(@class,'label-ellipsis')]//span[normalize-space()="
                + lit + "]]"
                + " | " + ORDERS_GRID_SCOPE
                + "//div[contains(@class,'ag-header-cell')][.//*[normalize-space()=" + lit + "]]";
    }

    public By gridColumnHeaderByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + " | //div[@role='columnheader'][@col-id=" + lit + "]"
                + " | //*[@role='grid']//*[@role='columnheader'][@col-id=" + lit + "]");
    }

    private static final String ORDERS_GRID_SCOPE =
            "("
                    + "//div[contains(@class,'ag-root-wrapper') or contains(@class,'ag-root')]"
                    + "[.//div[contains(@class,'ag-center-cols-container')]]"
                    + " | //*[@role='grid'][.//*[@role='rowgroup']]"
                    + " | //*[@role='grid'][.//*[@role='row']]"
                    + ")";

    /** Scriptless TC045 pattern: search input id is orderTable + columnId (e.g. orderTablepartner). */
    public static String orderTableSearchInputId(String columnId) {
        if (columnId == null || columnId.trim().isEmpty()) {
            return "";
        }
        return "orderTable" + columnId.trim();
    }

    public By orderTableColumnSearchInput(String columnId) {
        String lit = escapeXPathLiteral(orderTableSearchInputId(columnId));
        return By.XPath("//*[@id=" + lit + "]");
    }

    /** MSC custom column filter component in AG Grid header (PROD/DEV). */
    public By customColumnFilterInHeaderByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed"
                + " | //div[@role='columnheader'][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter"
                + " | //div[@role='columnheader'][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed"
                + " | //*[@role='grid']//*[@role='columnheader'][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter"
                + " | //*[@role='grid']//*[@role='columnheader'][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed"
                + " | //div[contains(@class,'ag-header-cell')][contains(translate(@col-id,"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                + escapeXPathLiteral(columnId.toLowerCase(java.util.Locale.US)) + ")]"
                + "//msc-custom-table-column-filter"
                + " | //div[contains(@class,'ag-header-cell')][contains(translate(@col-id,"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                + escapeXPathLiteral(columnId.toLowerCase(java.util.Locale.US)) + ")]"
                + "//msc-custom-table-column-filter-themed");
    }

    public By customColumnFilterInHeaderByLabel(String columnName) {
        String header = gridHeaderCellXPathByLabel(columnName);
        return By.XPath(header + "//msc-custom-table-column-filter"
                + " | " + header + "//msc-custom-table-column-filter-themed");
    }

    public By customColumnFilterTriggerByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter//div/div/div[1]/div/span"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter//div/div/div[1]/div"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed//div/div/div[1]/div"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed//span");
    }

    public By customColumnFilterTriggerByLabel(String columnName) {
        String header = gridHeaderCellXPathByLabel(columnName);
        return By.XPath(header + "//msc-custom-table-column-filter//div/div/div[1]/div/span"
                + " | " + header + "//msc-custom-table-column-filter//div/div/div[1]/div"
                + " | " + header + "//msc-custom-table-column-filter-themed//div/div/div[1]/div"
                + " | " + header + "//msc-custom-table-column-filter-themed//span"
                + " | " + header + "//msc-custom-table-column-filter"
                + " | " + header + "//msc-custom-table-column-filter-themed");
    }

    /** Scriptless TC022 — Offset Delivery date: text search trigger (span in filter row). */
    public By customColumnFilterSearchTriggerByLabel(String columnName) {
        String header = gridHeaderCellXPathByLabel(columnName);
        return By.XPath(header + "//msc-custom-table-column-filter//div/div/div[1]/div/span"
                + " | " + header + "//msc-custom-table-column-filter-themed//div/div/div[1]/div/span");
    }

    public By customColumnFilterSearchTriggerByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter//div/div/div[1]/div/span"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed//div/div/div[1]/div/span");
    }

    /** Scriptless TC022 — Delivery date: calendar icon trigger (i in second filter cell). */
    public By customColumnFilterCalendarTriggerByLabel(String columnName) {
        String header = gridHeaderCellXPathByLabel(columnName);
        return By.XPath(header + "//msc-custom-table-column-filter//div/div/div[1]/div[2]/i"
                + " | " + header + "//msc-custom-table-column-filter-themed//div/div/div[1]/div[2]/i");
    }

    public By customColumnFilterCalendarTriggerByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter//div/div/div[1]/div[2]/i"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//msc-custom-table-column-filter-themed//div/div/div[1]/div[2]/i");
    }

    /**
     * TC022 two-row header: filter row cell aligned to label header via aria-colindex.
     * Label and filter live in separate ag-header-row cells with the same aria-colindex.
     */
    public String filterRowCellXPathByLabel(String columnName) {
        String labelCell = "(" + gridHeaderCellXPathByLabel(columnName) + ")[1]";
        return "//*[contains(@class,'ag-header-cell') or @role='columnheader' or contains(@class,'ag-header-group-cell')]"
                + "[@aria-colindex=" + labelCell + "/@aria-colindex]"
                + "[.//msc-custom-table-column-filter or .//msc-custom-table-column-filter-themed]";
    }

    public By customColumnFilterInFilterRowByLabel(String columnName) {
        String filterCell = filterRowCellXPathByLabel(columnName);
        return By.XPath(filterCell + "//msc-custom-table-column-filter"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed");
    }

    public By customColumnFilterSearchTriggerInFilterRowByLabel(String columnName) {
        String filterCell = filterRowCellXPathByLabel(columnName);
        return By.XPath(filterCell + "//msc-custom-table-column-filter//div/div/div[1]/div/span"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed//div/div/div[1]/div/span"
                + " | " + filterCell + "//msc-custom-table-column-filter//div/div/div[1]/div"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed//div/div/div[1]/div"
                + " | " + filterCell + "//msc-custom-table-column-filter"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed");
    }

    public By customColumnFilterCalendarTriggerInFilterRowByLabel(String columnName) {
        String filterCell = filterRowCellXPathByLabel(columnName);
        return By.XPath(filterCell + "//msc-custom-table-column-filter//div/div/div[1]/div[2]/i"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed//div/div/div[1]/div[2]/i"
                + " | " + filterCell + "//msc-custom-table-column-filter//i[contains(@class,'calendar')]"
                + " | " + filterCell + "//msc-custom-table-column-filter-themed//i");
    }

    /** Scriptless TC022 — mat-calendar dropdown opened from Delivery date filter. */
    public By calendarDropdown() {
        return By.XPath("//*[@id='calendar-dropdown']");
    }

    public By gridColumnFilterIconByColId(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//*[contains(@class,'ag-icon-filter') or contains(@class,'filter-icon')"
                + " or @data-ref='eFilterButton' or contains(@class,'search-icon')]"
                + "[not(ancestor::*[contains(@class,'ag-sort-indicator')])]");
    }

    public By gridFloatingFilterInput(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-floating-filter-body')][@col-id=" + lit + "]//input"
                + " | //div[contains(@class,'ag-floating-filter')][@col-id=" + lit + "]//input");
    }

    public By gridFloatingFilterInputByColIndex(String colIndex) {
        String lit = escapeXPathLiteral(colIndex);
        return By.XPath("//div[contains(@class,'ag-floating-filter-body')][@aria-colindex=" + lit + "]//input"
                + " | //div[contains(@class,'ag-floating-filter')][@aria-colindex=" + lit + "]//input");
    }

    public By gridFloatingFilterInputByLabel(String columnName) {
        String litLower = escapeXPathLiteral(columnName.toLowerCase());
        return By.XPath("//div[contains(@class,'ag-header-cell')][contains("
                + "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), "
                + litLower + ")]//input");
    }

    public By gridColumnSearchInput(String columnId) {
        String lit = escapeXPathLiteral(columnId);
        return By.XPath("//div[contains(@class,'ag-floating-filter-body')][@col-id=" + lit + "]//input"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]//input"
                + " | //div[contains(@class,'ag-header-cell')][@col-id=" + lit + "]"
                + "//*[contains(@class,'search') or contains(@class,'filter') or self::mat-icon"
                + "     or (self::svg and not(ancestor::*[contains(@class,'ag-sort-indicator')]))]");
    }

    public By gridColumnSearchInputByLabel(String columnName) {
        String lit = escapeXPathLiteral(columnName);
        String litLower = escapeXPathLiteral(columnName.toLowerCase());
        return By.XPath("//div[contains(@class,'ag-header-cell')][contains(normalize-space(.), " + lit + ")]//input"
                + " | //div[contains(@class,'ag-header-cell')][contains(normalize-space(.), " + lit + ")]"
                + "//*[contains(@class,'search') or contains(@class,'filter') or self::mat-icon"
                + "     or (self::svg and not(ancestor::*[contains(@class,'ag-sort-indicator')]))]"
                + " | //div[contains(@class,'ag-header-cell') or @role='columnheader']"
                + "//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'), " + litLower + ")]/ancestor::div[contains(@class,'ag-header-cell')][1]"
                + "//*[contains(@class,'search') or contains(@class,'filter') or self::mat-icon"
                + "     or (self::svg and not(ancestor::*[contains(@class,'ag-sort-indicator')]))]"
                + " | //div[contains(@class,'ag-header-cell') or @role='columnheader']"
                + "//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'), " + litLower + ")]/ancestor::div[contains(@class,'ag-header-cell')][1]"
                + "//svg[not(ancestor::*[contains(@class,'ag-sort-indicator')])"
                + " and not(ancestor::*[contains(@class,'ag-header-cell-menu-button')])]");
    }

    public By tableViewByName(String viewName) {
        return tableViewInDropdown(viewName);
    }

    public By tableViewOptionInDropdown(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//div[contains(@class,'option') or contains(@class,'dropdown') or contains(@class,'team-filter')]"
                + "//span[normalize-space()=" + lit + "]"
                + " | //div[contains(@class,'dropdown-menu')]"
                + "//span[normalize-space()=" + lit + "]");
    }

    /** Innermost saved-view row — ancestor of exact text, not outer Team View container. */
    public By tableViewOptionRow(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//span[normalize-space()=" + lit + "]/ancestor::div[contains(@class,'option')][1]"
                + " | //div[contains(@class,'cdk-overlay-pane')]"
                + "//label[normalize-space()=" + lit + "]/ancestor::div[contains(@class,'option')][1]"
                + " | " + manageColumnsContext()
                + "//span[normalize-space()=" + lit + "]/ancestor::div[contains(@class,'option')][1]"
                + " | " + manageColumnsContext()
                + "//label[normalize-space()=" + lit + "]/ancestor::div[contains(@class,'option')][1]");
    }

    public By tableViewOptionLabel(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//label[normalize-space()=" + lit + "]"
                + " | " + manageColumnsContext()
                + "//label[normalize-space()=" + lit + "]");
    }

    /** Playwright parity: exact text match for saved view in dropdown list. */
    public By tableViewExactText(String viewName) {
        String lit = escapeXPathLiteral(viewName);
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//*[normalize-space()=" + lit + "][not(self::script)][not(self::style)]"
                + " | " + manageColumnsContext()
                + "//*[normalize-space()=" + lit + "][not(self::script)][not(self::style)]");
    }

    public By renameMenuItem() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//*[@role='menuitem'][normalize-space()='Rename' or " + ciContains("rename") + "]"
                + " | //*[contains(@class,'dropdown-menu')]//*[normalize-space()='Rename']"
                + " | //*[contains(@class,'context-menu')]//*[normalize-space()='Rename']"
                + " | //div[contains(@class,'option')][normalize-space()='Rename']"
                + " | //span[normalize-space()='Rename']"
                + " | //button[normalize-space()='Rename']"
                + " | //*[@role='menu']//*[normalize-space()='Rename' or " + ciContains("rename") + "]"
                + " | //*[contains(@class,'mat-mdc-menu-item')][normalize-space()='Rename' or " + ciContains("rename") + "]");
    }

    public By deleteMenuItem() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "//*[@role='menuitem'][normalize-space()='Delete' or " + ciContains("delete") + "]"
                + " | //*[contains(@class,'dropdown-menu')]//*[normalize-space()='Delete']"
                + " | //*[contains(@class,'context-menu')]//*[normalize-space()='Delete']"
                + " | //div[contains(@class,'option')][normalize-space()='Delete']"
                + " | //span[normalize-space()='Delete']"
                + " | //button[normalize-space()='Delete']"
                + " | //*[@role='menu']//*[normalize-space()='Delete' or " + ciContains("delete") + "]"
                + " | //*[contains(@class,'mat-mdc-menu-item')][normalize-space()='Delete' or " + ciContains("delete") + "]");
    }

    public By confirmDeleteButton() {
        return By.XPath("//ngb-modal-window[contains(@class,'show')]"
                + "//button[normalize-space()='Delete' or normalize-space()='Confirm' or normalize-space()='Yes']"
                + " | //div[@role='dialog'][contains(@class,'show')]"
                + "//button[normalize-space()='Delete' or normalize-space()='Confirm' or normalize-space()='Yes']"
                + " | //div[contains(@class,'modal')][contains(@class,'show')]"
                + "//button[normalize-space()='Delete' or normalize-space()='Confirm' or normalize-space()='Yes']");
    }

    public By columnSelectionCounter() {
        return By.XPath(manageColumnsContext()
                + "//*[contains(normalize-space(),' of ')]");
    }

    public By manageColumnsCloseButton() {
        return By.XPath(manageColumnsContext()
                + "//button[contains(@class,'close') or @aria-label='Close']");
    }

    /** First Orders grid row action menu (ellipsis in pinned-right column). */
    public By firstRowActionMenuButton() {
        return By.XPath(
                "(//div[contains(@class,'ag-pinned-right-cols-container')]"
                        + "//div[contains(@class,'ag-row') and not(contains(@class,'ag-row-level-1'))][1]"
                        + "//button[contains(@class,'ellipsisButton') or contains(@class,'ellipsis')])[1]"
                        + " | (//div[contains(@class,'ag-center-cols-container')]"
                        + "//div[contains(@class,'ag-row') and not(contains(@class,'ag-row-level-1'))"
                        + " and not(contains(@class,'ag-row-level-2'))][1]"
                        + "//button[contains(@class,'ellipsisButton') or contains(@class,'ellipsis')])[1]");
    }

    /** Row action menu Export option (opens Excel sub-menu). */
    public By rowMenuExportOption() {
        return By.XPath(
                "//div[contains(@class,'cdk-overlay-pane') or contains(@class,'dropdown-menu')]"
                        + "//*[normalize-space()='Export' or " + ciContains("export") + "]"
                        + " | //*[@role='menuitem'][normalize-space()='Export' or " + ciContains("export") + "]"
                        + " | //span[normalize-space()='Export']/ancestor::*[@role='menuitem' or contains(@class,'dropdown-item')][1]");
    }

    /** Row action menu Export sub-option: Excel Document (.xlsx). */
    public By rowMenuExcelExportOption() {
        return By.XPath(
                "//div[contains(@class,'cdk-overlay-pane') or contains(@class,'dropdown-menu')]"
                        + "//*[" + ciContains("excel document") + " or normalize-space()='Excel Document (.xlsx)']"
                        + " | //*[@role='menuitem'][" + ciContains("excel document") + "]"
                        + " | //span[" + ciContains("excel document") + "]/ancestor::*[@role='menuitem' or contains(@class,'dropdown-item')][1]");
    }

    // --- Akila left-filter / ManageColumnsUtil compatibility (delegates where possible) ---
    private static final String AKILA_MANAGE_COLUMNS_PANEL_XPATH =
            "//div[contains(@class,'multi-table-column-container')]"
                    + " | //div[contains(@class,'table-view') or contains(@class,'manage-column')]"
                    + "[.//span[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'manage columns')]]";
    private static final String AKILA_ORDER_COLUMNS_SECTION_XPATH =
            "//div[contains(@class,'multi-options-list')]"
                    + "[.//div[contains(@class,'table-column-name')]"
                    + "[normalize-space()='Order columns' or normalize-space()='ORDER COLUMNS']]"
                    + " | //div[contains(@class,'table-column')]"
                    + "[.//div[contains(text(),'Order columns') or contains(text(),'ORDER COLUMNS')]]";
    private static final String AKILA_LINE_ITEM_COLUMNS_SECTION_XPATH =
            "//div[contains(@class,'multi-options-list')]"
                    + "[.//div[contains(@class,'table-column-name')]"
                    + "[normalize-space()='Line item columns' or normalize-space()='LINE ITEM COLUMNS']]"
                    + " | //div[contains(@class,'table-column')]"
                    + "[.//div[contains(text(),'Line item columns') or contains(text(),'LINE ITEM COLUMNS')]]";
    private static final String AKILA_MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH =
            "//div[contains(@class,'multi-table-column-container')]"
                    + "//div[contains(@class,'dropdown-reset-container')]"
                    + " | //div[contains(@class,'dropdown-reset-container')]";

    public By manageColumnsPanelRoot() {
        return manageColumnsPanel();
    }

    public By orderColumnCheckbox(String columnLabel) {
        return orderTableColumnCheckbox(columnLabel);
    }

    public By orderColumnLabel(String columnLabel) {
        return orderTableColumnLabel(columnLabel);
    }

    public By lineItemColumnsScrollList() {
        return By.XPath("(" + AKILA_LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'options-list-scrollbar') or contains(@class,'cdk-drop-list')]"
                + " | //div[contains(@class,'options-list-scrollbar')]");
    }

    public By lineItemLevelColumnCheckbox(String columnLabel) {
        return LineitemCheckboxOnTableView(columnLabel);
    }

    public By lineItemLevelColumnLabel(String columnLabel) {
        return lineItemLevelColumnLabelInScrollList(columnLabel);
    }

    public By lineItemLevelColumnLabelInScrollList(String columnLabel) {
        return By.XPath("(" + AKILA_LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'options-list-scrollbar')]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']"
                + " | (" + AKILA_LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']"
                + " | //div[contains(@class,'options-list-scrollbar')]//label[normalize-space()='" + columnLabel + "']");
    }

    public By lineItemColumnCheckbox(String columnLabel) {
        return lineItemLevelColumnCheckbox(columnLabel);
    }

    public By lineItemColumnLabel(String columnLabel) {
        return lineItemLevelColumnLabel(columnLabel);
    }

    public By tableViewDropdownOption(String viewName) {
        return tableViewOptionInDropdown(viewName);
    }

    public By tableViewRowContextMenuButton(String viewName) {
        return By.XPath("//div[@class='option'][.//label[normalize-space()='" + viewName + "']"
                + " or .//span[normalize-space()='" + viewName + "']]"
                + "//i[contains(@class,'bi-three-dots-vertical')]");
    }

    public By firstTableViewDropdownOptionRow() {
        return By.XPath("(" + AKILA_MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH
                + "//div[contains(@class,'dropdown-menu') and contains(@class,'show')]//div[@class='option'])[1]"
                + " | (//div[contains(@class,'dropdown-menu') and contains(@class,'show')]//div[@class='option'])[1]");
    }

    public By firstTableViewRowContextMenuButton() {
        return By.XPath("(//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//div[@class='option'])[1]//i[contains(@class,'bi-three-dots-vertical')]");
    }

    public By setTableViewAsDefaultMenuItem() {
        return By.XPath("//a[contains(@class,'setToMyDefault')]"
                + " | //div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//a[contains(@class,'setToMyDefault')]");
    }

    public By activeTableViewInDropdown(String viewName) {
        return tableViewInDropdown(viewName);
    }

    public By tableViewDefaultMarker(String viewName) {
        return By.XPath("(" + AKILA_MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                + "//*[contains(normalize-space(),'" + viewName + "')"
                + " and (contains(normalize-space(),'*') or contains(normalize-space(),'(Default)'))]");
    }

    public By tableViewCreatedToast() {
        return By.XPath("//div[contains(@class,'toast-content')]//div[contains(text(),'Table view created')]");
    }

    public By saveNewTableViewPopup() {
        return By.XPath("//span[(text())='Save new table view']");
    }
}
