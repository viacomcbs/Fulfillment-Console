package com.paramount.test.ff.common.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.paramount.test.ff.common.base.BaseTest;
import com.synergy.common.utils.SleepUtils;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

public class WaitUtils {

	public DesktopBrowserElement waitForVisibilityOfElement(DesktopBrowserElement element, int timeOutInSec) {
		long time = TimeUnit.MILLISECONDS.convert(timeOutInSec, TimeUnit.SECONDS);
		try {
			return elementIfVisible(element, time);
		} catch (Exception e) {
			return null;
		}
	}

	public DesktopBrowserElement waitForVisibilityOfElement(By by, int timeOutInSec) {
		DesktopBrowserElement element = BaseTest.driver.get().finder().findElement(by);
		long time = TimeUnit.MILLISECONDS.convert(timeOutInSec, TimeUnit.SECONDS);
		try {
			return elementIfVisible(element, time);
		} catch (Exception e) {
			return null;
		}
	}

	public List<DesktopBrowserElement> waitForVisibilityOfAllElements(List<DesktopBrowserElement> elements,
			int timeOut) {
		for (DesktopBrowserElement element : elements) {
			elementIfVisible(element, timeOut);
			if (!element.isDisplayed())
				return null;
		}
		return elements.size() > 0 ? elements : null;
	}

	private DesktopBrowserElement elementIfVisible(DesktopBrowserElement element, long timeOut) {

		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeOut;

		// while (endTime > startTime && !element.isDisplayed()) {
		while (!element.isDisplayed() && endTime > startTime) {
			SleepUtils.sleep(1000);
			startTime = System.currentTimeMillis();
		}
		return element.isDisplayed() ? element : null;
	}

	public DesktopBrowserElement waitUntilElementIsClickable(DesktopBrowserElement element, int timeOut) {
		DesktopBrowserElement browserElement = waitForVisibilityOfElement(element, timeOut);
		try {
			if (browserElement != null && browserElement.isEnabled()) {
				return browserElement;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public DesktopBrowserElement waitUntilElementIsClickable(By by, int timeOut) {
		DesktopBrowserElement browserElement = waitForVisibilityOfElement(
				BaseTest.driver.get().finder().findElement(by), timeOut);
		try {
			if (browserElement != null && browserElement.isEnabled()) {
				return browserElement;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public  void waitForJSToLoad(int timeOutInSec) {
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

}
