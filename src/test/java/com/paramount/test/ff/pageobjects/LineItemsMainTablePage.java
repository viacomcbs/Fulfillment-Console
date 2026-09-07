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
}
