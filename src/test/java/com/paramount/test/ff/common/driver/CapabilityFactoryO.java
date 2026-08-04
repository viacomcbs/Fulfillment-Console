package com.paramount.test.ff.common.driver;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.listeners.SuiteListeners;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.TestUtil;
import com.paramount.test.ff.common.util.props.IProps;
import com.synergy.common.DeviceMatcher;
import com.synergy.common.SynergyKey;
import com.synergy.common.enums.BrowserType;
import com.synergy.common.enums.DeviceMatchType;
import com.synergy.common.enums.PlatformType;
import com.synergy.core.driver.AssetRequestPoller;
import com.synergy.core.driver.AssetRequestPollerResults;
import com.synergy.core.driver.DeviceCapabilities;
import com.synergy.core.driver.web.WebDriver;
import com.synergy.core.httpexecutor.AllowLister;
import com.synergy.core.reporting.AllureReportGenerator;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.time.Duration;

public class CapabilityFactoryO {
	

	public static final int DEFAULT_ELEMENT_TIMEOUT = 30000;
    private static final int DEFAULT_ELEMENT_POLLTIME = 1000;
    private static final int DEFAULT_INTERECT_DELAY = 3000;
    private static final int COMMAND_TIMEOUT = 300;


    public static String userDirectoryPath = System.getProperty("user.dir");
    public static String resourcesPath = File.separator + "src" + File.separator + "test" + File.separator + "resources"+ File.separator;
    public static String extensionFile = userDirectoryPath + resourcesPath + "IBM-Aspera-Connect.crx";//BaseTest.uploadExtensionFile(userDirectoryPath + resourcesPath + "IBM-Aspera-Connect.crx");
    public static String appFile = userDirectoryPath + resourcesPath + "Aspera.zip";
    public static String AppPath= File.separator +"Aspera" +File.separator+"Aspera Connect"+File.separator+"bin"+File.separator+"asperaconnect.exe";
    public static String webSession="";
    public static String fileLocation="";
    public static String ImageLocation="";
    public static SynergyKey serverKey = SynergyKey.fromString("0749d96b-00cd-493b-ac84-d536b27f5120");
    public static ThreadLocal<WebDriver> initiateDriver() throws InterruptedException {

       JSONObject disableSSL = new JSONObject();
       JSONObject exclusionObj = new JSONObject();
       JSONObject matchObj = new JSONObject();
       JSONObject inclusionObj = new JSONObject();
       inclusionObj.put("PrivateLabName", "SynergyPublicDevices");
       matchObj.put("Inclusions", inclusionObj);
       JSONArray ruleArray = new JSONArray();
       JSONObject bypassProxyRewrite = new JSONObject();
       bypassProxyRewrite.put("ruleType", "bypass proxy");
       bypassProxyRewrite.put("hosts", "sso.viacomcloud.com");
       JSONObject disableSSLProxyRewrite = new JSONObject();
       disableSSLProxyRewrite.put("ruleType", "disable ssl");
       ruleArray.add(bypassProxyRewrite);
       ruleArray.add(disableSSLProxyRewrite);
       DeviceCapabilities capabilities = new DeviceCapabilities();
       capabilities.addCapability("ProxyRules", ruleArray.toJSONString());
       capabilities.addCapability("AssetMatcher", matchObj.toJSONString());
       JSONObject options = new JSONObject();

        //new WhiteLister(TestUtil.getServerUrl()).whiteListClient();
        new AllowLister(SynergyKey.fromString(IProps.ConfigProps.USER_KEY)).addClientToAllowList
                ();


       options.put("switch1", "--start-maximized");
       options.put("switch2", "--disable-extensions");
       options.put("googlegeolocationaccess.enabled", true);
       options.put("profile.default_content_setting_values.geolocation", 1);
       options.put("profile.default_content_setting_values.notifications", 2);

       //gets rid of 'Save password?' popup by disabling
       options.put("credentials_enable_service", false);
       options.put("profile.password_manager_enabled", false);

       options.put("autofill.credit_card_enabled", false);

       options.put("autofill.profile_enabled", false);
       capabilities.addCapability("ChromeOptions", options);
       capabilities.addCapability("ProxyAutoSaveMinutes", 9);
       capabilities.addCapability("AcceptOrphanedAlerts", false);
       capabilities.addCapability("MaxThreadCount", 10);
       capabilities.addCapability("ChromeOptions", options);
       capabilities.addCapability("Platform", "Windows");
       capabilities.addCapability("UseProxyGUI", true);
       capabilities.addCapability("VerifyNetworkConnectivity", true);
       capabilities.addCapability("Browser", "Chrome");
//     capabilities.addCapability("ImageMatchSimilarity", .90);
//     capabilities.addCapability("ExternalProxyHost", TestRunInfo.getGeoVPN().getPublicHost());
       capabilities.addCapability("OCRType", "GoogleVision");
       capabilities.addCapability("MaxTestTime", 20);
       capabilities.addCapability("AllowSystemProxy", 15);
       capabilities.addCapability("Language", "en_US");
       capabilities.addCapability("DNSType","Viacom");
       capabilities.addCapability("ImageCollectorInterval", 1000);


       capabilities.addCapability("ProxyPort", 8888);


       capabilities.addCapability("LaunchGridHub", true);
       capabilities.addCapability("GridHubPort", 4445);
       capabilities.addCapability("LaunchGridNode", true);
       capabilities.addCapability("GridNodePort", 4446);
       capabilities.addCapability("GridHubIPAddress", "localhost");

        DeviceMatcher deviceMatcher = new DeviceMatcher()
                .withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
                .withInclusion(DeviceMatchType.BROWSER, BrowserType.CHROME.value())
                //.withInclusion(DeviceMatchType.BROWSER, BrowserType.EDGE.value())
                .withInclusion(DeviceMatchType.CLIENT_ID, IProps.ConfigProps.CLIENTID);


       WebDriver webDriver;
//     Thread.sleep(10000);
//  capabilities.removeCapability("ClientID");
       DeviceMatcher deviceMatchers = new DeviceMatcher()
             .withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
             .withInclusion(DeviceMatchType.BROWSER, "Chrome")

             .withInclusion(DeviceMatchType.PRIVATE_LAB_NAME, "SynergyPublicDevices");

//     AssetRequestPollerResults assetRequestPollerResults = new AssetRequestPoller(
//           SynergyKey.fromString(IProps.ConfigProps.USER_KEY), deviceMatchers)
//           .withMaxDuration(Duration.ofMinutes(30))
//           .ignoreNoDevicesAvailable(true)
//           .withMaxSessionStartAttempts(5) // gives our session start attempts an extra attempt
//           .withMaxSessionStartDuration(Duration.ofMinutes(4)) // increases the session start max duration to 4 minutes
//           .initiate()
//           .pollForDevice();

       webDriver = new WebDriver(getAssetRequestPollerResultsObject(SynergyKey.fromString(IProps.ConfigProps.USER_KEY), capabilities, deviceMatchers));



       System.out.println("Start :" + TestUtil.getServerUrl());
       BaseTest.driver.set(webDriver);
       System.out.println("Start :" + TestUtil.getServerUrl());


       getDriver().options().setCommandTimeout(COMMAND_TIMEOUT);
       getDriver().options().setElementTimeout(DEFAULT_ELEMENT_TIMEOUT);
       getDriver().options().setElementPollInterval(DEFAULT_ELEMENT_POLLTIME);
       getDriver().options().setSocketTimeout(60);
       SuiteListeners.BROWSER_NAME = BaseTest.driver.get().info().getBrowserInfo().getBrowserName();
       SuiteListeners.BROWSER_VERSION = BaseTest.driver.get().info().getBrowserInfo().getBrowserVersion();
       String sessionIDLogTag = AllureReportGenerator.getSessionIDLogTag(BaseTest.driver.get());
       Logger.logMessage(sessionIDLogTag);
       String Id = getDriver().getSessionID();

       System.out.println("Recording link: https://www.synergyplatform.tech/tests?sessionID=" + getDriver().getSessionID() + "&open=true");


       //System.out.println("Session Id: " + Id );
       return BaseTest.driver;

    }
    public static WebDriver getDriver() {
       return BaseTest.driver.get();
    }
    

    private static AssetRequestPollerResults getAssetRequestPollerResultsObject(SynergyKey key, DeviceCapabilities caps, DeviceMatcher devicematcher) {
       AssetRequestPollerResults assetRequestPollerResults = new AssetRequestPoller(
             key, devicematcher, caps)
             .ignoreNoDevicesAvailable(true)
             .withMaxDuration(Duration.ofMinutes(30))
             .withMaxSessionStartAttempts(5)
             .withMaxSessionStartDuration(Duration.ofMinutes(5))
             .initiate()
             .pollForDevice();
       return assetRequestPollerResults;
    }
	
}
