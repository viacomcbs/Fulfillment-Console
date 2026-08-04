package com.paramount.test.ff.common.loginUtil;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.driver.LocalCapabilityFactory;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.uitests.pageobjects.LoginPage;


public class Login  extends BaseTest {
	LoginPage loginPage= new LoginPage();
	
	public void loginToFF() throws InterruptedException {
		
		if(WaitUtil.isDisplay(loginPage.getUserNameField(), 30)) {
			Logger.logReportMessage("Entering username...");

			driver.get().finder().findElement(loginPage.getUserNameField()).sendKeys(Config.getString("Username"));
			driver.get().finder().findElement(loginPage.getNextButton()).click();
		}
		driver.get().options().setElementTimeout(3000);
		if(WaitUtil.isDisplay(loginPage.getPasswordField(), 60)) {
			Logger.logReportMessage("Entering Password...");
			driver.get().finder().findElement(loginPage.getPasswordField()).sendKeys(Config.getString("Password"));
			driver.get().finder().findElement(loginPage.getSignInButton()).click();
		}
		zoomOut();
		if (Config.isLocalExecution() && WaitUtil.isDisplay(loginPage.getNotification(), 5)) {
			driver.get().finder().findElement(loginPage.getNotification()).click();
			Thread.sleep(50000);
		} else if (!Config.isLocalExecution()) {
			Logger.logReportMessage("Server login — waiting for app redirect (service account, no Okta push)");
			WaitUtil.waitForJSToLoad(45);
			waitForFulfillmentConsoleRedirect(90);
		}
		//asperaHandle.isAsperaAlertDisplayed();
		//Verify.hardAssert(WaitUtil.isDisplay(loginPage.getHomePage(), 60),"Login Successful !");
		if (Config.isLocalExecution()) {
			Logger.logReportMessage("Zooming out the page");
			zoomOut();
			Logger.logReportMessage("Zoomed out the page");
		}else {
			zoomOutOnServer();
		}
		zoomOut();
		driver.get().options().setElementTimeout(LocalCapabilityFactory.DEFAULT_ELEMENT_TIMEOUT);
	}

	public void zoomOut() {
		try {
			Thread.sleep(2000);
			Robot robot= new Robot();
			for (int i = 1; i < 5; i++) {
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_SUBTRACT);
				robot.keyRelease(KeyEvent.VK_SUBTRACT);
				robot.keyRelease(KeyEvent.VK_CONTROL);
			}
			Thread.sleep(2000);
			
		} catch (Exception e) {
			System.err.println("Robot method did not worked.");
		}
		
	}
	
	public void zoomOutOnServer() {
		driver.get().browser().executeScript("document.body.style.zoom = '0.8'");
	}

	/** Poll until Fulfillment Console shell appears after Okta redirect (Synergy server runs). */
	private void waitForFulfillmentConsoleRedirect(int maxSeconds) {
		HomePage homePage = new HomePage();
		long endTime = System.currentTimeMillis() + (maxSeconds * 1000L);
		while (System.currentTimeMillis() < endTime) {
			try {
				if (WaitUtil.isDisplayFast(homePage.getHeaderTitle(), 2)) {
					Logger.logReportMessage("Fulfillment Console loaded after login redirect");
					return;
				}
				Object panel = driver.get().browser().executeScript(
						"return !!document.querySelector('msc-left-filter-panel');");
				if (Boolean.TRUE.equals(panel) || "true".equals(String.valueOf(panel))) {
					Logger.logReportMessage("Fulfillment Console filter panel detected after login redirect");
					return;
				}
			} catch (Exception ignored) {
				// keep polling
			}
			WaitUtil.waitForJSToLoad(5);
		}
		Logger.logReportMessage("Fulfillment Console redirect not confirmed within " + maxSeconds + "s — setup will retry");
	}
	

}
