package com.paramount.test.ff.common.util;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.synergy.core.driver.web.WebDriver;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Retries Synergy WebDriver calls when the cloud browser drops the connection mid-suite.
 */
public final class SynergyRetryUtil {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 4000L;
    /** Cap each browser script — Synergy LB recycle can block for hours without this. */
    private static final long EXECUTE_SCRIPT_TIMEOUT_MS = 120_000L;

    private SynergyRetryUtil() {
    }

    public static boolean isConnectionError(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("connection reset")
                        || lower.contains("connection refused")
                        || lower.contains("server failed to respond")
                        || lower.contains("failed to respond at")
                        || lower.contains("not listening at this location")
                        || lower.contains("no active session found")
                        || lower.contains("permanently blocked for this session")
                        || lower.contains("orphaned")
                        || lower.contains("max test time limit")
                        || lower.contains("max time exceeded")
                        || lower.contains("socketexception")
                        || lower.contains("recycled")
                        || lower.contains("json\" is null")
                        || lower.contains("forked vm terminated")
                        || lower.contains("timed out after")) {
                    return true;
                }
            }
            String className = current.getClass().getSimpleName();
            if ("SocketException".equals(className)
                    || "ConnectException".equals(className)
                    || "NoHttpResponseException".equals(className)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static void recoverSessionIfNeeded() {
        WebDriver webDriver = BaseTest.driver.get();
        if (webDriver != null) {
            try {
                webDriver.getSessionID();
                return;
            } catch (Exception ignored) {
                try {
                    webDriver.stop();
                } catch (Exception stopError) {
                    Logger.logConsoleMessage("Synergy session stop failed: " + stopError.getMessage());
                }
                BaseTest.driver.remove();
            }
        }
        Logger.logReportMessage("Synergy session lost — recovering WebDriver and Fulfillment session");
        ManageColumnsBaseTest.resetFulfillmentSession();
        FilterPanel_Util.resetYesterdayDateFilterState();
        BaseTest.ensureDriverStarted();
    }

    public static <T> T runWithRetry(String label, Callable<T> action) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= DEFAULT_MAX_ATTEMPTS; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                RuntimeException wrapped = e instanceof RuntimeException
                        ? (RuntimeException) e
                        : new RuntimeException(e);
                lastFailure = wrapped;
                if (!isConnectionError(wrapped) || attempt >= DEFAULT_MAX_ATTEMPTS) {
                    if (isConnectionError(wrapped) && attempt >= DEFAULT_MAX_ATTEMPTS) {
                        recoverSessionIfNeeded();
                        try {
                            return action.call();
                        } catch (Exception recoveryError) {
                            RuntimeException recoveryWrapped = recoveryError instanceof RuntimeException
                                    ? (RuntimeException) recoveryError
                                    : new RuntimeException(recoveryError);
                            throw recoveryWrapped;
                        }
                    }
                    throw wrapped;
                }
                Logger.logConsoleMessage(label + " failed (attempt " + attempt + "/" + DEFAULT_MAX_ATTEMPTS
                        + "): " + wrapped.getMessage());
                sleepQuietly(DEFAULT_RETRY_DELAY_MS);
            }
        }
        throw lastFailure != null ? lastFailure : new RuntimeException(label + " failed");
    }

    public static Object executeScript(String script) {
        return runWithRetry("executeScript", () -> BaseTest.requireDriver().browser().executeScript(script));
    }

    private static void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
