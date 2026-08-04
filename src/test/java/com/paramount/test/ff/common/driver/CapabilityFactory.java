package com.paramount.test.ff.common.driver;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.listeners.SuiteListeners;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.TestUtil;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.util.constants.Constants;
import com.synergy.common.DeviceMatcher;
import com.synergy.common.SynergyKey;
import com.synergy.common.enums.*;
import com.synergy.core.driver.AssetRequestPoller;
import com.synergy.core.driver.AssetRequestPollerResults;
import com.synergy.core.driver.DeviceCapabilities;
import com.synergy.core.driver.desktop.DesktopDriver;
import com.synergy.core.driver.web.WebDriver;

import org.json.simple.JSONObject;

import java.io.File;
import java.time.Duration;


public class CapabilityFactory {

	public static final int DEFAULT_ELEMENT_TIMEOUT = 5000;
	private static final int DEFAULT_ELEMENT_POLLTIME = 100;
	private static final int COMMAND_TIMEOUT = 300;

	public static String userDirectoryPath = System.getProperty("user.dir");
	public static String resourcesPath = File.separator + "src" + File.separator + "test" + File.separator + "resources"+ File.separator;
	public static String extensionFile = userDirectoryPath + resourcesPath + "IBM-Aspera-Connect.crx";//BaseTest.uploadExtensionFile(userDirectoryPath + resourcesPath + "IBM-Aspera-Connect.crx");
	public static String appFile = userDirectoryPath + resourcesPath + "Aspera.zip";
	public static String AppPath= File.separator +"Aspera" +File.separator+"Aspera Connect"+File.separator+"bin"+File.separator+"asperaconnect.exe";
	public static String webSession="";
	public static String fileLocation="";
	public static String ImageLocation="";


	@SuppressWarnings("unchecked")
	public static ThreadLocal<WebDriver> initiateDriver1() {
		DeviceCapabilities capabilities = new DeviceCapabilities();
		JSONObject matchObj = new JSONObject();
		JSONObject inclusionObj = new JSONObject();
		JSONObject disableSSL = new JSONObject();
//		new WhiteLister(TestUtil.getServerUrl()).whiteListClient();
		capabilities.addCapability("Platform", ConfigProps.OS);
		capabilities.addCapability("Browser", ConfigProps.BROWSER);
		capabilities.addCapability("OCRType", "GoogleVision");//"Tesseract");
		capabilities.addCapability("ImageMatchSimilarity", .30);
		capabilities.addCapability("MaxTestTime", 60);
		capabilities.addCapability("ImageCollectorInterval", 500);
		capabilities.addCapability("SessionStartRequestTimeout", 1800);
		capabilities.addCapability("BrowserExtension", extensionFile);
		//capabilities.addCapability("DeviceUUID", "6e71210a-1349-41c5-8fcd-93e0b1ff30ae");
		if (ConfigProps.ISLOCAL) {
			inclusionObj.put("ClientID", ConfigProps.CLIENTID);
		} else {
			inclusionObj.put("PrivateLabName", "SynergyPublicDevices");
		}
		matchObj.put("Inclusions", inclusionObj);
		capabilities.addCapability("AssetMatcher", matchObj.toJSONString());
		disableSSL.put("ruleType", "disable ssl");
		capabilities.addCapability("ProxyRules", disableSSL.toJSONString());
		try {
			WebDriver webDriver = new WebDriver(TestUtil.getServerUrl(), capabilities);
			/*
			 * new WebDriver(TestUtil.getServerUrl(), new
			 * AssetRequestPoller(TestUtil.getServerUrl(),
			 * capabilities).withMaxDuration(Duration.ofMinutes(5))
			 * .withMaxSessionStartAttempts(2).withMaxSessionStartDuration(Duration.
			 * ofMinutes(3)) .ignoreNoDevicesAvailable(true).initiate().poll());
			 */

			BaseTest.driver.set(webDriver);

		} catch (Exception e) {
			Logger.logReportMessage("Set Driver Failed due to: "+ e.getMessage()+"\n Check Synergy logs for more info.");
			System.err.println(e.getMessage());

		}
		if (getDriver() != null) {
			getDriver().options().setCommandTimeout(COMMAND_TIMEOUT);
			getDriver().options().setElementTimeout(DEFAULT_ELEMENT_TIMEOUT);
			getDriver().options().setElementPollInterval(DEFAULT_ELEMENT_POLLTIME);
			getDriver().options().setSocketTimeout(120);

			SuiteListeners.BROWSER_NAME = BaseTest.driver.get().info().getBrowserInfo().getBrowserName();
			SuiteListeners.BROWSER_VERSION = BaseTest.driver.get().info().getBrowserInfo().getBrowserVersion();
			webSession=BaseTest.driver.get().getSessionID();
			Logger.logMessage("WebDriver Session ID: " + BaseTest.driver.get().getSessionID());
			if (!ConfigProps.ISLOCAL) {
				BaseTest.uploadFileServersDownloadFolder(Constants.ASPERA_ZIP);
			}
		}
		return BaseTest.driver;
	}




