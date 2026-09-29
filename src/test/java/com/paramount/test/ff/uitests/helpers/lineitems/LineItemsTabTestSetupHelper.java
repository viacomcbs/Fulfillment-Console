package com.paramount.test.ff.uitests.helpers.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.pageobjects.NavigationPage;
import com.paramount.test.ff.pageobjects.TableView;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Line items tab test setup helpers (nav-pill tab switch per Scriptless TC049, post-login blank-page refresh).
 */
public final class LineItemsTabTestSetupHelper extends BaseTest {

    private final HomePage homePage = new HomePage();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();
    private final NavigationPage navigationPage = new NavigationPage();
    private final TableView tableView = new TableView();

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

        By lineItemsTab = navigationPage.lineItemsNavPillButton();
        if (!WaitUtil.isDisplayFast(lineItemsTab, DEFAULT_WAIT_SECONDS)) {
            lineItemsTab = tableView.lineItemsTab();
        }
        if (WaitUtil.isDisplayFast(lineItemsTab, DEFAULT_WAIT_SECONDS)) {
            DriverUtil.scrollToElement(lineItemsTab);
            boolean clicked = DriverUtil.clickOnElement(lineItemsTab, DEFAULT_WAIT_SECONDS);
            if (!clicked) {
                clicked = DriverUtil.clickOnElementJs(lineItemsTab, 5);
            }
            Verify.softAssert(clicked, "Line Items nav-pill click succeeded");
            WaitUtil.waitForJSToLoad(30);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        } else {
            Verify.softAssert(false, "Line Items tab button is visible");
        }

        Verify.softAssert(isLineItemsTabActive(), "Line Items tab is active");
    }

    private boolean isLineItemsTabActive() {
        if (WaitUtil.isDisplayFast(navigationPage.lineItemsNavPillActive(), 3)) {
            return true;
        }
        return WaitUtil.isDisplayFast(lineItemsTabContextMarker(), 3);
    }

    private By lineItemsTabContextMarker() {
        return navigationPage.lineItemsTabContextMarker();
    }
}
