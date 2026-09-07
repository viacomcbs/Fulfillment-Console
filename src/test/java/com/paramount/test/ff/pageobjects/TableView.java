package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

public class TableView {

    /** PROD Manage columns dropdown — {@code div.multi-table-column-container[cdkscrollable]}. */
    public static final String MANAGE_COLUMNS_PANEL_XPATH =
            "//div[contains(@class,'multi-table-column-container')]"
                    + " | //div[contains(@class,'table-view') or contains(@class,'manage-column')]"
                    + "[.//span[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'manage columns')]]";

    /** "Order columns" accordion — order-level fields (PTS Packaging ID, Order ID, etc.). */
    public static final String ORDER_COLUMNS_SECTION_XPATH =
            "//div[contains(@class,'multi-options-list')]"
                    + "[.//div[contains(@class,'table-column-name')]"
                    + "[normalize-space()='Order columns' or normalize-space()='ORDER COLUMNS']]"
                    + " | //div[contains(@class,'table-column')]"
                    + "[.//div[contains(text(),'Order columns') or contains(text(),'ORDER COLUMNS')]]";

    /** Line item columns section — header may be "Line item columns" or "LINE ITEM COLUMNS". */
    public static final String LINE_ITEM_COLUMNS_SECTION_XPATH =
            "//div[contains(@class,'multi-options-list')]"
                    + "[.//div[contains(@class,'table-column-name')]"
                    + "[normalize-space()='Line item columns' or normalize-space()='LINE ITEM COLUMNS']]"
                    + " | //div[contains(@class,'table-column')]"
                    + "[.//div[contains(text(),'Line item columns') or contains(text(),'LINE ITEM COLUMNS')]]";

    public By getTableViewButton() {
        return By.XPath("//button[@id='tableViewButton']");
    }
    /** Saved-views dropdown toggle inside the open Manage columns panel. */
    public static final String MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH =
            "//div[contains(@class,'multi-table-column-container')]"
                    + "//div[contains(@class,'dropdown-reset-container')]"
                    + " | //div[contains(@class,'dropdown-reset-container')]";

    public By standardViewdropdownButton() {
        return By.XPath(MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH);
    }