	public static WebDriver getDriver() {
		return BaseTest.driver.get();
	}


	//--------------------------------------------------------------------------------------------------------------

	public static ThreadLocal<WebDriver> initiateDriverCommented() {

		WebDriver webDriver;
		DesktopDriver desktopDriver=null;
		DeviceCapabilities capabilities= new DeviceCapabilities();


		capabilities.addCapability(SessionCapabilities.BROWSER_EXTENSION, extensionFile);
		capabilities.addCapability(SessionCapabilities.OCR_TYPE, "GoogleVision");
		capabilities.addCapability(SessionCapabilities.DESKTOP_UI_LOGGER, true);
		capabilities.addCapability(SessionCapabilities.IMAGE_MATCH_SIMILARITY, .30);
		capabilities.addCapability(SessionCapabilities.MAX_TEST_TIME, 60);
		capabilities.addCapability(SessionCapabilities.IMAGE_COLLECTOR_INTERVAL, 500);
		capabilities.addCapability(SessionCapabilities.SESSION_START_REQUEST_TIMEOUT, 1800);

		if (ConfigProps.ISLOCAL) {
			capabilities.addCapability(SessionCapabilities.CLIENT_ID, ConfigProps.CLIENTID);
			//inclusionObj.put("ClientID", ConfigProps.CLIENTID);
		}
		/*else {
			capabilities.addCapability(SessionCapabilities.PRIVATE_LAB_NAME, "SynergyPublicDevices");
			//inclusionObj.put("PrivateLabName", "SynergyPublicDevices");
		}*/
		JSONObject disableSSL = new JSONObject();
		disableSSL.put("ruleType", "disable ssl");
		capabilities.addCapability(SessionCapabilities.PROXY_RULES, disableSSL.toJSONString());


		// First, create a device matcher with the target platform (Mac/Windows) and your desired Browser type (chrome/firefox/safari/microsoftedge)
		DeviceMatcher deviceMatcher = new DeviceMatcher()
				.withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
				.withInclusion(DeviceMatchType.BROWSER, BrowserType.CHROME.value())
				//.withInclusion(DeviceMatchType.BROWSER, BrowserType.EDGE.value())
				.withInclusion(DeviceMatchType.CLIENT_ID, ConfigProps.CLIENTID);


		AssetRequestPollerResults assetRequestPollerResults = new AssetRequestPoller(SynergyKey.fromSynergyConfigFile(),
				deviceMatcher, capabilities)
				.withMaxDuration(Duration.ofMinutes(30))
				.withMaxSessionStartAttempts(5)
				.withMaxSessionStartDuration(Duration.ofMinutes(10))
				.initiate()
				.pollForDevice();

		// Next, construct your webdriver instance using your synergy key and the above device matcher
		try {
			webDriver = new WebDriver(SynergyKey.fromString(ConfigProps.USER_KEY), deviceMatcher, capabilities);
			BaseTest.driver.set(webDriver);
			System.out.println("WebDriver Session ID: " + webDriver.getSessionID());
			getDriver().options().setCommandTimeout(COMMAND_TIMEOUT);
			getDriver().options().setElementTimeout(DEFAULT_ELEMENT_TIMEOUT);
			getDriver().options().setElementPollInterval(DEFAULT_ELEMENT_POLLTIME);
			getDriver().options().setSocketTimeout(120);

			SuiteListeners.BROWSER_NAME = BaseTest.driver.get().info().getBrowserInfo().getBrowserName();
			SuiteListeners.BROWSER_VERSION = BaseTest.driver.get().info().getBrowserInfo().getBrowserVersion();


			String sessionID = webDriver.getSessionID();
			String uniqueDeviceID=webDriver.getSessionInfo().get("unique_device_id").toString();


		}catch (Exception e){
			e.printStackTrace();
			Logger.logMessage("Failed to initialize webDriver, check console for more details");
		}

		return BaseTest.driver;
	}

