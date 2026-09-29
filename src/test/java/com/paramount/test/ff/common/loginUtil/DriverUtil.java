package com.paramount.test.ff.common.loginUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.awt.Dimension;
import java.awt.Point;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.synergy.core.driver.By;
import com.synergy.core.driver.ScreenText;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import com.synergy.core.driver.elements.Element;
import com.synergy.core.enums.WebKey;

public class DriverUtil extends BaseTest{

	public static boolean clickOnElement(By by, int wait) {
		boolean isClicked=false;
		if(WaitUtil.isDisplay(by, wait)) {
			Logger.logConsoleMessage("Clicking on element... "+by);
			driver.get().finder().findElement(by).click();
			isClicked =true;
		}
		return isClicked;
	}

	/** Scrolls into view first; falls back to JS click when the grid or overlay intercepts the click. */
	public static boolean clickOnElementSafely(By by, int wait) {
		if (!WaitUtil.isDisplayQuiet(by, wait)) {
			return false;
		}
		scrollToElement(by);
		try {
			Logger.logConsoleMessage("Clicking on element... " + by);
			driver.get().finder().findElement(by).click();
			return true;
		} catch (Exception firstClickFailure) {
			Logger.logConsoleMessage("Normal click intercepted, retrying with JS click: " + by);
			try {
				driver.get().finder().findElement(by).executeScript("arguments[0].click();");
				return true;
			} catch (Exception jsClickFailure) {
				Logger.logConsoleMessage("JS click also failed: " + by);
				return false;
			}
		}
	}

	public static void switchToFrame(By by) {
		Element ele= driver.get().finder().findElement(by);
		driver.get().browser().switchToFrameByElement(ele);
	}
	public static boolean waitForElementVisibleExpicit(By by, int timeoutInSeconds) throws InterruptedException {
		if (driver.get() == null) {
			return false;
		}
		long endTime = System.currentTimeMillis() + (timeoutInSeconds * 1000L);
		while (System.currentTimeMillis() < endTime) {
			try {
				DesktopBrowserElement ele = driver.get().finder().findElement(by);
				if (ele.isDisplayed()) {
					return true;
				}
			} catch (Exception ignored) {
			}
			Thread.sleep(250);
		}
		Logger.logConsoleMessage("Element not visible after " + timeoutInSeconds + " seconds: " + by);
		return false;
	}

