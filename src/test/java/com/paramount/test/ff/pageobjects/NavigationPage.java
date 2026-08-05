package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Navigation locators for Fulfillment Console tabs (Orders / Line items nav-pills above grid).
 */
public class NavigationPage {

    private static final String EXCLUDE_FILTER_PANEL = "[not(ancestor::msc-left-filter-panel)]";
    /** Table toolbar nav-pills — PROD DOM: {@code ul.nav-pills > button.nav-link}. */
    private static final String TABLE_NAV_ROOT =
            "//app-fulfillment-main-table-container//div[contains(@class,'table-top')]";

    public By ordersTab() {
        return ordersNavPillButton();
    }

    public By lineItemsTab() {
        return lineItemsNavPillButton();
    }

    public By ordersNavPillButton() {
        return By.XPath(TABLE_NAV_ROOT + "//ul[contains(@class,'nav-pills')]//button[contains(@class,'nav-link')]"
                + "[normalize-space()='Orders']");
    }

    public By lineItemsNavPillButton() {
        return By.XPath(TABLE_NAV_ROOT + "//ul[contains(@class,'nav-pills')]//button[contains(@class,'nav-link')]"
                + "[normalize-space()='Line items' or normalize-space()='Line Items']");
    }

    public By lineItemsNavPillActive() {
        return By.XPath(TABLE_NAV_ROOT + "//ul[contains(@class,'nav-pills')]//button[contains(@class,'nav-link')]"
                + "[contains(@class,'active')]"
                + "[normalize-space()='Line items' or normalize-space()='Line Items']");
    }

    public By ordersNavPillActive() {
        return By.XPath(TABLE_NAV_ROOT + "//ul[contains(@class,'nav-pills')]//button[contains(@class,'nav-link')]"
                + "[contains(@class,'active')][normalize-space()='Orders']");
    }

    public By activeTab(String tabLabel) {
        String lowerLabel = tabLabel.toLowerCase();
        if ("line items".equals(lowerLabel)) {
            return lineItemsNavPillActive();
        }
        if ("orders".equals(lowerLabel)) {
            return ordersNavPillActive();
        }
        return By.XPath(
                tabLabelXPath(tabLabel)
                        + "[ancestor-or-self::*[contains(@class,'active') or contains(@class,'selected')"
                        + " or contains(@class,'is-active') or @aria-selected='true'][1]]"
                + " | //*[@role='tab' and (@aria-selected='true' or contains(@class,'active'))]"
                        + "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                        + lowerLabel + "')]"
                        + EXCLUDE_FILTER_PANEL);
    }

    /** Orders tab is the default landing view; use page content when tab chrome is not button-based. */
    public By ordersTabContextMarker() {
        return By.XPath("//*[contains(normalize-space(),'Show hidden orders')]"
                + " | //th[contains(normalize-space(),'Order start date')]"
                + " | //th[contains(normalize-space(),'Order ID') and not(contains(normalize-space(),'LineItem'))]"
                + " | //div[contains(@class,'header')]//span[normalize-space()='Order start date']");
    }

    public By lineItemsTabContextMarker() {
        return By.XPath("//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),"
                + " 'show hidden line items')]"
                + " | //th[contains(normalize-space(),'LineItem ID')]"
                + " | //th[contains(normalize-space(),'Line Item ID')]"
                + " | //div[contains(@class,'header')]//span[normalize-space()='LineItem ID']"
                + " | //span[contains(normalize-space(),'Manage Columns') and contains(normalize-space(),'Line item')]");
    }

    private By tabButton(String tabLabel) {
        return By.XPath(
                tabLabelXPath(tabLabel) + "/ancestor::*[self::button or @role='tab' or contains(@class,'tab')][1]"
                + " | " + tabLabelXPath(tabLabel) + "[self::button or @role='tab']"
                + " | " + tabLabelXPath(tabLabel));
    }

    private String tabLabelXPath(String tabLabel) {
        String lowerLabel = tabLabel.toLowerCase();
        if ("Line Items".equalsIgnoreCase(tabLabel)) {
            return "//*"
                    + EXCLUDE_FILTER_PANEL
                    + "[normalize-space()='Line Items' or normalize-space()='Line items'"
                    + " or translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='line items']";
        }
        return "//*"
                + EXCLUDE_FILTER_PANEL
                + "[normalize-space()='" + tabLabel + "'"
                + " or translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='"
                + lowerLabel + "']";
    }
}
