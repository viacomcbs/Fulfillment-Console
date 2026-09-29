package com.paramount.test.ff.common.loginUtil;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.driver.LocalCapabilityFactory;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;
import com.synergy.core.driver.web.WebDriver;

public class Login extends BaseTest {
    private static final String PAGE_ZOOM = "0.67";
    private static final int LOGIN_POLL_ELEMENT_TIMEOUT_MS = LeftFilterConstants.QUICK_ELEMENT_TIMEOUT_MS;
    private static final int SYNERGY_LOGIN_SHELL_TIMEOUT_SEC = 45;
    private static final int SYNERGY_LOGIN_RETRY_TIMEOUT_SEC = 15;
    private static final int DEFAULT_LOGIN_TIMEOUT_SEC = 45;
    private static boolean pageZoomApplied;

    private final LoginPage loginPage = new LoginPage();

    public static void resetSessionState() {
        pageZoomApplied = false;
    }

    public void loginToFF() {
        WebDriver webDriver = requireDriver();
        webDriver.options().setElementTimeout(FulfillmentJsUtil.FAST_ELEMENT_TIMEOUT_MS);

        if (FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            Logger.logReportMessage("Already logged in to Fulfillment Console");
            applyPageZoom(webDriver);
            return;
        }

        waitForLoginOrHomePage();

        if (isManualLogin()) {
            loginManually(webDriver);
            return;
        }

        if (WaitUtil.isDisplay(loginPage.getUserNameField(), 8)) {
            Logger.logReportMessage("Entering username...");
            webDriver.finder().findElement(loginPage.getUserNameField()).sendKeys(ConfigProps.OKTA_USERNAME);
            if (WaitUtil.isDisplayQuiet(loginPage.getNextButton(), 5)) {
                webDriver.finder().findElement(loginPage.getNextButton()).click();
            }
        }

        if (WaitUtil.isDisplay(loginPage.getPasswordField(), 12)) {
            Logger.logReportMessage("Entering Password...");
            webDriver.finder().findElement(loginPage.getPasswordField()).sendKeys(ConfigProps.OKTA_PASSWORD);
            webDriver.finder().findElement(loginPage.getSignInButton()).click();
            sleepQuiet(1000);
        }

        boolean homeLoaded = waitForHomeAfterLogin(webDriver);
        if (!homeLoaded) {
            logLoginDiagnostics(webDriver);
        }
        Verify.hardAssert(homeLoaded, "Login successful — Fulfillment Console home page loaded");

        dismissReleaseNotesIfPresent();
        applyPageZoom(webDriver);
    }

    private boolean isManualLogin() {
        if ("1".equals(System.getenv("FF_MANUAL_LOGIN"))) {
            return true;
        }
        String prop = System.getProperty("manualLogin");
        if (prop != null) {
            return Boolean.parseBoolean(prop);
        }
        return Config.getBoolean("ManualLogin");
    }

    private void loginManually(WebDriver webDriver) {
        Logger.logReportMessage(
                "Manual login: enter your username and password in the browser (waiting up to 5 minutes)...");
        boolean homeLoaded = waitForManualLoginHome(300);
        if (!homeLoaded) {
            try {
                webDriver.browser().getUrl(ConfigProps.getTargetURL());
                sleepQuiet(2000);
                homeLoaded = waitForManualLoginHome(60);
            } catch (Exception e) {
                Logger.logConsoleMessage("Fulfillment reload after manual login failed: " + e.getMessage());
            }
        }
        if (!homeLoaded) {
            logLoginDiagnostics(webDriver);
        }
        Verify.hardAssert(homeLoaded, "Manual login successful — Fulfillment Console home page loaded");
        Logger.logReportMessage("Manual login complete — page zoom deferred until orders grid is ready");
    }

