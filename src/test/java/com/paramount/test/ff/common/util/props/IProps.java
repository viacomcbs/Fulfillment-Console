package com.paramount.test.ff.common.util.props;

import com.paramount.test.ff.common.util.Config;

public class IProps {

	public interface ParamProps {
		String TEST_RIG = "TestRig";
	}

	public interface GroupProps {
		String FULL = "Full";
		String PERFORMANCE = "Performance";
		String SMOKE = "Smoke";
		String BROKEN = "Broken";
		String DEBUG = "Debug";
		String MAIN_NAV = "MainNav";
		String CONTACTS = "Contacts";
		String SALES = "Sales";
		String REGRESSION = "Regression";
		String SANITY = "Sanity";
		String WEBSITE = "Website";
		String WEBSITEGROUP = "WebsiteGrp";
		String CONTRACT = "Contract";
		String ORDERS1 = "Orders1";
		String ORDERS = "Orders";
		String AGREEMENT = "Agreement";
		String CRM = "Crm";
		String PACKAGES = "Packages";
		String ADMIN = "Admin";
		String ACCOUNTS = "Accounts";
		String OFFICE = "Office";
		String CAMPAIGN = "Campaign";
		String OPPORTUNITY = "Opportunity";
		String DASHBOARD = "Dashboard";
		String REPORTS = "Reports";
		String USERPERMISSION = "UserPermission";
		String REGIONSPECIFIC = "RegionSpecific";
		String WEEKLYEXECUTION = "WeeklyExecution";
		String SALESESTIMATE = "SalesEstimate";
		String ROLESANDPROFILES = "RolesAndProfiles";
		String CORPAGENCIES = "CorporateAgencies";
		String LOCALAGENCIES = "LocalAgencies";
		String PRODUCTS = "Products";
		String AUDIENCERATINGS = "AudienceRatings";
		String PROGRAMS = "Programs";
		String RECEIVER = "Receiver";
	}

	public interface AttributeProps {

	}

	public interface StaticProps {
		String GLOBAL_USER = "admin@";
		String ALL = "All";
		String ATTACHMENT_DATE_FORMAT = "MMddyyhhmmssSSSa";
		String PACKAGE_DATE_FORMAT = "MMddyyhhmmss";
		Integer MINOR_PAUSE_MS = 500;
		Integer SMALL_PAUSE_MS = 2000;
		Integer SMALL_PAUSE_S = 2;
		Integer MEDIUM_PAUSE_MS = 3000;
		Integer MEDIUM_PAUSE_S = 3;
		Integer LARGE_PAUSE_MS = 5000;
		Integer LARGE_PAUSE_S = 5;
		Integer XLARGE_PAUSE_MS = 10000;
		Integer XLARGE_PAUSE_S = 10;
		Integer IMPLICIT_WAIT_MS = 20000;
		Integer PAGE_LOAD_WAIT_S = 30;
		Integer WAIT_PLAYER_TIMEOUT = 60;
		Integer WAIT_PLAYER_PLAY = 100;
		Integer SLEEP_INTERVAL_IN_SEC = 1;
		Integer AD_TIME_OUT_S = 250;
		String LOCATOR_VARIABLE = "$inText";
		String LOCATOR_VARIABLE_UPPER = "$inTextU";
		String LOCATOR_VARIABLE_LOWER = "$inTextL";
		String LOCATOR_XPATH = "XPATH";
		String LOCATOR_CSS = "CSS";
		String LOCATOR_ID = "ID";
		String APPLICATION_LOG = "Application: ";
		String ETID_CATEGORY_LOG = "Enterprise Tester ID: ";
		String ALL_BROWSERS_DATA_PROVIDER = "allBrowsersDataProvider";
		String BROWSER_PERFORMANCE_DATA_PROVIDER = "browserPerformanceDataProvider";
		String LATEST_BROWSERS_DATA_PROVIDER = "latestBrowsersDataProvider";
		String ET_URL = "http://enttester-app-1.mtvn.ad.viacom.com/et5/home/index.rails#/script/edit/";
		String TEST_ID = "TestID-";
		String FAIL_FLG = "F";
		String SKIP_FLG = "S";
		String PASS_FLG = "P";
	}

