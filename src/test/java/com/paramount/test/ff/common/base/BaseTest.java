package com.paramount.test.ff.common.base;

import com.paramount.test.ff.common.driver.LocalCapabilityFactory;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.*;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.common.utils.SleepUtils;
import com.synergy.core.driver.desktop.DesktopDriver;
import com.synergy.core.driver.web.WebDriver;
import com.synergy.core.enums.FileSystemDirectory;
import com.synergy.core.reporting.FileUploader;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BaseTest {

	// Synergy Upgrade
	public static ThreadLocal<WebDriver> driver= new ThreadLocal<>();
	//public static WebDriver webDriver;
	public static ThreadLocal<DesktopDriver> desktopDriver = new ThreadLocal<>();
	public static List<String> pdfFilesList = new ArrayList<>();

	protected String runParams;
	protected DecimalFormat df;
	public static SoftAssert softAssert = new SoftAssert();

	/** When true, {@link #tearDownWebdriver()} does not stop the driver (used by left-filter suites). */
	protected boolean keepDriverAliveAfterTestMethod;

	// Utils

	static protected WaitUtils waitUtil=new WaitUtils();
	public static EmailExecutionReport email;

	// Test data reading
	public static DataFactory dataFactory;

	public BaseTest() {
		dataFactory = dataFactory == null ? dataFactory = new DataFactory() : dataFactory;
	}

	public void setRunParams(String runParams) {
		this.runParams = runParams;
	}

	@BeforeClass(alwaysRun = true)
	public void atStartSelenium() {
		TestRun.init(runParams);
		waitUtil = new WaitUtils();
		softAssert = new SoftAssert(new Object() {
		}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
		
		try {
			driver = LocalCapabilityFactory.initiateDriver();
			
			if (!ConfigProps.ISLOCAL) {
				//desktopDriver = CapabilityFactory.initiateDesktopDriver();
			}
						
		} catch (Exception e) {
			Verify.softAssert(false, "WebDriver or DesktopDriver is null as "+ e.getMessage());
		}
	}
	
	@AfterMethod(alwaysRun = true)
	public void tearDownWebdriver() {
		if (keepDriverAliveAfterTestMethod) {
			Logger.logMessage("===========TEST FINISHED (driver kept alive for suite)===========");
			return;
		}
		try {
			if (driver.get() != null) {
				driver.get().stop();
				driver.remove();
			}
			if (desktopDriver.get() != null) {
				desktopDriver.get().stop();
				desktopDriver.remove();
			}
			SleepUtils.sleep(10000);
		} catch (Exception e) {

		}
		Logger.logMessage("===========SESSION END===========");

	}

	/*@AfterClass
	public void tearDownWebdriver() {
		try {
			if (driver != null || desktopDriver !=null) {
				driver.get().stop();
				driver.remove();

				desktopDriver.get().stop();
				desktopDriver.remove();
				SleepUtils.sleep(10000);
			}
		} catch (Exception e) {

		}
		Logger.logMessage("===========SESSION END===========");

	}*/
	
	public static String uploadExtensionFile(String fileToUplaod) {
		File file = new File(fileToUplaod);
		FileUploader fileUploader = new FileUploader("https://www.synergyserver.tech?key="+ConfigProps.USER_KEY);
		String url = fileUploader.uploadFile(file); // the file and number of days to retain
		// returns the url to the file
		System.out.println("URL of stored file: " + url);
		return url;
	}

	public static String uploadFileServersDownloadFolder(String imageToSend) {
		String fileLocation = "";
		File fileToSend = new File(imageToSend);
		fileLocation = BaseTest.driver.get().fileSystem().exportFile(FileSystemDirectory.DOWNLOADS, fileToSend);
		Logger.logConsoleMessage("UPloaded file to Server's Download folder "+ fileLocation);
		return fileLocation;
	}

}
