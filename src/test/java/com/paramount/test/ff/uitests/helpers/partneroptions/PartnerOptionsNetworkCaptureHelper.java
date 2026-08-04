package com.paramount.test.ff.uitests.helpers.partneroptions;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.duplicatefilteroptions.DuplicateFilterOptionsCollectionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.synergy.core.driver.elements.DesktopBrowserElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Installs a browser fetch/XHR hook and extracts Partner filter labels from captured GraphQL payloads.
 */
public class PartnerOptionsNetworkCaptureHelper extends BaseTest {

    private static final int CAPTURE_SETTLE_POLL_MS = 500;
    private static final int CAPTURE_SCROLL_MAX_ATTEMPTS = 200;
    private static final int CAPTURE_STALE_SCROLL_ATTEMPTS = 80;
    private static final int CAPTURE_MAX_WALL_CLOCK_MS = 5 * 60 * 1000;

    private final LeftFilterPanelUtil leftFilterPanelUtil;
    private final DuplicateFilterOptionsCollectionHelper scrollHelper;
    private DesktopBrowserElement partnerViewport;

    public PartnerOptionsNetworkCaptureHelper(LeftFilterPanelUtil leftFilterPanelUtil) {
        this.leftFilterPanelUtil = leftFilterPanelUtil;
        this.scrollHelper = new DuplicateFilterOptionsCollectionHelper(leftFilterPanelUtil);
    }

    public List<String> collectPartnerOptionsViaNetwork(String filterName) throws InterruptedException {
        installNetworkCaptureHook();
        clearNetworkCaptureHook();

        if (leftFilterPanelUtil.isFilterExpanded(filterName)) {
            leftFilterPanelUtil.collapseFilter(filterName);
            Thread.sleep(500);
        }
        leftFilterPanelUtil.expandFilter(filterName);
        Thread.sleep(1500);
        partnerViewport = scrollHelper.getFilterOptionsViewport(filterName);

        List<String> partners = waitForCapturedPartnerLabels(filterName);
        if (partners.isEmpty() || !isCaptureComplete(filterName, partners)) {
            leftFilterPanelUtil.collapseFilter(filterName);
            Thread.sleep(500);
            clearNetworkCaptureHook();
            leftFilterPanelUtil.expandFilter(filterName);
            Thread.sleep(1500);
            partnerViewport = scrollHelper.getFilterOptionsViewport(filterName);
            partners = waitForCapturedPartnerLabels(filterName);
        }

        if (!partners.isEmpty()) {
            Logger.logMessage(filterName + ": collected " + partners.size()
                    + " partner option(s) from network GraphQL capture");
            return partners;
        }

        Logger.logMessage(filterName + ": network capture returned no partners; caller may fall back to UI scroll");
        return Collections.emptyList();
    }

    private boolean isCaptureComplete(String filterName, List<String> partners) {
        int expectedTotal = leftFilterPanelUtil.getTotalOptionCount(filterName);
        int uniquePartnerCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(partners);
        return expectedTotal <= 0 || uniquePartnerCount >= expectedTotal;
    }

    public void installNetworkCaptureHook() {
        executeHookScript(NETWORK_CAPTURE_INSTALL_SCRIPT);
    }

    public void clearNetworkCaptureHook() {
        executeHookScript(NETWORK_CAPTURE_CLEAR_SCRIPT);
    }

    private List<String> waitForCapturedPartnerLabels(String filterName) throws InterruptedException {
        int expectedTotal = leftFilterPanelUtil.getTotalOptionCount(filterName);
        List<String> bestLabels = Collections.emptyList();
        int staleScrollAttempts = 0;
        int lastUniqueCount = 0;
        long deadlineMs = System.currentTimeMillis() + CAPTURE_MAX_WALL_CLOCK_MS;

        for (int attempt = 0; attempt < CAPTURE_SCROLL_MAX_ATTEMPTS
                && staleScrollAttempts < CAPTURE_STALE_SCROLL_ATTEMPTS; attempt++) {
            if (System.currentTimeMillis() >= deadlineMs) {
                Logger.logMessage(filterName + ": network capture time cap reached with "
                        + bestLabels.size() + " raw row(s), "
                        + PartnerOptionsDuplicateAnalyzer.uniqueCount(bestLabels) + " unique partner(s)");
                break;
            }

            Thread.sleep(CAPTURE_SETTLE_POLL_MS);
            List<String> incrementalLabels = readCapturedPartnerLabels();
            clearNetworkCaptureHook();
            if (!incrementalLabels.isEmpty()) {
                bestLabels = PartnerOptionsGraphqlResponseParser.mergeBatches(bestLabels, incrementalLabels);
            }

            int uniqueCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(bestLabels);
            if (uniqueCount > lastUniqueCount) {
                lastUniqueCount = uniqueCount;
                staleScrollAttempts = 0;
            } else {
                staleScrollAttempts++;
            }

            if (expectedTotal > 0 && uniqueCount >= expectedTotal) {
                return bestLabels;
            }

            scrollPartnerListDown(filterName);
        }
        return bestLabels;
    }

