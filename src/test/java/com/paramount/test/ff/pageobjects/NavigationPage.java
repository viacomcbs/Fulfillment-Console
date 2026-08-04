package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Navigation locators for Fulfillment Console tabs.
 */
public class NavigationPage {

    private static final String EXCLUDE_FILTER_PANEL = "[not(ancestor::msc-left-filter-panel)]";

    public By ordersTab() {
        return tabButton("Orders");
    }

    public By lineItemsTab() {
        return tabButton("Line Items");
    }

    public By activeTab(String tabLabel) {
        String lowerLabel = tabLabel.toLowerCase();
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
                + " | //div[contains(@class,'header')]//span[normalize-space()='Order start date']");
    }

    public By lineItemsTabContextMarker() {
        return By.XPath("//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),"
                + " 'show hidden line items')]"
                + " | //th[contains(normalize-space(),'Line item start date')]"
                + " | //div[contains(@class,'header')]//span[normalize-space()='Line item start date']");
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
