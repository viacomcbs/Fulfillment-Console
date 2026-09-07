package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Shared session state for BSD-29967 suite (multi Environment workflow loop). */
public final class Bsd29967SessionHelper {

    private static volatile boolean sharedSessionEnabled;

    private static final ThreadLocal<SessionState> STATE = ThreadLocal.withInitial(SessionState::new);

    private static final class SessionState {
        boolean loggedIn;
        boolean calendarSetToYesterday;
        boolean environmentFilterApplied;
        boolean orderStartDateColumnEnabled;
        int expectedEnvironmentCount;
        String environmentLabel = "";
        String orderId = "";
        String environmentSlug = "";
        String omfOrderId = "";
        String fcWindowHandle = "";
        int multiSubmissionRowIndex = -1;
        final List<String> fcDetailsDsids = new ArrayList<>();
        final List<String> opsConsoleDsids = new ArrayList<>();
        final List<Bsd29967EnvironmentResult> environmentResults = new ArrayList<>();
        String opsExtractionSource = "";
        String opsExtractionDiagnostics = "";
    }

    private Bsd29967SessionHelper() {
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
        Bsd29967EmailReport.captureSynergySessionId();
    }

    public static boolean isCalendarSetToYesterday() {
        return state().calendarSetToYesterday;
    }

    public static void markCalendarSetToYesterday() {
        state().calendarSetToYesterday = true;
    }

    public static boolean isEnvironmentFilterApplied() {
        return state().environmentFilterApplied;
    }

    public static void markEnvironmentFilterApplied() {
        state().environmentFilterApplied = true;
    }

    public static boolean isOrderStartDateColumnEnabled() {
        return state().orderStartDateColumnEnabled;
    }

    public static void markOrderStartDateColumnEnabled() {
        state().orderStartDateColumnEnabled = true;
    }

    public static void setEnvironmentLabel(String label) {
        state().environmentLabel = label == null ? "" : label.trim();
    }

    public static String getEnvironmentLabel() {
        return state().environmentLabel;
    }

    public static void setOrderContext(String orderId, String environmentSlug) {
        state().orderId = orderId == null ? "" : orderId.trim();
        state().environmentSlug = environmentSlug == null ? "" : environmentSlug.trim();
    }

    public static String getOrderId() {
        return state().orderId;
    }

    public static String getEnvironmentSlug() {
        return state().environmentSlug;
    }

    public static void setMultiSubmissionRowIndex(int rowIndex) {
        state().multiSubmissionRowIndex = rowIndex;
    }

    public static int getMultiSubmissionRowIndex() {
        return state().multiSubmissionRowIndex;
    }

    public static void setFcDetailsDsids(List<String> dsids) {
        state().fcDetailsDsids.clear();
        if (dsids != null) {
            state().fcDetailsDsids.addAll(dsids);
        }
    }

    public static List<String> getFcDetailsDsids() {
        return Collections.unmodifiableList(new ArrayList<>(state().fcDetailsDsids));
    }

    public static void setOpsConsoleDsids(List<String> dsids) {
        state().opsConsoleDsids.clear();
        if (dsids != null) {
            state().opsConsoleDsids.addAll(dsids);
        }
    }

    public static List<String> getOpsConsoleDsids() {
        return Collections.unmodifiableList(new ArrayList<>(state().opsConsoleDsids));
    }

    public static void setOpsExtractionSource(String source) {
        state().opsExtractionSource = source == null ? "" : source.trim();
    }

    public static String getOpsExtractionSource() {
        return state().opsExtractionSource;
    }

    public static void setOpsExtractionDiagnostics(String diagnostics) {
        state().opsExtractionDiagnostics = diagnostics == null ? "" : diagnostics.trim();
    }

    public static String getOpsExtractionDiagnostics() {
        return state().opsExtractionDiagnostics;
    }

    public static void setOmfOrderId(String omfOrderId) {
        state().omfOrderId = omfOrderId == null ? "" : omfOrderId.trim();
    }

    public static String getOmfOrderId() {
        return state().omfOrderId;
    }

    public static void setFcWindowHandle(String handle) {
        state().fcWindowHandle = handle == null ? "" : handle;
    }

    public static String getFcWindowHandle() {
        return state().fcWindowHandle;
    }

    public static void clearEnvironmentResults() {
        state().environmentResults.clear();
        state().expectedEnvironmentCount = 0;
    }

    public static void setExpectedEnvironmentCount(int count) {
        state().expectedEnvironmentCount = Math.max(0, count);
        Bsd29967EmailReport.setExpectedEnvironmentCount(count);
    }

    public static int getExpectedEnvironmentCount() {
        return state().expectedEnvironmentCount;
    }

    public static void recordEnvironmentResult(Bsd29967EnvironmentResult result) {
        if (result != null) {
            state().environmentResults.add(result);
            Bsd29967EmailReport.snapshotEnvironmentResult(result);
            state().environmentLabel = result.getEnvironment();
            if (!result.getFcDsids().isEmpty()) {
                state().fcDetailsDsids.clear();
                state().fcDetailsDsids.addAll(result.getFcDsids());
            }
            if (!result.getOpsDsids().isEmpty()) {
                state().opsConsoleDsids.clear();
                state().opsConsoleDsids.addAll(result.getOpsDsids());
            }
            if (!result.getOrderId().isEmpty()) {
                state().orderId = result.getOrderId();
            }
        }
    }

    public static List<Bsd29967EnvironmentResult> getEnvironmentResults() {
        return Collections.unmodifiableList(new ArrayList<>(state().environmentResults));
    }

    public static int getPassedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_PASS);
    }

    public static int getFailedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_FAIL);
    }

    public static int getSkippedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_SKIP);
    }

    public static void resetWorkflowIterationState() {
        state().orderId = "";
        state().environmentSlug = "";
        state().omfOrderId = "";
        state().multiSubmissionRowIndex = -1;
        state().fcDetailsDsids.clear();
        state().opsConsoleDsids.clear();
        state().opsExtractionSource = "";
        state().opsExtractionDiagnostics = "";
    }

    private static int countEnvironmentsByStatus(String status) {
        int count = 0;
        for (Bsd29967EnvironmentResult result : state().environmentResults) {
            if (status.equals(result.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public static void stopSharedDriver() {
        Bsd29967EmailReport.captureSynergySessionId();
        try {
            if (BaseTest.driver.get() != null) {
                BaseTest.driver.get().stop();
                BaseTest.driver.remove();
                SleepUtils.sleep(3000);
            }
        } catch (Exception ignored) {
            // session may already be closed
        }
        Logger.logMessage("===========BSD-29967 SESSION END (thread "
                + Thread.currentThread().getName() + ")===========");
        STATE.remove();
    }
}
