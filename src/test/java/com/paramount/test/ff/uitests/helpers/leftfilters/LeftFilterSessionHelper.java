package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps one browser session alive across sequential left-filter validation tests in a suite.
 * Batch mode: refresh between filter flow tests (skip login) and renew Synergy session at ~25 min.
 */
public final class LeftFilterSessionHelper {

    /** Renew at 20 min — ~10 min buffer before Synergy 30 min MaxTestTime (renewal ~3–5 min). */
    private static final long SESSION_RENEW_MS = 20L * 60L * 1000L;

    private static volatile boolean loggedIn;
    private static volatile boolean sharedSessionEnabled;
    private static volatile boolean batchModeEnabled;
    private static volatile boolean calendarSetToYesterday;
    private static volatile boolean aAutomationViewReadyOrders;
    private static volatile boolean aAutomationViewReadyLineItems;
    private static volatile long synergySessionStartMs;
    private static volatile int completedFilterFlowTests;
    private static volatile String keepExpandedFilterName;
    private static volatile boolean postLoginHomeSettleComplete;
    private static final Set<String> manageColumnsReadyKeys = ConcurrentHashMap.newKeySet();

    private LeftFilterSessionHelper() {
    }

    public static void enableSharedSession() {
        sharedSessionEnabled = true;
    }

    public static void enableBatchMode() {
        batchModeEnabled = true;
        Logger.logReportMessage("Left filter batch mode enabled — refresh between filters, Synergy renewal at 20 min");
    }

    public static boolean isSharedSessionEnabled() {
        return sharedSessionEnabled;
    }

    public static boolean isBatchModeEnabled() {
        return batchModeEnabled;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void markLoggedIn() {
        loggedIn = true;
    }

    public static boolean isCalendarSetToYesterday() {
        return calendarSetToYesterday;
    }

    public static void markCalendarSetToYesterday() {
        calendarSetToYesterday = true;
    }

    public static void resetCalendarState() {
        calendarSetToYesterday = false;
    }

    public static boolean isAAutomationViewReady(ConsoleTab tab) {
        return tab == ConsoleTab.ORDERS ? aAutomationViewReadyOrders : aAutomationViewReadyLineItems;
    }

    public static void markAAutomationViewReady(ConsoleTab tab) {
        if (tab == ConsoleTab.ORDERS) {
            aAutomationViewReadyOrders = true;
        } else {
            aAutomationViewReadyLineItems = true;
        }
    }

    public static boolean isManageColumnReady(ConsoleTab tab, String columnLabel) {
        return manageColumnsReadyKeys.contains(manageColumnKey(tab, columnLabel));
    }

    public static void markManageColumnReady(ConsoleTab tab, String columnLabel) {
        manageColumnsReadyKeys.add(manageColumnKey(tab, columnLabel));
    }

    private static String manageColumnKey(ConsoleTab tab, String columnLabel) {
        return tab.name() + ":" + columnLabel;
    }

    public static void markSynergySessionStarted() {
        synergySessionStartMs = System.currentTimeMillis();
    }

    /** Marks session start when the Synergy driver is created (before login/setup delays). */
    public static void markSynergySessionStartedIfUnset() {
        if (synergySessionStartMs <= 0L) {
            markSynergySessionStarted();
        }
    }

    public static long getSynergySessionElapsedMs() {
        if (synergySessionStartMs <= 0L) {
            return 0L;
        }
        return System.currentTimeMillis() - synergySessionStartMs;
    }

    public static boolean shouldRenewSynergySession() {
        return batchModeEnabled && synergySessionStartMs > 0L
                && getSynergySessionElapsedMs() >= SESSION_RENEW_MS;
    }

    public static int getCompletedFilterFlowTests() {
        return completedFilterFlowTests;
    }

    public static void markFilterFlowTestCompleted() {
        completedFilterFlowTests++;
    }

    public static boolean shouldRefreshBetweenFilters() {
        return batchModeEnabled && loggedIn && completedFilterFlowTests > 0;
    }

    /** Per-filter suite: expand once in Basic TC and leave accordion open until Clear Filters teardown. */
    public static void markKeepFilterExpandedForSuite(String filterName) {
        keepExpandedFilterName = filterName;
        Logger.logReportMessage("Keep-expanded mode enabled for filter: " + filterName);
    }

    public static boolean shouldKeepFilterExpanded(String filterName) {
        return keepExpandedFilterName != null && keepExpandedFilterName.equals(filterName);
    }

    public static void clearKeepFilterExpanded() {
        keepExpandedFilterName = null;
    }

    /** Skip {@code clearAllActiveFiltersIfPresent} between tests when one filter stays selected. */
    public static boolean shouldPreserveFilterSelectionOnSessionReuse() {
        return keepExpandedFilterName != null && !keepExpandedFilterName.isBlank();
    }

    public static boolean isPostLoginHomeSettleComplete() {
        return postLoginHomeSettleComplete;
    }

    public static void markPostLoginHomeSettleComplete() {
        postLoginHomeSettleComplete = true;
    }

    /** Clears FC session flags after Synergy driver.stop — next setup performs full login. */
    public static void resetForSessionRenewal() {
        loggedIn = false;
        calendarSetToYesterday = false;
        aAutomationViewReadyOrders = false;
        aAutomationViewReadyLineItems = false;
        manageColumnsReadyKeys.clear();
        completedFilterFlowTests = 0;
        synergySessionStartMs = 0L;
        keepExpandedFilterName = null;
        postLoginHomeSettleComplete = false;
    }

    public static void reset() {
        loggedIn = false;
        sharedSessionEnabled = false;
        batchModeEnabled = false;
        calendarSetToYesterday = false;
        aAutomationViewReadyOrders = false;
        aAutomationViewReadyLineItems = false;
        manageColumnsReadyKeys.clear();
        completedFilterFlowTests = 0;
        synergySessionStartMs = 0L;
        keepExpandedFilterName = null;
        postLoginHomeSettleComplete = false;
    }

    /**
     * Synergy max-test-time or manual stop — drop login/calendar/manage-column flags and start a fresh driver.
     */
    public static void ensureBrowserSessionActive() {
        com.synergy.core.driver.web.WebDriver webDriver = BaseTest.driver.get();
        if (webDriver != null) {
            try {
                webDriver.getSessionID();
                return;
            } catch (Exception ignored) {
                try {
                    webDriver.stop();
                } catch (Exception stopError) {
                    Logger.logConsoleMessage("Left filter session stop failed: " + stopError.getMessage());
                }
                BaseTest.driver.remove();
            }
        }
        Logger.logReportMessage("Left filter: Synergy browser session lost — new session will login fresh");
        resetForSessionRenewal();
        BaseTest.ensureDriverStarted();
        markSynergySessionStarted();
    }

    public static void stopSharedDriver() {
        try {
            if (BaseTest.driver.get() != null) {
                BaseTest.driver.get().stop();
                BaseTest.driver.remove();
                SleepUtils.sleep(3000);
            }
        } catch (Exception ignored) {
            // session may already be closed
        }
        Logger.logMessage("===========LEFT FILTER SHARED SESSION END===========");
        reset();
    }
}
