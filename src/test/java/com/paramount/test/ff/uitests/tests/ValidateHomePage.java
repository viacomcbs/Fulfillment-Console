package com.paramount.test.ff.uitests.tests;

import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.uitests.helpers.HomePage_util;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;

import io.qameta.allure.Description;

public class ValidateHomePage  extends BaseTest{
	
	LoginPage loginPage;
	Login login;
	HomePage_util homePage_util;
    
	
	@BeforeTest
	public void setup() throws InterruptedException {
		loginPage = new LoginPage();
		login = new Login();
		homePage_util = new HomePage_util();

	}
	@Test(priority = 0)
	@Description("Validate Home Page")
	public void validateHomePage() throws InterruptedException {
		
		softAssert = new SoftAssert(new Object() {
		}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
		
		Logger.logReportMessage("Fulfillment Launching application");
		DriverUtil.launchApplicationOnBrowser();	

		login.loginToFF();
		Logger.log("Fulfillment Home page Validation");
		Logger.log("Validating Tutorial");
		homePage_util.validateHomePage(softAssert);
		softAssert.assertAll();

	}
     /*@Test(priority = 1)
	@Description("Validate Filter Label in Home Page")
	public void ValidateFilterLabel() throws InterruptedException {

		Logger.log("Filter Label Validation");
		homePage_util.validateFilterLabel(softAssert);


	}

	@Test(priority = 2)
	@Description("Validate Filter Label in Home Page")
	public void validateAvatarIconDisplayed() throws InterruptedException {

		Logger.log("Avatar icon  Label Validation");
		homePage_util.isAvatarDisplayed(softAssert);
	}*/
/*
	@Test(priority = 3)
	@Description("Validate Filter Label in Home Page")
	public void validateDetailsPanelOpened() throws InterruptedException {

		Logger.log("Avatar icon  Label Validation");
		homePage_util.isDetailsPanelDisplayed(softAssert);
	}

	@Test(priority = 1)
	@Description("Search a valid Materialid in Global Search")
   	public void validateGlobalSearch() throws InterruptedException {

		Logger.logReportMessage("MIC Launching application");
		DriverUtil.launchApplicationOnBrowser();

		login.loginToMIC();
		Logger.log("Global Search Validation");
		homePage_util.globalSearch();

	}*/
}