	public interface ConfigProps {

		String OS_VERSION = Config.getString("OSVersion");
		String DEVICE_CATEGORY = Config.getString("DeviceCategory");
		String DEVICE_NAME = Config.getString("DeviceName");
		String VERSION = Config.getString("Version");

		Integer SCROLL_X = 0;
		Integer SCROLL_Y = 50;
		String EVENTS_ARCSTAGE = Config.getString("EventsArcstage");
		Boolean CLEAR_AKAMAI_CACHE = Config.getBoolean("AkamaiCacheClear");
		String JIRA_TICKET_ID = Config.getString("JiraTicketID");

		// DEFAULT ENV CONFIG
		String OKTA_USERNAME = Config.getString("Username");
		String OKTA_PASSWORD = Config.getString("Password");

		//Filter for Apps
		String APP_NAME = Config.getString("Application");

		
		static String getTargetURL() {
			String env= Config.getString("TestEnvironment").toLowerCase();
			
			String url="https://dev-operationsconsole.paramountmsc.com/fulfillment/";
			if(env.equals("dev")) {
				url= Config.getString("TargetUrlDEV");
			}else if(env.equals("uat")) {
				url= Config.getString("TargetUrlUAT");
			}else if(env.equals("prod")) {
				url= Config.getString("TargetUrlPROD");
			}else if(env.equals("pr")) {
				url= Config.getString("TargetUrlPR");
			}
			return url;
		}
		String GO_TARGET_URL = getTargetURL();

		Integer TESTRAIL_PROJECT_ID = Config.getInt("TestRailProjectId");
		String TEST = "Test";
		String TEST_TYPE = Config.getString("TestType");

		// SYNERGY CONFIG
		String LAB_URL = Config.getString("LabUrl");
		String USER_KEY = Config.getString("UserKey");
		
		String LOCAL_URL= Config.getString("LocalURL");
		boolean ISLOCAL= Config.getBoolean("LocalExecution");
		String CLIENTID=Config.getString("ClientID");

		// RUNTIME CONFIG
		Boolean RUN_AS_FACTORY = Config.getBoolean("RunAsFactory");
		String BROWSER = Config.getString("Browser");
		String OS = Config.getString("OS");
		String GUI_TYPE = Config.getString("GuiType");
		Boolean RUN_ON_SAUCE = Config.getBoolean("RunOnSauce");
		Boolean UPLOAD_REPORT_JENKINS = Config.getBoolean("UploadReportToJenkins");
		Boolean SEND_REPORT_AUTOEMAILS = Config.getBoolean("SendReportAutoEmails");
		Boolean SEND_REPORT_CHAT = Config.getBoolean("SendChatReport");
		Boolean RERUN_ON_FAILURE = Config.getBoolean("ReRunOnFailure");
		Integer RERUN_ON_FAILURE_COUNT = Config.getInt("ReRunOnFailureCount");
		String SEND_REPORT_EMAIL_ADDRESS = Config.getString("SendReportEmailAddress");
		Boolean SEND_SPLUNK_DATA = Config.getBoolean("SendSplunkData");
		String PROXY_PORT = Config.getString("ProxyPort");
		String PROXY_HOST = Config.getString("ProxyHost");
		Integer PROXY_DEBUG_PORT = Config.getInt("ProxyDebugPort");
		long NETWORK_BANDWIDTH = Long.parseLong(Config.getString("NetworkBandwidth"));
		Integer NETWORK_LATENCY = Config.getInt("NetworkLatency");
		Boolean SET_HUE_LIGHTS = Config.getBoolean("SetHueLights");
		String APPLICATION = Config.getString("Application");
		String TEST_ENVIRONMENT = Config.getString("TestEnvironment");

