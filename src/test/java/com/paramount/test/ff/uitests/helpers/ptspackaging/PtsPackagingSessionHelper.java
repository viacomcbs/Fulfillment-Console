package com.paramount.test.ff.uitests.helpers.ptspackaging;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

/** Per-thread session state for BSD-29441 PTS Packaging ID suite (supports parallel execution). */
public final class PtsPackagingSessionHelper {

    private static volatile boolean sharedSessionEnabled;
    /** {@code false} = sequential suite; {@code tests} = Orders || Line Items; {@code classes} = each test class. */
    private static volatile String parallelMode = "false";

    private static final ThreadLocal<SessionState> STATE = ThreadLocal.withInitial(SessionState::new);

    private static final class SessionState {
        boolean loggedIn;
        boolean ptsDemandFilterAppliedOnOrders;
        boolean ptsDemandFilterAppliedOnLineItems;
        boolean calendarSetToYesterday;
        boolean automationViewConfiguredOnOrders;
        boolean automationViewConfiguredOnLineItems;
        boolean ptsColumnEnabledOnOrders;
        boolean ptsColumnEnabledOnLineItems;
        String lastOrdersColumnSearchTerm;
        boolean lineItemsConsoleTabActive;
    }

    private PtsPackagingSessionHelper() {
    }

    private static SessionState state() {
        return STATE.get();
    }

    public static void setParallelMode(String mode) {
        parallelMode = mode == null ? "false" : mode.trim().toLowerCase();
    }

    public static boolean isParallelTestsMode() {
        return "tests".equals(parallelMode);
    }

    public static boolean isParallelClassesMode() {
        return "classes".equals(parallelMode);
    }

    public static boolean isParallelExecution() {
        return isParallelTestsMode() || isParallelClassesMode();
    }

    public static void enableSharedSession() {
        sharedSessionEnabled = true;
    }

    public static boolean isSharedSessionEnabled() {
        return sharedSessionEnabled;
    }

    public static boolean isLoggedIn() {
        return state().loggedIn;
    }

    public static void markLoggedIn() {
        state().loggedIn = true;
        PtsPackagingEmailReport.captureSynergySessionId();
    }

    public static boolean isPtsDemandFilterAppliedOnOrders() {
        return state().ptsDemandFilterAppliedOnOrders;
    }

    public static void markPtsDemandFilterAppliedOnOrders() {
        state().ptsDemandFilterAppliedOnOrders = true;
    }

    public static boolean isPtsDemandFilterAppliedOnLineItems() {
        return state().ptsDemandFilterAppliedOnLineItems;
    }

    public static void markPtsDemandFilterAppliedOnLineItems() {
        state().ptsDemandFilterAppliedOnLineItems = true;
    }

    public static boolean isCalendarSetToYesterday() {
        return state().calendarSetToYesterday;
    }

    public static void markCalendarSetToYesterday() {
        state().calendarSetToYesterday = true;
    }

    /** Do not call on browser refresh — Yesterday default persists in the app after one-time setup. */
    public static void resetCalendarState() {
        state().calendarSetToYesterday = false;
    }

    public static boolean isAutomationViewConfiguredOnOrders() {
        return state().automationViewConfiguredOnOrders;
    }

    public static void markAutomationViewConfiguredOnOrders() {
        state().automationViewConfiguredOnOrders = true;
        state().ptsColumnEnabledOnOrders = true;
    }

    public static boolean isAutomationViewConfiguredOnLineItems() {
        return state().automationViewConfiguredOnLineItems;
    }

    public static void markAutomationViewConfiguredOnLineItems() {
        state().automationViewConfiguredOnLineItems = true;
        state().ptsColumnEnabledOnLineItems = true;
    }

    /** Browser refresh clears left filters — re-apply Demand system = PTS on next setup. */
    public static void resetPtsDemandFiltersAfterRefresh() {
        state().ptsDemandFilterAppliedOnOrders = false;
        state().ptsDemandFilterAppliedOnLineItems = false;
    }

    public static boolean isPtsColumnEnabledOnOrders() {
        return state().ptsColumnEnabledOnOrders;
    }

    public static void markPtsColumnEnabledOnOrders() {
        state().ptsColumnEnabledOnOrders = true;
    }

    public static void resetPtsColumnEnabledOnOrders() {
        state().ptsColumnEnabledOnOrders = false;
    }

    public static boolean isPtsColumnEnabledOnLineItems() {
        return state().ptsColumnEnabledOnLineItems;
    }

    public static void markPtsColumnEnabledOnLineItems() {
        state().ptsColumnEnabledOnLineItems = true;
    }

    public static void resetPtsColumnEnabledOnLineItems() {
        state().ptsColumnEnabledOnLineItems = false;
    }

    public static boolean isLineItemsConsoleTabActive() {
        return state().lineItemsConsoleTabActive;
    }

    public static void markLineItemsConsoleTabActive() {
        state().lineItemsConsoleTabActive = true;
    }

    /** O_009 column search term — reused by O_013 export without re-reading an empty grid. */
    public static void setLastOrdersColumnSearchTerm(String term) {
        state().lastOrdersColumnSearchTerm = term == null ? "" : term.trim();
    }

    public static String getLastOrdersColumnSearchTerm() {
        return state().lastOrdersColumnSearchTerm == null ? "" : state().lastOrdersColumnSearchTerm;
    }

    public static void resetForCurrentThread() {
        STATE.remove();
    }

    public static void resetAll() {
        sharedSessionEnabled = false;
        parallelMode = "false";
        STATE.remove();
    }

    public static void stopSharedDriver() {
        PtsPackagingEmailReport.captureSynergySessionId();
        try {
            if (BaseTest.driver.get() != null) {
                BaseTest.driver.get().stop();
                BaseTest.driver.remove();
                SleepUtils.sleep(3000);
            }
        } catch (Exception ignored) {
            // session may already be closed
        }
        Logger.logMessage("===========PTS PACKAGING SESSION END (thread "
                + Thread.currentThread().getName() + ")===========");
        resetForCurrentThread();
    }
}
