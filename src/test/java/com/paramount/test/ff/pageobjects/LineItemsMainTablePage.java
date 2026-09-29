package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Line items tab main grid — Line Item Status column ({@code td.revised-status-col} / {@code span.status-label}). */
public class LineItemsMainTablePage {

    public By lineItemsTableRows() {
        return By.XPath("//app-fulfillment-main-table-container//tbody//tr[contains(@class,'row')]"
                + " | //table//tbody//tr[contains(@class,'row')]");
    }

    /** Visible Line Item Status labels in the grid (no order-row expand). */
    public By visibleLineItemStatusLabels() {
        return By.XPath("//tbody//tr[contains(@class,'row')]//td[contains(@class,'revised-status-col')]"
                + "//span[contains(@class,'status-label')]"
                + " | //tbody//tr[contains(@class,'row')]//td[contains(@class,'revised-status')]"
                + "//span[contains(@class,'status-label')]");
    }

    /** Visible Activity Type values in the Line Items grid ({@code td.activity-type-col} / {@code div.default-cell}). */
    public By visibleActivityTypeCells() {
        return By.XPath("//div[@id='line_items_tab']//table[@id='lineItemTable']//tbody//tr[contains(@class,'row')]"
                + "//td[contains(@class,'activity-type-col')]//div[contains(@class,'default-cell')]"
                + " | //div[@id='line_items_tab']//tbody//tr[contains(@class,'row')]"
                + "//td[contains(@class,'activity-type-col')]//div[contains(@class,'default-cell')]"
                + " | //app-fulfillment-line-items-main-table//tbody//tr[contains(@class,'row')]"
                + "//td[contains(@class,'activity-type-col')]");
    }
}