		// DEFAULT ENV CONFIG
		String REGION = Config.getString("Region");
		String USERNAME = Config.getString("Username");
		String PASSWORD = Config.getString("Password");
		

		// FW
		String FW_URL = Config.getString("FWUrl");
		String FW_USERNAME = Config.getString("FWUsername");
		String FW_PASSWORD = Config.getString("FWPassword");

		// APP CONFIG
		String ELEMENT_FILE_PATH = Config.getFilePath("PathToElements");
		String SAUCE_CONNECT_FILE_PATH = Config.getFilePath("PathToSauceConnect");
		String APPLICATION_TITLE = Config.getString("ApplicationTitle");
		String BUILD_NUMBER = Config.getString("BuildNumber");

		// SAUCE CONFIG
		String SAUCE_USERNAME = Config.getString("SauceUsername");
		String SAUCE_KEY = Config.getString("SauceKey");

		// SPLUNK CONFIG
		String SPLUNK_USERNAME = Config.getString("SplunkUsername");
		String SPLUNK_PASSWORD = Config.getString("SplunkPassword");
		String SPLUNK_INDEX = Config.getString("SplunkIndex");
		String POST_SPLUNK = Config.getString("PostSplunkData");
		
		// REPORT CONFIG
		String EMAIL_SENDER_ADDRESS = Config.getString("EmailSenderAddress");
		String SCREENSHOT_FILE_PATH = Config.getFilePath("PathToScreenshots");
		String ALLURE_RESULTS_PATH = Config.getFilePath("PathToAllureResults");
		String REPORTING_URL = Config.getString("ReportingUrl");
		String MEGA_BEACON_URL = Config.getString("MegaBeaconUrl");
		String S3_BUCKET_NAME = Config.getString("S3BucketName");
		String HUE_BRIDGE_IP = Config.getString("HueBridgeIP");
		String HUE_USER_ID = Config.getString("HueUserId");
		String HUE_WEB_LIGHT_ID = Config.getString("HueWebLightId");

		// CHAT CONFIG
		String DTE_CHAT_WEBHOOK_URL = Config.getString("DTEChatWebHookUrl");

		// 3RD PARTY APP CONFIG

		// WEBDRIVER CONFIG
		Integer MAX_WAIT_TIME = Config.getInt("WaitForWaitTime");
		Integer VIDEO_PLAYING_WAIT_TIME = Config.getInt("VideoPlayingWaitTime");
		Integer VIDEO_IMAGE_WAIT_TIME = Config.getInt("VideoImageWaitTime");
		String SERVER_COMMAND_TIMEOUT = Config.getString("ServerCommandTimeout");
		Integer POLLING_TIME = Config.getInt("PollingTime");
		Integer VIDEO_COMPARE_RETRY_COUNT = Config.getInt("VideoCompareRetryCount");
		Integer PAGE_LOAD_WAIT_TIME = Config.getInt("PageLoadWaitTime");

		// RERUN FAILED TC ONLY
		Boolean RERUN_FAILED_CASES_FLAG = Config.getBoolean("reRunFlg");

		// EXECUTE FAILED TC ONLY
		Boolean EXECUTE_FAILED_CASES = Config.getBoolean("executeOnlyFailedAndSkippedTests");
		Boolean UPLOAD_PDF = Config.getBoolean("UploadPDF");

		int THREAD_COUNT = Config.getInt("ThreadCount");
		// Failure video
		boolean FAILURE_VIDEO_RECORDING = Config.getBoolean("FailureVideoRecording");
		
		// Update testrail
		String POST_DATA_TO_TESTRAIL = Config.getString("PostDataToTestRail");
		
		// Release name
		String RELEASE_NAME = Config.getString("ReleaseName");
	}


}
