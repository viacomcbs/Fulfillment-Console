package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.OpsConsolePage;
import com.synergy.core.driver.By;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paramount.test.ff.common.base.BaseTest.driver;

/** Ops-console navigation + latest job dsIdList extraction for BSD-29967. */
public class Bsd29967OpsConsoleUtil {

    private static final Pattern DSID_VALUE_PATTERN =
            Pattern.compile("\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern DSID_LIST_BLOCK_PATTERN =
            Pattern.compile("\"dsIdList\"\\s*:\\s*\\[|\"dsIDlist\"\\s*:\\s*\\[", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOB_TIME_PATTERN = Pattern.compile(
            "\"lastUpdated(?:Time|Timestamp)\"\\s*:\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE);

    private static final String CLEAR_NETWORK_CAPTURE_JS =
            "if (window.__bsd29967Capture) { window.__bsd29967Capture.payloads = []; } return true;";

    private static final int PAGE_LOAD_WAIT_S = 45;
    private static final int WORKFLOW_SETTLE_MS = 5000;
    private static final int EXTRACT_POLL_MS = 1500;
    private static final int EXTRACT_POLL_AFTER_DYNAMO = 6;

    private void prepareDynamoViewForExtraction() throws InterruptedException {
        ensureDynamoTabActiveForExtraction();
        runJsBool(SCROLL_EXPAND_JOBS_JS);
        Thread.sleep(400);
        runJsBool(AGGRESSIVE_EXPAND_NGX_JS);
        Thread.sleep(500);
        expandLatestJobDsIdListInDynamoTree();
        Thread.sleep(400);
    }

    private final OpsConsolePage opsConsolePage = new OpsConsolePage();

    public List<String> fetchLatestJobDsIdList(SoftAssert softAssert) throws InterruptedException {
        String orderId = Bsd29967SessionHelper.getOrderId();
        String environment = Bsd29967SessionHelper.getEnvironmentLabel();
        Verify.softAssert1(!orderId.isEmpty(), "Order ID captured from FC Details URL", softAssert);
        Verify.softAssert1(!environment.isEmpty(), "Environment label captured from filter", softAssert);
        if (orderId.isEmpty()) {
            return new ArrayList<>();
        }

        String fcHandle = driver.get().browser().getWindowHandle();
        Bsd29967SessionHelper.setFcWindowHandle(fcHandle);
        openOpsConsoleTab(orderId);
        WaitUtil.waitForJSToLoad(PAGE_LOAD_WAIT_S);
        installNetworkCapture();
        installClipboardHook();
        Thread.sleep(3000);

        trySelectWorkflow(environment);
        Thread.sleep(WORKFLOW_SETTLE_MS);
        installNetworkCapture();
        installClipboardHook();

        clickDynamoTab(softAssert);
        waitForDynamoContent();
        if (!ensureDynamoTabActiveForExtraction()) {
            Verify.softAssert1(false, "Ops-console Dynamo tab ready for extraction", softAssert);
        }
        clickInspectOrderIfNeeded();
        Thread.sleep(2000);
        prepareDynamoViewForExtraction();

        Bsd29967SessionHelper.setOpsExtractionSource("");
        Bsd29967SessionHelper.setOpsExtractionDiagnostics("");

        ExtractionOutcome outcome = tryExtractDsIdListOnce(true);
        List<String> dsids = outcome.dsids;
        if (!dsids.isEmpty()) {
            Bsd29967SessionHelper.setOpsExtractionSource(outcome.source);
            logExtractedDsids(dsids, outcome.jobIndex, outcome.source, softAssert);
        } else {
            dsids = pollForDsIdList(softAssert, 4, "Dynamo");
        }
        if (dsids.isEmpty()) {
            String diagnostics = collectExtractionDiagnostics();
            Bsd29967SessionHelper.setOpsExtractionDiagnostics(diagnostics);
            logNetworkPayloadDiagnostics();
            logExtractionDiagnostics();
            Verify.softAssert1(false, "Ops-console latest job dsIdList extracted", softAssert);
        }
        Bsd29967SessionHelper.setOpsConsoleDsids(dsids);
        switchBackToFc(fcHandle);
        return dsids;
    }

    private List<String> pollForDsIdList(SoftAssert softAssert, int attempts, String phase)
            throws InterruptedException {
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (attempt == 0 || attempt % 2 == 1) {
                prepareDynamoViewForExtraction();
            }
            ExtractionOutcome found = tryExtractDsIdListOnce(false);
            if (!found.dsids.isEmpty()) {
                Bsd29967SessionHelper.setOpsExtractionSource(found.source);
                logExtractedDsids(found.dsids, found.jobIndex, phase + "/poll-" + (attempt + 1)
                        + " (" + found.source + ")", softAssert);
                return found.dsids;
            }
            if (attempt == 0 || attempt % 2 == 1) {
                Logger.logReportMessage("Ops-console " + phase + " extract poll "
                        + (attempt + 1) + "/" + attempts
                        + " — network payloads=" + getNetworkPayloadCount()
                        + ", jobsIndex=" + findHighestJobArrayIndex());
            }
            Thread.sleep(EXTRACT_POLL_MS);
        }
        return Collections.emptyList();
    }

    /** Fast JS-only: expand jobs → highest jobs[] index → dsIdList (no 60s Selenium waits). */
    private void expandLatestJobDsIdListInDynamoTree() throws InterruptedException {
        runJsBool(SCROLL_DYNAMO_PANEL_JS);
        Thread.sleep(150);
        boolean expanded = runJsBool(EXPAND_LATEST_JOB_DSIDLIST_FAST_JS);
        int jobIndex = findHighestJobArrayIndex();
        Logger.logReportMessage("Ops-console Dynamo tree expand"
                + (expanded ? " OK" : " partial")
                + (jobIndex >= 0 ? " — jobs[" + jobIndex + "]" : ""));
        Thread.sleep(400);
    }

    private int findHighestJobArrayIndex() {
        Integer fromJs = runJsInteger(FIND_HIGHEST_JOB_INDEX_JS);
        if (fromJs == null || fromJs < 0) {
            return -1;
        }
        if (fromJs > 15) {
            Logger.logReportMessage("Ops-console jobs index " + fromJs
                    + " looks too high — capping scan to jobs container only");
        }
        return fromJs;
    }

    private TreeExpandResult extractDsidsFromExpandedDom() {
        TreeExpandResult fromValues = runJsTreeResult(READ_EXPANDED_DSID_VALUES_JS);
        if (fromValues != null && !fromValues.dsids.isEmpty()) {
            return fromValues;
        }
        return runJsTreeResult(COLLECT_DSIDS_JS);
    }

    private int getNetworkPayloadCount() {
        try {
            Object raw = driver.get().browser().executeScript(
                    "return window.__bsd29967Capture && window.__bsd29967Capture.payloads"
                            + " ? window.__bsd29967Capture.payloads.length : 0;");
            if (raw instanceof Number) {
                return ((Number) raw).intValue();
            }
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void expandDynamoJsonTree() throws InterruptedException {
        expandLatestJobDsIdListInDynamoTree();
    }

    private ExtractionOutcome tryExtractDsIdListOnce(boolean includeCopyClick) {
        TreeExpandResult domResult = extractDsidsFromExpandedDom();
        if (domResult != null && !domResult.dsids.isEmpty()) {
            storeOmfOrderId(domResult.omfOrderId);
            return ExtractionOutcome.fromTree(domResult, "dom-expanded");
        }

        TreeExpandResult previewResult = extractLatestJobDsidsFromNgxPreview();
        if (previewResult != null && !previewResult.dsids.isEmpty()) {
            storeOmfOrderId(previewResult.omfOrderId);
            return ExtractionOutcome.fromTree(previewResult, "ngx-preview");
        }

        TreeExpandResult graphqlResult = extractFromGraphqlReplay();
        if (graphqlResult != null && !graphqlResult.dsids.isEmpty()) {
            storeOmfOrderId(graphqlResult.omfOrderId);
            return ExtractionOutcome.fromTree(graphqlResult, "graphql-replay");
        }

        TreeExpandResult capturedResult = extractFromCapturedPayloads();
        if (capturedResult != null && !capturedResult.dsids.isEmpty()) {
            storeOmfOrderId(capturedResult.omfOrderId);
            return ExtractionOutcome.fromTree(capturedResult, "network-capture");
        }

        TreeExpandResult codeViewerResult = extractFromCodeViewer();
        if (codeViewerResult != null && !codeViewerResult.dsids.isEmpty()) {
            storeOmfOrderId(codeViewerResult.omfOrderId);
            return ExtractionOutcome.fromTree(codeViewerResult, "app-code-viewer");
        }

        List<String> fromDynamoApi = extractFromDynamoApi(null);
        if (!fromDynamoApi.isEmpty()) {
            return new ExtractionOutcome(fromDynamoApi, "Dynamo-API", -1);
        }

        TreeExpandResult apiResult = extractFromOrderApi();
        if (apiResult != null && !apiResult.dsids.isEmpty()) {
            storeOmfOrderId(apiResult.omfOrderId);
            return ExtractionOutcome.fromTree(apiResult, "Order-API");
        }

        TreeExpandResult angularStateResult = extractFromAngularOrderState();
        if (angularStateResult != null && !angularStateResult.dsids.isEmpty()) {
            storeOmfOrderId(angularStateResult.omfOrderId);
            return ExtractionOutcome.fromTree(angularStateResult, "Angular-order-state");
        }

        TreeExpandResult angularResult = extractFromNgxAngularComponent();
        if (angularResult != null && !angularResult.dsids.isEmpty()) {
            storeOmfOrderId(angularResult.omfOrderId);
            return ExtractionOutcome.fromTree(angularResult, "ngx-json-viewer");
        }

        TreeExpandResult ngReflectResult = extractFromNgReflectJson();
        if (ngReflectResult != null && !ngReflectResult.dsids.isEmpty()) {
            storeOmfOrderId(ngReflectResult.omfOrderId);
            return ExtractionOutcome.fromTree(ngReflectResult, "ng-reflect-json");
        }

        TreeExpandResult htmlResult = extractLatestJobDsidsFromPageHtml();
        if (htmlResult != null && !htmlResult.dsids.isEmpty()) {
            storeOmfOrderId(htmlResult.omfOrderId);
            return ExtractionOutcome.fromTree(htmlResult, "page-html");
        }

        TreeExpandResult ngxJsonResult = extractFromNgxJsonInput();
        if (ngxJsonResult != null && !ngxJsonResult.dsids.isEmpty()) {
            storeOmfOrderId(ngxJsonResult.omfOrderId);
            return ExtractionOutcome.fromTree(ngxJsonResult, "ngx-json-input");
        }

        List<String> fromNetwork = extractBestFromNetworkCapture();
        if (!fromNetwork.isEmpty()) {
            return new ExtractionOutcome(fromNetwork, "network-capture-java", -1);
        }

        TreeExpandResult visibleTextResult = extractLatestJobDsidsFromVisibleText();
        if (visibleTextResult != null && !visibleTextResult.dsids.isEmpty()) {
            storeOmfOrderId(visibleTextResult.omfOrderId);
            return ExtractionOutcome.fromTree(visibleTextResult, "visible-text");
        }

        try {
            TreeExpandResult locatorResult = expandJobsViaLocatorClicks();
            if (locatorResult != null && !locatorResult.dsids.isEmpty()) {
                storeOmfOrderId(locatorResult.omfOrderId);
                return ExtractionOutcome.fromTree(locatorResult, "locator-clicks");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (includeCopyClick) {
            try {
                TreeExpandResult copyResult = extractFromCopyButton();
                if (copyResult != null && !copyResult.dsids.isEmpty()) {
                    storeOmfOrderId(copyResult.omfOrderId);
                    return ExtractionOutcome.fromTree(copyResult, "Copy-button-json");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return ExtractionOutcome.empty();
    }

    private void storeOmfOrderId(String omfOrderId) {
        if (omfOrderId != null && !omfOrderId.isEmpty()) {
            Bsd29967SessionHelper.setOmfOrderId(omfOrderId);
        }
    }

    private void waitForDynamoContent() throws InterruptedException {
        for (int attempt = 0; attempt < 15; attempt++) {
            if (isDynamoTabActive() || isDynamoContentPaneActive()) {
                Logger.logReportMessage("Ops-console Dynamo content ready (attempt " + (attempt + 1) + ")");
                return;
            }
            Thread.sleep(2000);
        }
    }

    private void clickInspectOrderIfNeeded() throws InterruptedException {
        try {
            By inspectBtn = opsConsolePage.inspectOrderButton();
            if (WaitUtil.isDisplayFast(inspectBtn, 3)) {
                DriverUtil.clickOnElementJs(inspectBtn, 3);
                Thread.sleep(2000);
                installNetworkCapture();
                installClipboardHook();
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console Inspect Order click skipped: " + e.getMessage());
        }
    }

    private void openOpsConsoleTab(String orderId) throws InterruptedException {
        String url = OpsConsolePage.DEV_ORDER_URL_PREFIX + orderId;
        Logger.logReportMessage("Opening ops-console: " + url);
        driver.get().browser().executeScript("window.open('about:blank','_blank');");
        Set<String> handles = driver.get().browser().getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(Bsd29967SessionHelper.getFcWindowHandle())) {
                driver.get().browser().switchToWindow(handle);
                break;
            }
        }
        installNetworkCapture();
        driver.get().browser().getUrl(url);
        installNetworkCapture();
    }

    private void clearNetworkCapture() {
        try {
            driver.get().browser().executeScript(CLEAR_NETWORK_CAPTURE_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console network capture clear failed: " + e.getMessage());
        }
    }

    private void replayLastGraphqlCapture() {
        try {
            TreeExpandResult result = extractFromGraphqlReplay();
            if (result != null && !result.dsids.isEmpty()) {
                Logger.logReportMessage("Ops-console GraphQL replay dsIdList ("
                        + result.dsids.size() + ") jobs[" + result.jobIndex + "]");
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console GraphQL replay failed: " + e.getMessage());
        }
    }

    private TreeExpandResult extractFromGraphqlReplay() {
        return runJsTreeResult(EXTRACT_FROM_GRAPHQL_REPLAY_JS);
    }

    private TreeExpandResult extractFromCapturedPayloads() {
        return runJsTreeResult(EXTRACT_FROM_CAPTURED_PAYLOADS_JS);
    }

    private TreeExpandResult extractFromOrderApi() {
        String orderId = Bsd29967SessionHelper.getOrderId();
        String workflow = Bsd29967SessionHelper.getEnvironmentLabel();
        if (orderId.isEmpty()) {
            return null;
        }
        String script = EXTRACT_FROM_ORDER_API_JS
                .replace("{{ORDER_ID}}", orderId.replace("'", "\\'"))
                .replace("{{WORKFLOW}}", workflow == null ? "" : workflow.replace("'", "\\'"));
        return runJsTreeResult(script);
    }

    private TreeExpandResult extractFromNgxAngularComponent() {
        return runJsTreeResult(EXTRACT_FROM_NGX_ANGULAR_COMPONENT_JS);
    }

    private TreeExpandResult extractFromCodeViewer() {
        return runJsTreeResult(EXTRACT_FROM_CODE_VIEWER_JS);
    }

    private TreeExpandResult extractFromAngularOrderState() {
        return runJsTreeResult(EXTRACT_FROM_ANGULAR_ORDER_STATE_JS);
    }

    private TreeExpandResult extractFromCopyButton() throws InterruptedException {
        clearClipboardCapture();
        installClipboardHook();
        if (!ensureDynamoTabActiveForExtraction()) {
            Logger.logReportMessage("Ops-console Copy — Dynamo tab not active; skipping Copy");
            return null;
        }
        prepareDynamoViewForExtraction();

        if (!clickCopyJsonButton()) {
            Logger.logReportMessage("Ops-console Copy — Dynamo panel Copy button not found");
            return null;
        }
        Thread.sleep(1200);
        String json = captureCopyPayload();
        if (json.isEmpty()) {
            clickCopyJsonButton();
            Thread.sleep(1200);
            json = captureCopyPayload();
        }
        if (json.isEmpty()) {
            Logger.logReportMessage("Ops-console Copy — clipboard empty after Dynamo Copy click");
            return null;
        }

        Path saved = saveCopyPayload(json);
        Logger.logReportMessage("Ops-console Dynamo Copy payload captured ("
                + json.length() + " bytes)"
                + (saved != null ? " → " + saved.toAbsolutePath() : ""));

        if (isOpenSearchSnapshotPayload(json)) {
            Logger.logReportMessage("Ops-console Copy still returned OpenSearch snapshot — "
                    + "wrong tab Copy button was used");
            return null;
        }
        if (!isDynamoOrderPayload(json)) {
            Logger.logReportMessage("Ops-console Copy JSON is not Dynamo order/jobs payload — skipping");
            return null;
        }

        List<String> dsids = extractDsidsFromPayloadText(json, null);
        if (dsids.isEmpty()) {
            Logger.logReportMessage("Ops-console Copy JSON parsed but no jobs dsIdList found");
            return null;
        }

        TreeExpandResult result = new TreeExpandResult();
        result.dsids.addAll(dsids);
        result.omfOrderId = Bsd29967SessionHelper.getOmfOrderId();
        result.jobIndex = resolveLatestJobIndex(json);
        return result;
    }

    private String captureCopyPayload() {
        try {
            Object raw = driver.get().browser().executeScript(READ_CLIPBOARD_RAW_JS);
            if (raw == null) {
                return "";
            }
            String text = String.valueOf(raw).trim();
            if (text.length() < 20 || text.indexOf("orderId") < 0) {
                return "";
            }
            return text;
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console clipboard read failed: " + e.getMessage());
            return "";
        }
    }

    private Path saveCopyPayload(String json) {
        try {
            String orderId = Bsd29967SessionHelper.getOrderId();
            String safeOrderId = orderId.isEmpty() ? "unknown" : orderId.replaceAll("[^0-9A-Za-z_-]", "");
            Path dir = Paths.get("test-output", "ops-console");
            Files.createDirectories(dir);
            Path file = dir.resolve("bsd29967-" + safeOrderId + "-copy.json");
            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
            return file;
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console Copy payload save failed: " + e.getMessage());
            return null;
        }
    }

    private int resolveLatestJobIndex(String json) {
        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(json);
            JSONArray jobs = resolveJobsArray(parsed);
            if (jobs == null || jobs.isEmpty()) {
                return -1;
            }
            return indexOfLatestJob(jobs);
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean isOpenSearchSnapshotPayload(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        String trimmed = json.trim();
        return trimmed.startsWith("[")
                && trimmed.contains("\"_index\"")
                && trimmed.contains("order-final-status");
    }

    private boolean isDynamoOrderPayload(String json) {
        if (json == null || json.isEmpty() || isOpenSearchSnapshotPayload(json)) {
            return false;
        }
        return json.contains("\"jobs\"") && json.contains("dsIdList");
    }

    private int indexOfLatestJob(JSONArray jobs) {
        int bestIdx = -1;
        for (int i = 0; i < jobs.size(); i++) {
            Object item = jobs.get(i);
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject job = (JSONObject) item;
            if (!jobHasDsIdList(job)) {
                continue;
            }
            if (i > bestIdx) {
                bestIdx = i;
            }
        }
        if (bestIdx >= 0) {
            return bestIdx;
        }
        return jobs.size() - 1;
    }

    private boolean jobHasDsIdList(JSONObject job) {
        Object dsIdList = firstPresentKey(job, "dsIdList", "dsIDlist", "dsIDList", "dsidlist");
        return dsIdList instanceof JSONArray && !((JSONArray) dsIdList).isEmpty();
    }

    private long parseJobTime(JSONObject job, int fallbackIndex) {
        String[] keys = {"lastUpdatedTime", "lastUpdatedTimestamp", "createdTime", "createdTimestamp"};
        for (String key : keys) {
            Object raw = job.get(key);
            if (raw == null) {
                continue;
            }
            try {
                return Long.parseLong(String.valueOf(raw).trim());
            } catch (NumberFormatException ignored) {
                // try next key
            }
        }
        return fallbackIndex;
    }

    private void clearClipboardCapture() {
        try {
            driver.get().browser().executeScript(CLEAR_CLIPBOARD_HOOK_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console clipboard clear failed: " + e.getMessage());
        }
    }

    private void installClipboardHook() {
        try {
            driver.get().browser().executeScript(INSTALL_CLIPBOARD_HOOK_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console clipboard hook install failed: " + e.getMessage());
        }
    }

    private boolean clickCopyJsonButton() {
        try {
            if (!ensureDynamoTabActiveForExtraction()) {
                return false;
            }
            By copyBtn = opsConsolePage.copyJsonButton();
            if (WaitUtil.isDisplayFast(copyBtn, 5)) {
                DriverUtil.scrollToElement(copyBtn);
                if (DriverUtil.clickOnElementJs(copyBtn, 3)) {
                    return true;
                }
                return DriverUtil.clickOnElement(copyBtn, 5);
            }
            Object result = driver.get().browser().executeScript(CLICK_COPY_BUTTON_JS);
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console Copy button click failed: " + e.getMessage());
            return false;
        }
    }

    private boolean ensureDynamoTabActiveForExtraction() throws InterruptedException {
        dismissWorkflowDropdown();
        for (int attempt = 0; attempt < 6; attempt++) {
            if (isDynamoTabActive() || isDynamoContentPaneActive()) {
                return true;
            }
            if (!clickDynamoTabViaLocator()) {
                clickDynamoTabViaJs();
            }
            Thread.sleep(1200);
        }
        boolean active = isDynamoTabActive() || isDynamoContentPaneActive();
        if (!active) {
            Logger.logReportMessage("Ops-console Dynamo content pane not active after tab clicks");
        }
        return active;
    }

    private boolean isDynamoTabSelectedInBrowser() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var tab = document.getElementById('ngb-nav-1')"
                            + "  || document.querySelector('a[role=\"tab\"][aria-controls=\"ngb-nav-1-panel\"]');"
                            + "if (!tab) return false;"
                            + "return tab.getAttribute('aria-selected') === 'true'"
                            + "  || tab.classList.contains('active');"
                            + "})()");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDynamoContentPaneActive() {
        try {
            Object result = driver.get().browser().executeScript(IS_DYNAMO_CONTENT_PANE_ACTIVE_JS);
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private TreeExpandResult extractFromNgReflectJson() {
        return runJsTreeResult(EXTRACT_FROM_NG_REFLECT_JSON_JS);
    }

    private void logExtractionDiagnostics() {
        try {
            Object raw = driver.get().browser().executeScript(EXTRACTION_DIAGNOSTICS_JS);
            if (raw != null) {
                Logger.logReportMessage("Ops-console extraction diagnostics: " + raw);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console extraction diagnostics failed: " + e.getMessage());
        }
    }

    private String collectExtractionDiagnostics() {
        try {
            Object raw = driver.get().browser().executeScript(EXTRACTION_DIAGNOSTICS_JS);
            if (raw == null) {
                return "";
            }
            String diagnostics = String.valueOf(raw).trim();
            return diagnostics.isEmpty() ? "" : diagnostics;
        } catch (Exception e) {
            return "diagnostics-unavailable: " + e.getMessage();
        }
    }

    private void clickOpenSearchTab() throws InterruptedException {
        try {
            By tab = opsConsolePage.openSearchTab();
            if (WaitUtil.isDisplayFast(tab, 5)) {
                DriverUtil.clickOnElementJs(tab, 3);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console OpenSearch tab click failed: " + e.getMessage());
        }
    }

    private void installNetworkCapture() {
        try {
            driver.get().browser().executeScript(INSTALL_NETWORK_CAPTURE_JS);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console network capture install failed: " + e.getMessage());
        }
    }

    private void switchBackToFc(String fcHandle) {
        try {
            if (fcHandle != null && !fcHandle.isEmpty()) {
                driver.get().browser().switchToWindow(fcHandle);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Switch back to FC tab failed: " + e.getMessage());
        }
    }

    private void trySelectWorkflow(String environmentLabel) throws InterruptedException {
        String envUpper = environmentLabel.toUpperCase(Locale.ROOT);
        if (isCurrentWorkflow(envUpper)) {
            Logger.logReportMessage("Ops-console Workflow already set to " + environmentLabel);
            dismissWorkflowDropdown();
            return;
        }

        boolean selected = selectWorkflowViaJs(environmentLabel);
        if (!selected && WaitUtil.isDisplayFast(opsConsolePage.workflowDropdown(), 5)) {
            DriverUtil.clickOnElementJs(opsConsolePage.workflowDropdown(), 3);
            Thread.sleep(800);
            if (WaitUtil.isDisplayFast(opsConsolePage.workflowOption(environmentLabel), 8)) {
                selected = DriverUtil.clickOnElementJs(opsConsolePage.workflowOption(environmentLabel), 3);
            }
        }
        if (!selected) {
            selected = isCurrentWorkflow(envUpper);
        }
        if (!selected) {
            Logger.logReportMessage("Ops-console Workflow could not be set to '"
                    + environmentLabel + "' — continuing with current workflow for Dynamo fetch");
        } else {
            Logger.logReportMessage("Ops-console Workflow set to " + environmentLabel);
            Thread.sleep(WORKFLOW_SETTLE_MS);
            replayLastGraphqlCapture();
        }
        dismissWorkflowDropdown();
        Thread.sleep(500);
    }

    private boolean isCurrentWorkflow(String environmentUpper) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function(env) {"
                            + "function norm(s) { return (s || '').replace(/\\s+/g, ' ').trim().toUpperCase(); }"
                            + "var selects = document.querySelectorAll('select');"
                            + "for (var i = 0; i < selects.length; i++) {"
                            + "  var opt = selects[i].options[selects[i].selectedIndex];"
                            + "  if (opt && norm(opt.textContent).indexOf(env) >= 0) return true;"
                            + "}"
                            + "var mats = document.querySelectorAll('mat-select .mat-select-value, mat-select');"
                            + "for (var m = 0; m < mats.length; m++) {"
                            + "  if (norm(mats[m].textContent).indexOf(env) >= 0) return true;"
                            + "}"
                            + "return false;"
                            + "})('" + environmentUpper.replace("'", "\\'") + "');");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private void dismissWorkflowDropdown() {
        try {
            driver.get().browser().executeScript(
                    "document.dispatchEvent(new KeyboardEvent('keydown',"
                            + "{key:'Escape',code:'Escape',keyCode:27,which:27,bubbles:true}));"
                            + "if (document.body) document.body.click();");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Dismiss workflow dropdown failed: " + e.getMessage());
        }
    }

    private boolean selectWorkflowViaJs(String environmentLabel) {
        try {
            String safe = environmentLabel.replace("'", "\\'");
            Object result = driver.get().browser().executeScript(
                    "(function(env) {"
                            + "function norm(s) { return (s || '').replace(/\\s+/g, ' ').trim().toUpperCase(); }"
                            + "function matches(text) { return norm(text).indexOf(norm(env)) >= 0; }"
                            + "var selects = document.querySelectorAll('select');"
                            + "for (var i = 0; i < selects.length; i++) {"
                            + "  for (var j = 0; j < selects[i].options.length; j++) {"
                            + "    if (matches(selects[i].options[j].textContent)) {"
                            + "      selects[i].selectedIndex = j;"
                            + "      selects[i].dispatchEvent(new Event('change', {bubbles:true}));"
                            + "      return true;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "var combos = document.querySelectorAll('[role=\"combobox\"], mat-select, .mat-select');"
                            + "for (var c = 0; c < combos.length; c++) {"
                            + "  combos[c].click();"
                            + "  var opts = document.querySelectorAll('mat-option, [role=\"option\"]');"
                            + "  for (var o = 0; o < opts.length; o++) {"
                            + "    if (matches(opts[o].textContent)) {"
                            + "      opts[o].click();"
                            + "      return true;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "return false;"
                            + "})('" + safe + "');");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Workflow JS select failed: " + e.getMessage());
            return false;
        }
    }

    private void clickDynamoTab(SoftAssert softAssert) throws InterruptedException {
        dismissWorkflowDropdown();
        Verify.softAssert1(WaitUtil.isDisplay(opsConsolePage.dynamoTab(), 20),
                "Ops-console Dynamo tab visible", softAssert);
        if (!WaitUtil.isDisplay(opsConsolePage.dynamoTab(), 5)) {
            return;
        }

        boolean clicked = clickDynamoTabViaLocator();
        if (!clicked) {
            clicked = clickDynamoTabViaJs();
        }
        Verify.softAssert1(clicked, "Clicked Ops-console Dynamo tab", softAssert);
        Thread.sleep(1500);

        for (int attempt = 0; attempt < 15; attempt++) {
            if (isDynamoTabActive() || pageContainsJobsMarker()) {
                Logger.logReportMessage("Ops-console Dynamo tab active (attempt " + (attempt + 1) + ")");
                installNetworkCapture();
                Thread.sleep(WORKFLOW_SETTLE_MS);
                replayLastGraphqlCapture();
                return;
            }
            clickDynamoTabViaJs();
            Thread.sleep(1000);
        }
        Verify.softAssert1(isDynamoTabActive() || pageContainsJobsMarker(),
                "Ops-console Dynamo tab selected", softAssert);
    }

    private boolean clickDynamoTabViaLocator() {
        By tab = opsConsolePage.dynamoTab();
        DriverUtil.scrollToElement(tab);
        boolean clicked = DriverUtil.clickOnElementJs(tab, 0);
        if (!clicked) {
            clicked = DriverUtil.clickOnElement(tab, 15);
        }
        return clicked;
    }

    private boolean clickDynamoTabViaJs() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "function isVisible(el) {"
                            + "  if (!el) return false;"
                            + "  var rect = el.getBoundingClientRect();"
                            + "  return rect.width > 0 && rect.height > 0;"
                            + "}"
                            + "var ngbTab = document.getElementById('ngb-nav-1');"
                            + "if (ngbTab && isVisible(ngbTab)) {"
                            + "  ngbTab.scrollIntoView({block:'center'});"
                            + "  ngbTab.click();"
                            + "  return true;"
                            + "}"
                            + "function isWorkflowOverlay(el) {"
                            + "  return !!el.closest('mat-select, mat-option, select, option, [role=\"listbox\"]');"
                            + "}"
                            + "var tabs = document.querySelectorAll('a[role=\"tab\"]');"
                            + "for (var t = 0; t < tabs.length; t++) {"
                            + "  var label = (tabs[t].textContent || '').replace(/\\s+/g, ' ').trim();"
                            + "  if (label !== 'Dynamo' || !isVisible(tabs[t]) || isWorkflowOverlay(tabs[t])) continue;"
                            + "  tabs[t].scrollIntoView({block:'center'});"
                            + "  tabs[t].click();"
                            + "  return true;"
                            + "}"
                            + "return false;"
                            + "})()");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Dynamo tab JS click failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isDynamoTabActive() {
        return WaitUtil.isDisplayFast(opsConsolePage.dynamoTabSelected(), 2)
                || isDynamoTabSelectedInBrowser();
    }

    private boolean pageContainsJobsMarker() {
        return isDynamoTabActive()
                || WaitUtil.isDisplayFast(opsConsolePage.dynamoTabPanelActive(), 2)
                || isDynamoContentPaneActive();
    }

    private TreeExpandResult extractLatestJobDsidsFromVisibleText() {
        return runJsTreeResult(EXTRACT_LATEST_JOB_DSIDS_FROM_VISIBLE_TEXT_JS);
    }

    private TreeExpandResult extractFromNgxJsonInput() {
        return runJsTreeResult(EXTRACT_FROM_NGX_JSON_INPUT_JS);
    }

    private TreeExpandResult expandJobsViaLocatorClicks() throws InterruptedException {
        runJsBool(EXPAND_JOBS_VIA_LOCATOR_JS);
        Thread.sleep(800);
        return runJsTreeResult(COLLECT_DSIDS_JS);
    }

    private void logNetworkPayloadDiagnostics() {
        try {
            Object raw = driver.get().browser().executeScript(READ_NETWORK_CAPTURE_DIAG_JS);
            if (raw != null) {
                Logger.logReportMessage("Ops-console network diagnostics: " + raw);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console network diagnostics failed: " + e.getMessage());
        }
    }

    private TreeExpandResult extractLatestJobDsidsFromNgxPreview() {
        return runJsTreeResult(EXTRACT_LATEST_JOB_DSIDS_FROM_NGX_PREVIEW_JS);
    }

    private TreeExpandResult extractLatestJobDsidsFromPageHtml() {
        return runJsTreeResult(EXTRACT_LATEST_JOB_DSIDS_FROM_HTML_JS);
    }

    private void logExtractedDsids(List<String> dsids, int jobIndex, String source, SoftAssert softAssert) {
        Logger.logReportMessage("Ops-console dsIdList (" + dsids.size() + ") via " + source + ": " + dsids
                + " | jobIndex=" + jobIndex
                + " | omfOrderId=" + Bsd29967SessionHelper.getOmfOrderId());
        Verify.softAssert1(true, "Ops-console latest job dsIdList extracted", softAssert);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractBestFromNetworkCapture() {
        try {
            Object raw = driver.get().browser().executeScript(READ_NETWORK_CAPTURE_JS);
            if (raw == null) {
                return Collections.emptyList();
            }
            String json = String.valueOf(raw).trim();
            if (json.isEmpty() || "[]".equals(json)) {
                return Collections.emptyList();
            }
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(json);
            if (!(parsed instanceof JSONArray)) {
                return Collections.emptyList();
            }
            List<String> best = Collections.emptyList();
            int bestSize = 0;
            for (Object item : (JSONArray) parsed) {
                if (!(item instanceof String)) {
                    continue;
                }
                List<String> dsids = extractDsidsFromPayloadText((String) item, null);
                if (dsids.size() > bestSize) {
                    bestSize = dsids.size();
                    best = dsids;
                }
            }
            return best;
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console network capture read failed: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractFromDynamoApi(SoftAssert softAssert) {
        String orderId = Bsd29967SessionHelper.getOrderId();
        String workflow = Bsd29967SessionHelper.getEnvironmentLabel();
        if (orderId.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> workflowVariants = new LinkedHashSet<>();
        if (!workflow.isEmpty()) {
            workflowVariants.add(workflow);
            workflowVariants.add(workflow.toUpperCase(Locale.ROOT));
            workflowVariants.add(workflow.toLowerCase(Locale.ROOT));
        }
        workflowVariants.add("");

        for (String workflowVariant : workflowVariants) {
            try {
                String payload = fetchDynamoPayload(orderId, workflowVariant);
                if (payload.isEmpty()) {
                    continue;
                }
                List<String> dsids = extractDsidsFromPayloadText(payload, softAssert);
                if (!dsids.isEmpty()) {
                    Logger.logReportMessage("Ops-console Dynamo API returned dsIdList via workflow='"
                            + workflowVariant + "'");
                    return dsids;
                }
            } catch (Exception e) {
                Logger.logConsoleMessage("Ops-console Dynamo API fetch failed for workflow '"
                        + workflowVariant + "': " + e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    private String fetchDynamoPayload(String orderId, String workflow) {
        String safeOrder = orderId.replace("'", "\\'");
        String safeWorkflow = workflow.replace("'", "\\'");
        String script = FETCH_DYNAMO_API_JS
                .replace("{{ORDER_ID}}", safeOrder)
                .replace("{{WORKFLOW}}", safeWorkflow);
        Object raw = driver.get().browser().executeScript(script);
        if (raw == null) {
            return "";
        }
        return String.valueOf(raw).trim();
    }

    private List<String> extractDsidsFromPayloadText(String payload, SoftAssert softAssert) {
        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(payload);
            JSONArray jobs = resolveJobsArray(parsed);
            if (jobs != null && !jobs.isEmpty()) {
                JSONObject latestJob = pickLatestJob(jobs);
                if (latestJob != null) {
                    Object omf = latestJob.get("omfOrderId");
                    if (omf != null) {
                        Bsd29967SessionHelper.setOmfOrderId(String.valueOf(omf));
                    }
                    List<String> dsids = extractDsIdsFromJob(latestJob);
                    if (!dsids.isEmpty()) {
                        return dsids;
                    }
                }
            }
            return extractDsidsViaRegexFromPayload(payload);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console payload parse failed: " + e.getMessage());
            return extractDsidsViaRegexFromPayload(payload);
        }
    }

    private List<String> extractDsidsViaRegexFromPayload(String payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyList();
        }
        Matcher listMatcher = DSID_LIST_BLOCK_PATTERN.matcher(payload);
        List<String> lastDsids = Collections.emptyList();
        while (listMatcher.find()) {
            int listStart = listMatcher.start();
            String before = payload.substring(Math.max(0, listStart - 20000), listStart);
            if (before.contains("\"lineItems\"") && !before.contains("\"jobs\"")) {
                continue;
            }
            String listBody = extractBracketedArray(payload, listStart + listMatcher.group().length() - 1);
            if (listBody.isEmpty()) {
                continue;
            }
            List<String> dsids = new ArrayList<>();
            Matcher dsidMatcher = DSID_VALUE_PATTERN.matcher(listBody);
            while (dsidMatcher.find()) {
                dsids.add(dsidMatcher.group(1).trim());
            }
            if (!dsids.isEmpty()) {
                lastDsids = dsids;
            }
        }
        return lastDsids;
    }

    private String extractBracketedArray(String text, int openBracketIndex) {
        if (openBracketIndex < 0 || openBracketIndex >= text.length()
                || text.charAt(openBracketIndex) != '[') {
            return "";
        }
        int depth = 0;
        for (int i = openBracketIndex; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    return text.substring(openBracketIndex, i + 1);
                }
            }
        }
        return "";
    }

    private long latestTimestampFromContext(String before) {
        long best = Long.MIN_VALUE;
        Matcher timeMatcher = JOB_TIME_PATTERN.matcher(before);
        while (timeMatcher.find()) {
            try {
                long value = Long.parseLong(timeMatcher.group(1));
                if (value > best) {
                    best = value;
                }
            } catch (NumberFormatException ignored) {
                // skip malformed timestamp
            }
        }
        return best;
    }

    @SuppressWarnings("unchecked")
    private JSONArray resolveJobsArray(Object parsed) {
        return findBestJobsArray(parsed, 0);
    }

    @SuppressWarnings("unchecked")
    private JSONArray findBestJobsArray(Object node, int depth) {
        if (node == null || depth > 14) {
            return null;
        }
        JSONArray best = null;
        int bestScore = -1;

        if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            int score = scoreJobsArray(array);
            if (score > bestScore) {
                best = array;
                bestScore = score;
            }
            for (Object item : array) {
                JSONArray nested = findBestJobsArray(item, depth + 1);
                if (nested != null) {
                    int nestedScore = scoreJobsArray(nested);
                    if (nestedScore > bestScore) {
                        best = nested;
                        bestScore = nestedScore;
                    }
                }
            }
            return best;
        }
        if (node instanceof JSONObject) {
            JSONObject obj = (JSONObject) node;
            for (Object value : obj.values()) {
                JSONArray nested = findBestJobsArray(value, depth + 1);
                if (nested != null) {
                    int nestedScore = scoreJobsArray(nested);
                    if (nestedScore > bestScore) {
                        best = nested;
                        bestScore = nestedScore;
                    }
                }
            }
        }
        return best;
    }

    private int scoreJobsArray(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return -1;
        }
        int withDsIdList = 0;
        for (Object item : array) {
            if (item instanceof JSONObject && jobHasDsIdList((JSONObject) item)) {
                withDsIdList++;
            }
        }
        if (withDsIdList == 0) {
            return -1;
        }
        return withDsIdList * 100 + array.size();
    }

    private boolean looksLikeJobsArray(JSONArray array) {
        return scoreJobsArray(array) > 0;
    }

    private TreeExpandResult expandJobsTreeAndExtractDsids(boolean logFailure) throws InterruptedException {
        runJsBool(SCROLL_DYNAMO_PANEL_JS);
        Thread.sleep(400);
        runJsBool(SCROLL_EXPAND_JOBS_JS);
        Thread.sleep(500);

        TreeExpandResult collected = runJsTreeResult(COLLECT_DSIDS_JS);
        if (collected != null && !collected.dsids.isEmpty()) {
            return collected;
        }

        Integer jobIndex = runJsInteger(PICK_LATEST_JOB_INDEX_JS);
        if (jobIndex == null || jobIndex < 0) {
            if (logFailure) {
                Logger.logReportMessage("Ops-console could not resolve latest job index from jobs tree");
            }
            return collected;
        }

        runJsBool(EXPAND_JOB_INDEX_JS.replace("{{INDEX}}", String.valueOf(jobIndex)));
        Thread.sleep(400);
        runJsBool(EXPAND_DSIDLIST_JS);
        Thread.sleep(400);
        runJsBool(EXPAND_DSIDLIST_ITEMS_JS);
        Thread.sleep(300);

        TreeExpandResult result = runJsTreeResult(COLLECT_DSIDS_JS);
        if (result != null) {
            result.jobIndex = jobIndex;
        }
        return result;
    }

    private boolean runJsBool(String script) {
        try {
            Object result = driver.get().browser().executeScript(script);
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console JS step failed: " + e.getMessage());
            return false;
        }
    }

    private Integer runJsInteger(String script) {
        try {
            Object result = driver.get().browser().executeScript(script);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            if (result != null) {
                String text = String.valueOf(result).trim();
                if (text.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(text);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console JS index read failed: " + e.getMessage());
        }
        return null;
    }

    private TreeExpandResult runJsTreeResult(String script) {
        try {
            Object result = driver.get().browser().executeScript(script);
            return parseTreeExpandResult(result);
        } catch (Exception e) {
            Logger.logConsoleMessage("Ops-console JS collect failed: " + e.getMessage());
            return null;
        }
    }

    private static final String INSTALL_NETWORK_CAPTURE_JS =
            "(function() {"
                    + "if (!window.__bsd29967Capture) {"
                    + "  window.__bsd29967Capture = {installed: true, payloads: [], graphqlCalls: []};"
                    + "} else if (!window.__bsd29967Capture.graphqlCalls) {"
                    + "  window.__bsd29967Capture.graphqlCalls = [];"
                    + "}"
                    + "window.__bsd29967Capture.installed = true;"
                    + "function recordGraphql(url, requestBody) {"
                    + "  if (!url || (url.indexOf('graphql') < 0 && url.indexOf('appsync') < 0)) return;"
                    + "  window.__bsd29967Capture.graphqlCalls.push({"
                    + "    url: url, body: requestBody || ''"
                    + "  });"
                    + "  if (window.__bsd29967Capture.graphqlCalls.length > 12) {"
                    + "    window.__bsd29967Capture.graphqlCalls.shift();"
                    + "  }"
                    + "}"
                    + "function store(text, url) {"
                    + "  if (!text || text.length < 20) return;"
                    + "  var urlStr = url || '';"
                    + "  var fromAppsync = urlStr.indexOf('appsync') >= 0 || urlStr.indexOf('graphql') >= 0;"
                    + "  if (fromAppsync && text.length > 80) {"
                    + "    window.__bsd29967Capture.payloads.push(text);"
                    + "    if (window.__bsd29967Capture.payloads.length > 40) {"
                    + "      window.__bsd29967Capture.payloads.shift();"
                    + "    }"
                    + "    return;"
                    + "  }"
                    + "  if (!fromAppsync && text.indexOf('\"jobs\"') < 0 && text.indexOf('dsIdList') < 0"
                    + "      && text.indexOf('dsIDlist') < 0 && text.indexOf('\"dsId\"') < 0) return;"
                    + "  window.__bsd29967Capture.payloads.push(text);"
                    + "  if (window.__bsd29967Capture.payloads.length > 40) {"
                    + "    window.__bsd29967Capture.payloads.shift();"
                    + "  }"
                    + "}"
                    + "if (!window.__bsd29967FetchPatched) {"
                    + "  window.__bsd29967FetchPatched = true;"
                    + "  var origFetch = window.fetch;"
                    + "  window.fetch = function() {"
                    + "    var args = arguments;"
                    + "    var url = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url) || '';"
                    + "    var reqBody = args[1] && args[1].body ? String(args[1].body) : '';"
                    + "    recordGraphql(url, reqBody);"
                    + "    return origFetch.apply(this, args).then(function(resp) {"
                    + "      try {"
                    + "        var clone = resp.clone();"
                    + "        clone.text().then(function(t) { store(t, url); });"
                    + "      } catch (e) {}"
                    + "      return resp;"
                    + "    });"
                    + "  };"
                    + "}"
                    + "if (!window.__bsd29967XhrPatched) {"
                    + "  window.__bsd29967XhrPatched = true;"
                    + "  var origOpen = XMLHttpRequest.prototype.open;"
                    + "  var origSend = XMLHttpRequest.prototype.send;"
                    + "  XMLHttpRequest.prototype.open = function(method, url) {"
                    + "    this.__bsd29967Url = url; return origOpen.apply(this, arguments);"
                    + "  };"
                    + "  XMLHttpRequest.prototype.send = function(body) {"
                    + "    var xhr = this;"
                    + "    recordGraphql(xhr.__bsd29967Url, body ? String(body) : '');"
                    + "    xhr.addEventListener('load', function() {"
                    + "      try { if (xhr.responseText) store(xhr.responseText, xhr.__bsd29967Url); } catch (e) {}"
                    + "    });"
                    + "    return origSend.apply(this, arguments);"
                    + "  };"
                    + "}"
                    + "})()";

    private static final String READ_NETWORK_CAPTURE_DIAG_JS =
            "(function() {"
                    + "var cap = window.__bsd29967Capture;"
                    + "if (!cap || !cap.payloads) return 'no-capture';"
                    + "var out = [];"
                    + "for (var i = 0; i < cap.payloads.length; i++) {"
                    + "  var p = cap.payloads[i] || '';"
                    + "  out.push({"
                    + "    index: i,"
                    + "    length: p.length,"
                    + "    hasJobs: p.indexOf('\"jobs\"') >= 0,"
                    + "    hasDsIdList: p.indexOf('dsIdList') >= 0 || p.indexOf('dsIDlist') >= 0,"
                    + "    hasDsId: p.indexOf('\"dsId\"') >= 0,"
                    + "    preview: p.substring(0, 180).replace(/\\s+/g, ' ')"
                    + "  });"
                    + "}"
                    + "return JSON.stringify({payloadCount: cap.payloads.length,"
                    + " graphqlCalls: (cap.graphqlCalls || []).length, items: out});"
                    + "})()";

    private static final String READ_NETWORK_CAPTURE_JS =
            "return window.__bsd29967Capture && window.__bsd29967Capture.payloads"
                    + " ? JSON.stringify(window.__bsd29967Capture.payloads) : '[]';";

    private static final String EXTRACT_FROM_NGX_JSON_INPUT_JS =
            "(function() {"
                    + "function fromJobs(jobs) {"
                    + "  if (!jobs || !jobs.length) return null;"
                    + "  var best = null, bestIdx = -1;"
                    + "  for (var i = 0; i < jobs.length; i++) {"
                    + "    var job = jobs[i];"
                    + "    if (!job) continue;"
                    + "    var list = job.dsIdList || job.dsIDlist || job.dsIDList;"
                    + "    if (!list || !list.length) continue;"
                    + "    if (i > bestIdx) { bestIdx = i; best = job; }"
                    + "  }"
                    + "  if (!best) { best = jobs[jobs.length - 1]; bestIdx = jobs.length - 1; }"
                    + "  var list = best.dsIdList || best.dsIDlist || best.dsIDList || [];"
                    + "  var dsids = [];"
                    + "  for (var j = 0; j < list.length; j++) {"
                    + "    var item = list[j];"
                    + "    if (typeof item === 'string' && item) dsids.push(item);"
                    + "    else if (item && (item.dsId || item.dsID)) dsids.push(String(item.dsId || item.dsID));"
                    + "  }"
                    + "  if (!dsids.length) return null;"
                    + "  return {jobIndex: bestIdx, omfOrderId: best.omfOrderId ? String(best.omfOrderId) : '', dsids: dsids};"
                    + "}"
                    + "function readJson(el) {"
                    + "  if (!el) return null;"
                    + "  try {"
                    + "    if (window.ng && window.ng.getComponent) {"
                    + "      var comp = window.ng.getComponent(el);"
                    + "      if (comp && comp.json) return comp.json;"
                    + "    }"
                    + "  } catch (e) {}"
                    + "  if (el.json) return el.json;"
                    + "  if (el._json) return el._json;"
                    + "  var attr = el.getAttribute('ng-reflect-json');"
                    + "  if (attr) { try { return JSON.parse(attr); } catch (e) {} }"
                    + "  try {"
                    + "    var ctx = el.__ngContext__ || el.ngComponentInstance;"
                    + "    if (ctx) {"
                    + "      if (ctx.json) return ctx.json;"
                    + "      if (Array.isArray(ctx)) {"
                    + "        for (var c = 0; c < ctx.length; c++) {"
                    + "          if (ctx[c] && ctx[c].json) return ctx[c].json;"
                    + "          if (ctx[c] && ctx[c].jobs) return ctx[c];"
                    + "        }"
                    + "      }"
                    + "    }"
                    + "  } catch (e) {}"
                    + "  return null;"
                    + "}"
                    + "var viewers = document.querySelectorAll('ngx-json-viewer');"
                    + "for (var v = 0; v < viewers.length; v++) {"
                    + "  var json = readJson(viewers[v]);"
                    + "  if (!json) continue;"
                    + "  if (json.jobs) {"
                    + "    var hit = fromJobs(json.jobs);"
                    + "    if (hit) return hit;"
                    + "  }"
                    + "  if (json.data && json.data.jobs) {"
                    + "    var hit2 = fromJobs(json.data.jobs);"
                    + "    if (hit2) return hit2;"
                    + "  }"
                    + "}"
                    + "return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String FETCH_DYNAMO_API_JS =
            "(function() {"
                    + "var orderId = encodeURIComponent('{{ORDER_ID}}');"
                    + "var workflow = '{{WORKFLOW}}';"
                    + "var workflowParam = workflow ? ('?workflow=' + encodeURIComponent(workflow)) : '';"
                    + "var origin = window.location.origin || 'https://contentplatform.viacom.com';"
                    + "var urls = ["
                    + "  origin + '/ops-console-api-dev-ui/api/order/' + orderId + '/dynamo' + workflowParam,"
                    + "  origin + '/ops-console-api-dev-ui/api/dynamo/' + orderId + workflowParam,"
                    + "  origin + '/ops-console-api-dev/dynamo/' + orderId + workflowParam,"
                    + "  origin + '/ops-console-api-dev/order/' + orderId + '/dynamo' + workflowParam,"
                    + "  origin + '/ops-console-api-dev/order/' + orderId + workflowParam,"
                    + "  'https://contentplatform.viacom.com/ops-console-api-dev-ui/api/order/' + orderId + '/dynamo' + workflowParam,"
                    + "  'https://contentplatform.viacom.com/ops-console-api-dev/dynamo/' + orderId + workflowParam"
                    + "];"
                    + "try {"
                    + "  performance.getEntriesByType('resource').forEach(function(e) {"
                    + "    if (!e.name || e.name.indexOf(orderId) < 0) return;"
                    + "    if (e.name.indexOf('dynamo') >= 0 || e.name.indexOf('/order/') >= 0) urls.push(e.name);"
                    + "  });"
                    + "} catch (e) {}"
                    + "var lastStatus = 0;"
                    + "var lastUrl = '';"
                    + "for (var i = 0; i < urls.length; i++) {"
                    + "  try {"
                    + "    var xhr = new XMLHttpRequest();"
                    + "    xhr.open('GET', urls[i], false);"
                    + "    xhr.withCredentials = true;"
                    + "    xhr.send();"
                    + "    lastStatus = xhr.status;"
                    + "    lastUrl = urls[i];"
                    + "    if (xhr.status >= 200 && xhr.status < 300 && xhr.responseText"
                    + "        && (xhr.responseText.indexOf('jobs') >= 0 || xhr.responseText.indexOf('dsIdList') >= 0)) {"
                    + "      return xhr.responseText;"
                    + "    }"
                    + "  } catch (e) {}"
                    + "}"
                    + "return '';"
                    + "})()";

    private static final String EXTRACT_LATEST_JOB_DSIDS_FROM_HTML_JS =
            "(function() {"
                    + "function pushDsidsFromList(list, out) {"
                    + "  if (!list || !list.length) return;"
                    + "  for (var i = 0; i < list.length; i++) {"
                    + "    var item = list[i];"
                    + "    if (typeof item === 'string' && item) out.push(item);"
                    + "    else if (item && item.dsId) out.push(String(item.dsId));"
                    + "  }"
                    + "}"
                    + "function fromParsed(obj) {"
                    + "  if (!obj) return null;"
                    + "  var jobs = obj.jobs;"
                    + "  if (!jobs || !jobs.length) return null;"
                    + "  var best = null, bestIdx = -1;"
                    + "  for (var i = 0; i < jobs.length; i++) {"
                    + "    var job = jobs[i];"
                    + "    if (!job) continue;"
                    + "    var list = job.dsIdList || job.dsIDlist || job.dsIDList;"
                    + "    if (!list || !list.length) continue;"
                    + "    if (i > bestIdx) { bestIdx = i; best = job; }"
                    + "  }"
                    + "  if (!best) { best = jobs[jobs.length - 1]; bestIdx = jobs.length - 1; }"
                    + "  var dsids = [];"
                    + "  pushDsidsFromList(best.dsIdList, dsids);"
                    + "  if (!dsids.length) return null;"
                    + "  return {jobIndex: bestIdx, omfOrderId: best.omfOrderId ? String(best.omfOrderId) : '', dsids: dsids};"
                    + "}"
                    + "function parseJobsArrayJson(text) {"
                    + "  try { return JSON.parse(text); } catch (e) { return null; }"
                    + "}"
                    + "function extractJobsArrayFromHtml(html) {"
                    + "  var markers = ['\"jobs\"', '\\\"jobs\\\"', 'jobs'];"
                    + "  for (var m = 0; m < markers.length; m++) {"
                    + "    var pos = 0;"
                    + "    while ((pos = html.indexOf(markers[m], pos)) >= 0) {"
                    + "      var arrStart = html.indexOf('[', pos + markers[m].length);"
                    + "      if (arrStart < 0 || arrStart - pos > 40) { pos++; continue; }"
                    + "      var depth = 0;"
                    + "      for (var i = arrStart; i < html.length; i++) {"
                    + "        var c = html.charAt(i);"
                    + "        if (c === '[') depth++;"
                    + "        else if (c === ']') {"
                    + "          depth--;"
                    + "          if (depth === 0) {"
                    + "            var arr = parseJobsArrayJson(html.substring(arrStart, i + 1));"
                    + "            if (arr && arr.length) return arr;"
                    + "            break;"
                    + "          }"
                    + "        }"
                    + "      }"
                    + "      pos++;"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function pickLatestFromJobs(jobs) {"
                    + "  if (!jobs || !jobs.length) return null;"
                    + "  return fromParsed({jobs: jobs});"
                    + "}"
                    + "var blocks = document.querySelectorAll('pre, code, script[type=\"application/json\"]');"
                    + "for (var b = 0; b < blocks.length; b++) {"
                    + "  try {"
                    + "    var parsed = JSON.parse(blocks[b].textContent || '');"
                    + "    var hit = fromParsed(parsed);"
                    + "    if (hit) return hit;"
                    + "    if (parsed && parsed.data) { hit = fromParsed(parsed.data); if (hit) return hit; }"
                    + "  } catch (e) {}"
                    + "}"
                    + "var html = document.documentElement.innerHTML;"
                    + "var jobsArr = extractJobsArrayFromHtml(html);"
                    + "if (jobsArr) {"
                    + "  var fromArr = pickLatestFromJobs(jobsArr);"
                    + "  if (fromArr) return fromArr;"
                    + "}"
                    + "var lastDsids = [], lastOmf = '', lastIdx = -1, listOrdinal = -1;"
                    + "var listRe = /\"dsIdList\"\\s*:\\s*\\[/g;"
                    + "var listMatch;"
                    + "while ((listMatch = listRe.exec(html)) !== null) {"
                    + "  var before = html.substring(Math.max(0, listMatch.index - 12000), listMatch.index);"
                    + "  if (before.indexOf('lineItems') >= 0 && before.indexOf('\"jobs\"') < 0) continue;"
                    + "  var arrStart = listMatch.index + listMatch[0].length - 1;"
                    + "  var depth = 0, arrEnd = -1;"
                    + "  for (var j = arrStart; j < html.length; j++) {"
                    + "    var ch = html.charAt(j);"
                    + "    if (ch === '[') depth++;"
                    + "    else if (ch === ']') { depth--; if (depth === 0) { arrEnd = j; break; } }"
                    + "  }"
                    + "  if (arrEnd < 0) continue;"
                    + "  var listBody = html.substring(arrStart, arrEnd + 1);"
                    + "  var dsids = [];"
                    + "  var dsRe = /\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"/g;"
                    + "  var dm;"
                    + "  while ((dm = dsRe.exec(listBody)) !== null) dsids.push(dm[1]);"
                    + "  if (!dsids.length) continue;"
                    + "  listOrdinal++;"
                    + "  lastDsids = dsids;"
                    + "  lastIdx = listOrdinal;"
                    + "  var omfM = before.match(/\"omfOrderId\"\\s*:\\s*\"?(\\d+)\"?/g);"
                    + "  if (omfM && omfM.length) {"
                    + "    var omfVal = omfM[omfM.length - 1].match(/(\\d+)/);"
                    + "    lastOmf = omfVal ? omfVal[1] : '';"
                    + "  }"
                    + "}"
                    + "return {jobIndex: lastIdx, omfOrderId: lastOmf, dsids: lastDsids};"
                    + "})()";

    private static final String NGX_JSON_TREE_HELPERS_JS =
            "function norm(s) { return (s || '').replace(/\\s+/g, ' ').trim().replace(/^\"+|\"+$/g, '').replace(/:$/, ''); }"
                    + "function keyLabel(el) { return norm(el && el.textContent); }"
                    + "function pushDsidsFromJobList(list, out) {"
                    + "  if (!list || !list.length) return;"
                    + "  for (var i = 0; i < list.length; i++) {"
                    + "    var item = list[i];"
                    + "    if (typeof item === 'string' && item) out.push(item);"
                    + "    else if (item && (item.dsId || item.dsID)) out.push(String(item.dsId || item.dsID));"
                    + "  }"
                    + "}"
                    + "function unwrapAv(val) {"
                    + "  if (!val || typeof val !== 'object') return val;"
                    + "  if (val.S !== undefined) return val.S;"
                    + "  if (val.N !== undefined) return val.N;"
                    + "  if (val.BOOL !== undefined) return val.BOOL;"
                    + "  if (val.NULL) return null;"
                    + "  if (val.L) {"
                    + "    var arr = [];"
                    + "    for (var i = 0; i < val.L.length; i++) arr.push(unwrapAv(val.L[i]));"
                    + "    return arr;"
                    + "  }"
                    + "  if (val.M) {"
                    + "    var obj = {};"
                    + "    for (var k in val.M) {"
                    + "      if (Object.prototype.hasOwnProperty.call(val.M, k)) obj[k] = unwrapAv(val.M[k]);"
                    + "    }"
                    + "    return obj;"
                    + "  }"
                    + "  return val;"
                    + "}"
                    + "function deepUnwrap(val, depth) {"
                    + "  depth = depth || 0;"
                    + "  if (val == null || depth > 24) return val;"
                    + "  if (typeof val === 'string') {"
                    + "    var trimmed = val.trim();"
                    + "    if (trimmed.length > 20 && (trimmed.charAt(0) === '{' || trimmed.charAt(0) === '[')) {"
                    + "      try { return deepUnwrap(JSON.parse(trimmed), depth + 1); } catch (e) {}"
                    + "    }"
                    + "    return val;"
                    + "  }"
                    + "  var unwrapped = unwrapAv(val);"
                    + "  if (unwrapped !== val) return deepUnwrap(unwrapped, depth + 1);"
                    + "  if (Array.isArray(val)) {"
                    + "    var outArr = [];"
                    + "    for (var i = 0; i < val.length; i++) outArr.push(deepUnwrap(val[i], depth + 1));"
                    + "    return outArr;"
                    + "  }"
                    + "  if (typeof val === 'object') {"
                    + "    var out = {};"
                    + "    for (var key in val) {"
                    + "      if (Object.prototype.hasOwnProperty.call(val, key)) out[key] = deepUnwrap(val[key], depth + 1);"
                    + "    }"
                    + "    return out;"
                    + "  }"
                    + "  return val;"
                    + "}"
                    + "function pickLatestFromJobsArray(jobs) {"
                    + "  if (!jobs || !jobs.length) return null;"
                    + "  var best = null, bestIdx = -1;"
                    + "  for (var i = 0; i < jobs.length; i++) {"
                    + "    var job = jobs[i];"
                    + "    if (!job) continue;"
                    + "    var list = job.dsIdList || job.dsIDlist || job.dsIDList;"
                    + "    if (!list || !list.length) continue;"
                    + "    if (i > bestIdx) { bestIdx = i; best = job; }"
                    + "  }"
                    + "  if (!best) { best = jobs[jobs.length - 1]; bestIdx = jobs.length - 1; }"
                    + "  var dsids = [];"
                    + "  pushDsidsFromJobList(best.dsIdList || best.dsIDlist || best.dsIDList, dsids);"
                    + "  if (!dsids.length) return null;"
                    + "  return {jobIndex: bestIdx, omfOrderId: best.omfOrderId ? String(best.omfOrderId) : '', dsids: dsids};"
                    + "}"
                    + "function findJobsInObject(obj, depth) {"
                    + "  depth = depth || 0;"
                    + "  if (!obj || depth > 12) return null;"
                    + "  if (typeof obj !== 'string') obj = deepUnwrap(obj);"
                    + "  if (typeof obj === 'string' && obj.length > 40) {"
                    + "    try { return extractFromPayloadText(obj); } catch (e) {}"
                    + "    return null;"
                    + "  }"
                    + "  if (Array.isArray(obj)) {"
                    + "    var hasJobDsIdList = false;"
                    + "    for (var ai = 0; ai < obj.length; ai++) {"
                    + "      var j = obj[ai];"
                    + "      var list = j && (j.dsIdList || j.dsIDlist || j.dsIDList);"
                    + "      if (list && list.length) { hasJobDsIdList = true; break; }"
                    + "    }"
                    + "    if (hasJobDsIdList) return pickLatestFromJobsArray(obj);"
                    + "    return null;"
                    + "  }"
                    + "  if (typeof obj === 'object') {"
                    + "    if (obj.jobs && obj.jobs.length) {"
                    + "      var jobsHit = pickLatestFromJobsArray(obj.jobs);"
                    + "      if (jobsHit) return jobsHit;"
                    + "    }"
                    + "    for (var k in obj) {"
                    + "      if (!Object.prototype.hasOwnProperty.call(obj, k)) continue;"
                    + "      var nested = findJobsInObject(obj[k], depth + 1);"
                    + "      if (nested) return nested;"
                    + "    }"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function extractFromPayloadText(text) {"
                    + "  if (!text || text.length < 20) return null;"
                    + "  try {"
                    + "    var parsedHit = findJobsInObject(JSON.parse(text));"
                    + "    if (parsedHit && parsedHit.dsids && parsedHit.dsids.length) return parsedHit;"
                    + "  } catch (e) {}"
                    + "  var listRe = /\"dsIdList\"\\s*:\\s*\\[/gi;"
                    + "  var lastDsids = [], lastIdx = -1, lastOmf = '', listOrdinal = -1;"
                    + "  var m;"
                    + "  while ((m = listRe.exec(text)) !== null) {"
                    + "    var before = text.substring(Math.max(0, m.index - 12000), m.index);"
                    + "    if (before.indexOf('lineItems') >= 0 && before.indexOf('\"jobs\"') < 0) continue;"
                    + "    var arrStart = m.index + m[0].length - 1;"
                    + "    var depth = 0, arrEnd = -1;"
                    + "    for (var j = arrStart; j < text.length; j++) {"
                    + "      var ch = text.charAt(j);"
                    + "      if (ch === '[') depth++;"
                    + "      else if (ch === ']') { depth--; if (depth === 0) { arrEnd = j; break; } }"
                    + "    }"
                    + "    if (arrEnd < 0) continue;"
                    + "    var listBody = text.substring(arrStart, arrEnd + 1);"
                    + "    var dsids = [];"
                    + "    var dsRe = /\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"/g;"
                    + "    var dm;"
                    + "    while ((dm = dsRe.exec(listBody)) !== null) dsids.push(dm[1]);"
                    + "    if (!dsids.length) continue;"
                    + "    listOrdinal++;"
                    + "    lastDsids = dsids;"
                    + "    lastIdx = listOrdinal;"
                    + "    var omfM = before.match(/\"omfOrderId\"\\s*:\\s*\"?(\\d+)\"?/g);"
                    + "    if (omfM && omfM.length) {"
                    + "      var omfVal = omfM[omfM.length - 1].match(/(\\d+)/);"
                    + "      lastOmf = omfVal ? omfVal[1] : lastOmf;"
                    + "    }"
                    + "  }"
                    + "  if (!lastDsids.length) return null;"
                    + "  return {jobIndex: lastIdx, omfOrderId: lastOmf, dsids: lastDsids};"
                    + "}"
                    + "function scrollDynamoPanel() {"
                    + "  window.scrollBy(0, 520);"
                    + "  document.querySelectorAll('ngx-json-viewer, .mat-tab-body-content, [class*=\"dynamo\"]').forEach(function(el) {"
                    + "    try { el.scrollTop = el.scrollHeight; } catch (e) {}"
                    + "  });"
                    + "}"
                    + "function clickExpand(el) {"
                    + "  if (!el) return false;"
                    + "  try { el.scrollIntoView({block: 'center'}); } catch (e) {}"
                    + "  var row = el.closest('section') || el;"
                    + "  var main = row.querySelector(':scope > section.segment-main')"
                    + "      || row.querySelector('.segment-main') || row;"
                    + "  var toggle = main.querySelector('.toggler');"
                    + "  if (toggle) { toggle.click(); return true; }"
                    + "  if (main && main !== row) { main.click(); return true; }"
                    + "  row.click();"
                    + "  return true;"
                    + "}"
                    + "function activeTabRoot() {"
                    + "  var panes = document.querySelectorAll("
                    + "    '.mat-tab-body-active, .mat-mdc-tab-body-active, mat-tab-body.mat-mdc-tab-body-active');"
                    + "  for (var i = 0; i < panes.length; i++) {"
                    + "    var text = (panes[i].innerText || panes[i].textContent || '');"
                    + "    if (text.indexOf('jobs') >= 0 && text.indexOf('omfOrderId') >= 0"
                    + "        && text.indexOf('order-final-status') < 0 && text.indexOf('_index') < 0) {"
                    + "      return panes[i];"
                    + "    }"
                    + "  }"
                    + "  return document.querySelector('.mat-tab-body-active, .mat-mdc-tab-body-active');"
                    + "}"
                    + "function findJobsKey() {"
                    + "  var pane = activeTabRoot();"
                    + "  var root = pane || document;"
                    + "  for (var attempt = 0; attempt < 35; attempt++) {"
                    + "    var candidates = root.querySelectorAll('ngx-json-viewer .segment-key, .segment-key');"
                    + "    for (var i = 0; i < candidates.length; i++) {"
                    + "      if (keyLabel(candidates[i]) === 'jobs') return candidates[i];"
                    + "    }"
                    + "    var viewers = root.querySelectorAll('ngx-json-viewer');"
                    + "    for (var v = 0; v < viewers.length; v++) {"
                    + "      if ((viewers[v].innerText || '').indexOf('jobs') < 0"
                    + "          && (viewers[v].innerText || '').indexOf('omfOrderId') < 0) continue;"
                    + "      var keys = viewers[v].querySelectorAll('.segment-key');"
                    + "      for (var j = 0; j < keys.length; j++) {"
                    + "        if (keyLabel(keys[j]) === 'jobs') return keys[j];"
                    + "      }"
                    + "    }"
                    + "    scrollDynamoPanel();"
                    + "    window.scrollBy(0, 420);"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "function findJobsContainer() {"
                    + "  var jobsKey = findJobsKey();"
                    + "  if (!jobsKey) return null;"
                    + "  var row = jobsKey.closest('section');"
                    + "  if (!row) return null;"
                    + "  var children = row.querySelector(':scope > section.children')"
                    + "      || row.querySelector('section.children');"
                    + "  if (children) return children;"
                    + "  var sibling = row.nextElementSibling;"
                    + "  if (sibling && sibling.tagName === 'SECTION') return sibling;"
                    + "  var parent = row.parentElement;"
                    + "  if (!parent) return null;"
                    + "  var sections = parent.querySelectorAll(':scope > section');"
                    + "  for (var i = 0; i < sections.length; i++) {"
                    + "    if (sections[i] === row && i + 1 < sections.length) return sections[i + 1];"
                    + "  }"
                    + "  return parent;"
                    + "}"
                    + "function isJobSection(section) {"
                    + "  if (!section) return false;"
                    + "  var text = (section.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (/^\\d+:/.test(text) && (text.indexOf('omfOrderId') >= 0"
                    + "      || text.indexOf('dsIdList') >= 0 || text.indexOf('lastUpdated') >= 0)) return true;"
                    + "  return false;"
                    + "}";

    private static final String EXTRACT_FROM_CAPTURED_PAYLOADS_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var cap = window.__bsd29967Capture;"
                    + "if (!cap || !cap.payloads || !cap.payloads.length) {"
                    + "  return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "}"
                    + "var best = null, bestLen = 0;"
                    + "for (var i = 0; i < cap.payloads.length; i++) {"
                    + "  var hit = extractFromPayloadText(cap.payloads[i]);"
                    + "  if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "    bestLen = hit.dsids.length;"
                    + "    best = hit;"
                    + "  }"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_GRAPHQL_REPLAY_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var cap = window.__bsd29967Capture;"
                    + "var calls = cap && cap.graphqlCalls ? cap.graphqlCalls : [];"
                    + "var best = null, bestLen = 0;"
                    + "for (var i = calls.length - 1; i >= 0; i--) {"
                    + "  var call = calls[i];"
                    + "  if (!call || !call.url) continue;"
                    + "  try {"
                    + "    var xhr = new XMLHttpRequest();"
                    + "    xhr.open('POST', call.url, false);"
                    + "    xhr.withCredentials = true;"
                    + "    xhr.setRequestHeader('Content-Type', 'application/json');"
                    + "    xhr.send(call.body || '{}');"
                    + "    if (xhr.status >= 200 && xhr.status < 300 && xhr.responseText) {"
                    + "      var hit = extractFromPayloadText(xhr.responseText);"
                    + "      if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "        bestLen = hit.dsids.length;"
                    + "        best = hit;"
                    + "      }"
                    + "    }"
                    + "  } catch (e) {}"
                    + "}"
                    + "if (!best && cap && cap.payloads) {"
                    + "  for (var p = 0; p < cap.payloads.length; p++) {"
                    + "    var payloadHit = extractFromPayloadText(cap.payloads[p]);"
                    + "    if (payloadHit && payloadHit.dsids && payloadHit.dsids.length > bestLen) {"
                    + "      bestLen = payloadHit.dsids.length;"
                    + "      best = payloadHit;"
                    + "    }"
                    + "  }"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_ORDER_API_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var orderId = '{{ORDER_ID}}';"
                    + "var workflow = '{{WORKFLOW}}';"
                    + "var wf = workflow ? ('?workflow=' + encodeURIComponent(workflow)) : '';"
                    + "var origin = window.location.origin || 'https://contentplatform.viacom.com';"
                    + "var urls = ["
                    + "  origin + '/ops-console-api-dev-ui/api/order/' + orderId + '/opensearch' + wf,"
                    + "  origin + '/ops-console-api-dev-ui/api/opensearch/order/' + orderId + wf,"
                    + "  origin + '/ops-console-api-dev-ui/api/order/' + orderId + wf,"
                    + "  origin + '/ops-console-api-dev/order/' + orderId + '/opensearch' + wf,"
                    + "  origin + '/ops-console-api-dev-ui/order/' + orderId + '/opensearch' + wf,"
                    + "  'https://contentplatform.viacom.com/ops-console-api-dev-ui/api/order/' + orderId + '/opensearch' + wf"
                    + "];"
                    + "try {"
                    + "  performance.getEntriesByType('resource').forEach(function(e) {"
                    + "    if (!e.name || e.name.indexOf(orderId) < 0) return;"
                    + "    if (e.name.indexOf('opensearch') >= 0 || e.name.indexOf('/order/') >= 0) urls.push(e.name);"
                    + "  });"
                    + "} catch (e) {}"
                    + "var best = null, bestLen = 0;"
                    + "for (var i = 0; i < urls.length; i++) {"
                    + "  try {"
                    + "    var xhr = new XMLHttpRequest();"
                    + "    xhr.open('GET', urls[i], false);"
                    + "    xhr.withCredentials = true;"
                    + "    xhr.send();"
                    + "    if (xhr.status >= 200 && xhr.status < 300 && xhr.responseText) {"
                    + "      var hit = extractFromPayloadText(xhr.responseText);"
                    + "      if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "        bestLen = hit.dsids.length;"
                    + "        best = hit;"
                    + "      }"
                    + "    }"
                    + "  } catch (e) {}"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String CLEAR_CLIPBOARD_HOOK_JS =
            "window.__bsd29967Clipboard = ''; return true;";

    private static final String IS_DYNAMO_CONTENT_PANE_ACTIVE_JS =
            "(function() {"
                    + "function isVisible(el) {"
                    + "  if (!el) return false;"
                    + "  var rect = el.getBoundingClientRect();"
                    + "  return rect.width > 0 && rect.height > 0;"
                    + "}"
                    + "function paneText(pane) {"
                    + "  return (pane && (pane.innerText || pane.textContent || '')) || '';"
                    + "}"
                    + "function isOpenSearchPane(pane) {"
                    + "  var text = paneText(pane);"
                    + "  return text.indexOf('order-final-status') >= 0 || text.indexOf('_index') >= 0;"
                    + "}"
                    + "function isDynamoPane(pane) {"
                    + "  if (!pane || !isVisible(pane)) return false;"
                    + "  if (isOpenSearchPane(pane)) return false;"
                    + "  if (pane.id === 'ngb-nav-1-panel') return true;"
                    + "  var text = paneText(pane);"
                    + "  if (text.indexOf('jobs') >= 0 && text.indexOf('omfOrderId') >= 0) return true;"
                    + "  return !!pane.querySelector('ngx-json-viewer, app-code-viewer');"
                    + "}"
                    + "var dynamoTab = document.getElementById('ngb-nav-1')"
                    + "  || document.querySelector('a[role=\"tab\"][aria-controls=\"ngb-nav-1-panel\"]');"
                    + "if (dynamoTab) {"
                    + "  var tabActive = dynamoTab.getAttribute('aria-selected') === 'true'"
                    + "    || dynamoTab.classList.contains('active');"
                    + "  if (tabActive) {"
                    + "    var panelId = dynamoTab.getAttribute('aria-controls') || 'ngb-nav-1-panel';"
                    + "    var panel = document.getElementById(panelId);"
                    + "    if (!panel || isDynamoPane(panel)) return true;"
                    + "  }"
                    + "}"
                    + "var ngbPanel = document.getElementById('ngb-nav-1-panel');"
                    + "if (ngbPanel && ngbPanel.classList.contains('active') && isDynamoPane(ngbPanel)) return true;"
                    + "var panes = document.querySelectorAll("
                    + "  '.mat-tab-body-active, .mat-mdc-tab-body-active, mat-tab-body.mat-mdc-tab-body-active');"
                    + "for (var i = 0; i < panes.length; i++) {"
                    + "  if (isDynamoPane(panes[i])) return true;"
                    + "}"
                    + "var tabs = document.querySelectorAll('[role=\"tab\"]');"
                    + "for (var t = 0; t < tabs.length; t++) {"
                    + "  var label = (tabs[t].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (label !== 'Dynamo' || tabs[t].getAttribute('aria-selected') !== 'true') continue;"
                    + "  var panelId2 = tabs[t].getAttribute('aria-controls');"
                    + "  if (!panelId2) return true;"
                    + "  var panel2 = document.getElementById(panelId2);"
                    + "  if (isDynamoPane(panel2)) return true;"
                    + "}"
                    + "return false;"
                    + "})()";

    private static final String INSTALL_CLIPBOARD_HOOK_JS =
            "(function() {"
                    + "window.__bsd29967Clipboard = window.__bsd29967Clipboard || '';"
                    + "if (window.__bsd29967ClipboardHooked) return true;"
                    + "window.__bsd29967ClipboardHooked = true;"
                    + "function storeClip(text) {"
                    + "  if (text && text.length > 20) window.__bsd29967Clipboard = text;"
                    + "}"
                    + "document.addEventListener('copy', function() {"
                    + "  try {"
                    + "    var text = window.getSelection().toString();"
                    + "    storeClip(text);"
                    + "    var ta = document.querySelector('textarea');"
                    + "    if (ta && ta.value) storeClip(ta.value);"
                    + "  } catch (e) {}"
                    + "});"
                    + "if (navigator.clipboard && navigator.clipboard.writeText) {"
                    + "  var origWrite = navigator.clipboard.writeText.bind(navigator.clipboard);"
                    + "  navigator.clipboard.writeText = function(text) {"
                    + "    storeClip(text);"
                    + "    return origWrite(text);"
                    + "  };"
                    + "}"
                    + "if (!window.__bsd29967ExecHooked && document.execCommand) {"
                    + "  window.__bsd29967ExecHooked = true;"
                    + "  var origExec = document.execCommand;"
                    + "  document.execCommand = function(cmd) {"
                    + "    if (cmd === 'copy') {"
                    + "      var ta = document.querySelector('textarea');"
                    + "      if (ta && ta.value) storeClip(ta.value);"
                    + "    }"
                    + "    return origExec.apply(this, arguments);"
                    + "  };"
                    + "}"
                    + "return true;"
                    + "})()";

    private static final String CLICK_COPY_BUTTON_JS =
            "(function() {"
                    + "function isVisible(el) {"
                    + "  if (!el) return false;"
                    + "  var rect = el.getBoundingClientRect();"
                    + "  return rect.width > 0 && rect.height > 0;"
                    + "}"
                    + "function clickCopyInPane(pane) {"
                    + "  if (!pane) return false;"
                    + "  var buttons = pane.querySelectorAll('button');"
                    + "  for (var i = 0; i < buttons.length; i++) {"
                    + "    var text = (buttons[i].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "    if (text !== 'Copy' || !isVisible(buttons[i])) continue;"
                    + "    buttons[i].scrollIntoView({block: 'center'});"
                    + "    buttons[i].click();"
                    + "    return true;"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "var dynamoPanel = document.getElementById('ngb-nav-1-panel');"
                    + "if (dynamoPanel && dynamoPanel.classList.contains('active')"
                    + "  && clickCopyInPane(dynamoPanel)) return true;"
                    + "var buttons = document.querySelectorAll('button');"
                    + "for (var b = 0; b < buttons.length; b++) {"
                    + "  var label = (buttons[b].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (label !== 'Copy' || !isVisible(buttons[b])) continue;"
                    + "  buttons[b].scrollIntoView({block: 'center'});"
                    + "  buttons[b].click();"
                    + "  return true;"
                    + "}"
                    + "return false;"
                    + "})()";

    private static final String READ_CLIPBOARD_RAW_JS =
            "return window.__bsd29967Clipboard || '';";

    private static final String READ_CLIPBOARD_JSON_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var text = window.__bsd29967Clipboard || '';"
                    + "if (!text || text.length < 20) {"
                    + "  return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "}"
                    + "var hit = extractFromPayloadText(text);"
                    + "return hit || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_CODE_VIEWER_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function scanAny(val) {"
                    + "  if (!val) return null;"
                    + "  if (typeof val === 'string') return extractFromPayloadText(val);"
                    + "  return findJobsInObject(val);"
                    + "}"
                    + "function consider(hit) {"
                    + "  if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "    bestLen = hit.dsids.length; best = hit;"
                    + "  }"
                    + "}"
                    + "var best = null, bestLen = 0;"
                    + "var viewers = document.querySelectorAll('app-code-viewer');"
                    + "for (var v = 0; v < viewers.length; v++) {"
                    + "  try {"
                    + "    var el = viewers[v];"
                    + "    var comp = window.ng && window.ng.getComponent ? window.ng.getComponent(el) : null;"
                    + "    if (comp) {"
                    + "      var props = ['code','json','data','content','source','payload','orderData',"
                    + "        'parsedData','orderPayload','model','dynamo'];"
                    + "      for (var p = 0; p < props.length; p++) consider(scanAny(comp[props[p]]));"
                    + "      for (var key in comp) {"
                    + "        if (!Object.prototype.hasOwnProperty.call(comp, key)) continue;"
                    + "        var nested = comp[key];"
                    + "        if (nested && typeof nested === 'object' && nested.jobs) consider(scanAny(nested));"
                    + "      }"
                    + "    }"
                    + "    var ctx = el.__ngContext__;"
                    + "    if (ctx && ctx.length) {"
                    + "      for (var c = 0; c < ctx.length; c++) consider(scanAny(ctx[c]));"
                    + "    }"
                    + "    var pre = el.querySelector('pre, code, .p-4');"
                    + "    if (pre && pre.textContent && pre.textContent.indexOf('jobs') >= 0) {"
                    + "      consider(extractFromPayloadText(pre.textContent));"
                    + "    }"
                    + "  } catch (e) {}"
                    + "}"
                    + "if (!best && window.__bsd29967Clipboard) {"
                    + "  consider(extractFromPayloadText(window.__bsd29967Clipboard));"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_ANGULAR_ORDER_STATE_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "if (!window.ng || !window.ng.getComponent) {"
                    + "  return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "}"
                    + "var best = null, bestLen = 0, scanned = 0;"
                    + "var selectors = ['app-root', 'app-order-inspector', 'mat-tab-body', 'ngx-json-viewer', '*'];"
                    + "for (var s = 0; s < selectors.length; s++) {"
                    + "  var nodes = document.querySelectorAll(selectors[s]);"
                    + "  for (var i = 0; i < nodes.length && scanned < 600; i++) {"
                    + "    try {"
                    + "      var comp = window.ng.getComponent(nodes[i]);"
                    + "      if (!comp || typeof comp !== 'object') continue;"
                    + "      scanned++;"
                    + "      var props = ['orderData', 'data', 'json', 'dynamoData', 'orderPayload',"
                    + "        'payload', 'model', 'order', 'dynamo', 'response'];"
                    + "      for (var p = 0; p < props.length; p++) {"
                    + "        var val = comp[props[p]];"
                    + "        if (!val || typeof val !== 'object') continue;"
                    + "        var hit = findJobsInObject(val);"
                    + "        if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "          bestLen = hit.dsids.length;"
                    + "          best = hit;"
                    + "        }"
                    + "      }"
                    + "      for (var key in comp) {"
                    + "        if (!Object.prototype.hasOwnProperty.call(comp, key)) continue;"
                    + "        var nested = comp[key];"
                    + "        if (!nested || typeof nested !== 'object') continue;"
                    + "        if (!nested.jobs) continue;"
                    + "        var hit2 = findJobsInObject(nested);"
                    + "        if (hit2 && hit2.dsids && hit2.dsids.length > bestLen) {"
                    + "          bestLen = hit2.dsids.length;"
                    + "          best = hit2;"
                    + "        }"
                    + "      }"
                    + "    } catch (e) {}"
                    + "  }"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_NGX_ANGULAR_COMPONENT_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function readViewerJson(viewer) {"
                    + "  var json = null;"
                    + "  try {"
                    + "    if (window.ng && window.ng.getComponent) json = window.ng.getComponent(viewer).json;"
                    + "  } catch (e) {}"
                    + "  if (!json) {"
                    + "    var attr = viewer.getAttribute('ng-reflect-json');"
                    + "    if (attr) {"
                    + "      try { json = JSON.parse(attr.replace(/&quot;/g, '\"').replace(/&amp;/g, '&')); } catch (e) {}"
                    + "    }"
                    + "  }"
                    + "  return json;"
                    + "}"
                    + "function scanViewers(viewers) {"
                    + "  for (var v = 0; v < viewers.length; v++) {"
                    + "    var json = readViewerJson(viewers[v]);"
                    + "    if (!json) continue;"
                    + "    var hit = findJobsInObject(json);"
                    + "    if (hit && hit.dsids && hit.dsids.length) return hit;"
                    + "  }"
                    + "  return null;"
                    + "}"
                    + "var pane = activeTabRoot();"
                    + "var activeViewers = pane ? pane.querySelectorAll('ngx-json-viewer') : [];"
                    + "var activeHit = scanViewers(activeViewers);"
                    + "if (activeHit) return activeHit;"
                    + "var allHit = scanViewers(document.querySelectorAll('ngx-json-viewer'));"
                    + "if (allHit) return allHit;"
                    + "return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACT_FROM_NG_REFLECT_JSON_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function decodeAttr(attr) {"
                    + "  if (!attr) return '';"
                    + "  return attr.replace(/&quot;/g, '\"').replace(/&amp;/g, '&').replace(/&#34;/g, '\"');"
                    + "}"
                    + "var best = null, bestLen = 0;"
                    + "document.querySelectorAll('[ng-reflect-json]').forEach(function(el) {"
                    + "  var attr = el.getAttribute('ng-reflect-json');"
                    + "  if (!attr || attr.indexOf('jobs') < 0) return;"
                    + "  try {"
                    + "    var hit = extractFromPayloadText(decodeAttr(attr));"
                    + "    if (hit && hit.dsids && hit.dsids.length > bestLen) {"
                    + "      bestLen = hit.dsids.length;"
                    + "      best = hit;"
                    + "    }"
                    + "  } catch (e) {}"
                    + "});"
                    + "if (!best) {"
                    + "  var html = document.documentElement.innerHTML;"
                    + "  var re = /ng-reflect-json=\\\"([^\\\"]+)\\\"/g;"
                    + "  var m;"
                    + "  while ((m = re.exec(html)) !== null) {"
                    + "    var chunk = decodeAttr(m[1]);"
                    + "    if (chunk.indexOf('jobs') < 0) continue;"
                    + "    var hit2 = extractFromPayloadText(chunk);"
                    + "    if (hit2 && hit2.dsids && hit2.dsids.length > bestLen) {"
                    + "      bestLen = hit2.dsids.length;"
                    + "      best = hit2;"
                    + "    }"
                    + "  }"
                    + "}"
                    + "return best || {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "})()";

    private static final String EXTRACTION_DIAGNOSTICS_JS =
            "(function() {"
                    + "var pane = document.querySelector('.mat-tab-body-active, .mat-mdc-tab-body-active');"
                    + "var paneText = pane ? (pane.innerText || '') : '';"
                    + "var jobsKey = false;"
                    + "try {"
                    + "  var keys = (pane || document).querySelectorAll('.segment-key');"
                    + "  for (var i = 0; i < keys.length; i++) {"
                    + "    if ((keys[i].textContent || '').replace(/\\s+/g,'').indexOf('jobs') >= 0) { jobsKey = true; break; }"
                    + "  }"
                    + "} catch (e) {}"
                    + "var viewerCount = (pane || document).querySelectorAll('ngx-json-viewer').length;"
                    + "var codeViewerCount = document.querySelectorAll('app-code-viewer').length;"
                    + "var ngReflectCount = document.querySelectorAll('[ng-reflect-json]').length;"
                    + "return JSON.stringify({"
                    + "  activeTabHasJobs: paneText.indexOf('jobs') >= 0,"
                    + "  activeTabHasDsIdList: paneText.indexOf('dsIdList') >= 0,"
                    + "  activeTabHasOmf: paneText.indexOf('omfOrderId') >= 0,"
                    + "  jobsKeyFound: jobsKey,"
                    + "  viewerCount: viewerCount,"
                    + "  codeViewerCount: codeViewerCount,"
                    + "  ngReflectCount: ngReflectCount,"
                    + "  hasNgGetComponent: !!(window.ng && window.ng.getComponent),"
                    + "  clipboardBytes: (window.__bsd29967Clipboard || '').length"
                    + "});"
                    + "})()";

    private static final String EXPAND_ALL_JSON_TOGGLERS_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var togglers = document.querySelectorAll('ngx-json-viewer .toggler');"
                    + "var clicked = 0;"
                    + "for (var i = 0; i < togglers.length && clicked < 80; i++) {"
                    + "  try { togglers[i].click(); clicked++; } catch (e) {}"
                    + "}"
                    + "return clicked > 0;"
                    + "})()";

    private static final String EXPAND_JOBS_VIA_LOCATOR_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "for (var pass = 0; pass < 20; pass++) { scrollDynamoPanel(); }"
                    + "var jobsKey = findJobsKey();"
                    + "if (jobsKey) clickExpand(jobsKey);"
                    + "var bestIdx = -1;"
                    + "var keys = document.querySelectorAll('ngx-json-viewer .segment-key, ngx-json-viewer span');"
                    + "for (var i = 0; i < keys.length; i++) {"
                    + "  var label = keyLabel(keys[i]);"
                    + "  if (!/^\\d+$/.test(label)) continue;"
                    + "  var row = keys[i].closest('section');"
                    + "  if (!row) continue;"
                    + "  var ctx = (row.textContent || '').replace(/\\s+/g, ' ');"
                    + "  if (ctx.indexOf('omfOrderId') < 0 && ctx.indexOf('dsIdList') < 0"
                    + "      && ctx.indexOf('lastUpdated') < 0) continue;"
                    + "  var idx = parseInt(label, 10);"
                    + "  if (idx > bestIdx) bestIdx = idx;"
                    + "}"
                    + "if (bestIdx < 0) return false;"
                    + "for (var j = 0; j < keys.length; j++) {"
                    + "  if (keyLabel(keys[j]) !== String(bestIdx)) continue;"
                    + "  clickExpand(keys[j]);"
                    + "  break;"
                    + "}"
                    + "var dsKeys = document.querySelectorAll('ngx-json-viewer .segment-key, ngx-json-viewer span');"
                    + "for (var d = 0; d < dsKeys.length; d++) {"
                    + "  var dsLabel = keyLabel(dsKeys[d]);"
                    + "  if (dsLabel !== 'dsIdList' && dsLabel !== 'dsIDlist') continue;"
                    + "  var dsRow = dsKeys[d].closest('section');"
                    + "  var dsCtx = dsRow ? (dsRow.textContent || '') : '';"
                    + "  if (dsCtx.indexOf('lineItems') >= 0) continue;"
                    + "  clickExpand(dsKeys[d]);"
                    + "}"
                    + "return true;"
                    + "})()";

    private static final String FIND_HIGHEST_JOB_INDEX_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var jobsKey = findJobsKey();"
                    + "if (!jobsKey) return -1;"
                    + "clickExpand(jobsKey);"
                    + "var container = findJobsContainer();"
                    + "if (!container) return -1;"
                    + "var maxIdx = -1;"
                    + "container.querySelectorAll(':scope > section.segment, :scope > section').forEach(function(section) {"
                    + "  var keyEl = section.querySelector('.segment-key');"
                    + "  if (!keyEl) return;"
                    + "  var label = keyLabel(keyEl);"
                    + "  if (!/^\\d+$/.test(label)) return;"
                    + "  var ctx = (section.textContent || '').replace(/\\s+/g, ' ');"
                    + "  if (ctx.indexOf('omfOrderId') < 0) return;"
                    + "  var idx = parseInt(label, 10);"
                    + "  if (idx > maxIdx) maxIdx = idx;"
                    + "});"
                    + "return maxIdx;"
                    + "})()";

    private static final String EXPAND_LATEST_JOB_DSIDLIST_FAST_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var jobsKey = findJobsKey();"
                    + "if (!jobsKey) return false;"
                    + "clickExpand(jobsKey);"
                    + "var container = findJobsContainer();"
                    + "if (!container) return false;"
                    + "var bestIdx = -1;"
                    + "var bestKey = null;"
                    + "var bestSection = null;"
                    + "container.querySelectorAll(':scope > section.segment, :scope > section').forEach(function(section) {"
                    + "  var keyEl = section.querySelector('.segment-key');"
                    + "  if (!keyEl) return;"
                    + "  var label = keyLabel(keyEl);"
                    + "  if (!/^\\d+$/.test(label)) return;"
                    + "  var ctx = (section.textContent || '').replace(/\\s+/g, ' ');"
                    + "  if (ctx.indexOf('omfOrderId') < 0) return;"
                    + "  var idx = parseInt(label, 10);"
                    + "  if (idx > bestIdx) { bestIdx = idx; bestKey = keyEl; bestSection = section; }"
                    + "});"
                    + "if (!bestKey || !bestSection) return false;"
                    + "clickExpand(bestKey);"
                    + "var dsListKey = null;"
                    + "bestSection.querySelectorAll('.segment-key').forEach(function(k) {"
                    + "  var lbl = keyLabel(k);"
                    + "  if (lbl === 'dsIdList' || lbl === 'dsIDlist') dsListKey = k;"
                    + "});"
                    + "if (dsListKey) clickExpand(dsListKey);"
                    + "return true;"
                    + "})()";

    private static final String READ_EXPANDED_DSID_VALUES_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function addUnique(arr, seen, v) {"
                    + "  v = norm(v);"
                    + "  if (!v || v === '-' || seen[v]) return;"
                    + "  seen[v] = true;"
                    + "  arr.push(v);"
                    + "}"
                    + "function collectFromJobSection(section) {"
                    + "  var dsids = [];"
                    + "  var seen = {};"
                    + "  if (!section) return dsids;"
                    + "  var dsListRow = null;"
                    + "  section.querySelectorAll('.segment-key, span').forEach(function(k) {"
                    + "    var lbl = keyLabel(k);"
                    + "    if (lbl === 'dsIdList' || lbl === 'dsIDlist') dsListRow = k.closest('section') || section;"
                    + "  });"
                    + "  var root = dsListRow || section;"
                    + "  root.querySelectorAll('.segment-key, span').forEach(function(k) {"
                    + "    if (keyLabel(k) !== 'dsId') return;"
                    + "    var row = k.closest('section');"
                    + "    if (!row) return;"
                    + "    row.querySelectorAll('.string-value').forEach(function(s) {"
                    + "      addUnique(dsids, seen, s.textContent);"
                    + "    });"
                    + "  });"
                    + "  return dsids;"
                    + "}"
                    + "var dsids = [];"
                    + "var omf = '';"
                    + "var jobIdx = -1;"
                    + "var latestSection = null;"
                    + "var container = findJobsContainer();"
                    + "if (container) {"
                    + "  container.querySelectorAll(':scope > section.segment, :scope > section').forEach(function(section) {"
                    + "    var keyEl = section.querySelector('.segment-key');"
                    + "    if (!keyEl) return;"
                    + "    var label = keyLabel(keyEl);"
                    + "    if (!/^\\d+$/.test(label)) return;"
                    + "    var ctx = (section.textContent || '').replace(/\\s+/g, ' ');"
                    + "    if (ctx.indexOf('omfOrderId') < 0) return;"
                    + "    var idx = parseInt(label, 10);"
                    + "    if (idx > jobIdx) {"
                    + "      jobIdx = idx;"
                    + "      latestSection = section;"
                    + "      var omfM = ctx.match(/omfOrderId\\D*(\\d{6,})/);"
                    + "      if (omfM) omf = omfM[1];"
                    + "    }"
                    + "  });"
                    + "}"
                    + "if (latestSection) dsids = collectFromJobSection(latestSection);"
                    + "return {jobIndex: jobIdx, omfOrderId: omf, dsids: dsids};"
                    + "})()";

    private static final String EXTRACT_LATEST_JOB_DSIDS_FROM_NGX_PREVIEW_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function dsidsFromText(text) {"
                    + "  var dsids = [];"
                    + "  if (!text) return dsids;"
                    + "  var listPos = text.indexOf('dsIdList');"
                    + "  var chunk = listPos >= 0 ? text.substring(listPos) : text;"
                    + "  var dsRe = /\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"/g;"
                    + "  var dm;"
                    + "  while ((dm = dsRe.exec(chunk)) !== null) {"
                    + "    if (dm[1]) dsids.push(dm[1]);"
                    + "  }"
                    + "  return dsids;"
                    + "}"
                    + "function dsidsFromSection(section) {"
                    + "  var dsids = dsidsFromText((section.textContent || '').replace(/\\s+/g, ' '));"
                    + "  if (dsids.length) return dsids;"
                    + "  section.querySelectorAll('.object-key-val, section').forEach(function(row) {"
                    + "    var label = keyLabel(row.querySelector('.segment-key, .object-key, label.object-key, span')); "
                    + "    if (label !== 'dsIdList' && label !== 'dsIDlist') return;"
                    + "    row.querySelectorAll('span.string-value').forEach(function(span) {"
                    + "      var v = norm(span.textContent);"
                    + "      if (v && /^[A-Z0-9][A-Z0-9_-]{3,}$/i.test(v)) dsids.push(v);"
                    + "    });"
                    + "  });"
                    + "  return dsids;"
                    + "}"
                    + "var container = findJobsContainer();"
                    + "var bestIdx = -1, bestDsids = [], bestOmf = '';"
                    + "if (container) {"
                    + "  var sections = container.querySelectorAll('section');"
                    + "  for (var r = 0; r < sections.length; r++) {"
                    + "    if (!isJobSection(sections[r])) continue;"
                    + "    var text = (sections[r].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "    var idxM = text.match(/^(\\d+):/);"
                    + "    var idx = idxM ? parseInt(idxM[1], 10) : r;"
                    + "    var dsids = dsidsFromSection(sections[r]);"
                    + "    if (!dsids.length) continue;"
                    + "    if (idx > bestIdx) {"
                    + "      bestIdx = idx;"
                    + "      bestDsids = dsids;"
                    + "      var omfM = text.match(/omfOrderId\\D*(\\d{6,})/);"
                    + "      bestOmf = omfM ? omfM[1] : bestOmf;"
                    + "    }"
                    + "  }"
                    + "}"
                    + "if (!bestDsids.length) {"
                    + "  var jobsKey = findJobsKey();"
                    + "  if (jobsKey) {"
                    + "    var row = jobsKey.closest('section');"
                    + "    var parent = row ? row.parentElement : null;"
                    + "    if (parent) {"
                    + "      var allText = (parent.textContent || '').replace(/\\s+/g, ' ');"
                    + "      var parts = allText.split(/(?=\\d+:)/);"
                    + "      for (var p = 0; p < parts.length; p++) {"
                    + "        if (parts[p].indexOf('omfOrderId') < 0 && parts[p].indexOf('dsIdList') < 0) continue;"
                    + "        var idxM2 = parts[p].match(/^(\\d+):/);"
                    + "        var idx2 = idxM2 ? parseInt(idxM2[1], 10) : p;"
                    + "        var dsids2 = dsidsFromText(parts[p]);"
                    + "        if (!dsids2.length) continue;"
                    + "        if (idx2 > bestIdx) {"
                    + "          bestIdx = idx2; bestDsids = dsids2;"
                    + "          var omfM2 = parts[p].match(/omfOrderId\\D*(\\d{6,})/);"
                    + "          if (omfM2) bestOmf = omfM2[1];"
                    + "        }"
                    + "      }"
                    + "    }"
                    + "  }"
                    + "}"
                    + "return {jobIndex: bestIdx, omfOrderId: bestOmf, dsids: bestDsids};"
                    + "})()";

    private static final String AGGRESSIVE_EXPAND_NGX_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var expanded = 0;"
                    + "for (var pass = 0; pass < 30; pass++) {"
                    + "  var togglers = document.querySelectorAll("
                    + "    'ngx-json-viewer .toggler, ngx-json-viewer .expand, ngx-json-viewer .collapsed-icon,"
                    + "     ngx-json-viewer .expanded-icon, ngx-json-viewer .segment-main');"
                    + "  for (var i = 0; i < togglers.length; i++) {"
                    + "    try { togglers[i].click(); expanded++; } catch (e) {}"
                    + "  }"
                    + "  scrollDynamoPanel();"
                    + "}"
                    + "return expanded > 0;"
                    + "})()";

    private static final String EXTRACT_LATEST_JOB_DSIDS_FROM_VISIBLE_TEXT_JS =
            "(function() {"
                    + "function pushDsidsFromList(list, out) {"
                    + "  if (!list || !list.length) return;"
                    + "  for (var i = 0; i < list.length; i++) {"
                    + "    var item = list[i];"
                    + "    if (typeof item === 'string' && item) out.push(item);"
                    + "    else if (item && item.dsId) out.push(String(item.dsId));"
                    + "  }"
                    + "}"
                    + "function fromParsed(obj) {"
                    + "  if (!obj) return null;"
                    + "  var jobs = obj.jobs;"
                    + "  if (!jobs || !jobs.length) return null;"
                    + "  var best = null, bestIdx = -1;"
                    + "  for (var i = 0; i < jobs.length; i++) {"
                    + "    var job = jobs[i];"
                    + "    if (!job) continue;"
                    + "    var list = job.dsIdList || job.dsIDlist || job.dsIDList;"
                    + "    if (!list || !list.length) continue;"
                    + "    if (i > bestIdx) { bestIdx = i; best = job; }"
                    + "  }"
                    + "  if (!best) { best = jobs[jobs.length - 1]; bestIdx = jobs.length - 1; }"
                    + "  var dsids = [];"
                    + "  pushDsidsFromList(best.dsIdList, dsids);"
                    + "  if (!dsids.length) return null;"
                    + "  return {jobIndex: bestIdx, omfOrderId: best.omfOrderId ? String(best.omfOrderId) : '', dsids: dsids};"
                    + "}"
                    + "var bodyText = document.body ? (document.body.innerText || '') : '';"
                    + "if (!bodyText || bodyText.indexOf('dsIdList') < 0) {"
                    + "  return {jobIndex: -1, omfOrderId: '', dsids: []};"
                    + "}"
                    + "var blocks = bodyText.split(/(?=\\d+:\\s*\\{)/);"
                    + "var bestIdx = -1, bestDsids = [], bestOmf = '';"
                    + "for (var b = 0; b < blocks.length; b++) {"
                    + "  var block = blocks[b];"
                    + "  if (block.indexOf('dsIdList') < 0) continue;"
                    + "  var idxM = block.match(/^(\\d+):/);"
                    + "  var idx = idxM ? parseInt(idxM[1], 10) : b;"
                    + "  var dsids = [];"
                    + "  var dsRe = /\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"/g;"
                    + "  var dm;"
                    + "  while ((dm = dsRe.exec(block)) !== null) { if (dm[1]) dsids.push(dm[1]); }"
                    + "  if (!dsids.length) {"
                    + "    var plainRe = /\\b([A-Z][A-Z0-9_]{5,})\\b/g;"
                    + "    var pm;"
                    + "    while ((pm = plainRe.exec(block)) !== null) {"
                    + "      var val = pm[1];"
                    + "      if (val === 'dsIdList' || val === 'omfOrderId' || val === 'lastUpdatedTime') continue;"
                    + "      dsids.push(val);"
                    + "    }"
                    + "  }"
                    + "  if (!dsids.length) continue;"
                    + "  if (idx > bestIdx) {"
                    + "    bestIdx = idx; bestDsids = dsids;"
                    + "    var omfM = block.match(/omfOrderId\\D*(\\d{6,})/);"
                    + "    if (omfM) bestOmf = omfM[1];"
                    + "  }"
                    + "}"
                    + "if (!bestDsids.length) {"
                    + "  var html = document.documentElement.innerHTML;"
                    + "  var listRe = /\"dsIdList\"\\s*:\\s*\\[/g;"
                    + "  var listMatch, listIdx = 0;"
                    + "  while ((listMatch = listRe.exec(html)) !== null) {"
                    + "    var before = html.substring(Math.max(0, listMatch.index - 15000), listMatch.index);"
                    + "    if (before.indexOf('lineItems') >= 0 && before.indexOf('\"jobs\"') < 0) { listIdx++; continue; }"
                    + "    var arrStart = listMatch.index + listMatch[0].length - 1;"
                    + "    var depth = 0, arrEnd = -1;"
                    + "    for (var j = arrStart; j < html.length; j++) {"
                    + "      var ch = html.charAt(j);"
                    + "      if (ch === '[') depth++;"
                    + "      else if (ch === ']') { depth--; if (depth === 0) { arrEnd = j; break; } }"
                    + "    }"
                    + "    if (arrEnd < 0) { listIdx++; continue; }"
                    + "    var listBody = html.substring(arrStart, arrEnd + 1);"
                    + "    var dsids2 = [];"
                    + "    var dsRe2 = /\"dsId\"\\s*:\\s*\"([^\"\\\\]+)\"/g;"
                    + "    var dm2;"
                    + "    while ((dm2 = dsRe2.exec(listBody)) !== null) dsids2.push(dm2[1]);"
                    + "    if (!dsids2.length) { listIdx++; continue; }"
                    + "    bestDsids = dsids2;"
                    + "    bestIdx = listIdx;"
                    + "    var omfM2 = before.match(/\"omfOrderId\"\\s*:\\s*\"?(\\d+)\"?/g);"
                    + "    if (omfM2 && omfM2.length) {"
                    + "      var omfVal = omfM2[omfM2.length - 1].match(/(\\d+)/);"
                    + "      bestOmf = omfVal ? omfVal[1] : bestOmf;"
                    + "    }"
                    + "    listIdx++;"
                    + "  }"
                    + "}"
                    + "return {jobIndex: bestIdx, omfOrderId: bestOmf, dsids: bestDsids};"
                    + "})()";

    private static final String SCROLL_DYNAMO_PANEL_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "for (var i = 0; i < 12; i++) { scrollDynamoPanel(); }"
                    + "return true;"
                    + "})()";

    private static final String SCROLL_EXPAND_JOBS_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "for (var i = 0; i < 45; i++) {"
                    + "  var jobsKey = findJobsKey();"
                    + "  if (jobsKey) { clickExpand(jobsKey); return true; }"
                    + "  scrollDynamoPanel();"
                    + "}"
                    + "return false;"
                    + "})()";

    private static final String PICK_LATEST_JOB_INDEX_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var container = findJobsContainer();"
                    + "var bestIdx = -1;"
                    + "if (container) {"
                    + "  var sections = container.querySelectorAll('section');"
                    + "  for (var r = 0; r < sections.length; r++) {"
                    + "    if (!isJobSection(sections[r])) continue;"
                    + "    var text = (sections[r].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "    var m = text.match(/^(\\d+):/);"
                    + "    if (!m) continue;"
                    + "    var idx = parseInt(m[1], 10);"
                    + "    if (idx > bestIdx) bestIdx = idx;"
                    + "  }"
                    + "}"
                    + "if (bestIdx >= 0) return bestIdx;"
                    + "var jobsKey = findJobsKey();"
                    + "var html = '';"
                    + "if (jobsKey) {"
                    + "  var row = jobsKey.closest('section');"
                    + "  var parent = row ? row.parentElement : null;"
                    + "  html = parent ? (parent.innerHTML || '') : '';"
                    + "}"
                    + "if (!html) html = document.documentElement.innerHTML;"
                    + "var jobsPos = html.indexOf('\"jobs\"');"
                    + "if (jobsPos < 0) jobsPos = html.indexOf('jobs');"
                    + "var slice = jobsPos >= 0 ? html.substring(jobsPos, jobsPos + 200000) : html;"
                    + "var jobRe = /\"dsIdList\"\\s*:\\s*\\[/g;"
                    + "var match, pos = -1;"
                    + "while ((match = jobRe.exec(slice)) !== null) pos++;"
                    + "return pos;"
                    + "})()";

    private static final String EXPAND_JOB_INDEX_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var target = {{INDEX}};"
                    + "var container = findJobsContainer();"
                    + "if (!container) return false;"
                    + "var sections = container.querySelectorAll('section');"
                    + "for (var r = 0; r < sections.length; r++) {"
                    + "  if (!isJobSection(sections[r])) continue;"
                    + "  var text = (sections[r].textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (!text.match(new RegExp('^' + target + ':'))) continue;"
                    + "  return clickExpand(sections[r]);"
                    + "}"
                    + "var rows = container.querySelectorAll(':scope > section');"
                    + "if (target >= 0 && target < rows.length) return clickExpand(rows[target]);"
                    + "return false;"
                    + "})()";

    private static final String EXPAND_DSIDLIST_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var keys = document.querySelectorAll('ngx-json-viewer span, ngx-json-viewer section');"
                    + "for (var i = 0; i < keys.length; i++) {"
                    + "  var label = keyLabel(keys[i]);"
                    + "  if (label !== 'dsIdList' && label !== 'dsIDlist') continue;"
                    + "  var row = keys[i].closest('section');"
                    + "  var ctx = row ? (row.textContent || '') : '';"
                    + "  if (ctx.indexOf('lineItems') >= 0) continue;"
                    + "  return clickExpand(keys[i]);"
                    + "}"
                    + "return false;"
                    + "})()";

    private static final String EXPAND_DSIDLIST_ITEMS_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "var keys = document.querySelectorAll('ngx-json-viewer span, ngx-json-viewer section');"
                    + "var expanded = 0;"
                    + "for (var i = 0; i < keys.length; i++) {"
                    + "  var label = keyLabel(keys[i]);"
                    + "  if (!/^\\d+$/.test(label)) continue;"
                    + "  var row = keys[i].closest('section');"
                    + "  var ctx = row ? (row.textContent || '') : '';"
                    + "  if (ctx.indexOf('dsIdList') < 0 && ctx.indexOf('dsId') < 0) continue;"
                    + "  if (clickExpand(keys[i])) expanded++;"
                    + "  if (expanded >= 25) break;"
                    + "}"
                    + "return expanded > 0;"
                    + "})()";

    private static final String COLLECT_DSIDS_JS =
            "(function() {" + NGX_JSON_TREE_HELPERS_JS
                    + "function addUnique(arr, seen, v) {"
                    + "  v = norm(v);"
                    + "  if (!v || v === '-' || seen[v]) return;"
                    + "  seen[v] = true;"
                    + "  arr.push(v);"
                    + "}"
                    + "function collectFromSubtree(root) {"
                    + "  var dsids = [];"
                    + "  if (!root) return dsids;"
                    + "  root.querySelectorAll('span.object-key, label.object-key, ngx-json-viewer span').forEach(function(k) {"
                    + "    if (keyLabel(k) !== 'dsId') return;"
                    + "    var row = k.closest('.object-key-val, section');"
                    + "    if (!row) return;"
                    + "    var strVal = row.querySelector('.string-value, span.string-value');"
                    + "    if (strVal) { var v = norm(strVal.textContent); if (v) dsids.push(v); }"
                    + "    var sibs = row.querySelectorAll('span');"
                    + "    for (var s = 0; s < sibs.length; s++) {"
                    + "      var t = norm(sibs[s].textContent);"
                    + "      if (t && t !== 'dsId' && /^[A-Z0-9][A-Z0-9_-]{3,}$/i.test(t)) dsids.push(t);"
                    + "    }"
                    + "  });"
                    + "  if (!dsids.length) {"
                    + "    root.querySelectorAll('span').forEach(function(span) {"
                    + "      var t = norm(span.textContent);"
                    + "      if (!t || t === 'dsId' || t === 'dsIdList' || /^\\d+$/.test(t)) return;"
                    + "      if (/^[A-Z0-9][A-Z0-9_-]{3,}$/i.test(t)) dsids.push(t);"
                    + "    });"
                    + "  }"
                    + "  return dsids;"
                    + "}"
                    + "var dsids = [];"
                    + "var jobIdx = -1;"
                    + "var container = findJobsContainer();"
                    + "var latestSection = null;"
                    + "if (container) {"
                    + "  container.querySelectorAll(':scope > section.segment, :scope > section').forEach(function(section) {"
                    + "    var keyEl = section.querySelector('.segment-key');"
                    + "    if (!keyEl) return;"
                    + "    var lbl = keyLabel(keyEl);"
                    + "    if (!/^\\d+$/.test(lbl)) return;"
                    + "    var ctx = (section.textContent || '').replace(/\\s+/g, ' ');"
                    + "    if (ctx.indexOf('omfOrderId') < 0) return;"
                    + "    var idx = parseInt(lbl, 10);"
                    + "    if (idx > jobIdx) { jobIdx = idx; latestSection = section; }"
                    + "  });"
                    + "}"
                    + "if (latestSection) {"
                    + "  latestSection.querySelectorAll('.segment-key, span').forEach(function(k) {"
                    + "    var lbl = keyLabel(k);"
                    + "    if (lbl !== 'dsIdList' && lbl !== 'dsIDlist') return;"
                    + "    var row = k.closest('section');"
                    + "    if (row) dsids = collectFromSubtree(row);"
                    + "  });"
                    + "}"
                    + "if (!dsids.length) {"
                    + "  var dsListKeys = document.querySelectorAll('ngx-json-viewer span, ngx-json-viewer section');"
                    + "  for (var i = 0; i < dsListKeys.length; i++) {"
                    + "    var label = keyLabel(dsListKeys[i]);"
                    + "    if (label !== 'dsIdList' && label !== 'dsIDlist') continue;"
                    + "    var row = dsListKeys[i].closest('section');"
                    + "    var ctx = row ? (row.textContent || '') : '';"
                    + "    if (ctx.indexOf('lineItems') >= 0) continue;"
                    + "    var found = collectFromSubtree(row);"
                    + "    if (found.length) dsids = found;"
                    + "  }"
                    + "}"
                    + "var omf = '';"
                    + "var body = document.body ? (document.body.innerText || '') : '';"
                    + "var omfMatch = body.match(/omfOrderId[^\\d]*(\\d{6,})/);"
                    + "if (omfMatch) omf = omfMatch[1];"
                    + "return {jobIndex: jobIdx, omfOrderId: omf, dsids: dsids};"
                    + "})()";

    private TreeExpandResult parseTreeExpandResult(Object raw) {
        if (!(raw instanceof Map)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) raw;
        TreeExpandResult result = new TreeExpandResult();
        Object jobIndex = map.get("jobIndex");
        if (jobIndex instanceof Number) {
            result.jobIndex = ((Number) jobIndex).intValue();
        }
        Object omf = map.get("omfOrderId");
        result.omfOrderId = omf == null ? "" : String.valueOf(omf).trim();
        result.dsids.addAll(toStringList(map.get("dsids")));
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractDsIdsFromJob(JSONObject latestJob) {
        List<String> dsids = new ArrayList<>();
        Object dsIdList = firstPresentKey(latestJob, "dsIdList", "dsIDlist", "dsIDList", "dsidlist");
        if (!(dsIdList instanceof JSONArray)) {
            return dsids;
        }
        JSONArray list = (JSONArray) dsIdList;
        for (Object item : list) {
            if (item instanceof JSONObject) {
                JSONObject itemObj = (JSONObject) item;
                Object dsId = firstPresentKey(itemObj, "dsId", "dsID", "dsid");
                if (dsId != null) {
                    dsids.add(String.valueOf(dsId).trim());
                }
            } else if (item != null) {
                dsids.add(String.valueOf(item).trim());
            }
        }
        return dsids;
    }

    private Object firstPresentKey(JSONObject obj, String... keys) {
        for (String key : keys) {
            for (Object keyObj : obj.keySet()) {
                if (key.equalsIgnoreCase(String.valueOf(keyObj))) {
                    return obj.get(keyObj);
                }
            }
        }
        return null;
    }

    private List<String> toStringList(Object raw) {
        if (!(raw instanceof List)) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        for (Object item : (List<?>) raw) {
            if (item != null) {
                String text = String.valueOf(item).trim();
                if (!text.isEmpty()) {
                    values.add(text);
                }
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private JSONObject pickLatestJob(JSONArray jobs) {
        int latestIndex = indexOfLatestJob(jobs);
        if (latestIndex < 0 || latestIndex >= jobs.size()) {
            return null;
        }
        Object jobObj = jobs.get(latestIndex);
        if (!(jobObj instanceof JSONObject)) {
            return null;
        }
        JSONObject latest = (JSONObject) jobObj;
        Logger.logReportMessage("Ops-console latest job index=" + latestIndex
                + " (highest jobs[] index with dsIdList)");
        return latest;
    }

    private long jobTimestamp(JSONObject job) {
        long time = parseLong(job.get("lastUpdatedTime"));
        if (time == Long.MIN_VALUE) {
            time = parseLong(job.get("lastUpdatedTimestamp"));
        }
        if (time == Long.MIN_VALUE) {
            time = parseLong(job.get("createdTime"));
        }
        if (time == Long.MIN_VALUE) {
            time = parseLong(job.get("createdTimestamp"));
        }
        return time;
    }

    private long parseLong(Object value) {
        if (value == null) {
            return Long.MIN_VALUE;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    private static final class ExtractionOutcome {
        final List<String> dsids;
        final String source;
        final int jobIndex;

        private ExtractionOutcome(List<String> dsids, String source, int jobIndex) {
            this.dsids = dsids == null ? Collections.emptyList() : new ArrayList<>(dsids);
            this.source = source == null ? "" : source;
            this.jobIndex = jobIndex;
        }

        static ExtractionOutcome empty() {
            return new ExtractionOutcome(Collections.emptyList(), "", -1);
        }

        static ExtractionOutcome fromTree(TreeExpandResult result, String source) {
            return new ExtractionOutcome(result.dsids, source, result.jobIndex);
        }
    }

    private static final class TreeExpandResult {
        int jobIndex = -1;
        String omfOrderId = "";
        final List<String> dsids = new ArrayList<>();
    }
}
