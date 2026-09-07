package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/** Locators for ops-console API Dev UI (BSD-29967). */
public class OpsConsolePage {

    public static final String DEV_ORDER_URL_PREFIX =
            "https://contentplatform.viacom.com/ops-console-api-dev-ui/order/";

    public By workflowDropdown() {
        return By.XPath("//*[contains(normalize-space(),'Workflow')]/following::*[self::select or @role='combobox'"
                + " or contains(@class,'mat-select') or contains(@class,'dropdown')][1]"
                + " | //select[contains(@id,'workflow') or contains(@name,'workflow')]"
                + " | //mat-select[contains(@aria-label,'Workflow') or contains(@placeholder,'Workflow')]");
    }

    public By workflowOption(String environmentLabel) {
        String safe = environmentLabel.replace("'", "\\'");
        return By.XPath("//mat-option[contains(normalize-space(),'" + safe + "')]"
                + " | //option[contains(normalize-space(),'" + safe + "')]"
                + " | //*[self::li or self::div][@role='option' and contains(normalize-space(),'" + safe + "')]");
    }

    public By openSearchTab() {
        return By.XPath("//*[@role='tab' and normalize-space()='OpenSearch']"
                + " | //*[contains(@class,'mat-tab-label')][normalize-space()='OpenSearch'"
                + " or .//*[normalize-space()='OpenSearch']]"
                + " | //*[normalize-space()='OpenSearch' and not(ancestor::select)"
                + " and not(ancestor::mat-select) and not(ancestor::mat-option)]");
    }

    /** Dynamo tab in Order Inspector (ngb-nav: OpenSearch | Dynamo | Message History | Tools). */
    public By dynamoTab() {
        return By.XPath("//a[@id='ngb-nav-1' and @role='tab']"
                + " | //a[@role='tab' and normalize-space()='Dynamo' and contains(@class,'nav-link')]"
                + " | //a[@role='tab' and normalize-space()='Dynamo']"
                + " | //*[contains(@class,'mat-tab-label')][normalize-space()='Dynamo'"
                + " or .//*[normalize-space()='Dynamo']]");
    }

    public By dynamoTabSelected() {
        return By.XPath("//a[@id='ngb-nav-1' and @role='tab'"
                + " and (@aria-selected='true' or contains(@class,'active'))]"
                + " | //a[@role='tab' and normalize-space()='Dynamo'"
                + " and (@aria-selected='true' or contains(@class,'active'))]"
                + " | //*[contains(@class,'mat-tab-label') and contains(@class,'active')]"
                + "[.//*[normalize-space()='Dynamo'] or normalize-space()='Dynamo']");
    }

    /** Dynamo tab content panel (ngb-nav-1-panel). */
    public By dynamoTabPanel() {
        return By.XPath("//div[@id='ngb-nav-1-panel' and contains(@class,'tab-pane')]"
                + " | //div[@role='tabpanel' and @aria-labelledby='ngb-nav-1']");
    }

    public By dynamoTabPanelActive() {
        return By.XPath("//div[@id='ngb-nav-1-panel' and contains(@class,'active') and contains(@class,'show')]"
                + " | //div[@role='tabpanel' and @aria-labelledby='ngb-nav-1'"
                + " and contains(@class,'active')]");
    }

    public By jobsTreeNode() {
        return By.XPath("//*[normalize-space()='jobs' or normalize-space()='\"jobs\"'"
                + " or contains(normalize-space(),'\"jobs\"')]");
    }

    /** Array index node under jobs (0, 1, 2, ...). */
    public By jobArrayIndexNode(int index) {
        return By.XPath("//*[normalize-space()='jobs' or normalize-space()='\"jobs\"']"
                + "/following::*[normalize-space()='" + index + "' or normalize-space()='" + index + ":'][1]"
                + " | //*[normalize-space()='" + index + "' or normalize-space()='" + index + ":']"
                + "[ancestor::*[contains(normalize-space(),'jobs')]][1]");
    }

    public By dsIdListTreeNode() {
        return By.XPath("//*[normalize-space()='dsIdList' or normalize-space()='\"dsIdList\"'"
                + " or contains(normalize-space(),'dsIdList')]");
    }

    public By dsIdListArrayIndexNode(int index) {
        return By.XPath("//*[normalize-space()='dsIdList' or normalize-space()='\"dsIdList\"']"
                + "/following::*[normalize-space()='" + index + "' or normalize-space()='" + index + ":'][1]"
                + " | //*[normalize-space()='" + index + "' or normalize-space()='" + index + ":']"
                + "[ancestor::*[contains(normalize-space(),'dsIdList')]][1]");
    }

    /** Expand/collapse caret beside an ngx-json-viewer key (e.g. jobs, 2, dsIdList). */
    public By ngxJsonKeyToggler(String key) {
        String safe = key.replace("'", "\\'");
        return By.XPath("//ngx-json-viewer//span[contains(@class,'segment-key')]"
                + "[normalize-space()='" + safe + ":' or normalize-space()='" + safe + "'"
                + " or normalize-space()='\"" + safe + "\":']"
                + "/preceding-sibling::span[contains(@class,'toggler')][1]"
                + " | //ngx-json-viewer//span[contains(@class,'segment-key')]"
                + "[normalize-space()='" + safe + ":' or normalize-space()='" + safe + "']"
                + "/ancestor::section[1]//span[contains(@class,'toggler')][1]");
    }

    public By ngxJsonSegmentKey(String key) {
        String safe = key.replace("'", "\\'");
        return By.XPath("//ngx-json-viewer//span[contains(@class,'segment-key')]"
                + "[normalize-space()='" + safe + ":' or normalize-space()='" + safe + "'"
                + " or normalize-space()='\"" + safe + "\":']");
    }

    /** Inspect Order button beside Order ID field. */
    public By inspectOrderButton() {
        return By.XPath("//button[normalize-space()='Inspect Order']"
                + " | //*[self::button or self::a][normalize-space()='Inspect Order']");
    }

    /** Blue Copy button above Dynamo/OpenSearch JSON (copies full order payload). */
    public By copyJsonButton() {
        return By.XPath("//button[normalize-space()='Copy']");
    }

    /**
     * Copy button in the active Dynamo panel. After Dynamo tab is selected, only one Copy
     * button is visible in the Order Inspector tab bar.
     */
    public By copyJsonButtonInDynamoPanel() {
        return By.XPath("//div[@id='ngb-nav-1-panel' and contains(@class,'active')]"
                + "//button[normalize-space()='Copy']"
                + " | //div[@role='tabpanel' and @aria-labelledby='ngb-nav-1'"
                + " and contains(@class,'active')]//button[normalize-space()='Copy']"
                + " | //button[normalize-space()='Copy']");
    }
}
