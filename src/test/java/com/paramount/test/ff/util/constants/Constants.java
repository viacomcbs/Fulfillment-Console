package com.paramount.test.ff.util.constants;

import java.io.File;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.fileproperties.ConfigurationUtility;


public class Constants {
	
	public static String userDir= System.getProperty("user.dir");
	public static String srcJavaPath= userDir + File.separator + "src" + File.separator + "test" + File.separator+ "java" +File.separator;
	public static String srcResourcePath=userDir + File.separator + "src" + File.separator + "test" + File.separator+ "resources" +File.separator;
	public static String configPropertiesPath= srcResourcePath + "config.properties";
	public static String testReportsPath= userDir + File.separator + "Test Reports" + File.separator;
	public static String testDataPath=srcResourcePath + "Test Data" + File.separator;
	
	public static String testFilePath=srcResourcePath + "Files" + File.separator;
	
	public static int WAIT_TIME_MIN=60;
	public static int WAIT_TIME_SEC=10;
	
	public static String CHROME="chrome";
	public static String EDGE="edge";
	public static String FIREFOX="firefox";
	public static String env= ConfigurationUtility.getConfigProperty("env");
	public static String version= ConfigurationUtility.getConfigProperty("version");
	public static String localExecution=ConfigurationUtility.getConfigProperty("execute");
		
	public static String APP_URL= appURL(version, env);
	
	public static String ASPERA_EXT_PATH=srcResourcePath + "IBM-Aspera-Connect.crx";
	public static String TEST_DATA_FILE= getTestDataFile();
	public static String TEST_ASSET_FILES= srcResourcePath+ "Test Assets";
	
	public static String ASPERA_ZIP=String.join(File.separator, System.getProperty("user.dir"), "src", "test", "resources",
			"Aspera.zip");
	
	public static String getTestDataFile() {
		String testData="";
		if(version.equals("mip")) {
			testData=testDataPath + "TestMetadata_"+env+"_env.xlsx";
		}else if(version.equals("mfu")) {
			testData=testDataPath + "Pible_4.5_Metadata_dev.xlsx";
			//System.out.println("Under mfu testdata -->>"+ testData);
			
		}
		return testData;
	}
	
	public static String getAppURL(String env) {		
		if(env.equals("dev")) {
			return "https://dev-operationsconsole.paramountmsc.com/fulfillment/main";
		}else
		if(env.equals("uat")) {
			return "https://uat-operationsconsole.paramountmsc.com/fulfillmenmain";
		}else
		if(env.equals("prod")) {
			return "https://operationsconsole.paramountmsc.com/fulfillment/main";
		}else {
			System.err.println("Please enter env in Config.properties file!");
		}
		
		return "Please enter env in Config.properties file!";
	}
	
	public static String appURL(String version, String env) {
		Logger.logConsoleMessage("Script is running on MIP version :: " +version);
		if (version.equals("mip")){
			return getAppURL(env);
		} 
		else if(version.equals("mfu")){
			return getAppURLMIP2(env);
		}else{
			System.err.println("Please mention version of application -mip1/mip2 in Config.properties file!");
		}
		return "Please mention version of application -mip1/mip2";
	}
	
	public static String getAppURLMIP2(String env) {		
		if(env.equals("dev")) {
			return "https://dev.contentplatform.viacom.com/mip2";
		}else
		if(env.equals("uat")) {
			return "https://uat.contentplatform.viacom.com/mip2";
		}else
		if(env.equals("prod")) {
			return "https://contentplatform.viacom.com/mip2";
		}else {
			System.err.println("Please enter env in Config.properties file!");
		}
		
		return "Please enter env in Config.properties file!";
	}

	public static String getProvider(String forWhat){
		String env= Config.getString("TestEnvironment").toLowerCase();
		String provider="";
		if(env.equals("uat")){
			if(forWhat.equalsIgnoreCase("status")) {
				provider=Config.getString("UATProvider_E2E");
			}else {
				provider= Config.getString("UATProvider_INGEST");
			}
		}else if(env.equals("dev")){
			provider= Config.getString("DEVProvider_INGEST");
		}
		return provider;
	}

	public static String getOrder(String forWhat){
        return Config.getString(getProvider(forWhat));
	}

	public static String getOldOrder(){
		String env=Config.getString("TestEnvironment").toLowerCase();
		String order="";
		if(env.equals("uat")){
			order= "76380d0b-4cc1-4b3f-8317-033bfb3ccf56";
		}else if(env.equals("dev")){
			order= "400f9131-f7fc-4fc0-b8f9-78d5dc555f29";
		}
		return order;
	}
	
}