	public static boolean waitForElementInvisible(By by, int timeoutInSeconds) throws InterruptedException {
		if (driver.get() == null) {
			return true;
		}
		long endTime = System.currentTimeMillis() + (timeoutInSeconds * 1000L);
		while (System.currentTimeMillis() < endTime) {
			try {
				DesktopBrowserElement ele = driver.get().finder().findElement(by);
				if (!ele.isDisplayed()) {
					return true;
				}
			} catch (Exception ignored) {
				return true;
			}
			Thread.sleep(250);
		}
		Logger.logConsoleMessage("Element still visible after " + timeoutInSeconds + " seconds: " + by);
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

	/** Playwright parity: right-click only (no prior left-click). Tries multiple Synergy strategies. */
	public static boolean openContextMenuOnElement(By by, int wait) {
		return rightClickNative(by, wait) || rightClickAtBrowserCoordinates(by, wait)
				|| contextMenuViaShiftF10(by, wait) || rightClickViaJsEvents(by, wait);
	}

	private static DesktopBrowserElement prepareContextTarget(By by, int wait) {
		if (!WaitUtil.isDisplay(by, wait)) {
			return null;
		}
		DesktopBrowserElement element = driver.get().finder().findElement(by);
		try {
			element.scrollIntoView();
		} catch (Exception ignored) {
		}
		try {
			element.mouseOver();
		} catch (Exception ignored) {
		}
		return element;
	}

	public static boolean rightClickNative(By by, int wait) {
		DesktopBrowserElement element = prepareContextTarget(by, wait);
		if (element == null) {
			return false;
		}
		Logger.logConsoleMessage("Native right-click on element... " + by);
		try {
			element.rightClick();
			return true;
		} catch (Exception e) {
			Logger.logConsoleMessage("Native element right-click failed: " + e.getMessage());
			return false;
		}
	}

	public static boolean rightClickAtBrowserCoordinates(By by, int wait) {
		return rightClickAtBrowserCoordinates(by, wait, null);
	}

	public static boolean rightClickAtBrowserCoordinates(By by, int wait, java.util.function.BooleanSupplier menuProbe) {
		DesktopBrowserElement element = prepareContextTarget(by, wait);
		if (element == null) {
			return false;
		}
		int[][] coordinateAttempts = resolveContextMenuCoordinates(element);
		for (int[] coords : coordinateAttempts) {
			try {
				Logger.logConsoleMessage("Browser right-click at " + coords[0] + "," + coords[1] + " for " + by);
				driver.get().browser().rightClick(coords[0], coords[1]);
				if (menuProbe == null || menuProbe.getAsBoolean()) {
					return true;
				}
			} catch (Exception e) {
				Logger.logConsoleMessage("Browser coordinate right-click failed at " + coords[0] + ","
						+ coords[1] + ": " + e.getMessage());
			}
		}
		return false;
	}

	private static int[][] resolveContextMenuCoordinates(DesktopBrowserElement element) {
		java.util.List<int[]> coords = new java.util.ArrayList<>();
		try {
			Object scriptCoords = element.executeScript(
					"var el = arguments[0];"
							+ "var r = el.getBoundingClientRect();"
							+ "var cx = Math.round(r.left + r.width / 2);"
							+ "var cy = Math.round(r.top + r.height / 2);"
							+ "return {"
							+ "clientX: cx,"
							+ "clientY: cy,"
							+ "screenX: Math.round(window.screenX + cx),"
							+ "screenY: Math.round(window.screenY + cy + (window.outerHeight - window.innerHeight)),"
							+ "pageX: Math.round(cx + (window.scrollX || 0)),"
							+ "pageY: Math.round(cy + (window.scrollY || 0))"
							+ "};");
			addCoordFromScriptResult(coords, scriptCoords, "clientX", "clientY");
			addCoordFromScriptResult(coords, scriptCoords, "pageX", "pageY");
			addCoordFromScriptResult(coords, scriptCoords, "screenX", "screenY");
		} catch (Exception ignored) {
		}
		try {
			Point loc = element.getLocation();
			Dimension size = element.getSize();
			coords.add(new int[] { loc.x + (size.width / 2), loc.y + (size.height / 2) });
			Point win = driver.get().browser().getWindowPosition();
			coords.add(new int[] { win.x + loc.x + (size.width / 2), win.y + loc.y + (size.height / 2) + 80 });
		} catch (Exception ignored) {
		}
		return dedupeCoordinates(coords);
	}

	private static int[][] dedupeCoordinates(java.util.List<int[]> coords) {
		java.util.List<int[]> unique = new java.util.ArrayList<>();
		for (int[] coord : coords) {
			boolean exists = false;
			for (int[] seen : unique) {
				if (seen[0] == coord[0] && seen[1] == coord[1]) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				unique.add(coord);
			}
		}
		return unique.toArray(new int[0][]);
	}

	private static void addCoordFromScriptResult(java.util.List<int[]> coords, Object scriptCoords,
			String xKey, String yKey) {
		if (scriptCoords instanceof java.util.Map) {
			@SuppressWarnings("unchecked")
			java.util.Map<String, Object> map = (java.util.Map<String, Object>) scriptCoords;
			addCoordIfPresent(coords, map.get(xKey), map.get(yKey));
		}
	}

	private static void addCoordIfPresent(java.util.List<int[]> coords, Object xObj, Object yObj) {
		if (xObj instanceof Number && yObj instanceof Number) {
			coords.add(new int[] { ((Number) xObj).intValue(), ((Number) yObj).intValue() });
		}
	}

	/** OCR fallback: click visible screen text (e.g. Rename / Delete in context menu). */
	public static boolean clickScreenText(String text) {
		try {
			List<ScreenText> words = driver.get().screen().getText();
			String want = text.trim().toLowerCase();
			Logger.logConsoleMessage("OCR scan for '" + text + "' — " + words.size() + " tokens on screen");
			for (ScreenText word : words) {
				if (word.getText() == null) {
					continue;
				}
				String found = word.getText().trim();
				String foundLower = found.toLowerCase();
				if (!found.equalsIgnoreCase(text) && !foundLower.equals(want) && !foundLower.contains(want)) {
					continue;
				}
				Point loc = word.getLocation();
				Dimension size = word.getSize();
				int x = loc.x + Math.max(1, size.width / 2);
				int y = loc.y + Math.max(1, size.height / 2);
				Logger.logConsoleMessage("OCR click on screen text '" + found + "' at " + x + "," + y);
				driver.get().browser().click(x, y);
				return true;
			}
		} catch (Exception e) {
			Logger.logConsoleMessage("OCR click failed for '" + text + "': " + e.getMessage());
		}
		return false;
	}

	public static boolean contextMenuViaShiftF10(By by, int wait) {
		DesktopBrowserElement element = prepareContextTarget(by, wait);
		if (element == null) {
			return false;
		}
		Logger.logConsoleMessage("Shift+F10 context menu on element... " + by);
		try {
			element.executeScript("arguments[0].focus();");
			driver.get().browser().sendKeys(WebKey.SHIFT, WebKey.F10);
			return true;
		} catch (Exception e) {
			Logger.logConsoleMessage("Shift+F10 context menu failed: " + e.getMessage());
			return false;
		}
	}

	public static boolean rightClickViaJsEvents(By by, int wait) {
		DesktopBrowserElement element = prepareContextTarget(by, wait);
		if (element == null) {
			return false;
		}
		Logger.logConsoleMessage("JS pointer context menu on element... " + by);
		try {
			element.executeScript(
					"var el = arguments[0];"
							+ "var r = el.getBoundingClientRect();"
							+ "var cx = r.left + r.width / 2;"
							+ "var cy = r.top + r.height / 2;"
							+ "['pointerdown','mousedown','contextmenu','pointerup','mouseup'].forEach(function(type){"
							+ "  el.dispatchEvent(new MouseEvent(type,{bubbles:true,cancelable:true,view:window,"
							+ "button:2,buttons:2,clientX:cx,clientY:cy}));"
							+ "});");
			return true;
		} catch (Exception e) {
			Logger.logConsoleMessage("JS pointer context menu failed: " + e.getMessage());
			return false;
		}
	}

	public static boolean rightClickOnElement(By by, int wait) {
		Logger.logConsoleMessage("Right-clicking on element... " + by);
		return openContextMenuOnElement(by, wait);
	}

	public static boolean sendFunctionKeyToElement(By by, int wait, WebKey key) {
		DesktopBrowserElement element = prepareContextTarget(by, wait);
		if (element == null) {
			return false;
		}
		try {
			element.click();
			driver.get().browser().sendKeys(key);
			return true;
		} catch (Exception e) {
			Logger.logConsoleMessage("Function key " + key + " failed on " + by + ": " + e.getMessage());
			return false;
		}
	}

	public static void launchApplicationOnBrowser() {
		String targetUrl = ConfigProps.getTargetURL();
		try {
			String current = requireDriver().browser().getCurrentUrl();
			if (current != null && isFulfillmentConsoleUrl(current) && FulfillmentJsUtil.isFulfillmentConsoleReady()) {
				Logger.logReportMessage("Already on Fulfillment Console — skipping navigation");
				requireDriver().browser().maximizeWindow();
				return;
			}
		} catch (Exception ignored) {
		}
		Logger.logReportMessage("Launching Fulfillment Console: " + targetUrl);
        requireDriver().browser().getUrl(targetUrl);
        WaitUtil.waitForJSToLoad(8);
        requireDriver().browser().maximizeWindow();
        Logger.logReportMessage("Browser window maximized");
	}

	private static boolean isFulfillmentConsoleUrl(String url) {
		if (url == null) {
			return false;
		}
		String lower = url.toLowerCase();
		return lower.contains("fulfillment") || lower.contains("operationsconsole");
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

}
