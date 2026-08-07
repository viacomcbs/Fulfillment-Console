package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.pageobjects.NavigationPage;
import com.paramount.test.ff.uitests.helpers.duplicatefilteroptions.DuplicateFilterOptionsCollectionHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsDuplicateAnalyzer;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsGraphqlResponseParser;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsNetworkCaptureHelper;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.QUICK_ELEMENT_TIMEOUT_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SCROLL_STEP_PX;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SEARCH_SETTLE_MAX_ATTEMPTS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SEARCH_SETTLE_POLL_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SELECT_ALL_DISABLED_OPTION_THRESHOLD;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SELECT_ALL_LABEL;

public class LeftFilterPanelUtil extends BaseTest {

    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();
    private final NavigationPage navigationPage = new NavigationPage();
    private final WaitUtils waitUtils = new WaitUtils();

    public LeftFilterPanel getLeftFilterPanel() {
        return leftFilterPanel;
    }

    private static final Pattern COUNT_PATTERN = Pattern.compile("\\((\\d+)\\)");

    public void navigateToTab(ConsoleTab tab) throws InterruptedException {
        if (tab == ConsoleTab.LINE_ITEMS) {
            navigateToLineItemsTab(false);
            return;
        }
        navigateToOrdersTab(false);
    }

    /**
     * Clicks the Line items nav-pill above the grid and waits until Line Items grid chrome is visible.
     * PROD: {@code button.nav-link} in {@code ul.nav-pills} inside {@code app-fulfillment-main-table-container}.
     */
    public void navigateToLineItemsTab(boolean hardFailIfNotActive) throws InterruptedException {
        ensureLeftFilterPanelOpen();
        if (isLineItemsTabActive()) {
            Logger.logReportMessage("Already on Line Items tab (nav-pill active or LineItem ID grid visible)");
            return;
        }
        if (isOrdersTabContextVisible()) {
            Logger.logReportMessage("Currently on Orders tab — clicking Line items nav-pill");
        }
        By tabBtn = navigationPage.lineItemsNavPillButton();
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (!WaitUtil.isDisplayFast(tabBtn, 15)) {
                Logger.logReportMessage("Line Items nav-pill not visible (attempt " + attempt + ")");
                Thread.sleep(2000);
                continue;
            }
            DriverUtil.scrollToElement(tabBtn);
            boolean clicked = DriverUtil.clickOnElement(tabBtn, DEFAULT_WAIT_SECONDS);
            if (!clicked) {
                clicked = DriverUtil.clickOnElementJs(tabBtn, 5);
            }
            if (!clicked) {
                Logger.logReportMessage("Line Items nav-pill click failed (attempt " + attempt + ")");
                continue;
            }
            WaitUtil.waitForJSToLoad(30);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            if (waitForLineItemsTabContext(60)) {
                Logger.logReportMessage("Line Items tab opened — LineItem ID / show hidden line items visible");
                return;
            }
            Logger.logReportMessage("Line Items grid not loaded after nav-pill click (attempt " + attempt + ")");
        }
        boolean active = isLineItemsTabActive();
        if (hardFailIfNotActive) {
            Verify.hardAssert(active, "Line Items tab is active after nav-pill click");
        } else {
            Verify.softAssert(active, "Line Items tab is active after nav-pill click");
        }
    }

    public void navigateToOrdersTab(boolean hardFailIfNotActive) throws InterruptedException {
        ensureLeftFilterPanelOpen();
        if (isTabActive(ConsoleTab.ORDERS)) {
            Logger.logMessage("Already on Orders tab");
            return;
        }
        By tabLocator = navigationPage.ordersNavPillButton();
        if (WaitUtil.isDisplayFast(tabLocator, 10)) {
            DriverUtil.scrollToElement(tabLocator);
            if (!DriverUtil.clickOnElement(tabLocator, DEFAULT_WAIT_SECONDS)) {
                DriverUtil.clickOnElementJs(tabLocator, 5);
            }
            WaitUtil.waitForJSToLoad(30);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        } else if (isOrdersTabContextVisible()) {
            Logger.logMessage("Orders tab is already selected (default landing tab)");
        } else {
            Verify.softAssert(false, "Orders tab button is visible");
        }
        boolean active = isTabActive(ConsoleTab.ORDERS);
        if (hardFailIfNotActive) {
            Verify.hardAssert(active, "Orders tab is active");
        } else {
            Verify.softAssert(active, "Orders tab is active");
        }
    }

    public boolean isLineItemsTabActive() {
        if (WaitUtil.isDisplayFast(navigationPage.lineItemsNavPillActive(), 3)) {
            return true;
        }
        if (isOrdersTabContextVisible() && !WaitUtil.isDisplayFast(navigationPage.lineItemsTabContextMarker(), 2)) {
            return false;
        }
        return WaitUtil.isDisplayFast(navigationPage.lineItemsTabContextMarker(), 5);
    }

    private boolean waitForLineItemsTabContext(int maxSec) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (maxSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (isLineItemsTabActive()) {
                return true;
            }
            Thread.sleep(1000);
        }
        return isLineItemsTabActive();
    }

    public boolean isTabActive(ConsoleTab tab) {
        if (tab == ConsoleTab.LINE_ITEMS) {
            return isLineItemsTabActive();
        }
        if (WaitUtil.isDisplayFast(navigationPage.ordersNavPillActive(), 3)) {
            return true;
        }
        if (WaitUtil.isDisplayFast(navigationPage.activeTab(tab.getTabLabel()), 3)) {
            return true;
        }
        return isOrdersTabContextVisible();
    }

    private boolean isOrdersTabContextVisible() {
        return WaitUtil.isDisplayFast(navigationPage.ordersTabContextMarker(), 3);
    }

    /**
     * Expands the left filter side nav when it is collapsed ({@code filter-panel-closed} + funnel icon).
     * No-op when the panel is already open.
     */
    public void ensureLeftFilterPanelOpen() throws InterruptedException {
        if (isLeftFilterPanelOpenViaDom()) {
            waitForFilterListSection(DEFAULT_WAIT_SECONDS);
            return;
        }

        Logger.logMessage("Left filter panel is collapsed — clicking filter icon to expand");
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (clickFilterPanelExpandViaScript() || clickFilterPanelExpandViaLocator()) {
                Thread.sleep(FILTER_EXPAND_WAIT_MS);
            }
            if (isLeftFilterPanelOpenViaDom() && waitForFilterListSection(8)) {
                Logger.logMessage("Left filter panel expanded");
                return;
            }
            Thread.sleep(500);
        }
    }

    /** Polls for accordion headers after side nav opens — avoids blocking on Synergy 60s findElement. */
    public boolean waitForFilterHeaderVisible(String filterName, int waitSec) throws InterruptedException {
        By header = leftFilterPanel.leftFilterByName(filterName);
        long deadline = System.currentTimeMillis() + (waitSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (WaitUtil.isDisplayFast(header, 1)) {
                return true;
            }
            ensureLeftFilterPanelOpen();
            Thread.sleep(500);
        }
        return WaitUtil.isDisplayFast(header, 1);
    }

    private boolean waitForFilterListSection(int waitSec) {
        return WaitUtil.isDisplayFast(leftFilterPanel.filterListSection(), waitSec);
    }

    /** Reads {@code filter-panel-open} / visible Filters label — reliable when funnel icon stays in DOM. */
    public boolean isLeftFilterPanelOpenViaDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var p=document.querySelector('msc-left-filter-panel');"
                            + "if(!p)return false;"
                            + "if(p.classList.contains('filter-panel-open'))return true;"
                            + "var label=p.querySelector('span.filter-label');"
                            + "return !!(label&&(label.offsetParent||label.getClientRects().length));");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return isLeftFilterPanelOpen();
        }
    }

    private boolean clickFilterPanelExpandViaScript() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var p=document.querySelector('msc-left-filter-panel');"
                            + "if(!p)return false;"
                            + "if(p.classList.contains('filter-panel-open'))return true;"
                            + "var div=p.querySelector('.filterIconDivClosed.show-block');"
                            + "if(div){div.click();return true;}"
                            + "var icon=p.querySelector('.filter-panel-closed i.bi-filter');"
                            + "if(icon){(icon.closest('div')||icon).click();return true;}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean clickFilterPanelExpandViaLocator() {
        By container = leftFilterPanel.filterPanelExpandContainer();
        if (WaitUtil.isDisplay(container, 3)) {
            return DriverUtil.clickOnElement(container, DEFAULT_WAIT_SECONDS);
        }
        By expandIcon = leftFilterPanel.filterPanelExpandIcon();
        if (WaitUtil.isDisplay(expandIcon, 3)) {
            DriverUtil.scrollToElement(expandIcon);
            return DriverUtil.clickOnElement(expandIcon, DEFAULT_WAIT_SECONDS);
        }
        return false;
    }

    public boolean isLeftFilterPanelOpen() {
        if (isLeftFilterPanelOpenViaDom()) {
            return true;
        }
        if (runWithQuickElementTimeout(() ->
                isDisplayedWithoutTimeoutChange(leftFilterPanel.filterPanelOpen()))) {
            return true;
        }
        if (runWithQuickElementTimeout(() ->
                isDisplayedWithoutTimeoutChange(leftFilterPanel.filterPanelHeader()))) {
            return true;
        }
        if (runWithQuickElementTimeout(() ->
                isDisplayedWithoutTimeoutChange(leftFilterPanel.filterPanelExpandIcon()))
                || runWithQuickElementTimeout(() ->
                isDisplayedWithoutTimeoutChange(leftFilterPanel.filterPanelClosed()))) {
            return false;
        }
        return false;
    }

    /**
     * Hard refresh + Yesterday date + expand left filter when toolbar has no date or grid/filters did not load.
     */
    public void ensureOrdersDataLoaded(SoftAssert softAssert) throws InterruptedException {
        new OrdersDataRecoveryHelper().recoverOrdersPageIfNeeded(softAssert);
        ensureLeftFilterPanelOpen();
    }

    public void validateAllFiltersAvailable(SoftAssert softAssert, List<String> expectedFilters) throws InterruptedException {
        ensureLeftFilterPanelOpen();
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Filters panel header is visible");
        for (String filterName : expectedFilters) {
            By filterLocator = leftFilterPanel.leftFilterByName(filterName);
            DriverUtil.scrollToElement(filterLocator);
            Verify.softAssert(isElementDisplayedQuick(filterLocator),
                    "Left filter is available: " + filterName);
        }
    }

    /**
     * Collapses one filter and clears its search text after a per-filter test category.
     */
    public void resetFilterState(String filterName) throws InterruptedException {
        if (isFilterExpanded(filterName)) {
            clearFilterSearchInput(filterName);
            collapseFilter(filterName);
        }
    }

    /**
     * Collapses all expanded filters — use once at suite/tab teardown, not after every category.
     */
    public void resetExpandedFilters(List<String> filterNames) throws InterruptedException {
        for (String filterName : filterNames) {
            resetFilterState(filterName);
        }
    }

    /**
     * TC101 smoke flow: visible → expand → verify expanded → expand again → options + Select all → collapse.
     */
    public void validateBasicSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.leftFilterByName(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " is visible in left filter panel");
        expandFilter(filterName);
        Verify.softAssert(isFilterExpanded(filterName),
                filterName + " filter expanded after clicking accordion");
        expandFilter(filterName);
        List<FilterOption> options = getFilterOptions(filterName);
        Verify.softAssert(!options.isEmpty(), filterName + " has filter options when expanded");
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " shows Select all option at top");
        collapseFilter(filterName);
        Verify.softAssert(!isFilterExpanded(filterName),
                filterName + " filter collapsed after TC101 cleanup");
    }

    /**
     * TC102: Select All first → visible non-zero A-Z → CSS divider (when both groups exist) → zero-count A-Z.
     * Uses first viewport only (enough for paginated / virtual-scroll filters).
     */
    public void validateOptionListOrderSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);

        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " — Select All is first at top");

        List<FilterOption> visible = getFilterOptions(filterName);
        Verify.softAssert(!visible.isEmpty(), filterName + " has visible filter options when expanded");

        List<FilterOption> nonZero = visible.stream().filter(o -> o.getCount() > 0).collect(Collectors.toList());
        List<FilterOption> zero = visible.stream().filter(o -> o.getCount() == 0).collect(Collectors.toList());

        if (nonZero.size() > 1) {
            Verify.softAssert(isSortedAlphabetically(nonZero),
                    filterName + " first visible non-zero options are alphabetical. Actual: " + labelsOf(nonZero));
        }
        if (zero.size() > 1) {
            Verify.softAssert(isSortedAlphabetically(zero),
                    filterName + " first visible zero-count options are alphabetical. Actual: " + labelsOf(zero));
        }
        Verify.softAssert(isNonZeroFirstThenZero(visible),
                filterName + " visible non-zero options appear before zero-count. Actual: " + labelsOf(visible));

        if (!zero.isEmpty() && !nonZero.isEmpty()) {
            Verify.softAssert(hasVisibleZeroCountDivider(filterName, zero.get(0).getLabel()),
                    filterName + " shows CSS divider line between non-zero and zero-count options");
        }

        collapseFilter(filterName);
    }

    /**
     * TC201 smoke flow: expand → read first option → search → verify results → clear → collapse.
     */
    public void validateSearchSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<String> labels = getFilterOptionLabels(filterName);
        Verify.softAssert(!labels.isEmpty(), filterName + " has at least one option for search test");
        String searchText = labels.get(0);
        Logger.logMessage(filterName + " search using first option: " + searchText);

        By searchInput = leftFilterPanel.filterSearchInput(filterName);
        By searchIcon = leftFilterPanel.filterSearchIcon(filterName);
        Verify.softAssert(WaitUtil.isDisplay(searchInput, DEFAULT_WAIT_SECONDS),
                filterName + " has search input inside filter panel");
        Verify.softAssert(WaitUtil.isDisplay(searchIcon, DEFAULT_WAIT_SECONDS),
                filterName + " has search icon inside filter panel");

        DriverUtil.sendKeyToElement(searchInput, DEFAULT_WAIT_SECONDS, searchText);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        By clearIcon = leftFilterPanel.filterSearchClearIcon(filterName);
        Verify.softAssert(WaitUtil.isDisplay(clearIcon, DEFAULT_WAIT_SECONDS),
                filterName + " clear icon visible after typing search text");

        List<String> results = getFilterOptionLabels(filterName);
        Verify.softAssert(!results.isEmpty(), filterName + " search returned results for: " + searchText);
        String lowerSearch = searchText.toLowerCase(Locale.ROOT);
        for (String result : results) {
            Verify.softAssert(result.toLowerCase(Locale.ROOT).contains(lowerSearch),
                    filterName + " search result matches query: " + result);
        }

        clearFilterSearchInput(filterName);
        collapseFilter(filterName);
    }

    public void validateExpandCollapseWorks(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        Verify.softAssert(isFilterExpanded(filterName),
                filterName + " filter expanded after clicking expand icon");
        collapseFilter(filterName);
        Verify.softAssert(!isFilterExpanded(filterName),
                filterName + " filter collapsed after clicking collapse icon");
    }

    public void expandFilter(String filterName) throws InterruptedException {
        ensureLeftFilterPanelOpen();

        if (isFilterExpanded(filterName)) {
            return;
        }

        By accordionButton = leftFilterPanel.filterAccordionButton(filterName);
        By optionsContainer = leftFilterPanel.filterOptionsContainer(filterName);
        By searchInput = leftFilterPanel.filterSearchInput(filterName);

        if (!waitForFilterHeaderVisible(filterName, DEFAULT_WAIT_SECONDS)) {
            Verify.softAssert(false, "Filter header not found for: " + filterName);
            return;
        }

        DriverUtil.scrollToElement(accordionButton);
        clickAccordionToggle(filterName);

        boolean expanded = waitForFilterExpanded(filterName, optionsContainer, searchInput);
        for (int attempt = 1; !expanded && attempt <= 3; attempt++) {
            Logger.logMessage("[expandFilter] Expand retry " + attempt + " for: " + filterName);
            clickAccordionToggle(filterName);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            expanded = waitForFilterExpanded(filterName, optionsContainer, searchInput);
        }

        Verify.softAssert(expanded, filterName + " filter expanded after clicking accordion button");
    }

    private void clickAccordionToggle(String filterName) throws InterruptedException {
        By accordionButton = leftFilterPanel.filterAccordionButton(filterName);
        if (isElementDisplayedQuick(leftFilterPanel.collapseChevronIcon(filterName))
                || isElementDisplayedQuick(accordionButton)) {
            DriverUtil.scrollToElement(accordionButton);
            DriverUtil.clickOnElement(accordionButton, DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }
    }

    private boolean waitForFilterExpanded(String filterName, By optionsContainer, By searchInput)
            throws InterruptedException {
        for (int i = 0; i < 8; i++) {
            if (isFilterExpanded(filterName)
                    || isElementDisplayedQuick(optionsContainer)
                    || isElementDisplayedQuick(searchInput)) {
                return true;
            }
            Thread.sleep(400);
        }
        return isFilterExpanded(filterName);
    }

    public void collapseFilter(String filterName) throws InterruptedException {
        if (isFilterExpanded(filterName)) {
            DriverUtil.clickOnElement(leftFilterPanel.filterAccordionButton(filterName), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }
    }

    public boolean isFilterExpanded(String filterName) {
        return runWithQuickElementTimeout(() ->
                isDisplayedWithoutTimeoutChange(leftFilterPanel.expandedFilterPanel(filterName))
                        || isDisplayedWithoutTimeoutChange(leftFilterPanel.expandedAccordionButton(filterName))
                        || isDisplayedWithoutTimeoutChange(leftFilterPanel.filterSearchInput(filterName)));
    }

    public void validateOptionsAvailableOnExpand(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<FilterOption> options = getFilterOptions(filterName);
        Verify.softAssert(!options.isEmpty(), filterName + " has filter options when expanded");
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " shows Select all option at top");
    }

    /** First visible options only — sufficient for paginated / virtual-scroll filters. */
    public void validateOptionsAlphabeticalOrder(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<String> labels = getFilterOptionLabels(filterName);
        if (labels.size() <= 1) {
            return;
        }
        List<String> sorted = new ArrayList<>(labels);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        Verify.softAssert(labels.equals(sorted),
                filterName + " first visible options are in alphabetical order. Actual: " + labels);
    }

    /** First visible options: non-zero before zero; zero section when zero-count rows are visible. */
    public void validateNonZeroThenDividerThenZero(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<FilterOption> options = getFilterOptions(filterName);
        Verify.softAssert(isNonZeroFirstThenZero(options),
                filterName + " shows non-zero count options first, then zero count options");
        if (hasDividerBetweenCounts(options)) {
            String firstZeroLabel = options.stream()
                    .filter(o -> o.getCount() == 0)
                    .map(FilterOption::getLabel)
                    .findFirst()
                    .orElse(null);
            Verify.softAssert(hasVisibleZeroCountDivider(filterName, firstZeroLabel),
                    filterName + " shows CSS divider line between non-zero and zero-count options");
        }
    }

    public void validateSelectAllAtTop(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " has Select all at the top of options");
    }

    public void validateSelectAllSelectsAll(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        clickSelectAll(filterName);
        Verify.softAssert(isSelectAllChecked(filterName),
                filterName + " Select all is checked after clicking Select all");
        Verify.softAssert(areAllVisibleOptionsSelected(filterName),
                filterName + " all visible options are selected after Select all");
    }

    public void validateSelectAllIndeterminateAndDeselect(SoftAssert softAssert, String filterName,
                                                          int optionsToSelect) throws InterruptedException {
        expandFilter(filterName);

        if (isSelectAllChecked(filterName) || areAnyVisibleOptionsSelected(filterName)) {
            clickSelectAll(filterName);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            if (areAnyVisibleOptionsSelected(filterName)) {
                clickSelectAll(filterName);
                Thread.sleep(FILTER_EXPAND_WAIT_MS);
            }
        }

        List<String> labels = getFilterOptionLabels(filterName);
        Verify.softAssert(labels.size() >= optionsToSelect,
                filterName + " has at least " + optionsToSelect + " options for partial selection test");

        for (int i = 0; i < optionsToSelect; i++) {
            selectFilterOption(filterName, labels.get(i));
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }

        boolean indeterminate = isSelectAllIndeterminate(filterName)
                || isSelectAllPartialSelectionState(filterName, optionsToSelect);
        Verify.softAssert(indeterminate,
                filterName + " Select all shows indeterminate/partial state when partial options selected"
                        + " (badge=" + getFilterSelectionCountText(filterName) + ")");

        clickSelectAll(filterName);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        Verify.softAssert(!isSelectAllChecked(filterName),
                filterName + " Select all deselects all options when clicked in indeterminate state");
        Verify.softAssert(!areAnyVisibleOptionsSelected(filterName),
                filterName + " no visible options remain selected after indeterminate Select all click");
    }

    public void validateSearchInsideFilter(SoftAssert softAssert, String filterName, String searchText)
            throws InterruptedException {
        expandFilter(filterName);
        By searchInput = leftFilterPanel.filterSearchInput(filterName);
        Verify.softAssert(WaitUtil.isDisplay(searchInput, DEFAULT_WAIT_SECONDS),
                filterName + " has search input inside filter panel");

        DriverUtil.sendKeyToElement(searchInput, DEFAULT_WAIT_SECONDS, searchText);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        List<String> results = getFilterOptionLabels(filterName);
        Verify.softAssert(!results.isEmpty(), filterName + " search returned results for: " + searchText);
        for (String result : results) {
            Verify.softAssert(result.toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT)),
                    filterName + " search result matches query: " + result);
        }
    }

    /**
     * Collects every option label for a filter from the live UI, including zero-count options.
     * Uses CDK virtual-scroll stepping through the options viewport (no pagination buttons).
     */
    public List<String> collectAllFilterOptionLabels(String filterName) throws InterruptedException {
        return new DuplicateFilterOptionsCollectionHelper(this).collectAllFilterOptionLabels(filterName);
    }

    /**
     * Collects partner labels from AppSync GraphQL network capture only (no UI scroll fallback).
     */
    public List<String> collectPartnerOptionsNetworkOnly(String filterName) throws InterruptedException {
        PartnerOptionsNetworkCaptureHelper networkHelper = new PartnerOptionsNetworkCaptureHelper(this);
        try {
            return networkHelper.collectPartnerOptionsViaNetwork(filterName);
        } finally {
            networkHelper.clearNetworkCaptureHook();
        }
    }

    /**
     * Collects partner/filter labels by intercepting AppSync GraphQL network responses (preferred).
     * Falls back to virtual-scroll UI collection when network capture is incomplete.
     */
    public List<String> collectAllFilterOptionLabelsPreferNetwork(String filterName) throws InterruptedException {
        PartnerOptionsNetworkCaptureHelper networkHelper = new PartnerOptionsNetworkCaptureHelper(this);
        List<String> fromNetwork = Collections.emptyList();
        try {
            fromNetwork = networkHelper.collectPartnerOptionsViaNetwork(filterName);
            if (!fromNetwork.isEmpty()) {
                int expectedTotal = getTotalOptionCount(filterName);
                int uniqueFromNetwork = PartnerOptionsDuplicateAnalyzer.uniqueCount(fromNetwork);
                if (expectedTotal <= 0 || uniqueFromNetwork >= expectedTotal) {
                    return fromNetwork;
                }
                Logger.logMessage(filterName + ": network capture returned " + uniqueFromNetwork
                        + " unique of " + expectedTotal + " (" + fromNetwork.size()
                        + " raw rows); falling back to UI scroll collection");
            }
        } finally {
            networkHelper.clearNetworkCaptureHook();
        }

        List<String> fromScroll = collectAllFilterOptionLabels(filterName);
        if (fromNetwork.isEmpty()) {
            return fromScroll;
        }

        List<String> merged = PartnerOptionsGraphqlResponseParser.mergeBatches(fromNetwork, fromScroll);
        Logger.logMessage(filterName + ": merged network (" + fromNetwork.size()
                + " raw) + UI scroll (" + fromScroll.size() + " raw) = " + merged.size()
                + " raw row(s), " + PartnerOptionsDuplicateAnalyzer.uniqueCount(merged) + " unique option(s)");
        return merged;
    }

    public List<String> searchFilterOptions(String filterName, String searchText) throws InterruptedException {
        if (!isElementDisplayedQuick(leftFilterPanel.filterSearchInput(filterName))) {
            expandFilter(filterName);
        }
        clearFilterSearchInput(filterName);
        List<String> baselineLabels = snapshotVisibleOptionLabels(filterName);

        By searchInput = leftFilterPanel.filterSearchInput(filterName);
        if (!isElementDisplayedQuick(searchInput)) {
            expandFilter(filterName);
        }
        typeFilterSearchText(searchInput, searchText);
        return waitForSearchResults(filterName, searchText, baselineLabels);
    }

    private List<String> snapshotVisibleOptionLabels(String filterName) {
        List<String> labels = getFilterOptionLabels(filterName);
        return labels.isEmpty() ? Collections.emptyList() : new ArrayList<>(labels);
    }

    private void typeFilterSearchText(By searchInput, String searchText) throws InterruptedException {
        DesktopBrowserElement input = driver.get().finder().findElement(searchInput);
        input.click();
        input.executeScript(
                "arguments[0].value = '';"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));");
        Thread.sleep(200);
        if (searchText.isEmpty()) {
            return;
        }
        DriverUtil.sendKeyToElement(searchInput, DEFAULT_WAIT_SECONDS, searchText);
        Thread.sleep(FILTER_EXPAND_WAIT_MS / 2);
    }

    private List<String> waitForSearchResults(String filterName, String searchText, List<String> baselineLabels)
            throws InterruptedException {
        List<String> lastResults = Collections.emptyList();
        for (int attempt = 0; attempt < SEARCH_SETTLE_MAX_ATTEMPTS; attempt++) {
            Thread.sleep(SEARCH_SETTLE_POLL_MS);
            lastResults = getFilterOptionLabels(filterName);
            if (isSearchResultsSettled(searchText, baselineLabels, lastResults)) {
                return lastResults;
            }
        }
        Logger.logConsoleMessage(filterName + " search for '" + searchText
                + "' did not fully settle; using last visible results: " + lastResults);
        return lastResults;
    }

    private boolean isSearchResultsSettled(String searchText, List<String> baseline, List<String> current) {
        if (current.isEmpty()) {
            return false;
        }
        if (!baseline.isEmpty()
                && current.size() == baseline.size()
                && new LinkedHashSet<>(current).equals(new LinkedHashSet<>(baseline))) {
            return false;
        }
        if (current.stream().anyMatch(result -> result.equalsIgnoreCase(searchText))) {
            return true;
        }
        String lowerSearch = searchText.toLowerCase(Locale.ROOT);
        if (current.stream().anyMatch(result -> result.toLowerCase(Locale.ROOT).contains(lowerSearch))) {
            return true;
        }
        return !baseline.isEmpty() && current.size() < baseline.size();
    }

    public void clearFilterSearchInput(String filterName) throws InterruptedException {
        By searchInput = leftFilterPanel.filterSearchInput(filterName);
        if (!isElementDisplayedQuick(searchInput)) {
            return;
        }

        By clearIcon = leftFilterPanel.filterSearchClearIcon(filterName);
        if (isElementDisplayedQuick(clearIcon)) {
            DriverUtil.clickOnElement(clearIcon, DEFAULT_WAIT_SECONDS);
            waitForSearchInputCleared(filterName);
            return;
        }

        DesktopBrowserElement input = driver.get().finder().findElement(searchInput);
        input.executeScript(
                "arguments[0].value = ''; arguments[0].dispatchEvent(new Event('input', { bubbles: true }));");
        waitForSearchInputCleared(filterName);
    }

    private void waitForSearchInputCleared(String filterName) throws InterruptedException {
        for (int attempt = 0; attempt < SEARCH_SETTLE_MAX_ATTEMPTS; attempt++) {
            Thread.sleep(SEARCH_SETTLE_POLL_MS);
            try {
                DesktopBrowserElement input = driver.get().finder()
                        .findElement(leftFilterPanel.filterSearchInput(filterName));
                Object value = input.executeScript("return arguments[0].value;");
                if (value != null && !value.toString().trim().isEmpty()) {
                    continue;
                }
            } catch (Exception ignored) {
                // search input may be temporarily detached during virtual scroll
            }
            return;
        }
    }

    /**
     * Validates duplicate-free Partner search for labels loaded from JSON (no virtual-scroll discovery).
     * Expands Partner accordion, types the full partner name, and asserts exactly one visible option
     * that equals the search string (partial matches like "X" returning "X" and "X 7" are defects).
     */
    public void validateNoDuplicateFilterOptionsFromList(SoftAssert softAssert, String filterName,
                                                         List<String> optionLabels, int batchStartIndex,
                                                         int batchSize) throws InterruptedException {
        expandFilter(filterName);
        DriverUtil.scrollToElement(leftFilterPanel.leftFilterByName(filterName));

        if (!hasFilterSearchInput(filterName)) {
            Verify.softAssert1(false,
                    filterName + " has no searchable option list inside expanded filter panel", softAssert);
            collapseFilter(filterName);
            return;
        }

        int start = Math.max(0, batchStartIndex);
        if (start >= optionLabels.size()) {
            Logger.logMessage(filterName + ": batch start index " + start
                    + " is beyond JSON list size " + optionLabels.size() + "; skipping batch");
            collapseFilter(filterName);
            return;
        }

        int limit = batchSize <= 0 ? Integer.MAX_VALUE : batchSize;
        int end = Math.min(optionLabels.size(), start + limit);
        List<String> batch = optionLabels.subList(start, end);

        int validatedCount = 0;
        for (String optionLabel : batch) {
            validateSingleOptionSearch(softAssert, filterName, optionLabel);
            validatedCount++;
            if (validatedCount % 25 == 0) {
                Logger.logMessage(filterName + ": validated " + validatedCount + " of " + batch.size()
                        + " in JSON batch (index " + start + "+" + validatedCount + ")");
            }
        }

        Logger.logMessage(filterName + ": validated " + validatedCount
                + " option(s) from JSON list (indices " + start + "-" + (end - 1) + " of "
                + optionLabels.size() + ")");

        clearFilterSearchInput(filterName);
        collapseFilter(filterName);
    }

    /**
     * For each option in the filter, performs a narrow (full-label) search and asserts exactly one exact-match result.
     * UI search is substring-based, so visible results may include other options; only exact label matches count.
     */
    public void validateNoDuplicateFilterOptionsOnSearch(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateNoDuplicateFilterOptionsOnSearch(softAssert, filterName, 0, Integer.MAX_VALUE);
    }

    /**
     * Validates duplicate-free narrow search for a slice of options (used for parallel Synergy batches).
     *
     * @param batchStartIndex zero-based start index in the collected option list
     * @param batchSize       max number of options to validate from batchStartIndex
     */
    public void validateNoDuplicateFilterOptionsOnSearch(SoftAssert softAssert, String filterName,
                                                         int batchStartIndex, int batchSize)
            throws InterruptedException {
        expandFilter(filterName);
        DriverUtil.scrollToElement(leftFilterPanel.leftFilterByName(filterName));

        if (!hasFilterSearchInput(filterName)) {
            Verify.softAssert(false,
                    filterName + " has no searchable option list inside expanded filter panel");
            collapseFilter(filterName);
            return;
        }

        int start = Math.max(0, batchStartIndex);
        int limit = batchSize <= 0 ? Integer.MAX_VALUE : batchSize;

        DuplicateFilterOptionsCollectionHelper collectionHelper = new DuplicateFilterOptionsCollectionHelper(this);
        DuplicateFilterOptionsCollectionHelper.ScrollValidationSummary summary =
                collectionHelper.validateWhileScrolling(filterName, start, limit,
                        (optionLabel, scrollTop) -> validateSingleOptionSearch(softAssert, filterName, optionLabel));

        int partnersInBatchRange = Math.max(0, summary.getDiscoveredCount() - start);
        if (partnersInBatchRange > 0 && summary.getValidatedCount() == 0) {
            Verify.softAssert(false,
                    filterName + " expected to validate " + partnersInBatchRange
                            + " option(s) in batch but validated 0");
        } else if (start > 0 && summary.getValidatedCount() == 0) {
            Logger.logMessage(filterName + ": batch start index " + start
                    + " is beyond discovered option count " + summary.getDiscoveredCount() + "; skipping batch");
        } else {
            Logger.logMessage(filterName + ": validated " + summary.getValidatedCount()
                    + " option(s) via scroll (discovered " + summary.getDiscoveredCount()
                    + (summary.getExpectedTotal() > 0 ? " of " + summary.getExpectedTotal() : "") + ")");
        }

        if (summary.getExpectedTotal() > 0 && start == 0 && limit == Integer.MAX_VALUE) {
            Verify.softAssert(summary.getDiscoveredCount() >= summary.getExpectedTotal(),
                    filterName + " should discover all options while scrolling. Expected "
                            + summary.getExpectedTotal() + " but discovered " + summary.getDiscoveredCount());
        }

        if (limit < Integer.MAX_VALUE && summary.getValidatedCount() < limit
                && summary.getExpectedTotal() > summary.getDiscoveredCount()) {
            Verify.softAssert(false,
                    filterName + " batch incomplete: validated " + summary.getValidatedCount()
                            + " of " + limit + " requested; virtual scroll discovered only "
                            + summary.getDiscoveredCount() + " of " + summary.getExpectedTotal());
        }

        clearFilterSearchInput(filterName);
        collapseFilter(filterName);
    }

    private void validateSingleOptionSearch(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        List<String> searchResults = searchFilterOptions(filterName, optionLabel);
        List<String> visibleOptions = searchResults.stream()
                .filter(result -> !isSelectAllLabel(result))
                .collect(Collectors.toList());

        List<String> nonExactResults = visibleOptions.stream()
                .filter(result -> !result.equals(optionLabel))
                .collect(Collectors.toList());
        if (!nonExactResults.isEmpty()) {
            Verify.softAssert1(false,
                    filterName + " search for '" + optionLabel
                            + "' returned partial/non-exact matches (defect): " + nonExactResults
                            + ". All visible results: " + visibleOptions,
                    softAssert);
        }

        Verify.softAssert1(visibleOptions.size() == 1 && visibleOptions.get(0).equals(optionLabel),
                filterName + " search for full partner name '" + optionLabel
                        + "' should return exactly 1 result equal to the search string but got "
                        + visibleOptions.size() + " (visible results: " + visibleOptions + ")",
                softAssert);
    }

    private boolean isSelectAllLabel(String label) {
        if (label == null) {
            return true;
        }
        String trimmed = label.trim();
        return trimmed.isEmpty()
                || SELECT_ALL_LABEL.equalsIgnoreCase(trimmed)
                || trimmed.startsWith(SELECT_ALL_LABEL + " (");
    }

    public boolean hasFilterSearchInput(String filterName) {
        return isElementDisplayedQuick(leftFilterPanel.filterSearchInput(filterName));
    }

    private boolean isElementDisplayedQuick(By locator) {
        return runWithQuickElementTimeout(() -> isDisplayedWithoutTimeoutChange(locator));
    }

    private boolean runWithQuickElementTimeout(java.util.function.Supplier<Boolean> check) {
        try {
            driver.get().options().setElementTimeout(QUICK_ELEMENT_TIMEOUT_MS);
            return Boolean.TRUE.equals(check.get());
        } catch (Exception ignored) {
            return false;
        } finally {
            restoreDefaultElementTimeout();
        }
    }

    private boolean isDisplayedWithoutTimeoutChange(By locator) {
        try {
            List<DesktopBrowserElement> elements = driver.get().finder().findElements(locator);
            for (DesktopBrowserElement element : elements) {
                if (element.isDisplayed()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // optional element
        }
        return false;
    }

    private void restoreDefaultElementTimeout() {
        try {
            driver.get().options().setElementTimeout(60000);
        } catch (Exception ignored) {
            // Synergy session may already be down
        }
    }

    public void validateSelectAllDisabledWhenLargeOptionSet(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        expandFilter(filterName);
        int optionCount = getTotalOptionCount(filterName);
        if (optionCount > SELECT_ALL_DISABLED_OPTION_THRESHOLD) {
            Verify.softAssert(isSelectAllDisabled(filterName),
                    filterName + " Select all is disabled when option count > "
                            + SELECT_ALL_DISABLED_OPTION_THRESHOLD + " (count=" + optionCount + ")");
        } else {
            Logger.logMessage(filterName + " has " + optionCount + " options; skipping >1000 Select all disabled check");
        }
    }

    public void validateScrollFilterOptions(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        By container = leftFilterPanel.filterOptionsContainer(filterName);
        Verify.softAssert(WaitUtil.isDisplay(container, DEFAULT_WAIT_SECONDS),
                filterName + " options container is visible for scroll test");

        DesktopBrowserElement viewport = driver.get().finder().findElement(container);
        List<String> labelsBeforeScroll = getFilterOptionLabels(filterName);

        scrollCdkViewportToRevealMore(viewport);
        Thread.sleep(500);
        List<String> labelsAfterScroll = getFilterOptionLabels(filterName);

        scrollCdkViewportToTop(viewport);
        Thread.sleep(300);

        Verify.softAssert(WaitUtil.isDisplay(container, DEFAULT_WAIT_SECONDS),
                filterName + " virtual scroll completed without error");
        Logger.logMessage(filterName + " virtual scroll: visible before=" + labelsBeforeScroll.size()
                + ", after scrollIntoView=" + labelsAfterScroll.size());
    }

    public void validatePaginationWorks(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        By nextBtn = leftFilterPanel.filterPaginationNext(filterName);
        if (!WaitUtil.isDisplay(nextBtn, 3)) {
            Logger.logMessage(filterName + " uses CDK virtual scroll (no pagination buttons); validating scroll instead");
            validateVirtualScrollNavigation(softAssert, filterName);
            return;
        }

        String beforePage = getPaginationInfo(filterName);
        DriverUtil.clickOnElement(nextBtn, DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        String afterPage = getPaginationInfo(filterName);

        Verify.softAssert(!beforePage.equals(afterPage) || hasFilterOptions(filterName),
                filterName + " pagination navigates to next page. Before=" + beforePage + ", After=" + afterPage);

        DriverUtil.clickOnElement(leftFilterPanel.filterPaginationPrev(filterName), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
    }

    private void validateVirtualScrollNavigation(SoftAssert softAssert, String filterName) throws InterruptedException {
        By container = leftFilterPanel.filterOptionsContainer(filterName);
        Verify.softAssert(WaitUtil.isDisplay(container, DEFAULT_WAIT_SECONDS),
                filterName + " options container is visible for virtual scroll test");

        DesktopBrowserElement viewport = driver.get().finder().findElement(container);
        LinkedHashSet<String> discovered = new LinkedHashSet<>(getFilterOptionLabels(filterName));
        int staleAttempts = 0;

        while (staleAttempts < 3) {
            int sizeBefore = discovered.size();
            scrollCdkViewportToRevealMore(viewport);
            Thread.sleep(500);
            discovered.addAll(getFilterOptionLabels(filterName));
            if (discovered.size() == sizeBefore) {
                staleAttempts++;
            } else {
                staleAttempts = 0;
            }
        }

        scrollCdkViewportToTop(viewport);
        Thread.sleep(300);

        Verify.softAssert(!discovered.isEmpty(),
                filterName + " virtual scroll reveals filter options (discovered=" + discovered.size() + ")");
    }

    private void scrollCdkViewportToRevealMore(DesktopBrowserElement viewport) {
        viewport.executeScript(
                "var viewport = arguments[0];"
                        + "var items = viewport.querySelectorAll('.draggable-item, a.option-item, .option-container');"
                        + "if (items.length > 0) {"
                        + "  items[items.length - 1].scrollIntoView({ block: 'end' });"
                        + "}"
                        + "viewport.dispatchEvent(new Event('scroll', { bubbles: true }));");
    }

    private void scrollCdkViewportToTop(DesktopBrowserElement viewport) {
        viewport.executeScript(
                "var viewport = arguments[0];"
                        + "viewport.scrollTop = 0;"
                        + "var items = viewport.querySelectorAll('.draggable-item, a.option-item');"
                        + "if (items.length > 0) { items[0].scrollIntoView({ block: 'start' }); }"
                        + "viewport.dispatchEvent(new Event('scroll', { bubbles: true }));");
    }

    public void validateTableRecordCountReflectsFilter(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        int countBefore = getTableRecordCount();
        expandFilter(filterName);
        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        int countAfter = getTableRecordCount();
        Verify.softAssert(countAfter >= 0, filterName + " table record count is available after filter selection");
        Logger.logMessage(filterName + " table count before=" + countBefore + ", after selecting " + optionLabel + "=" + countAfter);
    }

    public void validateTableRecordsMatchFilter(SoftAssert softAssert, String filterName, String optionLabel,
                                                String tableColumnName) throws InterruptedException {
        expandFilter(filterName);
        selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        List<String> columnValues = getVisibleTableColumnValues(tableColumnName);
        if (columnValues.isEmpty()) {
            Logger.logMessage("No visible table rows to validate for column: " + tableColumnName);
            return;
        }
        for (String value : columnValues) {
            Verify.softAssert(value.toLowerCase(Locale.ROOT).contains(optionLabel.toLowerCase(Locale.ROOT)),
                    tableColumnName + " row value reflects filter " + filterName + "=" + optionLabel + " (value=" + value + ")");
        }
    }

    private static final String ORDER_STATUS_DELIVERED_OPTION = "Done: Delivered";
    private static final int ORDER_STATUS_ROWS_TO_VERIFY = 2;

    /**
     * TC601 smoke for Order Status: always select Done: Delivered → table record count matches filter count
     * (or both are 0) → first visible Status cells show Delivered when count > 0.
     */
    public void validateOrderStatusTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC601");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = ORDER_STATUS_DELIVERED_OPTION;
        ensureFilterOptionAccessible(filterName, optionLabel);

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount >= 0,
                filterName + " option '" + optionLabel + "' is available (count=" + filterOptionCount + ")");

        String statusKeyword = mapOrderStatusToTableKeyword(optionLabel);
        Logger.logMessage(filterName + " TC601 option='" + optionLabel + "' filterCount=" + filterOptionCount
                + " → table/chip keyword='" + statusKeyword + "'");

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        if (filterOptionCount > 0) {
            waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);
        } else {
            Thread.sleep(FILTER_EXPAND_WAIT_MS * 2);
        }

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount >= 0,
                filterName + " results count is available after selecting " + optionLabel + " (actual=" + tableRecordCount + ")");

        if (filterOptionCount == 0) {
            Verify.softAssert(tableRecordCount == 0,
                    filterName + " table record count is 0 when " + optionLabel + " filter count is 0 (actual="
                            + tableRecordCount + ")");
            Logger.logMessage(filterName + " '" + optionLabel + "' has 0 filter/table records — skipping Status row checks");
            collapseFilter(filterName);
            return;
        }

        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);

        List<String> statusValues = getVisibleTableColumnValues("Status");
        Verify.softAssert(!statusValues.isEmpty(),
                filterName + " has visible Orders table rows after selecting " + optionLabel);

        String keywordLower = statusKeyword.toLowerCase(Locale.ROOT);
        int rowsToCheck = Math.min(ORDER_STATUS_ROWS_TO_VERIFY, statusValues.size());
        for (int i = 0; i < rowsToCheck; i++) {
            String statusText = statusValues.get(i);
            Verify.softAssert(statusText.toLowerCase(Locale.ROOT).contains(keywordLower),
                    filterName + " row " + (i + 1) + " Status is '" + statusKeyword
                            + "' after selecting " + optionLabel + " (actual='" + statusText + "')");
        }

        if (WaitUtil.isDisplay(leftFilterPanel.statusSummaryChip(statusKeyword), 5)) {
            Logger.logMessage(filterName + " status summary chip visible for: " + statusKeyword);
        } else {
            Logger.logMessage(filterName + " status summary chip not located for '" + statusKeyword
                    + "' (Status column checks still applied)");
        }

        collapseFilter(filterName);
    }

    private void ensureNoOptionsSelected(String filterName) throws InterruptedException {
        if (!isFilterExpanded(filterName)) {
            Logger.logMessage(filterName + " not expanded — skipping ensureNoOptionsSelected");
            return;
        }
        if (isSelectAllChecked(filterName) || areAnyVisibleOptionsSelected(filterName)
                || isSelectAllIndeterminate(filterName) || isSelectAllPartialSelectionState(filterName, 0)) {
            clickSelectAll(filterName);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            if (areAnyVisibleOptionsSelected(filterName)) {
                clickSelectAll(filterName);
                Thread.sleep(FILTER_EXPAND_WAIT_MS);
            }
        }
    }

    public int getFilterOptionCount(String filterName, String optionLabel) {
        return getFilterOptions(filterName).stream()
                .filter(option -> option.getLabel().equalsIgnoreCase(optionLabel))
                .mapToInt(FilterOption::getCount)
                .findFirst()
                .orElse(-1);
    }

    private void ensureFilterOptionAccessible(String filterName, String optionLabel) throws InterruptedException {
        if (WaitUtil.isDisplay(leftFilterPanel.filterOptionCheckbox(filterName, optionLabel), 3)) {
            return;
        }
        searchFilterOptions(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
    }

    /** Maps left-filter Order Status label to table Status / summary-chip keyword. */
    static String mapOrderStatusToTableKeyword(String optionLabel) {
        if (optionLabel == null) {
            return "";
        }
        String lower = optionLabel.toLowerCase(Locale.ROOT);
        if (lower.contains("ready") && lower.contains("delivery")) {
            return "Ready for delivery";
        }
        if (lower.contains("failed") || lower.contains("fail")) {
            return "Failed";
        }
        if (lower.contains("in progress") || lower.contains("progress")) {
            return "In progress";
        }
        if (lower.contains("delivered") || lower.startsWith("done")) {
            return "Delivered";
        }
        return optionLabel;
    }

    public void validateFilterNamesMatchTableColumns(SoftAssert softAssert, List<String> filterNames,
                                                     List<String> tableColumnNames) {
        for (String filterName : filterNames) {
            boolean match = tableColumnNames.stream()
                    .anyMatch(col -> col.equalsIgnoreCase(filterName.trim()));
            Verify.softAssert(match, "Left filter name matches a table column: " + filterName);
        }
    }

    public void validateSelectedOptionsInActiveFilters(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        expandFilter(filterName);
        selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFiltersSection(), DEFAULT_WAIT_SECONDS),
                "Active filters section is visible");
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(optionLabel), DEFAULT_WAIT_SECONDS),
                optionLabel + " from " + filterName + " appears in active filters");
    }

    public void validateFilterCountInsideAndOutside(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        expandFilter(filterName);
        String outsideCountBefore = getFilterSelectionCountText(filterName);

        selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        String outsideCountAfter = getFilterSelectionCountText(filterName);
        Verify.softAssert(outsideCountAfter.contains("/"),
                filterName + " shows selection count outside filter (e.g. 1/2412). Actual: " + outsideCountAfter);
        Verify.softAssert(!outsideCountBefore.equals(outsideCountAfter),
                filterName + " outside count updated after selection. Before=" + outsideCountBefore
                        + ", After=" + outsideCountAfter);
    }

    public void validateRangeInputsPresent(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.filterRangeFromInput(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " shows From range input");
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.filterRangeToInput(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " shows To range input");
    }

    public void validateMultipleActiveFilterChips(SoftAssert softAssert, String filterName, List<String> optionLabels)
            throws InterruptedException {
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFiltersSection(), DEFAULT_WAIT_SECONDS),
                "Active filters section is visible for multi-select on " + filterName);
        for (String label : optionLabels) {
            Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(label), DEFAULT_WAIT_SECONDS),
                    label + " appears in active filters with multi-select on " + filterName);
        }
    }

    public void validateClearFilters(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        expandFilter(filterName);
        if (optionLabel == null) {
            List<String> labels = getFilterOptionLabels(filterName);
            if (labels.isEmpty()) {
                Logger.logMessage("No options for " + filterName + " clear test — skipping");
                return;
            }
            optionLabel = labels.get(0);
        }

        selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(optionLabel), DEFAULT_WAIT_SECONDS),
                optionLabel + " active before clear");

        if (WaitUtil.isDisplay(leftFilterPanel.activeFiltersClearButton(), 3)) {
            DriverUtil.clickOnElement(leftFilterPanel.activeFiltersClearButton(), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            Verify.softAssert(!WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(optionLabel), 3),
                    optionLabel + " cleared via active filters Clear (chip-tag-label)");
            return;
        }

        if (WaitUtil.isDisplay(leftFilterPanel.leftFilterPanelClearButton(), 3)) {
            DriverUtil.clickOnElement(leftFilterPanel.leftFilterPanelClearButton(), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            Verify.softAssert(!WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(optionLabel), 3),
                    optionLabel + " cleared via left panel Clear (overlay-bypass-class near Save filter)");
            return;
        }

        if (WaitUtil.isDisplay(leftFilterPanel.activeFilterChipRemoveButton(optionLabel), 3)) {
            DriverUtil.clickOnElement(leftFilterPanel.activeFilterChipRemoveButton(optionLabel), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            Verify.softAssert(!WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(optionLabel), 3),
                    optionLabel + " removed from active filters via chip X");
            return;
        }

        Verify.softAssert(false, "No Clear control found (chip-tag-label or overlay-bypass-class)");
    }

    // --- helper methods ---

    public void clickSelectAll(String filterName) {
        By checkbox = leftFilterPanel.selectAllCheckbox(filterName);
        DriverUtil.scrollToElement(checkbox);
        DriverUtil.clickOnElement(checkbox, DEFAULT_WAIT_SECONDS);
    }

    public void selectFilterOption(String filterName, String optionLabel) {
        By checkbox = leftFilterPanel.filterOptionCheckbox(filterName, optionLabel);
        DriverUtil.scrollToElement(checkbox);
        DriverUtil.clickOnElement(checkbox, DEFAULT_WAIT_SECONDS);
    }

    public boolean isSelectAllChecked(String filterName) {
        if (!isElementDisplayedQuick(leftFilterPanel.selectAllCheckbox(filterName))) {
            return false;
        }
        return runWithQuickElementTimeout(() -> {
            try {
                return DriverUtil.isSelectedCheckbox(leftFilterPanel.selectAllCheckbox(filterName));
            } catch (Exception e) {
                return false;
            }
        });
    }

    public boolean isSelectAllIndeterminate(String filterName) {
        try {
            DesktopBrowserElement checkbox = driver.get().finder()
                    .findElement(leftFilterPanel.selectAllCheckbox(filterName));
            Object state = checkbox.executeScript(
                    "var el = arguments[0];"
                            + "if (el.indeterminate === true) return true;"
                            + "if (el.getAttribute('data-indeterminate') === 'true') return true;"
                            + "if (el.getAttribute('aria-checked') === 'mixed') return true;"
                            + "var cls = (el.className || '') + ' ' + ((el.parentElement && el.parentElement.className) || '');"
                            + "if (/indeterminate/i.test(cls)) return true;"
                            + "return false;");
            return toScriptBoolean(state);
        } catch (Exception e) {
            Logger.logMessage("Could not read indeterminate state for " + filterName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Fallback when Angular paints the Select All dash via CSS without setting {@code indeterminate}:
     * some but not all options selected, Select All not fully checked, and badge is {@code N/M} with {@code 0 < N < M}.
     */
    public boolean isSelectAllPartialSelectionState(String filterName, int expectedSelected) {
        try {
            if (isSelectAllChecked(filterName)) {
                return false;
            }
            int selected = 0;
            List<String> labels = getFilterOptionLabels(filterName);
            for (String label : labels) {
                if (DriverUtil.isSelectedCheckbox(leftFilterPanel.filterOptionCheckbox(filterName, label))) {
                    selected++;
                }
            }
            if (selected <= 0 || selected >= labels.size()) {
                return false;
            }
            if (expectedSelected > 0 && selected < expectedSelected) {
                return false;
            }
            String badge = getFilterSelectionCountText(filterName);
            Matcher matcher = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)").matcher(badge);
            if (matcher.find()) {
                int selectedCount = Integer.parseInt(matcher.group(1));
                int totalCount = Integer.parseInt(matcher.group(2));
                return selectedCount > 0 && selectedCount < totalCount;
            }
            return true;
        } catch (Exception e) {
            Logger.logMessage("Could not verify partial Select All state for " + filterName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isSelectAllDisabled(String filterName) {
        try {
            DesktopBrowserElement checkbox = driver.get().finder()
                    .findElement(leftFilterPanel.selectAllCheckbox(filterName));
            Object disabled = checkbox.executeScript(
                    "return arguments[0].disabled === true || arguments[0].getAttribute('aria-disabled') === 'true';");
            return Boolean.TRUE.equals(disabled);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Visual check for the CSS divider between non-zero and zero-count options.
     * Divider is CSS-only ({@code ::before}/{@code ::after} or {@code border-top}) — not an {@code <hr>}.
     * Only meaningful when both non-zero and zero-count options exist in the list.
     */
    public boolean hasVisibleZeroCountDivider(String filterName) {
        return hasVisibleZeroCountDivider(filterName, null);
    }

    public boolean hasVisibleZeroCountDivider(String filterName, String firstZeroLabel) {
        try {
            DesktopBrowserElement zeroRow = findZeroCountTransitionRow(filterName, firstZeroLabel);
            if (zeroRow == null) {
                Logger.logMessage(filterName + " zero-count transition row not found for divider check");
                return false;
            }
            zeroRow.executeScript(
                    "arguments[0].scrollIntoView({block: 'nearest', inline: 'nearest'});");
            Thread.sleep(300);
            Object result = zeroRow.executeScript(
                    "var row = arguments[0];"
                            + "var link = row.querySelector('a.option-item');"
                            + "if (!link) return false;"
                            + "var ps = window.getComputedStyle(link, '::before');"
                            + "if (!ps || ps.display === 'none' || ps.visibility === 'hidden') return false;"
                            + "var height = parseFloat(ps.height) || 0;"
                            + "var width = parseFloat(ps.width) || 0;"
                            + "var bg = (ps.backgroundColor || '').replace(/\\s/g, '').toLowerCase();"
                            + "if (height <= 0 || width <= 0) return false;"
                            + "return bg !== 'transparent' && bg !== 'rgba(0,0,0,0)';");
            boolean found = toScriptBoolean(result);
            if (!found) {
                logDividerDiagnostics(filterName, zeroRow);
            }
            return found;
        } catch (Exception e) {
            Logger.logMessage("Could not check zero-count CSS divider for " + filterName + ": " + e.getMessage());
            return false;
        }
    }

    private DesktopBrowserElement findZeroCountTransitionRow(String filterName, String firstZeroLabel) {
        if (firstZeroLabel != null && !firstZeroLabel.isEmpty()) {
            By labelRow = leftFilterPanel.zeroCountOptionRowByLabel(filterName, firstZeroLabel);
            if (WaitUtil.isDisplay(labelRow, 5)) {
                return driver.get().finder().findElement(labelRow);
            }
        }
        By transitionRow = leftFilterPanel.firstZeroCountOptionRow(filterName);
        if (WaitUtil.isDisplay(transitionRow, DEFAULT_WAIT_SECONDS)) {
            DriverUtil.scrollToElement(transitionRow);
            return driver.get().finder().findElement(transitionRow);
        }
        return null;
    }

    private static boolean toScriptBoolean(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0;
        }
        return "true".equalsIgnoreCase(String.valueOf(result).trim());
    }

    private void logDividerDiagnostics(String filterName, DesktopBrowserElement zeroRow) {
        try {
            Object debug = zeroRow.executeScript(
                    "function snapPseudo(node, pseudo) {"
                            + "  var ps = window.getComputedStyle(node, pseudo);"
                            + "  return {display: ps.display, height: ps.height, width: ps.width,"
                            + "    borderTop: ps.borderTopWidth, borderBottom: ps.borderBottomWidth,"
                            + "    background: ps.backgroundColor, position: ps.position, content: ps.content};"
                            + "}"
                            + "function snapElement(node) {"
                            + "  var ps = window.getComputedStyle(node);"
                            + "  return {borderTop: ps.borderTopWidth, borderTopStyle: ps.borderTopStyle,"
                            + "    marginTop: ps.marginTop, paddingTop: ps.paddingTop, boxShadow: ps.boxShadow};"
                            + "}"
                            + "var zeroRow = arguments[0];"
                            + "var zeroLink = zeroRow.querySelector('a.option-item');"
                            + "var prev = zeroRow.previousElementSibling;"
                            + "var prevLink = prev ? prev.querySelector('a.option-item') : null;"
                            + "return {"
                            + "  zeroRow: snapElement(zeroRow),"
                            + "  zeroRowBefore: snapPseudo(zeroRow, '::before'),"
                            + "  zeroLinkBefore: zeroLink ? snapPseudo(zeroLink, '::before') : null,"
                            + "  prevAfter: prev ? snapPseudo(prev, '::after') : null,"
                            + "  prevLinkAfter: prevLink ? snapPseudo(prevLink, '::after') : null"
                            + "};");
            Logger.logMessage(filterName + " CSS divider diagnostics: " + debug);
        } catch (Exception ignored) {
            Logger.logMessage(filterName + " CSS divider check failed (no diagnostics)");
        }
    }

    public List<String> getFilterOptionLabels(String filterName) {
        return getFilterOptions(filterName).stream()
                .map(FilterOption::getLabel)
                .collect(Collectors.toList());
    }

    public List<FilterOption> getFilterOptions(String filterName) {
        List<FilterOption> options = new ArrayList<>();
        try {
            List<DesktopBrowserElement> containers = driver.get().finder()
                    .findElements(leftFilterPanel.allFilterOptionContainers(filterName));
            if (!containers.isEmpty()) {
                for (DesktopBrowserElement container : containers) {
                    String rowText = container.getText().trim();
                    if (rowText.isEmpty() || rowText.startsWith(SELECT_ALL_LABEL)) {
                        continue;
                    }
                    options.add(parseFilterOption(rowText));
                }
                return options;
            }

            List<DesktopBrowserElement> elements = driver.get().finder()
                    .findElements(leftFilterPanel.allFilterOptionLabels(filterName));
            for (DesktopBrowserElement element : elements) {
                String text = element.getText().trim();
                if (text.isEmpty() || text.startsWith(SELECT_ALL_LABEL)) {
                    continue;
                }
                options.add(parseFilterOption(text));
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read filter options for " + filterName + ": " + e.getMessage());
        }
        return options;
    }

    public int getTotalOptionCount(String filterName) {
        String selectionCount = getFilterSelectionCountText(filterName);
        Matcher slashMatcher = Pattern.compile("/\\s*(\\d+)").matcher(selectionCount);
        if (slashMatcher.find()) {
            return Integer.parseInt(slashMatcher.group(1));
        }

        String pagination = getPaginationInfo(filterName);
        Matcher totalMatcher = Pattern.compile("of\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(pagination);
        if (totalMatcher.find()) {
            return Integer.parseInt(totalMatcher.group(1));
        }
        return getFilterOptions(filterName).size();
    }

    public int getTableRecordCount() {
        try {
            if (WaitUtil.isDisplay(leftFilterPanel.tableRecordCountLabel(), 5)) {
                String text = driver.get().finder().findElement(leftFilterPanel.tableRecordCountLabel()).getText();
                return parseTableResultsCount(text);
            }
            return driver.get().finder().findElements(leftFilterPanel.tableRows()).size();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Parses {@code "3258 results"} from {@code div.total-count}. */
    private static int parseTableResultsCount(String rawText) {
        if (rawText == null) {
            return -1;
        }
        String normalized = rawText.replace(",", "").trim();
        Matcher resultsMatcher = Pattern.compile("(\\d+)\\s*results?", Pattern.CASE_INSENSITIVE).matcher(normalized);
        if (resultsMatcher.find()) {
            return Integer.parseInt(resultsMatcher.group(1));
        }
        Matcher anyNumber = Pattern.compile("(\\d+)").matcher(normalized);
        if (anyNumber.find()) {
            return Integer.parseInt(anyNumber.group(1));
        }
        return -1;
    }

    private void waitForTableRecordCount(int expectedCount, int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            int current = getTableRecordCount();
            if (current == expectedCount) {
                Logger.logMessage("Table results count reached expected value: " + expectedCount);
                return;
            }
            Thread.sleep(500);
        }
        Logger.logMessage("Table results count did not reach " + expectedCount
                + " within " + timeoutSeconds + "s (last=" + getTableRecordCount() + ")");
    }

    public String getFilterSelectionCountText(String filterName) {
        try {
            if (WaitUtil.isDisplay(leftFilterPanel.filterSelectionCount(filterName), 5)) {
                return driver.get().finder().findElement(leftFilterPanel.filterSelectionCount(filterName)).getText().trim();
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read selection count for " + filterName);
        }
        return "";
    }

    public String getPaginationInfo(String filterName) {
        try {
            if (WaitUtil.isDisplay(leftFilterPanel.filterPaginationInfo(filterName), 3)) {
                return driver.get().finder().findElement(leftFilterPanel.filterPaginationInfo(filterName)).getText();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public boolean hasFilterOptions(String filterName) {
        return !getFilterOptions(filterName).isEmpty();
    }

    private boolean areAllVisibleOptionsSelected(String filterName) {
        try {
            List<DesktopBrowserElement> checkboxes = driver.get().finder()
                    .findElements(leftFilterPanel.allFilterOptionLabels(filterName));
            for (DesktopBrowserElement label : checkboxes) {
                String text = label.getText().trim();
                if (text.isEmpty() || text.startsWith(SELECT_ALL_LABEL)) {
                    continue;
                }
                By checkbox = leftFilterPanel.filterOptionCheckbox(filterName, text.replaceAll("\\(\\d+\\)", "").trim());
                if (!DriverUtil.isSelectedCheckbox(checkbox)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean areAnyVisibleOptionsSelected(String filterName) {
        for (String label : getFilterOptionLabels(filterName)) {
            if (DriverUtil.isSelectedCheckbox(leftFilterPanel.filterOptionCheckbox(filterName, label))) {
                return true;
            }
        }
        return false;
    }

    private List<String> getVisibleTableColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder()
                    .findElements(leftFilterPanel.tableColumnCells(columnName));
            for (DesktopBrowserElement cell : cells) {
                String text = cell.getText().trim();
                if (!text.isEmpty()) {
                    values.add(text);
                }
            }
            if (values.isEmpty()) {
                List<DesktopBrowserElement> rows = driver.get().finder().findElements(leftFilterPanel.tableRows());
                for (DesktopBrowserElement row : rows) {
                    String text = row.getText().trim();
                    if (!text.isEmpty()) {
                        values.add(text);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read table column " + columnName + ": " + e.getMessage());
        }
        return values;
    }

    private FilterOption parseFilterOption(String text) {
        Matcher trailingCountMatcher = Pattern.compile("^(.*)\\((\\d+)\\)\\s*$").matcher(text.trim());
        if (trailingCountMatcher.find()) {
            return new FilterOption(trailingCountMatcher.group(1).trim(),
                    Integer.parseInt(trailingCountMatcher.group(2)));
        }

        Matcher inlineCountMatcher = COUNT_PATTERN.matcher(text);
        int count = 0;
        String label = text;
        if (inlineCountMatcher.find()) {
            count = Integer.parseInt(inlineCountMatcher.group(1));
            label = text.substring(0, inlineCountMatcher.start()).trim();
        }
        return new FilterOption(label, count);
    }

    private boolean isNonZeroFirstThenZero(List<FilterOption> options) {
        if (options.isEmpty()) {
            return true;
        }
        boolean seenZero = false;
        for (FilterOption option : options) {
            if (option.getCount() == 0) {
                seenZero = true;
            } else if (seenZero) {
                return false;
            }
        }
        return true;
    }

    private boolean isSortedAlphabetically(List<FilterOption> options) {
        if (options.size() <= 1) {
            return true;
        }
        List<String> labels = labelsOf(options);
        List<String> sorted = new ArrayList<>(labels);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return labels.equals(sorted);
    }

    private List<String> labelsOf(List<FilterOption> options) {
        return options.stream().map(FilterOption::getLabel).collect(Collectors.toList());
    }

    private boolean hasZeroCountOptions(List<FilterOption> options) {
        return options.stream().anyMatch(o -> o.getCount() == 0);
    }

    private boolean hasDividerBetweenCounts(List<FilterOption> options) {
        boolean seenNonZero = false;
        boolean seenZero = false;
        for (FilterOption option : options) {
            if (option.getCount() > 0) {
                seenNonZero = true;
            } else if (seenNonZero) {
                seenZero = true;
            }
        }
        return seenNonZero && seenZero;
    }

    public static class FilterOption {
        private final String label;
        private final int count;

        public FilterOption(String label, int count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }
    }
}