    public By tableViewDropdownToggle() {
        return By.XPath("(" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                + "//button[contains(@class,'dropdown-toggle') or @data-bs-toggle='dropdown']"
                + " | (" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]//button[.//span or .//label]"
                + " | (" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]");
    }

    /** Saved-views dropdown list — only when the menu is actually expanded ({@code .show}). */
    public static final String MANAGE_COLUMNS_VIEW_LIST_XPATH =
            "(" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                    + "//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                    + " | (" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                    + "//div[contains(@class,'dropdown-options-list') and contains(@class,'show')]";

    public By tableViewDropdownList() {
        return By.XPath(MANAGE_COLUMNS_VIEW_LIST_XPATH);
    }
    public By manageColumnOpenOrderTab() {
        return By.XPath("//span[contains(text(),'Manage columns')]");
    }
    public By selectStandardViewMode() {
        return By.XPath("//div[contains(@class,'option')][.//label[contains(normalize-space(),'Standard view')]]");
    }
    public By StandardViewMode() {
        return By.XPath("//label[contains(text(),'Standard view')]");
    }
    public By SaveConfirmationMessage() {
        return By.XPath("//div[contains(text(),'Table view created')]");
    }

    public By OrderColumnNameOnTableView(String s) {
        return orderColumnLabel(s);
    }

    /** Order-level checkbox under "Order columns" accordion (e.g. PTS Packaging ID). */
    public By orderColumnCheckbox(String columnLabel) {
        return By.XPath("(" + ORDER_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[contains(@class,'option-label') and normalize-space()='" + columnLabel + "']"
                + "/preceding-sibling::input[@type='checkbox']"
                + " | (" + ORDER_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[normalize-space()='" + columnLabel + "']"
                + "/preceding-sibling::input[@type='checkbox']");
    }

    public By orderColumnLabel(String columnLabel) {
        return By.XPath("(" + ORDER_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[contains(@class,'option-label') and normalize-space()='" + columnLabel + "']"
                + " | (" + ORDER_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[normalize-space()='" + columnLabel + "']");
    }
    public By PackageColumnNameOnTableView(String s) {
        return By.XPath("//div[contains(@class, 'table-column')]//div[contains(text(), 'Package columns')]//following::label[contains(text(), '"+s+"')]");
    }
    public By LineitemColumnNameOnTableView(String s) {
        return By.XPath("(" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[contains(@class,'option-label') and normalize-space()='" + s + "']"
                + " | (" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//label[normalize-space()='" + s + "']"
                + " | //div[contains(@class, 'table-column')]//div[contains(text(), 'Line item columns')]"
                + "//following::label[contains(text(), '" + s + "')]"
                + " | //div[contains(@class,'options-list-scrollbar')]//label[normalize-space()='" + s + "']");
    }

    /** Scrollable list under Line item columns — {@code div.options-list-scrollbar.cdk-drop-list}. */
    public By lineItemColumnsScrollList() {
        return By.XPath("(" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'options-list-scrollbar') or contains(@class,'cdk-drop-list')]"
                + " | //div[contains(@class,'options-list-scrollbar')]");
    }

    public By manageColumnsPanelRoot() {
        return By.XPath(MANAGE_COLUMNS_PANEL_XPATH);
    }

    public By lineItemColumnsSection() {
        return By.XPath(LINE_ITEM_COLUMNS_SECTION_XPATH);
    }

    /** Line-item row checkbox — scoped to LINE ITEM COLUMNS only (never Order columns). */
    public By lineItemLevelColumnCheckbox(String columnLabel) {
        return By.XPath("(" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'options-list-scrollbar')]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']/preceding-sibling::input[@type='checkbox']"
                + " | (" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']/preceding-sibling::input[@type='checkbox']");
    }

    public By lineItemLevelColumnLabel(String columnLabel) {
        return lineItemLevelColumnLabelInScrollList(columnLabel);
    }

    /** Line-item row label — scoped to LINE ITEM COLUMNS {@code draggable-item} only (not Order columns). */
    public By lineItemLevelColumnLabelInScrollList(String columnLabel) {
        return By.XPath("(" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'options-list-scrollbar')]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']"
                + " | (" + LINE_ITEM_COLUMNS_SECTION_XPATH + ")[1]"
                + "//div[contains(@class,'draggable-item')]"
                + "//label[normalize-space()='" + columnLabel + "']");
    }

    /** @deprecated use {@link #lineItemLevelColumnCheckbox} — PROD uses draggable-item, not option-container. */
    public By orderLineItemFlatColumnCheckbox(String columnLabel) {
        return lineItemLevelColumnCheckbox(columnLabel);
    }

    /** @deprecated use {@link #lineItemLevelColumnLabel}. */
    public By orderLineItemFlatColumnLabel(String columnLabel) {
        return lineItemLevelColumnLabel(columnLabel);
    }

    /**
     * Checkbox in Manage columns — line-item level on Orders tab or Line Items tab flat list.
     */
    public By lineItemColumnCheckbox(String columnLabel) {
        return lineItemLevelColumnCheckbox(columnLabel);
    }

    public By lineItemColumnLabel(String columnLabel) {
        return lineItemLevelColumnLabel(columnLabel);
    }
    public By CheckboxOnTableView(String s) {
        return By.XPath("//label[(text())='"+s+"']/preceding-sibling::input[@type='checkbox']");
    }
    public By columNameOnUI(String s) {
        return By.XPath("//label[(text())='"+s+"']/preceding-sibling::input[@type='checkbox']");
    }
    public By saveNewButtonTableView() {
        return By.XPath("//span[(text())='Save new view']");
    }
    public By saveNewTableViewPopup() {
        return By.XPath("//span[(text())='Save new table view']");
    }
    public By inputTableViewName() {
        return By.XPath("//input[@placeholder='Enter table view name']");
    }
    public By saveButtonOnTableViewPopup() {
        return By.XPath("//span[text()='Save new']");
    }
    /** Toggle shows {@code viewName} when that saved view is already loaded (dropdown closed). */
    public By activeTableViewToggleShows(String viewName) {
        return By.XPath("//div[@class='dropdown-reset-container']"
                + "//button[contains(normalize-space(),'" + viewName + "')]"
                + " | //div[@class='dropdown-reset-container']"
                + "//*[self::span or self::label][contains(normalize-space(),'" + viewName + "')]");
    }

    /** Active view name in the table-view dropdown (span or label; panel must be open). */
    public By tableViewDefaultSelected(String s) {
        return By.XPath("//div[@class='dropdown-reset-container']"
                + "//*[self::span or self::label][normalize-space()='" + s + "']"
                + " | //span[normalize-space()='" + s + "']"
                + " | //label[normalize-space()='" + s + "']");
    }

    public By activeTableViewInDropdown(String viewName) {
        return By.XPath("//div[@class='dropdown-reset-container']"
                + "//*[self::span or self::label][normalize-space()='" + viewName + "']");
    }

    /**
     * Named saved-view row in the Manage columns dropdown ({@code div.option} + {@code label}).
     */
    public By tableViewDropdownOption(String viewName) {
        return By.XPath("//div[@class='option'][.//label[normalize-space()='" + viewName + "']"
                + " or .//span[normalize-space()='" + viewName + "']]"
                + " | (" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                + "//div[@class='option'][.//label[normalize-space()='" + viewName + "']"
                + " or .//span[normalize-space()='" + viewName + "']]");
    }

    /** True when {@code viewName} is marked default ({@code *} or {@code (Default)} in the dropdown label). */
    public By tableViewDefaultMarker(String viewName) {
        return By.XPath("(" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH + ")[1]"
                + "//*[contains(normalize-space(),'" + viewName + "')"
                + " and (contains(normalize-space(),'*')"
                + " or contains(normalize-space(),'(Default)'))]"
                + " | //button[contains(@class,'dropdown')]"
                + "[contains(normalize-space(),'" + viewName + "')"
                + " and (contains(normalize-space(),'*')"
                + " or contains(normalize-space(),'(Default)'))]");
    }

    public By tableViewCreatedToast() {
        return By.XPath("//div[contains(@class,'toast-content')]//div[contains(text(),'Table view created')]");
    }

    public static final String SET_AS_DEFAULT_MENU_LABEL = "Set as default";

    /** First saved-view row after dropdown refresh (AAAAA sorts first alphabetically). */
    public By firstTableViewDropdownOptionRow() {
        return By.XPath("(" + MANAGE_COLUMNS_VIEW_DROPDOWN_XPATH
                + "//div[contains(@class,'dropdown-menu') and contains(@class,'show')]//div[@class='option'])[1]"
                + " | (//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//div[@class='option'])[1]");
    }

    /**
     * Three-dot icon on the first saved-view row — use after page refresh when {@code AAAAA} is first.
     * PROD validated pattern: {@code (//div[@class='option'])[1]//i[contains(@class,'bi-three-dots-vertical')]}
     */
    public By firstTableViewRowContextMenuButton() {
        return By.XPath("(//div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//div[@class='option'])[1]//i[contains(@class,'bi-three-dots-vertical')]");
    }

    /**
     * Three-dot icon on a saved view row — PROD validated:
     * {@code //div[@class='option'][.//label[normalize-space()='AAAAA']]//i[contains(@class,'bi-three-dots-vertical')]}
     */
    public By tableViewRowContextMenuButton(String viewName) {
        return By.XPath("//div[@class='option'][.//label[normalize-space()='" + viewName + "']"
                + " or .//span[normalize-space()='" + viewName + "']]"
                + "//i[contains(@class,'bi-three-dots-vertical')]");
    }

    /**
     * Set as default action after ⋮ click — PROD validated:
     * {@code //a[contains(@class,'setToMyDefault')]}
     */
    public By setTableViewAsDefaultMenuItem() {
        return By.XPath("//a[contains(@class,'setToMyDefault')]"
                + " | //div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "//a[contains(@class,'setToMyDefault')]");
    }

    public By confirmtiontableviewPopup() {
        return By.XPath("//div[contains(@class, 'toast-content')]//div[contains(text(), 'Table view created')]");
    }


    //span[text()='jjj']
  ////span[text()='Table view name']
//div[contains(@class, 'table-column')]//div[contains(text(), 'Order columns')]//following::label[contains(text(), '"+s+"')]
}
