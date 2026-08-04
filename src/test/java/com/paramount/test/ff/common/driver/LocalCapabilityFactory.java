package com.paramount.test.ff.common.driver;

import java.time.Duration;

import com.paramount.test.ff.common.listeners.SuiteListeners;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.TestUtil;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.core.httpexecutor.AllowLister;
import com.synergy.core.httpexecutor.WhiteLister;
import org.json.simple.JSONObject;
import com.synergy.common.DeviceMatcher;
import com.synergy.common.SynergyKey;
import com.synergy.common.enums.BrowserType;
import com.synergy.common.enums.DeviceMatchType;
import com.synergy.common.enums.PlatformType;
import com.synergy.core.driver.AssetRequestPoller;
import com.synergy.core.driver.AssetRequestPollerResults;
import com.synergy.core.driver.DeviceCapabilities;
import com.synergy.core.driver.web.WebDriver;
import com.paramount.test.ff.common.base.BaseTest;

public class LocalCapabilityFactory {
    public static final int DEFAULT_ELEMENT_TIMEOUT = 60000; // 60000
    private static final int DEFAULT_ELEMENT_POLLTIME = 1000;
    private static final int COMMAND_TIMEOUT = 300;
    /** Synergy public lab rejects MaxTestTime values above 30 minutes; local device allows up to 60. */
    private static int maxTestTimeMinutes() {
        return Config.isLocalExecution() ? 60 : 30;
    }

    @SuppressWarnings("unchecked")
    public static ThreadLocal<WebDriver> initiateDriver() {
        JSONObject disableSSL = new JSONObject();
        disableSSL.put("ruleType", "disable ssl");
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.addCapability("Platform", "Windows");
        capabilities.addCapability("Browser", "Chrome");
        //capabilities.addCapability("Browser", "MicrosoftEdge");
        capabilities.addCapability("OCRType", "GoogleVision");
        capabilities.addCapability("MaxTestTime", maxTestTimeMinutes());
        capabilities.addCapability("RegisterPersonalDevice", false);
        capabilities.addCapability("SessionStartRequestTimeout", 300);
        capabilities.addCapability("ProxyRules", disableSSL.toJSONString());
        DeviceMatcher deviceMatcher = new DeviceMatcher()
                .withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
                .withInclusion(DeviceMatchType.BROWSER, BrowserType.CHROME.value());
        boolean isLocal = Config.isLocalExecution();
        if (isLocal) {
            deviceMatcher.withInclusion(DeviceMatchType.CLIENT_ID, Config.getString("ClientID"));
        } else {
            deviceMatcher.withInclusion(DeviceMatchType.PRIVATE_LAB_NAME, "SynergyPublicDevices");
        }

        SynergyKey synergyKey = isLocal
                ? SynergyKey.fromSynergyConfigFile()
                : SynergyKey.fromString(Config.getString("UserKey"));

        AssetRequestPollerResults assetRequestPollerResults = new AssetRequestPoller(synergyKey,
                deviceMatcher, capabilities)
                .withMaxDuration(Duration.ofMinutes(30))
                .withMaxSessionStartAttempts(5)
                .withMaxSessionStartDuration(Duration.ofMinutes(10))
                .initiate()
                .pollForDevice();
        new WhiteLister(TestUtil.getServerUrl()).whiteListClient();
        new AllowLister(SynergyKey.fromString(ConfigProps.USER_KEY)).addClientToAllowList();
        Logger.logMessage((isLocal ? "Local" : "Synergy server")
                + " execution - Desktop Caps are :" + capabilities.getCapabilitiesAsJSON());
        //BaseTest.webDriver.set(new WebDriver(TestUtil.getServerUrl(), capabilities));
        BaseTest.driver.set(new WebDriver(assetRequestPollerResults));
        BaseTest.driver.get().options().setCommandTimeout(COMMAND_TIMEOUT);
        BaseTest.driver.get().options().setElementTimeout(DEFAULT_ELEMENT_TIMEOUT);
        BaseTest.driver.get().options().setElementPollInterval(DEFAULT_ELEMENT_POLLTIME);
//		getDriver().options().setCommandTimeout(COMMAND_TIMEOUT);
//		getDriver().options().setElementTimeout(DEFAULT_ELEMENT_TIMEOUT);
//		getDriver().options().setElementPollInterval(DEFAULT_ELEMENT_POLLTIME);
        String sessionID = BaseTest.driver.get().getSessionID();
        Logger.logMessage("WebDriver Session ID :" + sessionID);
        Logger.logMessage("WebDriver Session Info :" + BaseTest.driver.get().getSessionInfo());
        SuiteListeners.BROWSER_NAME = BaseTest.driver.get().info().getBrowserInfo().getBrowserName();
        SuiteListeners.BROWSER_VERSION = BaseTest.driver.get().info().getBrowserInfo().getBrowserVersion();
        return BaseTest.driver;
    }

    public static WebDriver getDriver() {
        return BaseTest.driver.get();
    }
}
