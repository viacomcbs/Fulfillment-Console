package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.WaitUtils;
import org.testng.asserts.SoftAssert;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.pageobjects.HomePage;

import static com.paramount.test.ff.common.base.BaseTest.driver;

public class HomePage_util extends com.paramount.test.ff.pageobjects.HomePage{
	
	HomePage homePage = new HomePage();
	WaitUtils WaitUtils = new WaitUtils();
	public void validateHomePage(SoftAssert softAssert) {		
		Verify.softAssert(WaitUtil.isDisplay(homePage.getHeaderTitle(), 5), "FULFILLMENT CONSOLE");

	}

	public void validateFilterLabel(SoftAssert softAssert) {
		Verify.softAssert(WaitUtil.isElementVisible(homePage.filterPanelLabel()),"Filter Panel is open");
	}

	public void isAvatarDisplayed(com.paramount.test.ff.common.util.SoftAssert softAssert){
		Verify.softAssert(WaitUtil.isElementVisible(homePage.AvatarFlagIcon()),"Avatar Icons are Visible");

	}


	public void isDetailsPanelDisplayed(com.paramount.test.ff.common.util.SoftAssert softAssert){
		Verify.softAssert(WaitUtil.isElementVisible(homePage.DetailsPanelButton()),"Details Panel is Open");

	}

	public void globalSearch(){
		WaitUtils.waitForVisibilityOfElement(homePage.inputSearchicon(),40);
		Logger.log("====Searching Global data==== ");
		driver.get().finder().findElement(homePage.inputSearchicon()).click();
		Verify.softAssert(WaitUtil.isElementVisible(homePage.inputGobalSearch()),"Global Search is Open");
		driver.get().finder().findElement(homePage.inputGobalSearch()).sendKeys("94a84e13-70a3-4362-852e-09dda713ef9b");


	}

	
}
