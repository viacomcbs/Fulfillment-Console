package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Locators for BSD-29870 — DSID column validation (MetadataOnlyDelivery / PP YT FSP UK). */
public class DsidPage {

    public static final String COLUMN_LABEL = "DSID";
    public static final String JOB_TYPE_FILTER = "Job type";
    public static final String JOB_TYPE_VALUE = "MetadataOnlyDelivery";
    public static final String PARTNER_FILTER = "Partner";
    public static final String PARTNER_VALUE = "PP YT FSP UK";
    public static final String DETAILS_PANEL_LABEL = "DSID";

    private final TableView tableView = new TableView();
    private final HomePage homePage = new HomePage();

    public By tableViewButton() {
        return tableView.getTableViewButton();
    }

    public By detailsButton() {
        return homePage.DetailsPanelButton();
    }

    public By gridColumnHeader() {
        return By.XPath("//th[@title='" + COLUMN_LABEL + "']"
                + " | //th[.//span[normalize-space()='" + COLUMN_LABEL + "']]"
                + " | //th[contains(normalize-space(),'" + COLUMN_LABEL + "')]");
    }

    public By gridDataRows() {
        return By.XPath("//table[@id='orderTable']//tbody//tr"
                + " | //tbody//tr[contains(@class,'row')]");
    }

    public By gridRowByIndex(int oneBasedIndex) {
        return By.XPath("(//table[@id='orderTable']//tbody//tr | //tbody//tr[contains(@class,'row')])["
                + oneBasedIndex + "]");
    }

    public By detailsPanelDsidSection() {
        return By.XPath("//*[contains(@class,'details') or contains(@class,'panel')]"
                + "//*[contains(@class,'label') and normalize-space()='" + DETAILS_PANEL_LABEL + "']");
    }

    public By detailsPanelDsidValues() {
        return By.XPath("//*[contains(@class,'label') and normalize-space()='" + DETAILS_PANEL_LABEL + "']"
                + "/following-sibling::*[contains(@class,'value')]"
                + "//span[contains(@class,'demandSystemId-value')]//span"
                + " | //*[contains(@class,'label') and normalize-space()='" + DETAILS_PANEL_LABEL + "']"
                + "/ancestor::div[contains(@class,'field') or contains(@class,'row')][1]"
                + "//*[contains(@class,'value')]//span[contains(@class,'demandSystemId-value')]"
                + " | //*[contains(@class,'label') and normalize-space()='" + DETAILS_PANEL_LABEL + "']"
                + "/following-sibling::*[contains(@class,'value')]//span");
    }
}
