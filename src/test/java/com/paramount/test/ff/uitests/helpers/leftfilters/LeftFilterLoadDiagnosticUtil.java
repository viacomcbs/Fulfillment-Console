package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagNetworkUtil;

import java.util.Arrays;
import java.util.List;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Captures console + API errors while left filter loads after login. */
public class LeftFilterLoadDiagnosticUtil extends BaseTest {

    private static final List<String> KEY_FILTERS = Arrays.asList(
            OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName(),
            OrdersLeftFilter.ORDER_STATUS.getDisplayName(),
            OrdersLeftFilter.PARTNER.getDisplayName());

    private static final int FILTER_LOAD_WAIT_MS = 60_000;

    private final HomePage homePage = new HomePage();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    public void captureLeftFilterLoadErrors(
            LeftFilterPanelUtil leftFilterPanelUtil, FeatureFlagNetworkUtil networkUtil) throws InterruptedException {
        logPageState();

        if (isFilterPanelInDom()) {
            if (leftFilterPanelUtil.isLeftFilterPanelOpenViaDom()) {
                Logger.logReportMessage("Left filter panel already open");
            } else {
                Logger.logReportMessage("Left filter panel in DOM but collapsed — attempting expand once");
                leftFilterPanelUtil.ensureLeftFilterPanelOpen();
            }
        } else {
            Logger.logReportMessage("msc-left-filter-panel not in DOM — left filter never rendered after login");
        }

        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        WaitUtil.waitForJSToLoad(30);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        waitForKeyFiltersToLoad();

        logFilterLoadStatus(leftFilterPanelUtil);

        // Allow async fetch GraphQL body parsing to complete.
        Thread.sleep(5000);

        Logger.logReportMessage("=== DIAGNOSTIC: Console errors (after login, left-filter load window) ===");
        logCapturedErrors(networkUtil.readConsoleErrors(), networkUtil.readApiFailures());

        Logger.logReportMessage("GraphQL requests observed: " + networkUtil.getGraphqlRequestCount());
        Logger.logReportMessage("Authenticated API requests observed: " + networkUtil.getAuthenticatedRequestCount());
    }

    private void logPageState() {
        Logger.logReportMessage("=== DIAGNOSTIC: Page state after login ===");
        try {
            Logger.logReportMessage("Current URL: " + driver.get().browser().getCurrentUrl());
        } catch (Exception e) {
            Logger.logReportMessage("Current URL: unavailable (" + e.getMessage() + ")");
        }
        Logger.logReportMessage("Fulfillment Console header visible: "
                + WaitUtil.isDisplayFast(homePage.getHeaderTitle(), 5));
        Logger.logReportMessage("msc-left-filter-panel in DOM: " + isFilterPanelInDom());
    }

    private boolean isFilterPanelInDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    "return !!document.querySelector('msc-left-filter-panel');");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForKeyFiltersToLoad() throws InterruptedException {
        long deadline = System.currentTimeMillis() + FILTER_LOAD_WAIT_MS;
        int poll = 0;
        while (System.currentTimeMillis() < deadline) {
            poll++;
            if (WaitUtil.isDisplayFast(leftFilterPanel.leftFilterByName(
                    OrdersLeftFilter.ORDER_STATUS.getDisplayName()), 3)) {
                Logger.logReportMessage("Order Status filter appeared after login (poll " + poll + ")");
                return;
            }
            Logger.logReportMessage("Waiting for left filter accordions after login (poll " + poll + ")...");
            Thread.sleep(3000);
        }
        Logger.logReportMessage("Order Status filter did not appear within " + (FILTER_LOAD_WAIT_MS / 1000) + "s");
    }

    private void logFilterLoadStatus(LeftFilterPanelUtil leftFilterPanelUtil) throws InterruptedException {
        Logger.logReportMessage("=== DIAGNOSTIC: Left filter load status after login ===");
        Logger.logReportMessage("Filter panel header visible: "
                + WaitUtil.isDisplayFast(leftFilterPanel.filterPanelHeader(), 5));
        Logger.logReportMessage("Filter list section visible: "
                + WaitUtil.isDisplayFast(leftFilterPanel.filterListSection(), 5));

        for (String filterName : KEY_FILTERS) {
            boolean labelVisible = WaitUtil.isDisplayFast(leftFilterPanel.leftFilterByName(filterName), 5);
            boolean accordionVisible = WaitUtil.isDisplayFast(leftFilterPanel.filterAccordionButton(filterName), 5);
            Logger.logReportMessage(filterName + " label visible: " + labelVisible);
            Logger.logReportMessage(filterName + " accordion visible: " + accordionVisible);
        }

        Logger.logReportMessage("Orders table record count: " + leftFilterPanelUtil.getTableRecordCount());
    }

    private void logCapturedErrors(List<String> consoleErrors, List<String> apiFailures) {
        if (consoleErrors.isEmpty()) {
            Logger.logReportMessage("No console.error / uncaught JS errors captured.");
        } else {
            for (int i = 0; i < consoleErrors.size(); i++) {
                Logger.logReportMessage("Console[" + (i + 1) + "]: " + consoleErrors.get(i));
            }
        }

        if (apiFailures.isEmpty()) {
            Logger.logReportMessage("No HTTP 4xx/5xx or GraphQL errors captured.");
        } else {
            for (int i = 0; i < apiFailures.size(); i++) {
                Logger.logReportMessage("API[" + (i + 1) + "]: " + apiFailures.get(i));
            }
        }
    }
}
