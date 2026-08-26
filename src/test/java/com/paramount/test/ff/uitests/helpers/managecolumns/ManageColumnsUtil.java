package com.paramount.test.ff.uitests.helpers.managecolumns;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.paramount.test.ff.pageobjects.TableView;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions.Section;

/**
 * Shared Manage columns panel actions for all Fulfillment Console automation suites
 * (left-filter per-filter tests, table refresh, DSID, PTS packaging, feature flags).
 */
public class ManageColumnsUtil extends BaseTest {

    public static final int DEFAULT_PANEL_WAIT_S = 10;
    public static final int DEFAULT_PANEL_SETTLE_MS = 800;
    public static final int DEFAULT_CHECKBOX_WAIT_S = 30;
    public static final int DEFAULT_POLL_MS = 400;

    private final TableView tableView = new TableView();
    private final PtsPackagingIdPage ptsPage = new PtsPackagingIdPage();

    public boolean isPanelOpen() {
        return isPanelOpenInDom();
    }

    public boolean isPanelOpen(int waitSec) throws InterruptedException {
        long endTime = System.currentTimeMillis() + (waitSec * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (isPanelOpenInDom()) {
                return true;
            }
            Thread.sleep(200);
        }
        return isPanelOpenInDom();
    }

    public boolean isPanelOpenInDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var spans = document.querySelectorAll('span');"
                            + "for (var i = 0; i < spans.length; i++) {"
                            + "  if ((spans[i].textContent || '').indexOf('Manage columns') < 0) continue;"
                            + "  var node = spans[i];"
                            + "  while (node) {"
                            + "    var r = node.getBoundingClientRect();"
                            + "    if (r.width > 0 && r.height > 0) return true;"
                            + "    node = node.parentElement;"
                            + "  }"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return WaitUtil.isDisplayFast(tableView.manageColumnsPanelRoot(), 1)
                    || WaitUtil.isDisplayFast(ptsPage.manageColumnsPanelHeading(), 1);
        }
    }

    public void openPanel(SoftAssert softAssert) throws InterruptedException {
        openPanel(softAssert, 2);
    }

    public void openPanel(SoftAssert softAssert, int maxAttempts) throws InterruptedException {
        if (isPanelOpen(1)) {
            Logger.logReportMessage("Manage columns panel already open");
            return;
        }
        DriverUtil.scrollToElement(tableView.getTableViewButton());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Verify.softAssert1(DriverUtil.clickOnElement(tableView.getTableViewButton(), 5),
                    "Clicked Manage columns gear (#tableViewButton) attempt " + attempt, softAssert);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
            if (isPanelOpen(DEFAULT_PANEL_WAIT_S)) {
                Verify.softAssert1(true, "Manage columns panel is open", softAssert);
                return;
            }
            Logger.logReportMessage("Manage columns panel not open after attempt " + attempt);
        }
        Verify.softAssert1(isPanelOpen(2), "Manage columns panel is open", softAssert);
    }

    public void closePanel() throws InterruptedException {
        closePanel(null);
    }

    public void closePanel(SoftAssert softAssert) throws InterruptedException {
        if (!isPanelOpenInDom()) {
            return;
        }
        DriverUtil.clickOnElement(tableView.getTableViewButton(), 5);
        Thread.sleep(500);
        if (isPanelOpenInDom()) {
            try {
                driver.get().browser().executeScript(
                        "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', keyCode:27, bubbles:true}));");
                Thread.sleep(300);
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    public void expandLineItemColumnsAccordionIfNeeded() {
        try {
            driver.get().browser().executeScript(
                    "var section=document.evaluate("
                            + "\"(//div[contains(@class,'multi-options-list')]"
                            + "[.//div[contains(@class,'table-column-name')]"
                            + "[normalize-space()='Line item columns' or normalize-space()='LINE ITEM COLUMNS']])[1]\","
                            + "document,null,XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                            + "if(!section){return false;}"
                            + "var accordion=section.querySelector('msc-multi-select-accordion');"
                            + "if(!accordion){return true;}"
                            + "var open=accordion.querySelector('.accordion-collapse.show,.accordion-body');"
                            + "if(open && open.offsetParent!==null){return true;}"
                            + "var toggle=accordion.querySelector("
                            + "'.accordion-button,.accordion-header,[data-bs-toggle=\\\"collapse\\\"]');"
                            + "if(toggle){toggle.click();return true;}"
                            + "return false;");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not expand Line item columns accordion: " + e.getMessage());
        }
    }

    public void scrollAllLists() {
        try {
            driver.get().browser().executeScript(
                    "function scrollEl(el){if(!el)return;el.scrollTop=el.scrollHeight;}"
                            + "var panel=document.querySelector('div.multi-table-column-container');"
                            + "if(!panel){return;}"
                            + "scrollEl(panel);"
                            + "panel.querySelectorAll('.options-list-scrollbar,.cdk-drop-list,.cdk-virtual-scroll-viewport')"
                            + ".forEach(scrollEl);");
            Thread.sleep(500);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns lists: " + e.getMessage());
        }
    }

    public void scrollLineItemList() {
        scrollAllLists();
    }

    public void scrollLabelIntoView(String columnLabel) {
        try {
            String escaped = columnLabel.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    "var name='" + escaped + "';"
                            + "var labels=[].slice.call(document.querySelectorAll("
                            + "'div.multi-options-list label.option-label,div.options-list-scrollbar label.option-label'));"
                            + "var label=labels.find(function(el){return (el.textContent||'').trim()===name;});"
                            + "if(label){label.scrollIntoView({block:'center',inline:'nearest'});}");
            Thread.sleep(200);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Manage columns label " + columnLabel + ": " + e.getMessage());
        }
    }

    /** Enables every unchecked checkbox in the open Manage columns panel. Returns count toggled. */
    public int selectAllUncheckedCheckboxes() {
        try {
            scrollAllLists();
            Object result = driver.get().browser().executeScript(
                    "var panel=document.querySelector('div.multi-table-column-container');"
                            + "if(!panel){return 0;}"
                            + "panel.querySelectorAll('.options-list-scrollbar,.cdk-drop-list,.cdk-virtual-scroll-viewport')"
                            + ".forEach(function(el){el.scrollTop=el.scrollHeight;});"
                            + "var toggled=0;"
                            + "panel.querySelectorAll('input[type=\\\"checkbox\\\"]').forEach(function(cb){"
                            + "  if(cb.disabled||cb.checked){return;}"
                            + "  try{cb.click();toggled++;}catch(e){}"
                            + "});"
                            + "return toggled;");
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            return Integer.parseInt(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not select all Manage columns checkboxes: " + e.getMessage());
            return 0;
        }
    }

    public By columnCheckbox(Section section, String columnLabel) {
        switch (section) {
            case ORDER:
                return By.XPath("//div[contains(@class,'table-column')]//div[contains(text(), 'Order columns')]"
                        + "//following::label[normalize-space()='" + columnLabel + "']"
                        + "/preceding-sibling::input[@type='checkbox']"
                        + " | //label[normalize-space()='" + columnLabel + "']"
                        + "/preceding-sibling::input[@type='checkbox']");
            case PACKAGE:
                return By.XPath("//div[contains(@class,'table-column')]//div[contains(text(), 'Package columns')]"
                        + "//following::label[normalize-space()='" + columnLabel + "']"
                        + "/preceding-sibling::input[@type='checkbox']");
            case ORDER_LINE_ITEM:
            case LINE_ITEM:
                return tableView.lineItemColumnCheckbox(columnLabel);
            default:
                return tableView.CheckboxOnTableView(columnLabel);
        }
    }

    public By columnLabel(Section section, String columnLabel) {
        switch (section) {
            case ORDER:
                return tableView.OrderColumnNameOnTableView(columnLabel);
            case PACKAGE:
                return tableView.PackageColumnNameOnTableView(columnLabel);
            case ORDER_LINE_ITEM:
            case LINE_ITEM:
                return tableView.lineItemColumnLabel(columnLabel);
            default:
                return tableView.CheckboxOnTableView(columnLabel);
        }
    }

    public void validateColumnListed(Section section, String columnLabel, SoftAssert softAssert) {
        Verify.softAssert1(WaitUtil.isDisplay(columnLabel(section, columnLabel), DEFAULT_PANEL_WAIT_S),
                columnLabel + " listed under " + section.name() + " in Manage columns", softAssert);
    }

    public boolean waitForCheckbox(Section section, String columnLabel, int totalSeconds)
            throws InterruptedException {
        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        long deadline = System.currentTimeMillis() + (totalSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            expandLineItemColumnsAccordionIfNeeded();
            scrollLineItemList();
            scrollLabelIntoView(columnLabel);
            if (WaitUtil.isDisplayFast(checkbox, 2) || WaitUtil.isDisplayFast(label, 2)) {
                return true;
            }
            Thread.sleep(DEFAULT_POLL_MS);
        }
        scrollLabelIntoView(columnLabel);
        return WaitUtil.isDisplayFast(checkbox, 1) || WaitUtil.isDisplayFast(label, 1);
    }

    public void enableColumnIfNeeded(Section section, String columnLabel, SoftAssert softAssert)
            throws InterruptedException {
        By checkbox = columnCheckbox(section, columnLabel);
        By label = columnLabel(section, columnLabel);
        if (!WaitUtil.isDisplayFast(checkbox, 3) && !WaitUtil.isDisplayFast(label, 3)) {
            validateColumnListed(section, columnLabel, softAssert);
        }
        if (DriverUtil.isSelectedCheckbox(checkbox)) {
            Logger.logReportMessage(columnLabel + " already enabled in Manage columns");
            return;
        }
        boolean toggled = clickColumnCheckbox(checkbox, label, columnLabel);
        Verify.softAssert1(toggled, "Enabled " + columnLabel + " in Manage columns", softAssert);
        saveChangesIfPresent(softAssert);
    }

    public boolean clickColumnCheckbox(By checkbox, By label, String columnLabel) {
        scrollLineItemList();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (DriverUtil.clickOnElement(label, 5)) {
            Logger.logMessage("Clicked " + columnLabel + " label in Manage columns");
            return true;
        }
        if (DriverUtil.clickOnElement(checkbox, 5)) {
            Logger.logMessage("Clicked " + columnLabel + " checkbox in Manage columns");
            return true;
        }
        try {
            driver.get().finder().findElement(checkbox).executeScript("arguments[0].click();");
            Logger.logMessage("JS-clicked " + columnLabel + " checkbox in Manage columns");
            return true;
        } catch (Exception e) {
            try {
                driver.get().finder().findElement(label).executeScript("arguments[0].click();");
                Logger.logMessage("JS-clicked " + columnLabel + " label in Manage columns");
                return true;
            } catch (Exception ex) {
                Logger.logConsoleMessage("Failed to toggle " + columnLabel + " in Manage columns: " + ex.getMessage());
                return false;
            }
        }
    }

    public void saveChangesIfPresent(SoftAssert softAssert) throws InterruptedException {
        By saveBtn = ptsPage.saveChangesButtonInPanel();
        if (WaitUtil.isDisplayFast(saveBtn, 2)) {
            Verify.softAssert1(DriverUtil.clickOnElement(saveBtn, 5),
                    "Clicked Save changes in Manage columns", softAssert);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
        }
        if (WaitUtil.isDisplayFast(ptsPage.saveChangesConfirmButton(), 3)) {
            DriverUtil.clickOnElement(ptsPage.saveChangesConfirmButton(), 5);
            Thread.sleep(DEFAULT_PANEL_SETTLE_MS);
        }
    }

    /** Maps legacy {@code FeatureFlagColumnUtil.ColumnSection} values. */
    public static Section toSection(com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection section) {
        return section == com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection.ORDER
                ? Section.ORDER : Section.LINE_ITEM;
    }
}