    private void scrollPartnerListDown(String filterName) throws InterruptedException {
        if (partnerViewport != null) {
            scrollHelper.scrollPartnerListDown(filterName, partnerViewport);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readCapturedPartnerLabels() {
        Object raw = executeHookScript(NETWORK_CAPTURE_READ_SCRIPT);
        if (raw == null) {
            return Collections.emptyList();
        }

        String jsonText = raw.toString();
        if (jsonText.isEmpty() || "[]".equals(jsonText)) {
            return Collections.emptyList();
        }

        try {
            JSONParser parser = new JSONParser();
            JSONArray captures = (JSONArray) parser.parse(jsonText);
            return PartnerOptionsGraphqlResponseParser.extractPartnerLabels(captures);
        } catch (ParseException e) {
            Logger.logConsoleMessage("Could not parse network capture payload: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Object executeHookScript(String script) {
        DesktopBrowserElement body = driver.get().finder().findElement(com.synergy.core.driver.By.TagName("body"));
        return body.executeScript(script);
    }

    private static final String NETWORK_CAPTURE_INSTALL_SCRIPT =
            "if (!window.__fcPartnerCaptureInstalled) {"
                    + "window.__fcPartnerCapture = [];"
                    + "window.__fcPartnerCaptureInstalled = true;"
                    + "function __fcRecord(url, requestBody, responseBody) {"
                    + "  if (!url || url.indexOf('graphql') < 0) return;"
                    + "  window.__fcPartnerCapture.push({"
                    + "    url: url,"
                    + "    request: requestBody || '',"
                    + "    response: responseBody || ''"
                    + "  });"
                    + "  if (window.__fcPartnerCapture.length > 25) {"
                    + "    window.__fcPartnerCapture.shift();"
                    + "  }"
                    + "}"
                    + "if (!window.__fcPartnerCaptureFetchInstalled && window.fetch) {"
                    + "  var __fcOrigFetch = window.fetch;"
                    + "  window.fetch = function() {"
                    + "    var args = arguments;"
                    + "    var url = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url) || '';"
                    + "    var reqBody = args[1] && args[1].body ? args[1].body : '';"
                    + "    return __fcOrigFetch.apply(this, args).then(function(res) {"
                    + "      try {"
                    + "        var clone = res.clone();"
                    + "        clone.text().then(function(text) { __fcRecord(url, reqBody, text); });"
                    + "      } catch (e) {}"
                    + "      return res;"
                    + "    });"
                    + "  };"
                    + "  window.__fcPartnerCaptureFetchInstalled = true;"
                    + "}"
                    + "if (!window.__fcPartnerCaptureXhrInstalled) {"
                    + "  var __fcOrigOpen = XMLHttpRequest.prototype.open;"
                    + "  var __fcOrigSend = XMLHttpRequest.prototype.send;"
                    + "  XMLHttpRequest.prototype.open = function(method, url) {"
                    + "    this.__fcUrl = url;"
                    + "    this.__fcRequestBody = '';"
                    + "    return __fcOrigOpen.apply(this, arguments);"
                    + "  };"
                    + "  XMLHttpRequest.prototype.send = function(body) {"
                    + "    this.__fcRequestBody = body || '';"
                    + "    this.addEventListener('load', function() {"
                    + "      try { __fcRecord(this.__fcUrl, this.__fcRequestBody, this.responseText); } catch (e) {}"
                    + "    });"
                    + "    return __fcOrigSend.apply(this, arguments);"
                    + "  };"
                    + "  window.__fcPartnerCaptureXhrInstalled = true;"
                    + "}"
                    + "}"
                    + "return true;";

    private static final String NETWORK_CAPTURE_READ_SCRIPT =
            "return JSON.stringify(window.__fcPartnerCapture || []);";

    private static final String NETWORK_CAPTURE_CLEAR_SCRIPT =
            "window.__fcPartnerCapture = []; return true;";
}
