package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Table toolbar above grid — Refresh table button next to Export (BSD-29582). */
public class TableRefreshHeaderPage {

    public By tableButtonSection() {
        return By.XPath("//div[contains(@class,'button-section')]");
    }

    public By exportButton() {
        return By.XPath("//div[contains(@class,'button-section')]//button[contains(@class,'export-btn')]"
                + " | //button[contains(@class,'export-btn') and contains(normalize-space(),'Export')]");
    }

    /** Primary: {@code button.refresh-table-btn} in {@code div.button-section} (PROD DOM). */
    public By refreshButton() {
        return By.XPath("//div[contains(@class,'button-section')]//button[contains(@class,'refresh-table-btn')]"
                + " | //button[contains(@class,'refresh-table-btn') and contains(normalize-space(),'Refresh table')]");
    }

    public By refreshTableButtonLabel() {
        return By.XPath("//button[contains(@class,'refresh-table-btn')"
                + " and contains(normalize-space(),'Refresh table')]");
    }

    public By refreshIcon() {
        return By.XPath("//button[contains(@class,'refresh-table-btn')]//i[contains(@class,'bi-arrow-repeat')]");
    }

    /** Legacy table view dropdown (still present on some views). */
    public By tableViewDropdownContainer() {
        return By.XPath("//div[@class='dropdown-reset-container']");
    }

    public By ordersDataRows() {
        return By.XPath("//table[@id='orderTable']//tbody//tr[contains(@class,'row') and not(contains(@class,'inner'))]"
                + " | //table[@id='orderTable']//tbody//tr[contains(@class,'clickable-row')]");
    }

    public By lineItemsDataRows() {
        return By.XPath("//app-fulfillment-main-table-container//tbody//tr[contains(@class,'row')]"
                + " | //table//tbody//tr[contains(@class,'row')]");
    }

    public By visibleOrderIdCells() {
        return By.XPath("//table[@id='orderTable']//tbody//tr[contains(@class,'row') and not(contains(@class,'inner'))]"
                + "//td[contains(@class,'order-id-col')]");
    }

    public By orderIdColumnHeader() {
        return By.XPath("//th[@title='Order ID' or contains(normalize-space(),'Order ID')]"
                + "[not(contains(normalize-space(),'LineItem'))]");
    }

    public By visibleLineItemIdCells() {
        return By.XPath("//app-fulfillment-main-table-container//tbody//tr[contains(@class,'row')]"
                + "//td[contains(@class,'lineitem-id-col') or contains(@class,'line-item-id-col')"
                + " or contains(@class,'lineItem-id-col')]");
    }

    public By lineItemIdColumnHeader() {
        return By.XPath("//th[@title='LineItem ID' or @title='Line Item ID'"
                + " or contains(normalize-space(),'LineItem ID')]");
    }
}
