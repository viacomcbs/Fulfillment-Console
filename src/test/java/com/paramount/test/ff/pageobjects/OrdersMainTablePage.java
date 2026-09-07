package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Orders tab main grid — row expand and expanded line-item Activity Type cells. */
public class OrdersMainTablePage {

    private static final String ORDER_TABLE = "//table[contains(@id,'orderTable')]";
    private static final String ORDER_DATA_ROWS =
            ORDER_TABLE + "//tbody//tr[(contains(@class,'row') or contains(@class,'clickable-row'))"
                    + " and not(contains(@class,'inner'))]";

    public By orderRowExpandChevron(int rowIndexOneBased) {
        String row = "(" + ORDER_DATA_ROWS + ")[" + rowIndexOneBased + "]";
        return By.XPath("(" + row + ")//td[contains(@class,'collapse')]"
                + "//i[contains(@class,'bi-chevron-right') or contains(@class,'bi-chevron-down')]"
                + " | (" + row + ")//td[contains(@class,'collapse')]"
                + "//span[contains(@class,'bi-chevron-right') or contains(@class,'bi-chevron-down')]"
                + " | (" + row + ")//td[contains(@class,'collapse-all-col')]"
                + " | (" + row + ")//td[contains(@class,'collapse')]");
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

    /** Line Item Status labels in the expanded order line-item grid ({@code revised-status-col}). */
    public By expandedLineItemStatusLabels() {
        return By.XPath("//table[contains(@id,'orderTable')]//td[contains(@class,'revised-status-col')]"
                + "//span[contains(@class,'status-label')]"
                + " | //table[contains(@id,'orderTable')]//td[contains(@class,'revised-status')]"
                + "//span[contains(@class,'status-label')]"
                + " | //tr[contains(@class,'inner')]//td[contains(@class,'revised-status-col')]"
                + "//span[contains(@class,'status-label')]");
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
