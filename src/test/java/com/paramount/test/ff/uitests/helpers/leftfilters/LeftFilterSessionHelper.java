package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps one browser session alive across sequential left-filter validation tests in a suite.
 */
public final class LeftFilterSessionHelper {

    private static volatile boolean loggedIn;
    private static volatile boolean sharedSessionEnabled;
    private static volatile boolean calendarSetToYesterday;
    private static volatile boolean aAutomationViewReadyOrders;
    private static volatile boolean aAutomationViewReadyLineItems;
    private static final Set<String> manageColumnsReadyKeys = ConcurrentHashMap.newKeySet();

    private LeftFilterSessionHelper() {
    }

    public static void enableSharedSession() {
        sharedSessionEnabled = true;
    }

    public static boolean isSharedSessionEnabled() {
        return sharedSessionEnabled;
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

    public static void reset() {
        loggedIn = false;
        sharedSessionEnabled = false;
        calendarSetToYesterday = false;
        aAutomationViewReadyOrders = false;
        aAutomationViewReadyLineItems = false;
        manageColumnsReadyKeys.clear();
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
