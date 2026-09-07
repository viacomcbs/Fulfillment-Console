package com.paramount.test.ff.uitests.helpers.featureflag;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Captures GraphQL traffic and Authorization headers for BSD-28459 auth/network checks. */
public class FeatureFlagNetworkUtil extends BaseTest {

    private static final String INSTALL_SCRIPT =
            "if (!window.__ffLdCapture) {"
                    + "window.__ffLdCapture = {graphql: 0, authed: 0, failures: []};"
                    + "}"
                    + "function note(url, headers) {"
                    + "  if (!url) return;"
                    + "  var u = String(url);"
                    + "  if (u.indexOf('graphql') >= 0) window.__ffLdCapture.graphql++;"
                    + "  var auth = '';"
                    + "  if (headers) {"
                    + "    auth = headers.Authorization || headers.authorization || '';"
                    + "    if (!auth && headers.get) auth = headers.get('Authorization') || '';"
                    + "  }"
                    + "  if (auth && auth.length > 10) window.__ffLdCapture.authed++;"
                    + "}"
                    + "function noteFailure(url, status, detail) {"
                    + "  try { window.__ffLdCapture.failures.push(String(url) + ' | HTTP ' + status"
                    + "    + (detail ? ' | ' + detail : '')); } catch (e) {}"
                    + "}"
                    + "if (window.__ffLdFetchPatched) { return; }"
                    + "window.__ffLdFetchPatched = true;"
                    + "var origFetch = window.fetch;"
                    + "window.fetch = function() {"
                    + "  var reqUrl = arguments[0] && arguments[0].url ? arguments[0].url : arguments[0];"
                    + "  try { note(reqUrl, arguments[1] && arguments[1].headers); } catch (e) {}"
                    + "  return origFetch.apply(this, arguments).then(function(resp) {"
                    + "    try { if (resp && !resp.ok) noteFailure(resp.url || reqUrl, resp.status, resp.statusText); }"
                    + "    catch (e) {} return resp;"
                    + "  });"
                    + "};"
                    + "if (!window.__ffXhrPatched) {"
                    + "window.__ffXhrPatched = true;"
                    + "var origOpen = XMLHttpRequest.prototype.open;"
                    + "var origSend = XMLHttpRequest.prototype.send;"
                    + "var origSetHeader = XMLHttpRequest.prototype.setRequestHeader;"
                    + "XMLHttpRequest.prototype.open = function(method, url) {"
                    + "  this.__ffUrl = url; this.__ffHeaders = {};"
                    + "  return origOpen.apply(this, arguments);"
                    + "};"
                    + "XMLHttpRequest.prototype.setRequestHeader = function(name, value) {"
                    + "  if (this.__ffHeaders) this.__ffHeaders[name] = value;"
                    + "  return origSetHeader.apply(this, arguments);"
                    + "};"
                    + "XMLHttpRequest.prototype.send = function() {"
                    + "  var xhr = this;"
                    + "  try { note(xhr.__ffUrl, xhr.__ffHeaders); } catch (e) {}"
                    + "  xhr.addEventListener('load', function() {"
                    + "    try { if (xhr.status >= 400) noteFailure(xhr.__ffUrl, xhr.status, xhr.statusText); }"
                    + "    catch (e) {}"
                    + "  });"
                    + "  return origSend.apply(this, arguments);"
                    + "};"
                    + "}";

    private static final String CLEAR_SCRIPT =
            "if (window.__ffLdCapture) {"
                    + " window.__ffLdCapture.graphql = 0;"
                    + " window.__ffLdCapture.authed = 0;"
                    + " window.__ffLdCapture.failures = [];"
                    + " }";

    private static final String READ_SCRIPT =
            "return window.__ffLdCapture ? JSON.stringify(window.__ffLdCapture) : '{\"graphql\":0,\"authed\":0}';";

    private static final String CONSOLE_ERROR_INSTALL =
            "if (!window.__ffConsoleErrors) {"
                    + "window.__ffConsoleErrors = [];"
                    + "var origErr = console.error;"
                    + "console.error = function() {"
                    + "  try { window.__ffConsoleErrors.push(Array.prototype.slice.call(arguments).join(' ')); } catch (e) {}"
                    + "  return origErr.apply(console, arguments);"
                    + "};"
                    + "window.addEventListener('error', function(e) {"
                    + "  if (e && e.message) window.__ffConsoleErrors.push(e.message);"
                    + "});"
                    + "}";

    private static final String CONSOLE_ERROR_READ =
            "return window.__ffConsoleErrors ? JSON.stringify(window.__ffConsoleErrors) : '[]';";

    private static final String FAILURE_READ =
            "return window.__ffLdCapture && window.__ffLdCapture.failures"
                    + " ? JSON.stringify(window.__ffLdCapture.failures) : '[]';";

    public void installCaptureHook() {
        execute(INSTALL_SCRIPT);
    }

    /** Installs network + console hooks on the current SPA page (after login). */
    public void installDiagnosticHooksOnCurrentPage() {
        execute("delete window.__ffLdFetchPatched; delete window.__ffXhrPatched;");
        installCaptureHook();
        installConsoleErrorHook();
    }

    public void clearCapture() {
        execute(CLEAR_SCRIPT);
    }

    public void installConsoleErrorHook() {
        execute(CONSOLE_ERROR_INSTALL);
    }

    public int getGraphqlRequestCount() {
        return readCaptureInt("graphql");
    }

    public int getAuthenticatedRequestCount() {
        return readCaptureInt("authed");
    }

    @SuppressWarnings("unchecked")
    public List<String> readConsoleErrors() {
        try {
            Object result = driver.get().browser().executeScript(CONSOLE_ERROR_READ);
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
            Logger.logMessage("Could not read console errors: " + e.getMessage());
            return Collections.emptyList();
        }
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
            Logger.logMessage("Could not read API failures: " + e.getMessage());
            return Collections.emptyList();
        }
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
            Logger.logMessage("Could not read network capture field " + field + ": " + e.getMessage());
            return 0;
        }
    }

    private void execute(String script) {
        try {
            driver.get().browser().executeScript(script);
        } catch (Exception e) {
            Logger.logMessage("Network hook script failed: " + e.getMessage());
        }
    }
}
