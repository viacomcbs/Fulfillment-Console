package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Orders tab main grid — row expand and expanded line-item Activity Type cells. */
public class OrdersMainTablePage {

    private static final String ORDER_TABLE = "//table[contains(@id,'orderTable')]";
    private static final String ORDER_DATA_ROWS =
            ORDER_TABLE + "//tbody//tr[(contains(@class,'row') or contains(@class,'clickable-row'))"
                    + " and not(contains(@class,'inner'))]";

    /** Expand chevron only — never the full row/collapse cell (row click opens details panel). */
    public By orderRowExpandChevron(int rowIndexOneBased) {
        String row = "(" + ORDER_DATA_ROWS + ")[" + rowIndexOneBased + "]";
        String collapseCell = "(" + row + ")//td[contains(@class,'collapse-all-col')]"
                + " | (" + row + ")//td[contains(@class,'collapse')]";
        return By.XPath("(" + collapseCell + ")//i[contains(@class,'bi-chevron-right')]"
                + " | (" + collapseCell + ")//i[contains(@class,'bi-chevron-down')]"
                + " | (" + collapseCell + ")//span[contains(@class,'bi-chevron-right')]"
                + " | (" + collapseCell + ")//span[contains(@class,'bi-chevron-down')]");
    }

    public By orderRowExpandedChevronDown(int rowIndexOneBased) {
        String row = "(" + ORDER_DATA_ROWS + ")[" + rowIndexOneBased + "]";
        return By.XPath("(" + row + ")//td[contains(@class,'collapse')]"
                + "//i[contains(@class,'bi-chevron-down')]");
    }

    /** Expanded inner-table wrapper or inner row (visible after clicking expand). */
    public By expandedInnerTableMarker() {
        return By.XPath(ORDER_TABLE + "//tr[contains(@class,'row-horizontal-scroll')]"
                + " | " + ORDER_TABLE + "//div[contains(@class,'inner-table-wrapper')]"
                + " | " + ORDER_TABLE + "//tr[contains(@class,'inner')]"
                + " | //app-oc-ff-package//msc-custom-table");
    }

    /** PROD-validated: every Activity Type cell in the expanded order line-item grid. */
    public By expandedLineItemActivityTypeCellsExact() {
        return By.XPath("//td[@class='col line-item-activity-type']");
    }

    /** Every Language cell in the expanded order line-item grid (PROD: {@code td.col.line-item-language}). */
    public By expandedLineItemLanguageCellsExact() {
        return By.XPath("//td[contains(@class,'line-item-language')]");
    }

    /** Type column header in expanded line-item grid (PROD: {@code th[@title='Type' and contains(@class,'line-item-type')]}). */
    public By expandedLineItemTypeHeader() {
        return By.XPath("//div[contains(@class,'inner-table-wrapper')]//th[@title='Type' and contains(@class,'line-item-type')]"
                + " | //div[contains(@class,'package-line-items')]//th[@title='Type' and contains(@class,'line-item-type')]");
    }

    /** Every Type cell in the expanded order line-item grid (PROD: {@code td.col.line-item-type.sticky-col}). */
    public By expandedLineItemJobCellsExact() {
        return By.XPath("//div[contains(@class,'inner-table-wrapper')]//td[contains(@class,'line-item-type')]"
                + " | //div[contains(@class,'package-line-items')]//td[contains(@class,'line-item-type')]");
    }

    /** Type value labels inside expanded line-item Type cells (PROD: {@code td...line-item-type//span.label}). */
    public By expandedLineItemTypeValueLabels() {
        return By.XPath("//div[contains(@class,'inner-table-wrapper')]//td[contains(@class,'line-item-type')]//span[contains(@class,'label')]"
                + " | //div[contains(@class,'package-line-items')]//td[contains(@class,'line-item-type')]//span[contains(@class,'label')]");
    }

    /**
     * TC622: first line-item Type value in the first expanded order row only.
     * DOM: tr.clickable-row → inner-table-wrapper → td.line-item-type → span.label (e.g. Audio).
     */
    public By firstOrderFirstLineItemTypeValueLabel() {
        String firstOrderRow = "(" + ORDER_DATA_ROWS + ")[1]";
        String firstOrderInnerGrid = "(" + firstOrderRow + "//div[contains(@class,'inner-table-wrapper')]"
                + " | " + firstOrderRow + "//div[contains(@class,'package-line-items')])";
        return By.XPath(firstOrderInnerGrid + "//td[contains(@class,'line-item-type')][1]"
                + "//span[contains(@class,'label')]");
    }

    /** Line Item Status labels in the expanded order line-item grid ({@code revised-status-col}). */
    public By expandedLineItemStatusLabels() {
        return By.XPath("//table[contains(@id,'orderTable')]//td[contains(@class,'revised-status-col')]"
                + "//span[contains(@class,'status-label')]"
                + " | //table[contains(@id,'orderTable')]//td[contains(@class,'revised-status')]"
                + "//span[contains(@class,'status-label')]"
                + " | //tr[contains(@class,'inner')]//td[contains(@class,'revised-status-col')]"
                + "//span[contains(@class,'status-label')]");
    }

    /** Package header rows visible after expanding an order (PROD: {@code div.package-details}). */
    public By expandedPackageDetailRows() {
        return By.XPath("//app-oc-ff-package//div[contains(@class,'package-details')]"
                + " | //div[contains(@class,'package-card')]//div[contains(@class,'package-details')]");
    }

    /** Flag icon inside an order row Status cell ({@code revised-status-col}). */
    public By orderRowStatusFlagIcon(int rowIndexOneBased) {
        String row = "(" + ORDER_DATA_ROWS + ")[" + rowIndexOneBased + "]";
        return By.XPath("(" + row + ")//td[contains(@class,'revised-status-col')]"
                + "//i[contains(@class,'bi-flag')]"
                + " | (" + row + ")//td[contains(@class,'revised-status')]"
                + "//i[contains(@class,'bi-flag')]"
                + " | (" + row + ")//td[contains(@class,'revised-status-col')]"
                + "//*[contains(@class,'flag')]"
                + " | (" + row + ")//td[contains(@class,'revised-status-col')]"
                + "//msc-flag-icon)");
    }
}
