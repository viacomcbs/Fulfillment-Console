package com.paramount.test.ff.uitests.helpers.tablerefresh;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Captures filterOrder and LineItemViewLanding GraphQL traffic for BSD-29582. */
public class TableRefreshNetworkUtil extends BaseTest {

    private static final String INSTALL_SCRIPT =
            "if (!window.__trCapture) { window.__trCapture = {filterOrder:0,lineItemViewLanding:0,failures:[]}; }"
                    + "if (window.__trFetchPatched) { return; }"
                    + "window.__trFetchPatched = true;"
                    + "function noteBody(body) {"
                    + "  if (!body) return;"
                    + "  var s = typeof body === 'string' ? body : (body.toString ? body.toString() : '');"
                    + "  if (s.indexOf('filterOrder') >= 0) window.__trCapture.filterOrder++;"
                    + "  if (s.indexOf('LineItemViewLanding') >= 0) window.__trCapture.lineItemViewLanding++;"
                    + "}"
                    + "function noteFailure(url, status, detail) {"
                    + "  try { window.__trCapture.failures.push(String(url) + ' | HTTP ' + status"
                    + "    + (detail ? ' | ' + detail : '')); } catch (e) {}"
                    + "}"
                    + "var origFetch = window.fetch;"
                    + "window.fetch = function() {"
                    + "  var reqUrl = arguments[0] && arguments[0].url ? arguments[0].url : arguments[0];"
                    + "  try {"
                    + "    var b = arguments[1] && arguments[1].body;"
                    + "    if (b && typeof b.text === 'function') {"
                    + "      b.clone().text().then(function(t){ noteBody(t); });"
                    + "    } else { noteBody(b); }"
                    + "  } catch (e) {}"
                    + "  return origFetch.apply(this, arguments).then(function(resp) {"
                    + "    try { if (resp && !resp.ok) noteFailure(resp.url || reqUrl, resp.status, resp.statusText); }"
                    + "    catch (e) {} return resp;"
                    + "  });"
                    + "};"
                    + "if (!window.__trXhrPatched) {"
                    + "window.__trXhrPatched = true;"
                    + "var origOpen = XMLHttpRequest.prototype.open;"
                    + "var origSend = XMLHttpRequest.prototype.send;"
                    + "XMLHttpRequest.prototype.open = function(m, url) { this.__trUrl = url; return origOpen.apply(this, arguments); };"
                    + "XMLHttpRequest.prototype.send = function(body) {"
                    + "  var xhr = this;"
                    + "  try { noteBody(body); } catch (e) {}"
                    + "  xhr.addEventListener('load', function() {"
                    + "    try { if (xhr.status >= 400) noteFailure(xhr.__trUrl, xhr.status, xhr.statusText); } catch (e) {}"
                    + "  });"
                    + "  return origSend.apply(this, arguments);"
                    + "};"
                    + "}";

    private static final String CLEAR_SCRIPT =
            "if (window.__trCapture) {"
                    + " window.__trCapture.filterOrder = 0;"
                    + " window.__trCapture.lineItemViewLanding = 0;"
                    + " window.__trCapture.failures = [];"
                    + "}";

    private static final String READ_SCRIPT =
            "return window.__trCapture ? JSON.stringify(window.__trCapture) :"
                    + " '{\"filterOrder\":0,\"lineItemViewLanding\":0,\"failures\":[]}';";

    private static final String FAILURE_READ =
            "return window.__trCapture && window.__trCapture.failures"
                    + " ? JSON.stringify(window.__trCapture.failures) : '[]';";

    public void installCaptureHook() {
        execute("delete window.__trFetchPatched; delete window.__trXhrPatched;");
        execute(INSTALL_SCRIPT);
    }

    public void clearCapture() {
        execute(CLEAR_SCRIPT);
    }

    public int getFilterOrderCount() {
        return readCaptureInt("filterOrder");
    }

    public int getLineItemViewLandingCount() {
        return readCaptureInt("lineItemViewLanding");
    }

    public boolean waitForFilterOrderCall(int timeoutSeconds) throws InterruptedException {
        return waitForFieldAtLeast("filterOrder", 1, timeoutSeconds);
    }

    public boolean waitForLineItemViewLandingCall(int timeoutSeconds) throws InterruptedException {
        return waitForFieldAtLeast("lineItemViewLanding", 1, timeoutSeconds);
    }

    @SuppressWarnings("unchecked")
    public List<String> readApiFailures() {
        try {
            Object result = driver.get().browser().executeScript(FAILURE_READ);
            if (result == null) {
                return Collections.emptyList();
            }
            String json = String.valueOf(result);
            if (json.length() < 3) {
                return Collections.emptyList();
            }
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            if (json.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(json.split(","));
        } catch (Exception e) {
            Logger.logMessage("Could not read table refresh API failures: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean waitForFieldAtLeast(String field, int minimum, int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (readCaptureInt(field) >= minimum) {
                return true;
            }
            Thread.sleep(400);
        }
        return readCaptureInt(field) >= minimum;
    }

    private int readCaptureInt(String field) {
        try {
            Object result = driver.get().browser().executeScript(READ_SCRIPT);
            if (result == null) {
                return 0;
            }
            String text = String.valueOf(result);
            String marker = "\"" + field + "\":";
            int idx = text.indexOf(marker);
            if (idx < 0) {
                return 0;
            }
            int start = idx + marker.length();
            int end = start;
            while (end < text.length() && Character.isDigit(text.charAt(end))) {
                end++;
            }
            return Integer.parseInt(text.substring(start, end));
        } catch (Exception e) {
            Logger.logMessage("Could not read table refresh capture field " + field + ": " + e.getMessage());
            return 0;
        }
    }

    private void execute(String script) {
        try {
            driver.get().browser().executeScript(script);
        } catch (Exception e) {
            Logger.logMessage("Table refresh network hook failed: " + e.getMessage());
        }
    }
}
