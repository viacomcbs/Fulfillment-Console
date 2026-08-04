package com.paramount.test.ff.common.loginUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.driver.LocalCapabilityFactory;
import com.paramount.test.ff.common.util.Logger;
import com.synergy.common.utils.SleepUtils;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;


public class WaitUtil extends BaseTest{
	
	public static boolean isElementVisible(By by) {
		boolean isElement = false;
		try {
			waitUtil.waitForVisibilityOfElement(by, 60);
			isElement = driver.get().finder().findElement(by).isDisplayed();
			return isElement;
		} catch (Exception e) {
			Logger.logConsoleMessage("Exception :: " + by + " Element is not Visible");
			isElement = false;
			return isElement;
		}
	}
	
	public static DesktopBrowserElement isElementVisible1(By by) {
		DesktopBrowserElement isElement = null;
		waitUtil.waitForVisibilityOfElement(by, 60);
		try {
			isElement = driver.get().finder().findElement(by);
			if (isElement == null) {
				Logger.logConsoleMessage(by + " -- Element not found");
			}
		} catch (Exception e) {
			Logger.logReportMessage("Element not found " + e.getMessage());
		}
		return isElement;
	}
	
	public static boolean isDisplay(By by, int waitFor) {
		Logger.logConsoleMessage("Waiting for Element to Display "+ by);
		//isElementVisible1(by);
		try {
			waitUtil.waitForVisibilityOfElement(by, waitFor);
			boolean isDis= driver.get().finder().findElement(by).isDisplayed();
			return isDis;
		}catch(Exception e) {
			Logger.logConsoleMessage("Element not found "+ e.getMessage());
			return false;
		}
		
	}

	/**
	 * Polls {@code findElements} instead of {@code findElement} so absent elements do not
	 * block on Synergy's default 60s element timeout (used for panel open/closed checks).
	 */
	public static boolean isDisplayFast(By by, int waitForSec) {
		long endTime = System.currentTimeMillis() + (waitForSec * 1000L);
		while (System.currentTimeMillis() < endTime) {
			try {
				driver.get().options().setElementTimeout(2000);
				List<DesktopBrowserElement> elements = driver.get().finder().findElements(by);
				if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
					return true;
				}
			} catch (Exception ignored) {
				// element may be stale while panel animates
			} finally {
				try {
					driver.get().options().setElementTimeout(LocalCapabilityFactory.DEFAULT_ELEMENT_TIMEOUT);
				} catch (Exception ignored) {
					// session may be closing
				}
			}
			SleepUtils.sleep(300);
		}
		return false;
	}
	
	//use for negative scenario
	public static boolean isElementVisibleForSec(By by) {
		boolean isElement = false;
		try {
			waitUtil.waitForVisibilityOfElement(by, 1);
			isElement = driver.get().finder().findElement(by).isDisplayed();
			return isElement;
		} catch (Exception e) {
			Logger.logConsoleMessage("Exception :: " + by + " Element is not Visible");
			isElement = false;
			return isElement;
		}
	}
	
	public  static void waitForJSToLoad(int timeOutInSec) {
		//Logger.logConsoleMessage("Waiting for Page load for sec "+ timeOutInSec);
		long time = TimeUnit.MILLISECONDS.convert(timeOutInSec, TimeUnit.SECONDS);
		long startTime = System.currentTimeMillis();
		long endTime = startTime + time;
		String jsLoadResult = "";
		String jqueryLoadResult = "";

		while (endTime > startTime) {
			jsLoadResult = BaseTest.driver.get().browser().executeScript("return document.readyState");
			//jqueryLoadResult = BaseTest.driver.get().get().browser().executeScript("return jQuery.active");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if (jsLoadResult.equalsIgnoreCase("complete"))// && jqueryLoadResult.equalsIgnoreCase("0"))
				break;
		}
	} 
	
	public void waitForAjaxToLoad(int timeOutInSec) {
		long time = TimeUnit.MILLISECONDS.convert(timeOutInSec, TimeUnit.SECONDS);
		long startTime = System.currentTimeMillis();
		long endTime = startTime + time;
		String jsLoadResult = "";
		String jqueryLoadResult = "";

		while (endTime > startTime) {
			jsLoadResult = BaseTest.driver.get().browser().executeScript("return document.readyState");
			//jqueryLoadResult = BaseTest.driver.get().get().browser().executeScript("return jQuery.active");
			SleepUtils.sleep(2000);
			if (jsLoadResult.equalsIgnoreCase("complete") && jqueryLoadResult.equalsIgnoreCase("0"))
				break;
		}
	}

	


	
	/*
	 * public static boolean waitUtilElementIsVisible(org.openqa.selenium.By by) {
	 * 
	 * try { driver.get()Wait wait = new driver.get()Wait((driver.get()) driver, 60);
	 * wait.until(ExpectedConditions.presenceOfElementLocated(by)); boolean
	 * isElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by)) !=
	 * null; //WebElement l = driver.findElement(by); //((JavascriptExecutor)
	 * driver).executeScript("arguments[0].scrollIntoView(true);", l); if
	 * (isElement) { return true; } } catch (Exception e) {
	 * //Verify.test.log(Status.FAIL, "WebElement "+by+ " not Found!"); return
	 * false; } return false; }
	 * 
	 * public static boolean isElementPresent(org.openqa.selenium.By by, int
	 * waitTime) {
	 * 
	 * try { driver.get()Wait wait = new driver.get()Wait((driver.get()) driver, waitTime);
	 * boolean isElement=wait.until(ExpectedConditions.presenceOfElementLocated(by))
	 * !=null; WebElement l = (WebElement) ((driver.get()) driver).findElements(by);
	 * ((JavascriptExecutor)
	 * driver).executeScript("arguments[0].scrollIntoView(true);", l); return
	 * isElement; }catch (Exception e) { //Verify.test.log(Status.FAIL,
	 * "WebElement "+by+ " not Found!"); } return false; }
	 * 
	 * public static boolean isElementVisible(org.openqa.selenium.By by, int
	 * waitTime) {
	 * 
	 * try { driver.get()Wait wait = new driver.get()Wait((driver.get()) driver,waitTime);
	 * boolean
	 * isElement=wait.until(ExpectedConditions.visibilityOfElementLocated(by))
	 * !=null;
	 * 
	 * WebElement l = ((driver.get()) driver).findElement(by); ((JavascriptExecutor)
	 * driver).executeScript("arguments[0].scrollIntoView(true);", l); return
	 * isElement; }catch (Exception e) { //Verify.test.log(Status.FAIL,
	 * "WebElement "+by+ " not Found!"); } return false; }
	 */
}
