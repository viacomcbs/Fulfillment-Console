package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Locators for BSD-28459 feature-flag cleanup regression (columns, history, GLIM). */
public class FeatureFlagPage {

    public static final String MATERIAL_ID = "Material ID";
    public static final String EDIT_CRID = "Edit CRID";
    public static final String UUID = "UUID";
    public static final String PARTNER_END_DATE = "Partner end date";
    public static final String OPEN_TEXT_ID = "Open Text ID";

    private final TableView tableView = new TableView();

    public By tableViewButton() {
        return tableView.getTableViewButton();
    }

    public By manageColumnsPanelHeading() {
        return By.XPath("//span[contains(normalize-space(),'Manage columns')"
                + " or contains(normalize-space(),'Manage Columns')]");
    }

    public By orderColumnLabel(String columnLabel) {
        return tableView.OrderColumnNameOnTableView(columnLabel);
    }

    public By lineItemColumnLabel(String columnLabel) {
        return tableView.LineitemColumnNameOnTableView(columnLabel);
    }

    public By columnCheckbox(String columnLabel) {
        return tableView.CheckboxOnTableView(columnLabel);
    }

    public By saveChangesButton() {
        return By.XPath("//button[contains(normalize-space(),'Save changes') and not(@disabled)]");
    }

    public By gridColumnHeader(String columnLabel) {
        return By.XPath("//th[@title='" + columnLabel + "']"
                + " | //th[.//span[normalize-space()='" + columnLabel + "']]"
                + " | //th[contains(normalize-space(),'" + columnLabel + "')]");
    }

    public By gridDataRows() {
        return By.XPath("//tbody//tr[contains(@class,'row')]"
                + " | //table[@id='orderTable']//tbody//tr");
    }

    public By detailsButton() {
        return By.XPath("//button[normalize-space()='Details']");
    }

    public By gridFirstRow() {
        return By.XPath("//tbody//tr[contains(@class,'row')][1]"
                + " | //table[@id='orderTable']//tbody//tr[1]");
    }

    public By orderHistorySection() {
        return By.XPath("//*[contains(normalize-space(),'Order History')"
                + " or contains(normalize-space(),'History Log')"
                + " or contains(normalize-space(),'History log')]");
    }

    public By auditLogSection() {
        return By.XPath("//*[contains(normalize-space(),'Audit Log')"
                + " or contains(normalize-space(),'Audit log')]");
    }

    public By partnerFilterPagination() {
        return By.XPath("//msc-left-filter-panel//button[contains(@class,'pagination')]"
                + " | //msc-left-filter-panel//*[contains(@class,'page-item')]"
                + " | //msc-left-filter-panel//a[contains(@class,'page-link')]");
    }

    public By glimPlayAllButton() {
        return By.XPath("//button[contains(normalize-space(),'Play all')"
                + " or contains(normalize-space(),'Play All')]");
    }
}
