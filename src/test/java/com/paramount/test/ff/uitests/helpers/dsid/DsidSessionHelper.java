package com.paramount.test.ff.uitests.helpers.dsid;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

/** Shared session state for BSD-29870 DSID validation suite. */
public final class DsidSessionHelper {

    private static volatile boolean sharedSessionEnabled;

    private static final ThreadLocal<SessionState> STATE = ThreadLocal.withInitial(SessionState::new);

    private static final class SessionState {
        boolean loggedIn;
        boolean calendarSetToToday;
        boolean filtersAppliedOnOrders;
        boolean filtersAppliedOnLineItems;
        boolean dsidColumnEnabledOnOrders;
        boolean dsidColumnEnabledOnLineItems;
        int ordersRowIndexWithDsid = -1;
        String ordersTableDsid = "";
        int lineItemsRowIndexWithDsid = -1;
        String lineItemsTableDsid = "";
    }

    private DsidSessionHelper() {
    }

    private static SessionState state() {
        return STATE.get();
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
        DsidEmailReport.captureSynergySessionId();
    }

    public static boolean isCalendarSetToToday() {
        return state().calendarSetToToday;
    }

    public static void markCalendarSetToToday() {
        state().calendarSetToToday = true;
    }

    public static boolean isFiltersAppliedOnOrders() {
        return state().filtersAppliedOnOrders;
    }

    public static void markFiltersAppliedOnOrders() {
        state().filtersAppliedOnOrders = true;
    }

    public static boolean isFiltersAppliedOnLineItems() {
        return state().filtersAppliedOnLineItems;
    }

    public static void markFiltersAppliedOnLineItems() {
        state().filtersAppliedOnLineItems = true;
    }

    public static boolean isDsidColumnEnabledOnOrders() {
        return state().dsidColumnEnabledOnOrders;
    }

    public static void markDsidColumnEnabledOnOrders() {
        state().dsidColumnEnabledOnOrders = true;
    }

    public static boolean isDsidColumnEnabledOnLineItems() {
        return state().dsidColumnEnabledOnLineItems;
    }

    public static void markDsidColumnEnabledOnLineItems() {
        state().dsidColumnEnabledOnLineItems = true;
    }

    public static void setOrdersRowWithDsid(int rowIndex, String dsidValue) {
        state().ordersRowIndexWithDsid = rowIndex;
        state().ordersTableDsid = dsidValue == null ? "" : dsidValue.trim();
    }

    public static int getOrdersRowIndexWithDsid() {
        return state().ordersRowIndexWithDsid;
    }

    public static String getOrdersTableDsid() {
        return state().ordersTableDsid == null ? "" : state().ordersTableDsid;
    }

    public static void setLineItemsRowWithDsid(int rowIndex, String dsidValue) {
        state().lineItemsRowIndexWithDsid = rowIndex;
        state().lineItemsTableDsid = dsidValue == null ? "" : dsidValue.trim();
    }

    public static int getLineItemsRowIndexWithDsid() {
        return state().lineItemsRowIndexWithDsid;
    }

    public static String getLineItemsTableDsid() {
        return state().lineItemsTableDsid == null ? "" : state().lineItemsTableDsid;
    }

    public static void stopSharedDriver() {
        DsidEmailReport.captureSynergySessionId();
        try {
            if (BaseTest.driver.get() != null) {
                BaseTest.driver.get().stop();
                BaseTest.driver.remove();
                SleepUtils.sleep(3000);
            }
        } catch (Exception ignored) {
            // session may already be closed
        }
        Logger.logMessage("===========DSID SESSION END (thread "
                + Thread.currentThread().getName() + ")===========");
        STATE.remove();
    }
}
