package com.paramount.test.ff.common.util;

import com.synergy.common.utils.SleepUtils;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import com.synergy.core.driver.web.WebDriver;
import com.synergy.core.exceptions.NoSuchElementException;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WebInteract {

	private static final int WAIT_FOR_VISIBLE_DEFAULT_TIMEOUT = 20;
	private static final int WAIT_FOR_PRESENT_DEFAULT_TIMEOUT = 20;
	private static final int DEFAULT_POLL_WAIT_MS = 1000;
	private static final int DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS = 6000;

	private WebDriver webDriver;
	private DesktopBrowserElement desktopBrowserElement;
	private List<DesktopBrowserElement> desktopBrowserElements;
	private final String simpleName = null;


	//MQELocatorType elementType;
	String elementDetails;
	private By locator;
	private String elementName;

	/*public WebInteract(MQELocatorType elementType, String elementDetails, String elmentName) {
		this.webDriver = BaseTest.driver.get();
		this.elementType = elementType;
		this.elementDetails = elementDetails;
		this.elementName = elmentName;
		this.locator = identifyLocator();
		//this.element = webDriver.findElement(locator);
	}*/

	public String getElementName() {
		return elementName;
	}
/*
	private By identifyLocator() {
		switch (elementType) {
		case ID:
			return By.ID(elementDetails);
		case CSS:
			return By.CSS(elementDetails);
		case NAME:
			return By.Name(elementDetails);
		case XPATH:
			return By.XPath(elementDetails);
		case CLASS_NAME:
			return By.ClassName(elementDetails);
			case LINK_TEXT:
				return By.LinkText(elementDetails);
		default:
			return null;
		}
	}
	*/

	private boolean isDisplay() {
		try {
			desktopBrowserElement = staleHandler();
			return desktopBrowserElement.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isPresentNotDisplay() {
		try {
			staleHandler().toNativeElement();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isVisible() {
		try {
			Logger.logMessage("Is the '" + this.elementName + "' element Visible");
			return staleHandler().toNativeElement().isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isVisible(int timeoutSec) {

		long timeoutExpiredMs = System.currentTimeMillis() + timeoutSec * 1000L;
		long waitMillis;
		do {
			if (!isDisplay()) {
				sleep(DEFAULT_POLL_WAIT_MS);
			} else {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		try {
			Logger.logMessage("Is the '" + this.elementName + "' element Visible");
			return staleHandler().toNativeElement().isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isPresent() {
		// this is corrcet logger msg - need to change everything according to this
		Logger.logMessage("Is the '" + this.elementName + "' element Present");
		return isVisible();
	}

	public boolean isPresent(int time) {
		boolean value = false;
		int count = 1;
		while(value || count <=(time/100)) {
			try {
				desktopBrowserElement.wait(100);
			} catch (InterruptedException e) {
			}
			value = desktopBrowserElement.isDisplayed();
			count++;
		}
		return value;
	}

	public WebInteract waitForPresent(int timeoutSec) {
		Logger.logMessage("Wait for the '" + this.elementName + "' element to be visible.");
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutSec * 1000L;
		long waitMillis;
		do {
			if (!isPresentNotDisplay()) {
				sleep(DEFAULT_POLL_WAIT_MS);
			} else {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		if (!isPresentNotDisplay()) {
			throw new RuntimeException(String.format("Element '%s' did not appear", elementName));
		}
		return this;
	}

	public WebInteract waitForVisible(int timeoutSec) {
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutSec * 1000L;
		long waitMillis;
		do {
			if (!isDisplay()) {
				sleep(DEFAULT_POLL_WAIT_MS);
			} else {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		if (!isDisplay()) {
			throw new RuntimeException(String.format("Element '%s' did not appear", elementName));
		}

		return this;
	}

	public List<DesktopBrowserElement> getAllElements() {
		List<DesktopBrowserElement> ElementIntract = new ArrayList<DesktopBrowserElement>();
		List<DesktopBrowserElement> eles = staleHandlerAll();
		for (int index = 0; index < eles.size(); index++) {
			ElementIntract.add(eles.get(index));
		}
		return ElementIntract;
	}

	public Integer getCountOfElements() {
		List<DesktopBrowserElement> eles = staleHandlerAll();
		if (eles == null) {
			return 0;
		} else {
			return eles.size();
		}
	}

	public List<String> getTextFromAll() {
		List<String> TextValue = new ArrayList<String>();

		if (staleHandlerAll().size() > 0) {
			for (int index = 0; index < staleHandlerAll().size(); index++) {
				TextValue.add(staleHandlerAll().get(index).getText());
			}
		}
		return TextValue;
	}

	public List<String> getAttributeValueFromAll(String attribute) {
		List<String> attributeValue = new ArrayList<String>();

		if (desktopBrowserElements.size() > 0) {
			for (int index = 0; index < desktopBrowserElements.size(); index++) {
				attributeValue.add(desktopBrowserElements.get(index).getAttribute(attribute));
			}
		}
		return attributeValue;
	}

	public WebInteract waitForAllPresent() {
		long timeoutExpiredMs = System.currentTimeMillis() + 10 * 1000;
		long waitMillis;
		int count = 0;
		do {
			desktopBrowserElements = staleHandlerAll();
			if ((desktopBrowserElements.size() > 0)) {
				for (int index = 0; index < desktopBrowserElements.size(); index++) {
					if (desktopBrowserElements.get(index).isDisplayed()) count = count + 1;
				}
				if (count == staleHandlerAll().size()) break;

			} else {
				sleep(DEFAULT_POLL_WAIT_MS);
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		if (!staleHandler().toNativeElement().isDisplayed()) {
			throw new RuntimeException(String.format("Element '%s' did not appear", elementName));
		}

		return this;
	}

	public WebInteract waitForAllVisible(int time) {
		long timeoutExpiredMs = System.currentTimeMillis() + 10 * 1000;
		long waitMillis;
		int count = 0;
		do {
			if ((staleHandlerAll().size() > 0)) {
				for (int index = 0; index < staleHandlerAll().size(); index++) {
					if (staleHandlerAll().get(index).isDisplayed()) count = count + 1;
				}
				if (count == staleHandlerAll().size()) break;

			} else {
				sleep(DEFAULT_POLL_WAIT_MS);
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		if (!staleHandler().toNativeElement().isDisplayed()) {
			throw new RuntimeException(String.format("Element '%s' did not appear", elementName));
		}

		return this;
	}

	public void waitForNotVisible(int timeoutSec) {
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutSec * 1000L;
		long waitMillis;
		do {
			try {
				webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
				if (isDisplay()) {
					sleep(DEFAULT_POLL_WAIT_MS);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		try {
			webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
			if (isDisplay()) {
				throw new RuntimeException(String.format("Element '%s' did not disappear", elementName));
			}
		} catch (NoSuchElementException e) {
			// do nothing - we're good element no logner exists
		}
	}

	public void waitForNotPresent(int timeoutSec) {
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutSec * 1000L;
		long waitMillis;
		do {
			try {
				webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
				if (isDisplay()) {
					sleep(DEFAULT_POLL_WAIT_MS);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		try {
			webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
			if (isDisplay()) {
				throw new RuntimeException(String.format("Element '%s' did not disappear", elementName));
			}
		} catch (NoSuchElementException e) {
			// do nothing - we're good element no logner exists
		}
	}

	public void waitForNotPresent() {
		long timeoutExpiredMs = System.currentTimeMillis() + 10 * 1000;
		long waitMillis;
		do {
			try {
				webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
				if (isDisplay()) {
					sleep(DEFAULT_POLL_WAIT_MS);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
			waitMillis = timeoutExpiredMs - System.currentTimeMillis();
		} while (waitMillis > 0);

		try {
			webDriver.options().setElementTimeout(DEFAULT_ELEMENT_NOT_PRESENT_WAIT_MS);
			if (isDisplay()) {
				throw new RuntimeException(String.format("Element '%s' did not disappear", elementName));
			}
		} catch (NoSuchElementException e) {
		}
	}


	public WebInteract waitForVisible() {
		Logger.logMessage("Wait for the '" + this.elementName + "' element to be visible.");
		return waitForVisible(WAIT_FOR_VISIBLE_DEFAULT_TIMEOUT);
	}
	

	public WebInteract waitForPresent() {
		Logger.logMessage("Wait for the '" + this.elementName + "' element to be present.");
		return waitForPresent(WAIT_FOR_PRESENT_DEFAULT_TIMEOUT);
	}

	private DesktopBrowserElement staleHandler() {
		// stale state is handled in synergy
		return webDriver.finder().findElement(locator);
	}

	private List<DesktopBrowserElement> staleHandlerAll() {
		// stale state is handled in synergy
		return webDriver.finder().findElements(locator);
	}

	public static void sleep(Integer sleepInMS) {
		try {
			Thread.sleep(sleepInMS);
		} catch (InterruptedException e) {
			Logger.logMessage(e.getMessage());
		}
	}


	public void navigate(String url) {
		webDriver.browser().getUrl(url);
	}

	public WebDriver getDriver() {
		return webDriver;
	}

	public void refresh() {
		Logger.logMessage("Refresh the current page.");
		webDriver.browser().refresh();
	}

	public WebInteract mouseOver() {
		Logger.logMessage("Mouse over (hover over) the '" + this.elementName + "' element.");
		staleHandler().mouseOver();
		return this;
	}

	public WebInteract openUrl(String url) {
		Logger.logMessage("Open url '" + url + "'.");
		webDriver.browser().getUrl(url);
		webDriver.browser().maximizeWindow();
		return this;
	}

	public WebInteract click() {
		Logger.logMessage("Click the '" + this.elementName + "' element.");
		staleHandler().click();
		return this;
	}

	public String getUrl() {
		return webDriver.browser().getCurrentUrl();

	}



	public WebInteract clearText() {
		Logger.logMessage("Clear the text in the '" + this.elementName + "' element.");
		staleHandler().clear();
		return this;
	}

	public String getText() {
		return desktopBrowserElement.getText();
	}

	public void goBack() {
		webDriver.browser().goBack();
	}

	public WebInteract maximizeWindow() {
		Logger.logMessage("Maximize the browser window.");
		webDriver.browser().maximizeWindow();
		return this;
	}

	public String getCurrentWindowId() {
		return webDriver.browser().getWindowHandle();
	}

	public Set<String> getAllWindowIds() {
		return webDriver.browser().getWindowHandles();
	}

	public void switchToWindow(String id) {
		Logger.logMessage("Switch to window with id '" + id + "'.");
		webDriver.browser().switchToWindow(id);
	}

	public WebInteract closeWindow() {
		Logger.logMessage("Close the current window.");
		webDriver.browser().closeWindow();
		return this;
	}

	public WebInteract setWindowSize(int x, int y) {
		Logger.logMessage("Set window size to '" + x + "', '" + y + "'.");
		webDriver.browser().setWindowSize(x, y);
		return this;
	}

	public WebInteract scroll(int numOfScrolls, int x, int y) {
		Logger.logMessage("Wait for the '" + this.elementName + "' element text to be visible within '" + numOfScrolls + "' " + "by '" + x + "," + y + "'.");
		webDriver.browser().scroll(numOfScrolls, x, y);
		return this;
	}

	public boolean isSelected() {
		Logger.logMessage("Is the '" + this.elementName + "' element selected: ");
		return staleHandler().isSelected();
	}

	public String getAttributeValue(String attributeName) {
		return staleHandler().getAttribute(attributeName);
	}

	public WebInteract type(String text) {
		// Logger.logMessage("Type '" + this.handleInputText(text) + "' into the '" + this.simpleName + "' element.");
		staleHandler().sendKeys(text);
		return this;
	} // type=senKeys in synergy

	
	public WebInteract sendKeys(String text) {
		Logger.logMessage("Type '" + text + "' into the '" + simpleName + "' element.");
		desktopBrowserElement.sendKeys(text);
		return this;
	}
	
	public void sendKeycode(int keycode) {
		Logger.logMessage("Send keycode '" + keycode + "' to the browser window.");
		staleHandler().sendKeycode(keycode);
	}

	public WebInteract scrollIntoView() {
		Logger.logMessage("Scroll the '" + this.elementName + "' element into view.");
		staleHandler().scrollIntoView();
		return this;
	}

	public WebInteract pause(Integer waitTimeInMS) {
		Logger.logMessage("Pause for '" + waitTimeInMS + "' milliseconds.");
		SleepUtils.sleep(waitTimeInMS);
		return this;
	}


	public String getCSSValue(String name) {
		return staleHandler().getCssValue(name);
	}

	public String executeScript(String script) {
		return webDriver.browser().executeScript(script);
	}

	private void getFailureArtifacts(WebDriver webDriver) {
		try {
			// TODO - Synergy - page source is not available
			//String pageSource = webDriver.getPageSource();
			String pageSource = webDriver.screen().getTextAsString();
			pageSource = pageSource.replace(System.lineSeparator(), "");
			Logger.logMessage(pageSource);
		} catch (Exception e2) {
			Logger.logMessage(e2.getMessage());
		}

		try {
			File screenshot = webDriver.screen().getImage();
			Logger.logMessage("Screenshot saved to: " + screenshot.getAbsolutePath());
		} catch (Exception e2) {
			Logger.logMessage(e2.getMessage());
		}
	}

}

