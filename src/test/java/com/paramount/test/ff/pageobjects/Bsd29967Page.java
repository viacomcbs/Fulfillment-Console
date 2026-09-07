package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Locators for BSD-29967 — multi-submission (i) icon and Orders grid helpers. */
public class Bsd29967Page {

    public static final String MULTI_SUBMISSION_COLUMN = "Order start date";
    /** Actual Orders grid TD/TH class for Order start date on DEV/PROD. */
    public static final String ORDER_START_DATE_COL_CLASS = "start-time-col";
    public static final String MULTI_SUBMISSION_INFO_ICON_CLASS = "bi-info-circle";
    public static final String FOCUS_URL_FRAGMENT = "/fulfillment/focus?tableView=orders&order=";

    private static final String ORDER_TABLE = "//table[@id='orderTable']";
    /** Data rows only — exclude expanded inner line-item rows. */
    private static final String ORDER_DATA_ROWS = ORDER_TABLE + "//tbody//tr[not(contains(@class,'inner'))]";
    /** (i) icon appears only inside Order start date cells. */
    private static final String ORDER_START_DATE_INFO_ICON = ORDER_TABLE
            + "//tbody//td[contains(@class,'" + ORDER_START_DATE_COL_CLASS + "')]"
            + "//i[contains(@class,'" + MULTI_SUBMISSION_INFO_ICON_CLASS + "')]";

    private final DsidPage dsidPage = new DsidPage();
    private final HomePage homePage = new HomePage();

    public By gridDataRows() {
        return dsidPage.gridDataRows();
    }

    public By gridRowByIndex(int oneBasedIndex) {
        return By.XPath("(//table[@id='orderTable']//tbody//tr[not(contains(@class,'inner'))]"
                + " | //tbody//tr[contains(@class,'row') and not(contains(@class,'inner'))])["
                + oneBasedIndex + "]");
    }

    /** All multi-submission (i) icons in Order start date column. */
    public By orderStartDateInfoIcons() {
        return By.XPath(ORDER_START_DATE_INFO_ICON);
    }

    /** Nth (i) icon in Order start date (1-based). */
    public By orderStartDateInfoIconByIndex(int oneBasedIndex) {
        return By.XPath("(" + ORDER_START_DATE_INFO_ICON + ")[" + oneBasedIndex + "]");
    }

    /** Row ancestor of the Nth (i) icon in Order start date (1-based). */
    public By rowWithOrderStartDateInfoIcon(int oneBasedIndex) {
        return By.XPath("(" + ORDER_START_DATE_INFO_ICON + ")[" + oneBasedIndex + "]/ancestor::tr[1]");
    }

    /** Click target on the row that contains the Nth (i) icon in Order start date. */
    public By multiSubmissionRowClickTarget(int oneBasedIconIndex) {
        String row = "(" + ORDER_START_DATE_INFO_ICON + ")[" + oneBasedIconIndex + "]/ancestor::tr[1]";
        return By.XPath(row + "//td[contains(@class,'episode') or contains(@class,'title') or contains(@class,'season')]"
                + " | " + row + "//td[contains(@class,'" + ORDER_START_DATE_COL_CLASS + "')]");
    }

    /**
     * Preferred click target on an Orders row — Episode/Title (opens Details, not Status/expand/icon).
     */
    public By rowDetailsClickTarget(int oneBasedIndex) {
        String row = "(" + ORDER_DATA_ROWS + ")[" + oneBasedIndex + "]";
        return By.XPath(row + "//td[contains(@class,'episode') or contains(@class,'title') or contains(@class,'season')]"
                + " | " + row + "//td[contains(@class,'" + ORDER_START_DATE_COL_CLASS + "')]"
                + " | " + row + "//td[contains(@class,'order-id')]"
                + " | " + row + "//td[contains(@class,'asset-id')]"
                + " | " + row + "//td[contains(@class,'xytech')]"
                + " | " + row + "//td[not(contains(@class,'status'))"
                + " and not(contains(@class,'collapse')) and not(contains(@class,'checkbox'))"
                + " and not(contains(@class,'select'))][1]");
    }

    public By detailsButton() {
        return homePage.DetailsPanelButton();
    }

    public By orderStartDateColumnHeader() {
        return By.XPath("//th[@title='" + MULTI_SUBMISSION_COLUMN + "']"
                + " | //th[contains(@class,'" + ORDER_START_DATE_COL_CLASS + "')]"
                + " | //th[.//span[normalize-space()='" + MULTI_SUBMISSION_COLUMN + "']]"
                + " | //th[contains(normalize-space(),'" + MULTI_SUBMISSION_COLUMN + "')]");
    }

    public By orderStartDateCells() {
        return By.XPath(ORDER_TABLE + "//tbody//td[contains(@class,'" + ORDER_START_DATE_COL_CLASS + "')]");
    }

    public By detailsPanelDsidSection() {
        return By.XPath("//div[contains(@class,'section-label') and normalize-space()='Identifiers']"
                + "/following-sibling::div[contains(@class,'section-content')]"
                + "//div[contains(@class,'label') and normalize-space()='DSID']"
                + " | //*[contains(@class,'details') or contains(@class,'panel')]"
                + "//*[contains(@class,'label') and normalize-space()='DSID']");
    }

    /** Identifiers section container in FC Details panel. */
    public By detailsPanelIdentifiersSection() {
        return By.XPath("//div[contains(@class,'section-label') and normalize-space()='Identifiers']"
                + "/following-sibling::div[contains(@class,'section-content')][1]");
    }

    /** DSID values under Identifiers → DSID only (multi-submission Details panel). */
    public By detailsPanelDsIdsValues() {
        return By.XPath("//div[contains(@class,'section-label') and normalize-space()='Identifiers']"
                + "/following-sibling::div[contains(@class,'section-content')]"
                + "//div[contains(@class,'label') and normalize-space()='DSID']"
                + "/following-sibling::div//span[contains(@class,'dsIds-value')]");
    }
}
