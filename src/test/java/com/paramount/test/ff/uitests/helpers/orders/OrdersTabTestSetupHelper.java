package com.paramount.test.ff.uitests.helpers.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.synergy.common.utils.SleepUtils;

/**
 * Orders tab test setup helpers (post-login wait for app render).
 */
public final class OrdersTabTestSetupHelper extends BaseTest {

    private static final int POST_LOGIN_JS_WAIT_SECONDS = 30;
    private static final int POST_LOGIN_SETTLE_MS = 15000;
    private static final int APP_SHELL_POLL_SECONDS = 30;

    private final HomePage homePage = new HomePage();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();
    private final LeftFilterPanelUtil leftFilterPanelUtil = new LeftFilterPanelUtil();

    public void prepareOrdersTab(SoftAssert softAssert) {
        ensureFulfillmentConsoleReady();
        Verify.softAssert(WaitUtil.isDisplayFast(homePage.getHeaderTitle(), APP_SHELL_POLL_SECONDS),
                "FULFILLMENT CONSOLE");
        Verify.softAssert(WaitUtil.isDisplayFast(leftFilterPanel.filterPanelHeader(), APP_SHELL_POLL_SECONDS)
                        || isLeftFilterPanelInDom(),
                "Filter panel is visible on Orders tab");
    }

    /** Wait for SPA render after URL load — no browser refresh retries. */
    public void ensureFulfillmentConsoleReady() {
        Logger.logReportMessage("Waiting for Fulfillment Console to load after URL...");
        WaitUtil.waitForJSToLoad(POST_LOGIN_JS_WAIT_SECONDS);
        try {
            Thread.sleep(POST_LOGIN_SETTLE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean waitForLoginReadyForLeftFilters() throws InterruptedException {
        ensureFulfillmentConsoleReady();
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (pollForFulfillmentAppShell(APP_SHELL_POLL_SECONDS) && anchorLeftFilterPanel()) {
                Logger.logReportMessage("Login ready — filter panel anchored (attempt " + attempt + ")");
                return true;
            }
            Logger.logReportMessage("Filter panel not visible yet — waiting (attempt " + attempt + " of 3)");
            logCurrentUrl();
            WaitUtil.waitForJSToLoad(10);
            Thread.sleep(5000);
        }
        return pollForFulfillmentAppShell(15) && anchorLeftFilterPanel();
    }

    public boolean isFilterPanelAnchored(int waitSeconds) {
        long endTime = System.currentTimeMillis() + (waitSeconds * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (isFilterPanelVisibleFast()) {
                return true;
            }
            SleepUtils.sleep(500);
        }
        try {
            return anchorLeftFilterPanel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean anchorLeftFilterPanel() throws InterruptedException {
        if (isFilterPanelVisibleFast()) {
            return true;
        }
        if (!isLeftFilterPanelInDom() && !WaitUtil.isDisplayFast(homePage.getHeaderTitle(), 3)) {
            return false;
        }
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        Thread.sleep(1000);
        return isFilterPanelVisibleFast();
    }

    private boolean isFilterPanelVisibleFast() {
        if (leftFilterPanelUtil.isLeftFilterPanelOpenViaDom()) {
            return true;
        }
        return WaitUtil.isDisplayFast(leftFilterPanel.filterPanelOpen(), 2)
                || WaitUtil.isDisplayFast(leftFilterPanel.filterPanelHeader(), 3)
                || WaitUtil.isDisplayFast(leftFilterPanel.filterPanelExpandIcon(), 2)
                || WaitUtil.isDisplayFast(leftFilterPanel.leftFilterPanel(), 2)
                || WaitUtil.isDisplayFast(homePage.filterPanelLabel(), 2);
    }

    private boolean pollForFulfillmentAppShell(int maxSeconds) {
        long endTime = System.currentTimeMillis() + (maxSeconds * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (WaitUtil.isDisplayFast(homePage.getHeaderTitle(), 1)
                    || isLeftFilterPanelInDom()
                    || WaitUtil.isDisplayFast(homePage.inputGobalSearch(), 1)
                    || WaitUtil.isDisplayFast(leftFilterPanel.leftFilterPanel(), 1)) {
                return true;
            }
            SleepUtils.sleep(1000);
        }
        return false;
    }

    private boolean isLeftFilterPanelInDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    "return !!document.querySelector('msc-left-filter-panel');");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return WaitUtil.isDisplayFast(leftFilterPanel.leftFilterPanel(), 1);
        }
    }

    private void logCurrentUrl() {
        try {
            Logger.logReportMessage("Current URL: " + driver.get().browser().getCurrentUrl());
        } catch (Exception ignored) {
            // diagnostic only
        }
    }
}
