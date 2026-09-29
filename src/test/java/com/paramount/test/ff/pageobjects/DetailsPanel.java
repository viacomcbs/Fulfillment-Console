package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Details panel locators for Fulfillment Console Orders tab (PROD UI).
 * BSD-29302: Shipping Dock Package ID field and copy action.
 */
public class DetailsPanel {

    public static final String SHIPPING_DOCK_PACKAGE_ID_LABEL = "Shipping Dock Package ID";
    public static final String FILE_SIZE_LABEL = "File Size";
    public static final String DEMAND_SYSTEM_LABEL = "Demand system";
    public static final String DELIVERY_PROTOCOL_LABEL = "Delivery Protocol";
    public static final String ENDPOINT_INFO_TAB_LABEL = "Endpoint Info";

    private static String ciContains(String text) {
        return "contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + text.toLowerCase() + "')";
    }

    public By detailsPanelContainer() {
        return By.XPath("//div[contains(@class,'details-panel') or contains(@class,'detail-panel')"
                + " or contains(@class,'side-panel') or contains(@class,'order-details')]"
                + " | //aside[contains(@class,'detail') or contains(@class,'panel')]"
                + " | //div[contains(@class,'panel')][.//*[" + ciContains(SHIPPING_DOCK_PACKAGE_ID_LABEL) + "]]");
    }

    public By shippingDockPackageIdLabel() {
        String lit = TableView.escapeXPathLiteral(SHIPPING_DOCK_PACKAGE_ID_LABEL);
        return By.XPath("//div[contains(@class,'details') or contains(@class,'panel') or contains(@class,'side')]"
                + "//*[normalize-space()=" + lit + " or " + ciContains(SHIPPING_DOCK_PACKAGE_ID_LABEL) + "]"
                + " | //*[normalize-space()=" + lit + "]");
    }

    public By shippingDockPackageIdCopyButton() {
        String lit = TableView.escapeXPathLiteral(SHIPPING_DOCK_PACKAGE_ID_LABEL);
        return By.XPath("//*[normalize-space()=" + lit + " or " + ciContains(SHIPPING_DOCK_PACKAGE_ID_LABEL) + "]"
                + "/ancestor::div[contains(@class,'field') or contains(@class,'detail') or contains(@class,'row')][1]"
                + "//button[contains(@aria-label,'Copy') or contains(@class,'copy') or contains(@class,'clipboard')]"
                + " | //*[normalize-space()=" + lit + "]/following-sibling::*"
                + "//button[contains(@aria-label,'Copy') or contains(@class,'copy')]"
                + " | //*[normalize-space()=" + lit + "]/.."
                + "//button[contains(@aria-label,'Copy') or contains(@class,'copy')]"
                + " | //button[contains(@aria-label,'Copy')][ancestor::*[.//*[" + ciContains(SHIPPING_DOCK_PACKAGE_ID_LABEL) + "]]]");
    }

    public By packageRowWithShippingDockId() {
        return By.XPath("//div[contains(@class,'package-details') or contains(@class,'inner-package')"
                + " or contains(@class,'package-group')][contains(.,'Shipping Dock Package ID')]");
    }

    public By firstOrderRow() {
        return By.XPath("//div[contains(@class,'cdk-virtual-scroll-content-wrapper')]"
                + "//div[contains(@class,'table-row') or contains(@class,'ag-row')][1]"
                + " | //div[@role='row'][1]");
    }

    /** Job Request → Demand system value (PROD: {@code span.demandSystem-value}). */
    public By detailsPanelDemandSystemValue() {
        String lit = TableView.escapeXPathLiteral(DEMAND_SYSTEM_LABEL);
        return By.XPath("//div[contains(@class,'label') and normalize-space()=" + lit + "]"
                + "/following-sibling::div[contains(@class,'value')]"
                + "//span[contains(@class,'demandSystem-value')]"
                + " | //div[contains(@class,'label') and normalize-space()=" + lit + "]"
                + "/following-sibling::div[contains(@class,'value')]");
    }

    /** Right panel Endpoint Info tab (next tab after Details). */
    public By endpointInfoTab() {
        String lit = TableView.escapeXPathLiteral(ENDPOINT_INFO_TAB_LABEL);
        return By.XPath("//button[normalize-space()=" + lit + " and (@role='tab' or contains(@class,'nav-link'))]"
                + " | //a[contains(@class,'nav-item')]//button[normalize-space()=" + lit + "]");
    }

    /** Endpoint Info tab → Delivery Protocol value (PROD: {@code span.protocol-value}). */
    public By endpointInfoDeliveryProtocolValue() {
        String lit = TableView.escapeXPathLiteral(DELIVERY_PROTOCOL_LABEL);
        return By.XPath("//div[contains(@class,'label') and normalize-space()=" + lit + "]"
                + "/following-sibling::div[contains(@class,'value')]"
                + "//span[contains(@class,'protocol-value')]"
                + " | //div[contains(@class,'label') and normalize-space()=" + lit + "]"
                + "/following-sibling::div[contains(@class,'value')]");
    }

}
