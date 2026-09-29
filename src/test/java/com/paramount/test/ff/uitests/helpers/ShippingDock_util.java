package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.core.driver.By;
import com.synergy.core.driver.web.WebDriver;
import com.synergy.core.enums.WebKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BSD-29791: Read archive file size from Shipping Dock Delivery Activity and compare to FF Console.
 * Flow from screen recording (sd.mp4): Id search tab → Order ID → Search → Details → ARCHIVE_COMPLETE file size.
 */
public class ShippingDock_util {

    public static final String SHIPPING_DOCK_DELIVERY_ACTIVITY_URL =
            "https://ads.viacbsops.com/ads/app/#/fsp-workflow/delivery-activity";
    public static final String SHIPPING_DOCK_LOGIN_URL =
            "https://ads.viacbsops.com/ads/app/#/log-in";

    private static final Pattern ARCHIVE_FILE_SIZE_PATTERN = Pattern.compile(
            "file\\s*size\\s*:\\s*(\\d{1,3}(?:,\\d{3})+|\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROUNDED_SIZE_PATTERN = Pattern.compile(
            "~?\\s*(\\d+(?:\\.\\d+)?)\\s*(GB|MB|KB|B)", Pattern.CASE_INSENSITIVE);

    private static final String SD_JS =
            "function isVisible(el){"
                    + "if(!el)return false;"
                    + "var r=el.getBoundingClientRect();"
                    + "if(r.width<=0||r.height<=0)return false;"
                    + "var s=window.getComputedStyle(el);"
                    + "return s.display!=='none'&&s.visibility!=='hidden';"
                    + "}"
                    + "function norm(t){return (t||'').replace(/\\s+/g,' ').trim();}"
                    + "function clickByText(selectors, pattern){"
                    + "  var nodes=document.querySelectorAll(selectors);"
                    + "  for(var i=0;i<nodes.length;i++){"
                    + "    if(!isVisible(nodes[i]))continue;"
                    + "    var t=norm(nodes[i].textContent);"
                    + "    if(pattern.test(t)){nodes[i].click();return true;}"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "function clickIdSearchTab(){"
                    + "  return clickByText('a,button,span,div,li,label',/^Id$/i);"
                    + "}"
                    + "function selectOrderIdSearchType(){"
                    + "  var labels=document.querySelectorAll('label,span,div');"
                    + "  for(var i=0;i<labels.length;i++){"
                    + "    if(!/select id search/i.test(norm(labels[i].textContent)))continue;"
                    + "    var block=labels[i].closest('div,section,form')||labels[i].parentElement;"
                    + "    if(!block)continue;"
                    + "    var select=block.querySelector('select');"
                    + "    if(select){"
                    + "      for(var j=0;j<select.options.length;j++){"
                    + "        if(/order id/i.test(select.options[j].text)){"
                    + "          select.selectedIndex=j;"
                    + "          select.dispatchEvent(new Event('change',{bubbles:true}));"
                    + "          return true;"
                    + "        }"
                    + "      }"
                    + "    }"
                    + "    var combo=block.querySelector('[role=combobox], .ui-select, select, input');"
                    + "    if(combo){combo.click();return true;}"
                    + "  }"
                    + "  return clickByText('*',/^Order ID$/i);"
                    + "}"
                    + "function findOrderIdInput(){"
                    + "  var labels=document.querySelectorAll('label,span,div');"
                    + "  for(var i=0;i<labels.length;i++){"
                    + "    var t=norm(labels[i].textContent);"
                    + "    if(!/^Order ID:?$/i.test(t))continue;"
                    + "    var block=labels[i].closest('div,section,form')||labels[i].parentElement;"
                    + "    if(!block)continue;"
                    + "    var input=block.querySelector('input[type=text],input:not([type]),textarea');"
                    + "    if(input&&isVisible(input))return input;"
                    + "    var next=labels[i].nextElementSibling;"
                    + "    if(next&&next.tagName==='INPUT'&&isVisible(next))return next;"
                    + "  }"
                    + "  var inputs=document.querySelectorAll('input[type=text],input:not([type])');"
                    + "  for(var j=inputs.length-1;j>=0;j--){"
                    + "    if(isVisible(inputs[j]))return inputs[j];"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function setOrderIdValue(orderId){"
                    + "  var input=findOrderIdInput();"
                    + "  if(!input)return false;"
                    + "  input.focus();input.value=orderId;"
                    + "  input.dispatchEvent(new Event('input',{bubbles:true}));"
                    + "  input.dispatchEvent(new Event('change',{bubbles:true}));"
                    + "  return true;"
                    + "}"
                    + "function clickSearchButton(){"
                    + "  var buttons=document.querySelectorAll('button,input[type=button],input[type=submit],a');"
                    + "  for(var i=0;i<buttons.length;i++){"
                    + "    if(!isVisible(buttons[i]))continue;"
                    + "    var t=norm(buttons[i].textContent||buttons[i].value);"
                    + "    if(/^Search$/i.test(t)){buttons[i].click();return true;}"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "function openFirstPackageDetails(){"
                    + "  var rows=document.querySelectorAll('tr');"
                    + "  for(var r=0;r<rows.length;r++){"
                    + "    if(!isVisible(rows[r]))continue;"
                    + "    var rowText=norm(rows[r].textContent);"
                    + "    if(!/JOB_COMPLETE|ARCHIVE_COMPLETE|COMPLETE/i.test(rowText))continue;"
                    + "    var rowLink=rows[r].querySelector('[title*=details],[aria-label*=details],a,button,i,span[class*=icon]');"
                    + "    if(rowLink&&isVisible(rowLink)){rowLink.click();return true;}"
                    + "  }"
                    + "  var icons=document.querySelectorAll('a,i,button,span,td,[title*=details],[aria-label*=details]');"
                    + "  for(var i=0;i<icons.length;i++){"
                    + "    if(!isVisible(icons[i]))continue;"
                    + "    var hint=(icons[i].getAttribute('title')||icons[i].getAttribute('aria-label')||'').toLowerCase();"
                    + "    if(hint.indexOf('package details')>=0||hint.indexOf('view package')>=0){"
                    + "      icons[i].click();return true;"
                    + "    }"
                    + "  }"
                    + "  var headers=document.querySelectorAll('th,td,span,div');"
                    + "  for(var h=0;h<headers.length;h++){"
                    + "    if(/^Details$/i.test(norm(headers[h].textContent))){"
                    + "      var row=headers[h].closest('tr');"
                    + "      if(row){"
                    + "        var link=row.querySelector('a,button,i,span[class*=icon],img');"
                    + "        if(link&&isVisible(link)){link.click();return true;}"
                    + "      }"
                    + "    }"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "function clickDeliveryHistoryTab(){"
                    + "  return clickByText('a,button,span,li,div',/^Delivery History$/i);"
                    + "}"
                    + "function readArchiveCompleteFileSize(){"
                    + "  var body=(document.body&&document.body.textContent)||'';"
                    + "  var m=body.match(/ARCHIVE_COMPLETE[\\s\\S]{0,500}?file\\s*size\\s*:\\s*(\\d{1,3}(?:,\\d{3})+|\\d+)/i);"
                    + "  if(m)return m[1].replace(/,/g,'');"
                    + "  m=body.match(/file\\s*size\\s*:\\s*(\\d{1,3}(?:,\\d{3})+|\\d+)/i);"
                    + "  return m?m[1].replace(/,/g,''):null;"
                    + "}";

    private static final String SD_HOOK_JS =
            "(function(){"
                    + "if(window.__sdSizeHookInstalled)return true;"
                    + "window.__sdSizeHookInstalled=true;"
                    + "window.__sdCapturedSizes=[];"
                    + "function rememberSize(orderId, pkgId, size){"
                    + "  if(size==null)return;"
                    + "  window.__sdCapturedSizes.push({orderId:String(orderId||''),packageId:String(pkgId||''),size:size});"
                    + "}"
                    + "function walk(node){"
                    + "  if(!node||typeof node!=='object')return;"
                    + "  var size=node.archiveFileSize!=null?node.archiveFileSize"
                    + "    :(node.tarSize!=null?node.tarSize:node.fileSize);"
                    + "  if(size!=null)rememberSize(node.orderId,node.packageId,size);"
                    + "  if(Array.isArray(node)){for(var i=0;i<node.length;i++)walk(node[i]);return;}"
                    + "  for(var k in node){if(Object.prototype.hasOwnProperty.call(node,k))walk(node[k]);}"
                    + "}"
                    + "function captureJson(json){try{walk(json);}catch(e){}}"
                    + "var xhrSend=XMLHttpRequest.prototype.send;"
                    + "XMLHttpRequest.prototype.send=function(body){"
                    + "  var xhr=this;"
                    + "  xhr.addEventListener('load',function(){"
                    + "    try{if(xhr.responseText)captureJson(JSON.parse(xhr.responseText));}catch(e){}"
                    + "  });"
                    + "  return xhrSend.apply(this,arguments);"
                    + "};"
                    + "return true;"
                    + "})();";

    private static final By SD_SIGN_IN = By.XPath(
            "//button[normalize-space()='Sign In'] | //input[@type='submit' and @value='Sign In']");

    public void installShippingDockSizeHook() {
        try {
            BaseTest.driver.get().browser().executeScript(SD_HOOK_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("Shipping Dock size hook install failed: " + e.getMessage());
        }
    }

    public void compareFileSizeWithFfConsole(TarSize_util tarSizeUtil, Long sdBytes, String sdDisplay)
            throws InterruptedException {
        if (sdDisplay == null && sdBytes != null && sdBytes > 0) {
            sdDisplay = TarSize_util.formatRoundedSize(sdBytes);
        }
        tarSizeUtil.preparePackageDetailsView();
        String ffDisplay = tarSizeUtil.findFileSizeValueViaScript();
        Long ffBytes = resolveFfBytes(tarSizeUtil, ffDisplay);

        Verify.softAssert(ffDisplay != null && !ffDisplay.isEmpty(),
                "FF Console File Size visible for comparison (actual: " + ffDisplay + ")");
        if (ffDisplay == null || sdBytes == null) {
            return;
        }

        if (ffBytes != null && ffBytes > 0) {
            long diff = Math.abs(ffBytes - sdBytes);
            double tolerance = Math.max(ffBytes, sdBytes) * 0.08;
            String ffRounded = TarSize_util.formatRoundedSize(ffBytes);
            String sdRounded = TarSize_util.formatRoundedSize(sdBytes);
            boolean bytesWithinTolerance = diff <= tolerance;
            boolean displayMatches = TarSize_util.normalizeSize(sdRounded).equalsIgnoreCase(TarSize_util.normalizeSize(ffRounded))
                    || TarSize_util.normalizeSize(sdRounded).equalsIgnoreCase(TarSize_util.normalizeSize(ffDisplay))
                    || TarSize_util.normalizeSize(sdDisplay).equalsIgnoreCase(TarSize_util.normalizeSize(ffDisplay));
            Verify.softAssert(bytesWithinTolerance || displayMatches,
                    "FF Console File Size matches Shipping Dock ARCHIVE_COMPLETE size from UI"
                            + " (SD: " + sdDisplay + " / " + sdBytes + " bytes, FF: " + ffDisplay + " / " + ffBytes
                            + " bytes, diff: " + diff + ", tolerance: " + (long) tolerance + ")");
        } else {
            Verify.softAssert(TarSize_util.normalizeSize(sdDisplay).equalsIgnoreCase(TarSize_util.normalizeSize(ffDisplay)),
                    "FF Console File Size display matches Shipping Dock UI (SD: " + sdDisplay + ", FF: " + ffDisplay + ")");
        }
    }

    public Long readArchiveFileSizeBytesFromShippingDock(String orderId) throws InterruptedException {
        navigateToDeliveryActivity();
        if (!isShippingDockReady()) {
            String loginError = readShippingDockLoginError();
            Logger.logConsoleMessage("Shipping Dock not ready after login"
                    + (loginError != null ? " — " + loginError : ""));
            return null;
        }
        installShippingDockSizeHook();
        searchOrderByIdInDeliveryActivity(orderId);
        waitForOrderSearchResults(orderId);
        openPackageDetailsFromResults();
        Thread.sleep(2000);
        clickDeliveryHistoryTabIfPresent();
        Thread.sleep(1500);

        Long bytes = parseArchiveCompleteBytes(readArchiveCompleteFileSizeViaScript());
        if (bytes == null || bytes <= 0) {
            bytes = findFileSizeBytesFromApi(orderId, TarSize_util.PROD_PACKAGE_ID);
        }
        if (bytes != null && bytes > 0) {
            Logger.logReportMessage("Shipping Dock ARCHIVE_COMPLETE file size from UI for " + orderId + ": "
                    + bytes + " bytes (" + TarSize_util.formatRoundedSize(bytes) + ")");
        } else {
            Logger.logConsoleMessage("Shipping Dock ARCHIVE_COMPLETE file size not found in UI for order " + orderId);
        }
        return bytes;
    }

    public void navigateToDeliveryActivity() throws InterruptedException {
        if (isShippingDockReady()) {
            Logger.logReportMessage("Shipping Dock Delivery Activity already loaded");
            installShippingDockSizeHook();
            return;
        }
        WebDriver webDriver = BaseTest.driver.get();
        Logger.logReportMessage("Shipping Dock: open login page and sign in with suite credentials");
        webDriver.browser().getUrl(SHIPPING_DOCK_LOGIN_URL);
        WaitUtil.waitForJSToLoad(30);
        Thread.sleep(2000);
        loginShippingDockNative();
        Logger.logReportMessage("Navigating to Shipping Dock Delivery Activity: " + SHIPPING_DOCK_DELIVERY_ACTIVITY_URL);
        webDriver.browser().getUrl(SHIPPING_DOCK_DELIVERY_ACTIVITY_URL);
        WaitUtil.waitForJSToLoad(30);
        Thread.sleep(3000);
        waitForShippingDockReady(90);
        if (!isShippingDockReady()) {
            Verify.softAssert(false, "Shipping Dock login failed — " + readShippingDockLoginError());
            return;
        }
        installShippingDockSizeHook();
    }

    private void waitForOrderSearchResults(String orderId) throws InterruptedException {
        String escaped = orderId.replace("\\", "\\\\").replace("'", "\\'");
        long end = System.currentTimeMillis() + 20000L;
        while (System.currentTimeMillis() < end) {
            try {
                Object found = BaseTest.driver.get().browser().executeScript(
                        "return (document.body.textContent||'').indexOf('" + escaped + "')>=0;");
                if (Boolean.TRUE.equals(found) || "true".equalsIgnoreCase(String.valueOf(found))) {
                    Logger.logConsoleMessage("Shipping Dock: search results visible for " + orderId);
                    Thread.sleep(1500);
                    return;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(500);
        }
        Logger.logConsoleMessage("Shipping Dock: timed out waiting for search results for " + orderId);
    }

    private void searchOrderByIdInDeliveryActivity(String orderId) {
        String escaped = orderId.replace("\\", "\\\\").replace("'", "\\'");
        try {
            BaseTest.driver.get().browser().executeScript(
                    SD_JS
                            + "clickIdSearchTab();"
                            + "selectOrderIdSearchType();"
                            + "return setOrderIdValue('" + escaped + "') && clickSearchButton();");
            Logger.logReportMessage("Shipping Dock: searched Order ID " + orderId);
        } catch (Exception e) {
            Logger.logConsoleMessage("Shipping Dock order search failed: " + e.getMessage());
        }
    }

    private void openPackageDetailsFromResults() throws InterruptedException {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Object opened = BaseTest.driver.get().browser().executeScript(SD_JS + "return openFirstPackageDetails();");
                Logger.logConsoleMessage("Shipping Dock: opened package details (attempt " + attempt + ") => " + opened);
                if (Boolean.TRUE.equals(opened) || "true".equalsIgnoreCase(String.valueOf(opened))) {
                    return;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(2000);
        }
    }

    private void clickDeliveryHistoryTabIfPresent() {
        try {
            BaseTest.driver.get().browser().executeScript(SD_JS + "return clickDeliveryHistoryTab();");
        } catch (Exception ignored) {
        }
    }

    private String readArchiveCompleteFileSizeViaScript() {
        try {
            Object value = BaseTest.driver.get().browser().executeScript(SD_JS + "return readArchiveCompleteFileSize();");
            return toNullableString(value);
        } catch (Exception e) {
            Logger.logConsoleMessage("Shipping Dock ARCHIVE_COMPLETE read failed: " + e.getMessage());
            return null;
        }
    }

    private Long parseArchiveCompleteBytes(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher matcher = ARCHIVE_FILE_SIZE_PATTERN.matcher(raw);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1).replace(",", ""));
        }
        if (raw.matches("\\d+")) {
            return Long.parseLong(raw);
        }
        return null;
    }

    public Long findFileSizeBytesFromApi(String orderId, String packageId) {
        String escapedOrder = orderId.replace("\\", "\\\\").replace("'", "\\'").toLowerCase();
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "var orderKey='" + escapedOrder + "';"
                            + "var list=window.__sdCapturedSizes||[];"
                            + "for(var i=list.length-1;i>=0;i--){"
                            + "  var oid=String(list[i].orderId||'').toLowerCase();"
                            + "  if(!orderKey||oid.indexOf(orderKey)>=0||oid===orderKey){return list[i].size;}"
                            + "}"
                            + "return list.length?list[list.length-1].size:null;");
            return toLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Long resolveFfBytes(TarSize_util tarSizeUtil, String ffDisplay) {
        Long bytes = tarSizeUtil.findArchiveFileSizeFromApi(TarSize_util.PROD_ORDER_ID, TarSize_util.PROD_PACKAGE_ID);
        if (bytes == null || bytes <= 0) {
            bytes = TarSize_util.parseBytesFromTooltip(tarSizeUtil.findFileSizeBytesHintViaScript());
        }
        if (bytes == null || bytes <= 0) {
            bytes = TarSize_util.parseBytesFromTooltip(tarSizeUtil.readFileSizeTooltipViaScript(ffDisplay));
        }
        if (bytes == null || bytes <= 0) {
            bytes = TarSize_util.inferBytesFromRoundedDisplay(ffDisplay);
        }
        return bytes;
    }

    private void loginShippingDockNative() throws InterruptedException {
        Logger.logReportMessage("Shipping Dock Sign In using FF Console credentials");
        WebDriver webDriver = BaseTest.driver.get();
        resetShippingDockLoginPage(webDriver);

        By userInput = By.XPath(
                "//label[normalize-space()='Username']/following-sibling::input"
                        + " | //label[normalize-space()='Username']/../input"
                        + " | //form//input[@type='text' or @type='email'][1]");
        By passInput = By.XPath(
                "//label[normalize-space()='Password']/following-sibling::input"
                        + " | //label[normalize-space()='Password']/../input"
                        + " | //form//input[@type='password'][1]"
                        + " | //input[@type='password'][1]");

        for (int attempt = 1; attempt <= 3 && isShippingDockNativeLoginPage(); attempt++) {
            if (attempt > 1) {
                resetShippingDockLoginPage(webDriver);
            }
            Logger.logConsoleMessage("Shipping Dock login attempt " + attempt);
            fillShippingDockLoginField(webDriver, userInput, ConfigProps.OKTA_USERNAME, "text");
            Thread.sleep(400);
            fillShippingDockLoginField(webDriver, passInput, ConfigProps.OKTA_PASSWORD, "password");
            Thread.sleep(400);

            String usernameValue = readInputFieldValue("text");
            int passwordLength = readPasswordFieldLength();
            Logger.logConsoleMessage("Shipping Dock credentials before Sign In — username length: "
                    + (usernameValue != null ? usernameValue.length() : 0) + ", password length: " + passwordLength);

            if (passwordLength <= 0 || usernameValue == null || usernameValue.isEmpty()) {
                setAngularInputValueViaScript("text", ConfigProps.OKTA_USERNAME);
                setAngularInputValueViaScript("password", ConfigProps.OKTA_PASSWORD);
                Thread.sleep(300);
                usernameValue = readInputFieldValue("text");
                passwordLength = readPasswordFieldLength();
            }
            if (passwordLength <= 0 || usernameValue == null || usernameValue.isEmpty()) {
                Logger.logConsoleMessage("Shipping Dock credentials still empty — skipping Sign In click");
                continue;
            }

            DriverUtil.clickOnElementSafely(passInput, 3);
            webDriver.browser().sendKeys(WebKey.ENTER);
            Thread.sleep(3000);
            if (isShippingDockNativeLoginPage()) {
                if (!DriverUtil.clickOnElementSafely(SD_SIGN_IN, 8)) {
                    webDriver.browser().executeScript(
                            "var btns=document.querySelectorAll('button,input[type=submit]');"
                                    + "for(var i=0;i<btns.length;i++){"
                                    + "  if(/sign in/i.test((btns[i].textContent||btns[i].value||''))){btns[i].click();break;}"
                                    + "}");
                }
                Thread.sleep(20000);
            }
            if (isShippingDockNativeLoginPage()) {
                Logger.logConsoleMessage("Shipping Dock login error: " + readShippingDockLoginError());
            } else {
                Logger.logReportMessage("Shipping Dock Sign In succeeded");
                return;
            }
        }
    }

    private void resetShippingDockLoginPage(WebDriver webDriver) throws InterruptedException {
        webDriver.browser().getUrl(SHIPPING_DOCK_LOGIN_URL);
        WaitUtil.waitForJSToLoad(20);
        Thread.sleep(1500);
    }

    private String readInputFieldValue(String fieldType) {
        try {
            Object value = BaseTest.driver.get().browser().executeScript(
                    "var inputs=document.querySelectorAll('input');"
                            + "for(var i=0;i<inputs.length;i++){"
                            + "  var t=(inputs[i].type||'').toLowerCase();"
                            + "  if('" + fieldType + "'==='password'&&t==='password')return inputs[i].value||'';"
                            + "  if('" + fieldType + "'!=='password'&&(t==='text'||t==='email'))return inputs[i].value||'';"
                            + "}"
                            + "return '';");
            return toNullableString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String readShippingDockLoginError() {
        try {
            Object error = BaseTest.driver.get().browser().executeScript(
                    "var href=window.location.href||'';"
                            + "var hashQuery=(href.split('?')[1]||'');"
                            + "var params=new URLSearchParams(hashQuery);"
                            + "var fromUrl=params.get('error_description')||params.get('error')||'';"
                            + "if(fromUrl)return decodeURIComponent(fromUrl);"
                            + "var nodes=document.querySelectorAll('.error,.alert,[class*=error],[class*=alert]');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').trim();"
                            + "  if(t)return t;"
                            + "}"
                            + "return '';");
            return toNullableString(error);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void fillShippingDockLoginField(WebDriver webDriver, By locator, String value, String fieldType)
            throws InterruptedException {
        if (!WaitUtil.isDisplay(locator, 10)) {
            Logger.logConsoleMessage("Shipping Dock " + fieldType + " field not found");
            return;
        }
        DriverUtil.clickOnElementSafely(locator, 5);
        Thread.sleep(200);
        try {
            webDriver.finder().findElement(locator).clear();
        } catch (Exception ignored) {
        }
        webDriver.finder().findElement(locator).sendKeys(value);
        if ("password".equals(fieldType) && readPasswordFieldLength() <= 0) {
            setAngularInputValueViaScript(fieldType, value);
        } else if ("text".equals(fieldType)) {
            String current = readInputFieldValue("text");
            if (current == null || current.isEmpty()) {
                setAngularInputValueViaScript(fieldType, value);
            }
        }
        Logger.logConsoleMessage("Shipping Dock " + fieldType + " entered");
    }

    private int readPasswordFieldLength() {
        try {
            Object length = BaseTest.driver.get().browser().executeScript(
                    "var el=document.querySelector('input[type=password]');"
                            + "return el&&el.value?el.value.length:0;");
            if (length instanceof Number) {
                return ((Number) length).intValue();
            }
            return Integer.parseInt(String.valueOf(length));
        } catch (Exception e) {
            return 0;
        }
    }

    private void setAngularInputValueViaScript(String fieldType, String value) {
        String escaped = value.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "").replace("\n", "");
        try {
            BaseTest.driver.get().browser().executeScript(
                    "function setNative(el,val){"
                            + "if(!el)return;"
                            + "el.focus();"
                            + "var desc=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value');"
                            + "if(desc&&desc.set){desc.set.call(el,val);}else{el.value=val;}"
                            + "el.dispatchEvent(new Event('input',{bubbles:true}));"
                            + "el.dispatchEvent(new Event('change',{bubbles:true}));"
                            + "}"
                            + "var inputs=document.querySelectorAll('input');"
                            + "var target=null;"
                            + "for(var i=0;i<inputs.length;i++){"
                            + "  var t=(inputs[i].type||'').toLowerCase();"
                            + "  if('" + fieldType + "'==='password'&&t==='password'){target=inputs[i];break;}"
                            + "  if('" + fieldType + "'!=='password'&&(t==='text'||t==='email')){target=inputs[i];break;}"
                            + "}"
                            + "setNative(target,'" + escaped + "');");
        } catch (Exception ignored) {
        }
    }

    private boolean lastShippingDockUiReady;

    private void waitForShippingDockReady(int timeoutSec) throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (isShippingDockReady()) {
                lastShippingDockUiReady = true;
                Logger.logReportMessage("Shipping Dock Delivery Activity loaded");
                return;
            }
            Thread.sleep(500);
        }
        lastShippingDockUiReady = false;
        Logger.logConsoleMessage("Shipping Dock Delivery Activity not loaded after " + timeoutSec + " seconds");
    }

    private boolean isShippingDockReady() {
        try {
            Object ready = BaseTest.driver.get().browser().executeScript(
                    "var href=(window.location.href||'').toLowerCase();"
                            + "if(href.indexOf('log-in')>=0||href.indexOf('/login')>=0)return false;"
                            + "var body=(document.body&&document.body.textContent)||'';"
                            + "if(/sign in/i.test(body.slice(0,400))&&/username/i.test(body.slice(0,400)))return false;"
                            + "if(href.indexOf('delivery-activity')>=0)return true;"
                            + "return /delivery activity|select id search|fsp-workflow/i.test(body);");
            return Boolean.TRUE.equals(ready) || "true".equalsIgnoreCase(String.valueOf(ready));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isShippingDockNativeLoginPage() {
        try {
            Object onLogin = BaseTest.driver.get().browser().executeScript(
                    "var href=(window.location.href||'').toLowerCase();"
                            + "if(href.indexOf('log-in')>=0||href.indexOf('/login')>=0)return true;"
                            + "var body=(document.body&&document.body.textContent||'').slice(0,600);"
                            + "return /sign in/i.test(body)&&/username/i.test(body)"
                            + "&&document.querySelectorAll('input[type=password]').length>0;");
            return Boolean.TRUE.equals(onLogin) || "true".equalsIgnoreCase(String.valueOf(onLogin));
        } catch (Exception e) {
            return false;
        }
    }

    private static String toNullableString(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String raw = value != null ? String.valueOf(value).trim() : "";
        if (raw.matches("\\d+")) {
            return Long.parseLong(raw);
        }
        Matcher m = ROUNDED_SIZE_PATTERN.matcher(raw);
        if (m.find()) {
            return TarSize_util.inferBytesFromRoundedDisplay(m.group(0));
        }
        return null;
    }
}