	public static ThreadLocal<DesktopDriver> initiateDesktopDriver() {
		DesktopDriver desktopDriver=null;

		JSONObject disableSSL = new JSONObject();
		disableSSL.put("ruleType", "disable ssl");

		// Constructing Desktop Driver
		DeviceMatcher deviceMatcherDeskTop= new DeviceMatcher()
				.withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
				.withInclusion(DeviceMatchType.DEVICE_UUID, BaseTest.driver.get().getSessionInfo().get("unique_device_id").toString());





		DeviceCapabilities capabilitiesDesktop= new DeviceCapabilities();
		capabilitiesDesktop.addCapability(SessionCapabilities.OCR_TYPE, "GoogleVision");
		capabilitiesDesktop.addCapability(SessionCapabilities.SESSION_ID, BaseTest.driver.get().getSessionID());
		capabilitiesDesktop.addCapability(SessionCapabilities.MAX_TEST_TIME, 60);
		capabilitiesDesktop.addCapability(SessionCapabilities.PROXY_RULES, disableSSL.toJSONString());
		capabilitiesDesktop.addCapability("unique_device_id", BaseTest.driver.get().getSessionInfo().get("unique_device_id").toString());

		// Next, construct your desktop driver instance using your synergy key, uniqueDeviceID and the above device matcher
		try{
			desktopDriver= new DesktopDriver(SynergyKey.fromString(ConfigProps.USER_KEY), deviceMatcherDeskTop, capabilitiesDesktop);
			BaseTest.desktopDriver.set(desktopDriver);
			Logger.logMessage("Desktop Driver Session ID: "+desktopDriver.getSessionID());

		}catch (Exception e){
			e.printStackTrace();
			Logger.logMessage("Failed to initialize Desktop driver, check console for more details");
		}


		return BaseTest.desktopDriver;
	}

	DeviceCapabilities capabilities= new DeviceCapabilities();
	DeviceMatcher deviceMatcher = new DeviceMatcher()
			.withInclusion(DeviceMatchType.PLATFORM, PlatformType.WINDOWS.value())
			.withInclusion(DeviceMatchType.BROWSER, BrowserType.CHROME.value())
			//.withInclusion(DeviceMatchType.BROWSER, BrowserType.EDGE.value())
			.withInclusion(DeviceMatchType.CLIENT_ID, ConfigProps.CLIENTID);

	AssetRequestPollerResults assetRequestPollerResults = new AssetRequestPoller(SynergyKey.fromSynergyConfigFile(),
			deviceMatcher, capabilities)
			.withMaxDuration(Duration.ofMinutes(30))
			.withMaxSessionStartAttempts(5)
			.withMaxSessionStartDuration(Duration.ofMinutes(10))
			.initiate()
			.pollForDevice();


	public  ThreadLocal<WebDriver> initiateDriver(){
	WebDriver webDriver;



		//capabilities.addCapability("resolution", 768 * 1366);
//		capabilities.addCapability("ClientID", "0000-0007-2681-5914-3744-3918-85");
		//if (!TestUtil.isLabExecution())
		//capabilities.addCapability("ClientID", TestUtil.getClientID());
		Logger.logMessage("Desktop Caps are :" + capabilities.getCapabilitiesAsJSON());
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

	}


