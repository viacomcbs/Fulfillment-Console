package com.paramount.test.ff.common.loginUtil;

import java.util.Iterator;
import java.util.Set;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import com.synergy.core.driver.elements.Element;

public class DriverUtil extends BaseTest{

	public static boolean clickOnElement(By by, int wait) {
		boolean isClicked=false;
		if(WaitUtil.isDisplay(by, wait)) {
			Logger.logConsoleMessage("Clicking on element... "+by);
			try {
				driver.get().finder().findElement(by).click();
				isClicked = true;
			} catch (Exception e) {
				Logger.logConsoleMessage("Native click failed, trying JS click: " + by);
				isClicked = clickOnElementJs(by, 0);
			}
		}
		return isClicked;
	}

	/** JS click — avoids ElementClickIntercepted on sticky grid headers (Synergy PROD). */
	public static boolean clickOnElementJs(By by, int wait) {
		if (wait > 0 && !WaitUtil.isDisplay(by, wait)) {
			return false;
		}
		try {
			DesktopBrowserElement el = driver.get().finder().findElement(by);
			el.scrollIntoView();
			el.executeScript("arguments[0].scrollIntoView({block:'nearest',inline:'center'});");
			el.executeScript("arguments[0].click();");
			Logger.logConsoleMessage("JS-clicked element: " + by);
			return true;
		} catch (Exception e) {
			try {
				driver.get().finder().findElement(by).executeScript(
						"arguments[0].dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true}));");
				Logger.logConsoleMessage("JS dispatchEvent click: " + by);
				return true;
			} catch (Exception e2) {
				Logger.logConsoleMessage("JS click failed: " + by + " — " + e2.getMessage());
				return false;
			}
		}
	}

	public static void switchToFrame(By by) {
		Element ele= driver.get().finder().findElement(by);
		driver.get().browser().switchToFrameByElement(ele);
	}
	public static boolean waitForElementVisibleExpicit(By by, int timeoutInSeconds) throws InterruptedException {

		long endTime = System.currentTimeMillis() + (timeoutInSeconds * 1000);

		while (System.currentTimeMillis() < endTime) {
			try {
				DesktopBrowserElement ele =
						driver.get().finder().findElement(by);

				if (ele.isDisplayed()) {
					return true; // ✅ visible
				}
			} catch (Exception ignored) {
			}
			Thread.sleep(500);
		}

		Logger.logConsoleMessage("Element not visible after " + timeoutInSeconds + " seconds: " + by);
		return false;
	}
	public static boolean sendKeyToElement(By by, int wait, String sendKeyMessage) {
		boolean isClicked=false;
		if(WaitUtil.isDisplay(by, wait)) {
			driver.get().finder().findElement(by).sendKeys(sendKeyMessage);
			isClicked =true;
		}
		return isClicked;
	}

	public static boolean isDisplayed(By by, int wait) {
		try {
			driver.get().finder().findElement(by).scrollIntoView();
			driver.get().finder().findElement(by).executeScript("arguments[0].scrollIntoView(true);");
		} catch (Exception e) {
			Logger.logConsoleMessage("Can not scroll to element...");
		}
		return WaitUtil.isDisplay(by, wait);
	}

	public static void switchToFrame(By by, int wait) {
		if(WaitUtil.isDisplay(by, wait)) {
			Element ele= driver.get().finder().findElement(by);
			driver.get().browser().switchToFrameByElement(ele);
		}
	}
	public static boolean isSelectedCheckbox(By by) {
		DesktopBrowserElement ele = driver.get().finder().findElement(by);
		if (ele.isSelected()) {
			Logger.logConsoleMessage("Checkbox is selected: " + by);
			return true;
		} else {
			Logger.logConsoleMessage("Checkbox is not selected: " + by);
			return false;
		}
	}

	public static void scrollToElement(By by) {
		try {
			driver.get().finder().findElement(by).executeScript("arguments[0].scrollIntoView(true);");
		} catch (Exception e) {
			Logger.logConsoleMessage("Can not scroll to element...");
		}
	}

	public static DesktopBrowserElement getElement(By by) {
		DesktopBrowserElement ele= null;
		try {
			ele= driver.get().finder().findElement(by);
		}catch(Exception e){
			Logger.logConsoleMessage("Element not found "+by);
		}
		return ele;
	}

	public static void launchApplicationOnBrowser() {
		String targetUrl = Config.getTargetUrl();
		Logger.logReportMessage("Launching application on browser: " + targetUrl);
		driver.get().browser().getUrl(targetUrl);
		WaitUtil.waitForJSToLoad(60);
		driver.get().browser().maximizeWindow();
		//driver.get().browser().executeScript("document.body.style.zoom='80%'");

	}

	public static void openNewSubTab() {
		String currentTab= driver.get().browser().getWindowHandle();

		driver.get().browser().executeScript("window.open('URL here')");

		Set <String> tabs = driver.get().browser().getWindowHandles();
		Iterator<String> value = tabs.iterator();

        while (value.hasNext()) {
        	 String childWindow= value.next();
           if(!currentTab.equals(childWindow)) {
        	   driver.get().browser().switchToWindow(childWindow);
           }
        }

	}

}
