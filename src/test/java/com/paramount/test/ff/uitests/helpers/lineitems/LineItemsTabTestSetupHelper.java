package com.paramount.test.ff.uitests.helpers.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Line items tab test setup helpers (nav-link tab buttons, post-login blank-page refresh).
 * Kept in test helpers so base/common framework files are not modified.
 */
public final class LineItemsTabTestSetupHelper extends BaseTest {

    private static final String NAV_TAB_BASE = "//button[contains(@class,'nav-link')";
    private static final String LOWER_TEXT =
            "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    private static final String ACTIVE_CLASS =
            "contains(concat(' ', normalize-space(@class), ' '), ' active ')";

    private final HomePage homePage = new HomePage();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    public void prepareLineItemsTab(SoftAssert softAssert) throws InterruptedException {
        driver.get().options().setElementTimeout(60000);
        waitForFulfillmentConsoleReady(45, 3);
        Verify.softAssert(WaitUtil.isDisplay(homePage.getHeaderTitle(), 60), "FULFILLMENT CONSOLE");
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.filterPanelHeader(), 60),
                "Filter panel is visible before navigating to Line Items tab");
        navigateToLineItemsTab(softAssert);
    }

    private void waitForFulfillmentConsoleReady(int waitSecondsPerAttempt, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            WaitUtil.waitForJSToLoad(30);
            boolean headerVisible = WaitUtil.isDisplay(homePage.getHeaderTitle(), waitSecondsPerAttempt);
            boolean filtersVisible = WaitUtil.isDisplay(leftFilterPanel.filterPanelHeader(), 15);
            if (headerVisible || filtersVisible) {
                Logger.logReportMessage("Fulfillment Console ready (attempt " + attempt + ")");
                return;
            }
            if (attempt < maxAttempts) {
                Logger.logReportMessage("Fulfillment Console did not render — refreshing browser (attempt "
                        + attempt + " of " + maxAttempts + ")");
                driver.get().browser().refresh();
            }
        }
        Logger.logConsoleMessage("Fulfillment Console did not load after " + maxAttempts + " attempt(s)");
    }

    private void navigateToLineItemsTab(SoftAssert softAssert) throws InterruptedException {
        if (isLineItemsTabActive()) {
            Logger.logMessage("Already on Line Items tab");
            Verify.softAssert(true, "Line Items tab is active");
            return;
        }

        By lineItemsTab = lineItemsNavTab(false);
        if (WaitUtil.isDisplayFast(lineItemsTab, DEFAULT_WAIT_SECONDS)) {
            DriverUtil.scrollToElement(lineItemsTab);
            DriverUtil.clickOnElement(lineItemsTab, DEFAULT_WAIT_SECONDS);
            WaitUtil.waitForJSToLoad(30);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        } else {
            Verify.softAssert(false, "Line Items tab button is visible");
        }

        Verify.softAssert(isLineItemsTabActive(), "Line Items tab is active");
    }

    private boolean isLineItemsTabActive() {
        if (WaitUtil.isDisplayFast(lineItemsNavTab(true), 3)) {
            return true;
        }
        return WaitUtil.isDisplayFast(lineItemsTabContextMarker(), 3);
    }

    private By lineItemsNavTab(boolean activeOnly) {
        String xpath = NAV_TAB_BASE;
        if (activeOnly) {
            xpath += " and " + ACTIVE_CLASS;
        }
        xpath += " and contains(" + LOWER_TEXT + ", 'line items')]";
        return By.XPath(xpath);
    }

    private By lineItemsTabContextMarker() {
        return By.XPath(lineItemsNavTabXPath(true)
                + " | //*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),"
                + " 'show hidden line items')]"
                + " | //th[contains(normalize-space(),'Line item start date')]"
                + " | //div[contains(@class,'header')]//span[normalize-space()='Line item start date']");
    }

    private String lineItemsNavTabXPath(boolean activeOnly) {
        String xpath = NAV_TAB_BASE;
        if (activeOnly) {
            xpath += " and " + ACTIVE_CLASS;
        }
        return xpath + " and contains(" + LOWER_TEXT + ", 'line items')]";
    }
}
