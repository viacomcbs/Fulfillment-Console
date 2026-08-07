package com.paramount.test.ff.uitests.helpers.featureflag;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

/** Shared browser session for BSD-28459 feature-flag cleanup suite. */
public final class FeatureFlagSessionHelper {

    private static volatile boolean sharedSessionEnabled;

    private static final ThreadLocal<SessionState> STATE = ThreadLocal.withInitial(SessionState::new);

    private static final class SessionState {
        boolean loggedIn;
        boolean calendarSetToYesterday;
    }

    private FeatureFlagSessionHelper() {
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
    }

    public static boolean isCalendarSetToYesterday() {
        return state().calendarSetToYesterday;
    }

    public static void markCalendarSetToYesterday() {
        state().calendarSetToYesterday = true;
    }

    public static void resetForCurrentThread() {
        STATE.remove();
    }

    public static void resetAll() {
        sharedSessionEnabled = false;
        STATE.remove();
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
        Logger.logMessage("===========FEATURE FLAG SESSION END (thread "
                + Thread.currentThread().getName() + ")===========");
        resetForCurrentThread();
    }
}