    private boolean waitForManualLoginHome(int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isFulfillmentHomeLoaded()) {
                return true;
            }
            sleepQuiet(1000);
        }
        return isFulfillmentHomeLoaded();
    }

    private boolean waitForHomeAfterLogin(WebDriver webDriver) {
        int primaryTimeout = isSynergyServerLogin() ? SYNERGY_LOGIN_SHELL_TIMEOUT_SEC : DEFAULT_LOGIN_TIMEOUT_SEC;
        int retryTimeout = isSynergyServerLogin() ? SYNERGY_LOGIN_RETRY_TIMEOUT_SEC : 20;
        boolean homeLoaded = waitForHomeWithLoginHandling(webDriver, primaryTimeout);
        if (!homeLoaded) {
            sleepQuiet(1000);
            homeLoaded = waitForHomeWithLoginHandling(webDriver, retryTimeout);
        }
        if (!homeLoaded) {
            try {
                webDriver.browser().getUrl(ConfigProps.getTargetURL());
                sleepQuiet(1500);
                homeLoaded = waitForHomeWithLoginHandling(webDriver, retryTimeout);
            } catch (Exception e) {
                Logger.logConsoleMessage("Fulfillment reload after login failed: " + e.getMessage());
            }
        }
        return homeLoaded;
    }

    private boolean waitForHomeWithLoginHandling(WebDriver webDriver, int timeoutSec) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        setLoginPollElementTimeout(webDriver);
        try {
            while (System.currentTimeMillis() < end) {
                if (isSynergyServerLogin()) {
                    dismissStaySignedInIfPresent();
                } else {
                    advanceOktaLoginIfNeeded(webDriver);
                }
                if (isFulfillmentHomeLoaded()) {
                    if (isSynergyServerLogin() && FulfillmentJsUtil.isFulfillmentLoginShellReady()
                            && !FulfillmentJsUtil.isFulfillmentConsoleReady()) {
                        Logger.logReportMessage(
                                "Synergy login shell ready — applying zoom while grid data continues loading");
                    }
                    return true;
                }
                sleepQuiet(300);
            }
            if (isSynergyServerLogin()) {
                dismissStaySignedInIfPresent();
            } else {
                advanceOktaLoginIfNeeded(webDriver);
            }
            return isFulfillmentHomeLoaded();
        } finally {
            restoreLoginPollElementTimeout(webDriver);
        }
    }

    private boolean isSynergyServerLogin() {
        return !Config.isLocalExecution();
    }

    private boolean isFulfillmentHomeLoaded() {
        if (isSynergyServerLogin() && FulfillmentJsUtil.isFulfillmentLoginShellReady()) {
            return true;
        }
        if (FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            return true;
        }
        return WaitUtil.isDisplayFast(loginPage.getHomePage(), 2);
    }

    /** Synergy service-account login: no Okta Verify push — only optional stay-signed-in. */
    private void dismissStaySignedInIfPresent() {
        if (WaitUtil.isDisplayFast(loginPage.getStaySignedInYes(), 1)) {
            Logger.logReportMessage("Confirming stay signed in...");
            DriverUtil.clickOnElementSafely(loginPage.getStaySignedInYes(), 3);
        }
    }

    private void advanceOktaLoginIfNeeded(WebDriver webDriver) {
        if (WaitUtil.isDisplayFast(loginPage.getOktaVerifyFactor(), 1)) {
            Logger.logReportMessage("Selecting Okta Verify push factor...");
            DriverUtil.clickOnElementSafely(loginPage.getOktaVerifyFactor(), 3);
            sleepQuiet(1000);
        }
        if (WaitUtil.isDisplayFast(loginPage.getOktaPushOrVerifyButton(), 1)) {
            Logger.logReportMessage("Submitting Okta Verify push...");
            DriverUtil.clickOnElementSafely(loginPage.getOktaPushOrVerifyButton(), 3);
            sleepQuiet(1000);
        }
        dismissStaySignedInIfPresent();
    }

    private void setLoginPollElementTimeout(WebDriver webDriver) {
        try {
            webDriver.options().setElementTimeout(LOGIN_POLL_ELEMENT_TIMEOUT_MS);
        } catch (Exception ignored) {
        }
    }

    private void restoreLoginPollElementTimeout(WebDriver webDriver) {
        try {
            webDriver.options().setElementTimeout(FulfillmentJsUtil.FAST_ELEMENT_TIMEOUT_MS);
        } catch (Exception ignored) {
            try {
                webDriver.options().setElementTimeout(LocalCapabilityFactory.DEFAULT_ELEMENT_TIMEOUT);
            } catch (Exception ignoredAgain) {
            }
        }
    }

    private void logLoginDiagnostics(WebDriver webDriver) {
        try {
            Object diag = webDriver.browser().executeScript(
                    "return JSON.stringify({"
                            + "url:location.href,"
                            + "title:document.title,"
                            + "tableViewButton:!!document.querySelector('#tableViewButton'),"
                            + "agRoot:!!document.querySelector('.ag-root,.ag-root-wrapper'),"
                            + "headers:document.querySelectorAll('.ag-header-cell').length"
                            + "});");
            Logger.logConsoleMessage("Fulfillment login diagnostics: " + diag);
        } catch (Exception e) {
            Logger.logConsoleMessage("Fulfillment login diagnostics unavailable: " + e.getMessage());
        }
    }

    private void waitForLoginOrHomePage() {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            if (FulfillmentJsUtil.isFulfillmentConsoleReady()) {
                return;
            }
            if (WaitUtil.isDisplayQuiet(loginPage.getUserNameField(), 1)
                    || WaitUtil.isDisplayQuiet(loginPage.getPasswordField(), 1)) {
                return;
            }
            sleepQuiet(400);
        }
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void ensureMaximizedWithPageZoom() {
        applyPageZoom(requireDriver());
    }

    private void dismissReleaseNotesIfPresent() {
        if (FulfillmentJsUtil.dismissReleaseNotesModal()) {
            Logger.logReportMessage("Dismissed Fulfillment Console release-notes modal");
            sleepQuiet(500);
        }
    }

    private void applyPageZoom(WebDriver webDriver) {
        Logger.logReportMessage("Maximizing browser and applying page zoom to " + PAGE_ZOOM);
        try {
            webDriver.browser().maximizeWindow();
            webDriver.browser().executeScript(
                    "document.documentElement.style.zoom = '" + PAGE_ZOOM + "';"
                            + "if (document.body) { document.body.style.zoom = '" + PAGE_ZOOM + "'; }");
            pageZoomApplied = true;
        } catch (Exception e) {
            Logger.logConsoleMessage("Unable to apply page zoom via script: " + e.getMessage());
        }
    }
}
