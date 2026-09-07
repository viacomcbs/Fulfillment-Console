package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.WaitUtils;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.pageobjects.LineItemsMainTablePage;
import com.paramount.test.ff.pageobjects.NavigationPage;
import com.paramount.test.ff.pageobjects.OrdersMainTablePage;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.paramount.test.ff.pageobjects.TableView;
import com.paramount.test.ff.uitests.helpers.duplicatefilteroptions.DuplicateFilterOptionsCollectionHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsDuplicateAnalyzer;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsGraphqlResponseParser;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsNetworkCaptureHelper;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final OrdersMainTablePage ordersMainTablePage = new OrdersMainTablePage();
    private final LineItemsMainTablePage lineItemsMainTablePage = new LineItemsMainTablePage();
    private final TableView tableView = new TableView();
    private final PtsPackagingIdPage ptsPackagingIdPage = new PtsPackagingIdPage();
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
     * Expands the left filter side nav when it is collapsed ({@code filter-panel-closed}).
     * Clicks the vertical left wall ({@code div.main-body}) — not only the funnel icon.
     * No-op when the panel is already open.
     */
    public void ensureLeftFilterPanelOpen() throws InterruptedException {
        if (isLeftFilterPanelOpenViaDom() && waitForFilterListSection(3)) {
            return;
        }

        Logger.logMessage("Left filter panel is collapsed — clicking vertical left wall (main-body) to expand");
        for (int attempt = 1; attempt <= 5; attempt++) {
            if (clickFilterPanelExpandViaScript() || clickFilterPanelExpandViaLocator()) {
                Thread.sleep(FILTER_EXPAND_WAIT_MS);
            }
            if (isLeftFilterPanelOpenViaDom() && waitForFilterListSection(8)) {
                Logger.logMessage("Left filter panel expanded (attempt " + attempt + ")");
                return;
            }
            Thread.sleep(800);
        }
        Verify.softAssert(isLeftFilterPanelOpenViaDom(),
                "Left filter panel expanded after clicking collapsed left wall");
    }

    /** Polls for accordion headers after side nav opens — scrolls filter list when header is below fold. */
    public boolean waitForFilterHeaderVisible(String filterName, int waitSec) throws InterruptedException {
        By header = leftFilterPanel.leftFilterByName(filterName);
        long deadline = System.currentTimeMillis() + (waitSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            scrollFilterListTowardFilter(filterName);
            if (WaitUtil.isDisplayFast(header, 1)) {
                DriverUtil.scrollToElement(header);
                return true;
            }
            ensureLeftFilterPanelOpen();
            Thread.sleep(500);
        }
        scrollFilterListTowardFilter(filterName);
        return WaitUtil.isDisplayFast(header, 1);
    }

    private void scrollFilterListTowardFilter(String filterName) {
        try {
            String escapedName = filterName.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    "(function(){"
                            + "var name='" + escapedName + "';"
                            + "function findLabel(){"
                            + "  var labels=document.querySelectorAll('msc-left-filter-panel span.accordion-label');"
                            + "  for(var i=0;i<labels.length;i++){"
                            + "    var t=(labels[i].textContent||'').trim();"
                            + "    if(t===name||t.indexOf(name)>=0){return labels[i];}"
                            + "  }"
                            + "  return null;"
                            + "}"
                            + "var el=findLabel();"
                            + "if(el){el.scrollIntoView({block:'center'});return;}"
                            + "var scrollables=document.querySelectorAll("
                            + "'msc-left-filter-panel .filter-list-section,"
                            + " msc-left-filter-panel cdk-virtual-scroll-viewport,"
                            + " msc-left-filter-panel .sideNav,"
                            + " msc-left-filter-panel .filter-panel-body');"
                            + "for(var s=0;s<scrollables.length;s++){"
                            + "  var node=scrollables[s];"
                            + "  node.scrollTop=Math.min(node.scrollTop+300,node.scrollHeight);"
                            + "}"
                            + "})();");
            Thread.sleep(300);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll filter list toward " + filterName + ": " + e.getMessage());
        }
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
                            + "function clickEl(el){"
                            + "if(!el)return false;"
                            + "el.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));"
                            + "el.click();"
                            + "return true;"
                            + "}"
                            + "if(clickEl(p.querySelector('.main-body')))return true;"
                            + "if(clickEl(p.querySelector('.filterIconDivClosed.show-block')))return true;"
                            + "var icon=p.querySelector('.filter-panel-closed i.bi-filter');"
                            + "if(icon)return clickEl(icon.closest('div')||icon);"
                            + "return clickEl(p);");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean clickFilterPanelExpandViaLocator() {
        By collapsedWall = leftFilterPanel.filterPanelCollapsedWall();
        if (clickFilterPanelExpandTarget(collapsedWall)) {
            return true;
        }
        By container = leftFilterPanel.filterPanelExpandContainer();
        if (clickFilterPanelExpandTarget(container)) {
            return true;
        }
        return clickFilterPanelExpandTarget(leftFilterPanel.filterPanelExpandIcon());
    }

    private boolean clickFilterPanelExpandTarget(By target) {
        if (!WaitUtil.isDisplay(target, 3)) {
            return false;
        }
        DriverUtil.scrollToElement(target);
        if (DriverUtil.clickOnElement(target, DEFAULT_WAIT_SECONDS)) {
            return true;
        }
        return DriverUtil.clickOnElementJs(target, 5);
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

        List<FilterOption> nonZero = visible.stream()
                .filter(o -> o.getCount() > 0 && !isAmbiguousFilterOptionLabel(o.getLabel()))
                .collect(Collectors.toList());
        List<FilterOption> zero = visible.stream().filter(o -> o.getCount() == 0).collect(Collectors.toList());

        // Assigned To: Unassigned is pinned directly below Select all — not sorted A-Z with person names.
        List<FilterOption> nonZeroForAlpha = nonZero.stream()
                .filter(o -> !isUnassignedFilterOption(o.getLabel()))
                .collect(Collectors.toList());
        FilterOption unassignedOption = nonZero.stream()
                .filter(o -> isUnassignedFilterOption(o.getLabel()))
                .findFirst()
                .orElse(null);
        if (unassignedOption != null && !nonZero.isEmpty()
                && !isUnassignedFilterOption(nonZero.get(0).getLabel())) {
            Verify.softAssert(false,
                    filterName + " Unassigned (when present with count > 0) should be first non-zero option below Select all. Actual: "
                            + labelsOf(nonZero));
        }

        if (nonZeroForAlpha.size() > 1) {
            Verify.softAssert(isSortedAlphabetically(nonZeroForAlpha),
                    filterName + " first visible non-zero person options are alphabetical. Actual: "
                            + labelsOf(nonZeroForAlpha));
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

        List<String> partialLabels = resolvePartialSelectLabelsForIndeterminate(filterName, optionsToSelect);
        Verify.softAssert(!partialLabels.isEmpty(),
                filterName + " has at least one option for partial selection test");

        for (String label : partialLabels) {
            selectFilterOption(filterName, label);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }

        int selectedForAssertion = partialLabels.size();
        boolean indeterminate = isSelectAllIndeterminate(filterName)
                || isSelectAllPartialSelectionState(filterName, selectedForAssertion);
        Verify.softAssert(indeterminate,
                filterName + " Select all shows indeterminate/partial state when partial options selected"
                        + " (partial=" + partialLabels + ", badge=" + getFilterSelectionCountText(filterName) + ")");

        clickSelectAll(filterName);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        Verify.softAssert(!isSelectAllChecked(filterName),
                filterName + " Select all deselects all options when clicked in indeterminate state");
        Verify.softAssert(!areAnyVisibleOptionsSelected(filterName),
                filterName + " no visible options remain selected after indeterminate Select all click");
    }

    /**
     * Flag filter: {@code Is flagged} cascades to all reason children, so picking the first two visible labels
     * ({@code Is not flagged} + {@code Is flagged}) selects everything — Select All is fully checked, not indeterminate.
     * Use exactly one partial option instead.
     */
    private List<String> resolvePartialSelectLabelsForIndeterminate(String filterName, int optionsToSelect)
            throws InterruptedException {
        if (isFlagFilter(filterName)) {
            String single = resolveFlagPartialSelectLabelForIndeterminate(filterName);
            if (single == null) {
                return Collections.emptyList();
            }
            Logger.logMessage(filterName + " indeterminate Select All — partial option: " + single);
            return Collections.singletonList(single);
        }
        List<String> labels = getFilterOptionLabels(filterName);
        if (labels.size() < optionsToSelect) {
            return Collections.emptyList();
        }
        return labels.subList(0, optionsToSelect);
    }

    private static boolean isFlagFilter(String filterName) {
        return FlagFilterConstants.FILTER_DISPLAY_NAME.equalsIgnoreCase(filterName);
    }

    /** Prefer {@code Is not flagged}; else first non-zero flagged child; else {@code Is flagged}. */
    private String resolveFlagPartialSelectLabelForIndeterminate(String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<String> labels = getFilterOptionLabels(filterName);
        if (labels.contains(FlagFilterConstants.IS_NOT_FLAGGED)) {
            return FlagFilterConstants.IS_NOT_FLAGGED;
        }
        for (String child : FlagFilterConstants.FLAGGED_CHILD_OPTIONS) {
            if (labels.contains(child) && getFilterOptionCount(filterName, child) > 0) {
                return child;
            }
        }
        if (labels.contains(FlagFilterConstants.IS_FLAGGED)) {
            return FlagFilterConstants.IS_FLAGGED;
        }
        return labels.isEmpty() ? null : labels.get(0);
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

    private static final String ORDER_STATUS_TABLE_COLUMN = "Status";

    /**
     * TC601 smoke for Order Status (Brand-style): first non-zero option → filter count matches table record count
     * → every visible Status column cell matches the mapped table keyword (e.g. Done: Delivered → Delivered).
     */
    public void validateOrderStatusTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC601");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no non-zero filter options — skipping TC601");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");

        String statusKeyword = mapOrderStatusToTableKeyword(optionLabel);
        Logger.logMessage(filterName + " TC601 option='" + optionLabel + "' filterCount=" + filterOptionCount
                + " → table/chip keyword='" + statusKeyword + "'");

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);

        List<String> statusValues = getVisibleTableColumnValues(ORDER_STATUS_TABLE_COLUMN);
        Verify.softAssert(!statusValues.isEmpty(),
                filterName + " has visible Status column values after selecting " + optionLabel);

        String keywordLower = statusKeyword.toLowerCase(Locale.ROOT);
        for (int i = 0; i < statusValues.size(); i++) {
            String statusText = statusValues.get(i);
            Verify.softAssert(statusText.toLowerCase(Locale.ROOT).contains(keywordLower),
                    filterName + " row " + (i + 1) + " Status is '" + statusKeyword
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + statusText + "')");
        }
        Logger.logMessage(filterName + " Status column OK — all " + statusValues.size()
                + " visible row(s) match keyword '" + statusKeyword + "' for '" + optionLabel + "'");

        if (WaitUtil.isDisplay(leftFilterPanel.statusSummaryChip(statusKeyword), 5)) {
            Logger.logMessage(filterName + " status summary chip visible for: " + statusKeyword);
        } else {
            Logger.logMessage(filterName + " status summary chip not located for '" + statusKeyword
                    + "' (Status column checks still applied)");
        }

        collapseFilter(filterName);
    }

    /**
     * TC613 smoke for Brand: first non-zero option (skips literal {@code All}) → filter count matches
     * table record count → every visible Brand column cell on the Orders grid matches that brand
     * (order-level column; no row expand — same grid read pattern as {@link #validateTableRecordsMatchFilter}).
     */
    public void validateBrandTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC613");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC613");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage(filterName + " TC613 option='" + optionLabel + "' filterCount=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount >= 0,
                filterName + " results count available after selecting " + optionLabel
                        + " (actual=" + tableRecordCount + ")");
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);

        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.BRAND);
        List<String> brandValues = getVisibleTableColumnValues(ManageColumnOptions.BRAND);
        Logger.logMessage(filterName + " TC613 Brand column values fetched (" + brandValues.size()
                + " visible cell(s)): " + brandValues);
        Verify.softAssert(!brandValues.isEmpty(),
                filterName + " has visible Brand column values after selecting " + optionLabel
                        + " (scroll Brand column into view on Orders grid)");

        for (int i = 0; i < brandValues.size(); i++) {
            String brandText = brandValues.get(i);
            Verify.softAssert(brandCellMatchesSelectedOption(brandText, optionLabel),
                    filterName + " row " + (i + 1) + " Brand is '" + optionLabel
                            + "' after filter selection (actual='" + brandText + "')");
        }
        Logger.logMessage(filterName + " Brand column OK — all " + brandValues.size()
                + " visible row(s) match '" + optionLabel + "'");

        collapseFilter(filterName);
    }

    /**
     * TC609 smoke for Assigned To: when a named person has count &gt; 0, filter count matches table record count
     * and every visible Assigned to cell shows initials (same rule as Submitted by).
     * When no named person has count &gt; 0, selects {@code Unassigned} (below Select all) and verifies all
     * visible Assigned to cells are blank.
     */
    public void validateAssignedToTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC609");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String personOption = resolveFirstNonZeroPersonFilterOption(filterName);
        if (personOption != null) {
            validateAssignedToPersonTableSync(softAssert, filterName, personOption);
        } else {
            String unassignedOption = resolveUnassignedFilterOption(filterName);
            if (unassignedOption == null) {
                Logger.logMessage(filterName + " has no non-zero person option and no Unassigned option — skipping TC609");
                collapseFilter(filterName);
                return;
            }
            validateAssignedToUnassignedTableSync(softAssert, filterName, unassignedOption);
        }

        collapseFilter(filterName);
    }

    private void validateAssignedToPersonTableSync(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " person option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        String expectedInitials = toSubmittedByTableInitials(optionLabel);
        Logger.logMessage(filterName + " TC609 person option='" + optionLabel + "' filterCount=" + filterOptionCount
                + " → expected table initials='" + expectedInitials + "'");

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");

        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.ASSIGNED_TO);
        List<String> assignedToValues = getVisibleTableColumnValues(ManageColumnOptions.ASSIGNED_TO);
        Verify.softAssert(!assignedToValues.isEmpty(),
                filterName + " has visible Assigned to column values after selecting " + optionLabel);

        for (int i = 0; i < assignedToValues.size(); i++) {
            String cellText = assignedToValues.get(i);
            Verify.softAssert(assignedToCellMatchesSelectedPerson(cellText, optionLabel),
                    filterName + " row " + (i + 1) + " Assigned to initials are '" + expectedInitials
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Assigned to column OK — all " + assignedToValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");
    }

    private void validateAssignedToUnassignedTableSync(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " Unassigned option has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage(filterName + " TC609 Unassigned option filterCount=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches Unassigned filter count"
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");

        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.ASSIGNED_TO);
        // Include blank cells — Unassigned orders have an Assigned to column with no initials/name.
        List<String> assignedToValues = getVisibleTableColumnValues(ManageColumnOptions.ASSIGNED_TO, true);
        Verify.softAssert(!assignedToValues.isEmpty(),
                filterName + " Assigned to column is visible on Orders grid after selecting Unassigned"
                        + " (cells may be blank when no assignee)");

        for (int i = 0; i < assignedToValues.size(); i++) {
            String cellText = assignedToValues.get(i);
            Verify.softAssert(assignedToCellIsBlank(cellText),
                    filterName + " row " + (i + 1) + " Assigned to is blank for Unassigned filter"
                            + " (actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Assigned to column OK — all " + assignedToValues.size()
                + " visible row(s) are blank for Unassigned filter");
    }

    /** First checkbox option with count &gt; 0, excluding {@code All} and {@code Unassigned}. */
    private String resolveFirstNonZeroPersonFilterOption(String filterName) throws InterruptedException {
        List<String> personOptions = resolveAllNonZeroPersonFilterOptions(filterName);
        if (personOptions.isEmpty()) {
            return null;
        }
        return LeftFilterOptionRotationUtil.pickNext(filterName + "#person", personOptions, 1).getFirstOrNull();
    }

    private List<String> resolveAllNonZeroPersonFilterOptions(String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<String> labels = new ArrayList<>();
        for (FilterOption option : getFilterOptions(filterName)) {
            if (option.getCount() > 0
                    && !isAmbiguousFilterOptionLabel(option.getLabel())
                    && !isUnassignedFilterOption(option.getLabel())) {
                labels.add(option.getLabel());
            }
        }
        return labels;
    }

    /** {@code Unassigned} option when it has count &gt; 0 (typically directly below Select all). */
    private String resolveUnassignedFilterOption(String filterName) throws InterruptedException {
        for (FilterOption option : getFilterOptions(filterName)) {
            if (isUnassignedFilterOption(option.getLabel()) && option.getCount() > 0) {
                return option.getLabel();
            }
        }
        return null;
    }

    static boolean isUnassignedFilterOption(String label) {
        return label != null && label.trim().equalsIgnoreCase("Unassigned");
    }

    private static boolean assignedToCellMatchesSelectedPerson(String cellValue, String optionLabel) {
        if (cellValue == null || optionLabel == null) {
            return false;
        }
        return cellValue.trim().equalsIgnoreCase(toSubmittedByTableInitials(optionLabel));
    }

    private static boolean assignedToCellIsBlank(String cellValue) {
        if (cellValue == null) {
            return true;
        }
        String trimmed = cellValue.trim();
        return trimmed.isEmpty() || "-".equals(trimmed) || "—".equals(trimmed);
    }

    /**
     * TC605 smoke for Submitted by: first non-zero option (skips literal {@code All}) → filter count matches
     * table record count → every visible Submitted By column cell shows initials of the selected name
     * (e.g. {@code Akilandeswari Sundararajan} → {@code AS}).
     */
    public void validateSubmittedByTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC605");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC605");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        String expectedInitials = toSubmittedByTableInitials(optionLabel);
        Logger.logMessage(filterName + " TC605 option='" + optionLabel + "' filterCount=" + filterOptionCount
                + " → expected table initials='" + expectedInitials + "'");

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount >= 0,
                filterName + " results count available after selecting " + optionLabel
                        + " (actual=" + tableRecordCount + ")");
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);

        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.SUBMITTED_BY);
        List<String> submittedByValues = getVisibleTableColumnValues(ManageColumnOptions.SUBMITTED_BY);
        Logger.logMessage(filterName + " TC605 Submitted By column values fetched (" + submittedByValues.size()
                + " visible cell(s)): " + submittedByValues);
        Verify.softAssert(!submittedByValues.isEmpty(),
                filterName + " has visible Submitted By column values after selecting " + optionLabel
                        + " (scroll Submitted By column into view on Orders grid)");

        for (int i = 0; i < submittedByValues.size(); i++) {
            String cellText = submittedByValues.get(i);
            Verify.softAssert(submittedByCellMatchesSelectedOption(cellText, optionLabel),
                    filterName + " row " + (i + 1) + " Submitted By initials are '" + expectedInitials
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Submitted By column OK — all " + submittedByValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");

        collapseFilter(filterName);
    }

    static String toSubmittedByTableInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return initials.toString();
    }

    private static boolean submittedByCellMatchesSelectedOption(String cellValue, String optionLabel) {
        if (cellValue == null || optionLabel == null) {
            return false;
        }
        return cellValue.trim().equalsIgnoreCase(toSubmittedByTableInitials(optionLabel));
    }

    /**
     * TC603 smoke for Environment: first non-zero option (skips literal {@code All}) → filter count matches
     * Orders table record count. No Manage columns and no table column value checks (Environment is not on the grid).
     */
    public void validateEnvironmentTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC603");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC603");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage(filterName + " TC603 option='" + optionLabel + "' filterCount=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount >= 0,
                filterName + " results count available after selecting " + optionLabel
                        + " (actual=" + tableRecordCount + ")");
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);

        collapseFilter(filterName);
    }

    /**
     * TC109 for Flag: Select All first, then {@code Is not flagged} → {@code Is flagged} → reason children.
     * Separator line before {@code Is flagged} appears only when {@code Is flagged} count is 0.
     */
    public void validateFlagOptionListOrderSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);

        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " — Select All is first at top");

        List<String> labels = getFilterOptionLabels(filterName);
        Verify.softAssert(!labels.isEmpty(), filterName + " has visible filter options when expanded");

        int lastIndex = -1;
        for (String expected : FlagFilterConstants.FLAG_OPTION_ORDER) {
            int index = labels.indexOf(expected);
            if (index < 0) {
                continue;
            }
            Verify.softAssert(index > lastIndex,
                    filterName + " option order — '" + expected + "' follows prior options. Visible: " + labels);
            lastIndex = index;
        }

        Verify.softAssert(labels.contains(FlagFilterConstants.IS_NOT_FLAGGED),
                filterName + " shows '" + FlagFilterConstants.IS_NOT_FLAGGED + "' option");
        Verify.softAssert(labels.contains(FlagFilterConstants.IS_FLAGGED),
                filterName + " shows '" + FlagFilterConstants.IS_FLAGGED + "' option");

        int isFlaggedCount = getFilterOptionCount(filterName, FlagFilterConstants.IS_FLAGGED);
        boolean separatorVisible = hasVisibleZeroCountDivider(filterName, FlagFilterConstants.IS_FLAGGED);
        if (isFlaggedCount == 0) {
            Verify.softAssert(separatorVisible,
                    filterName + " shows separator before '" + FlagFilterConstants.IS_FLAGGED
                            + "' when Is flagged count is 0");
        } else {
            Verify.softAssert(!separatorVisible,
                    filterName + " hides separator before '" + FlagFilterConstants.IS_FLAGGED
                            + "' when Is flagged count > 0 (actual=" + isFlaggedCount + ")");
        }

        collapseFilter(filterName);
    }

    /**
     * TC608 for Flag on Orders view:
     * <ul>
     *   <li>{@code Is not flagged} — filter count = table count; no flag icon in Status column</li>
     *   <li>{@code Is flagged} or a non-zero child reason — filter count = table count; flag icon on every visible row</li>
     * </ul>
     */
    public void validateFlagTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC608");
            return;
        }
        ensureNoOptionsSelected(filterName);

        int notFlaggedCount = getFilterOptionCount(filterName, FlagFilterConstants.IS_NOT_FLAGGED);
        if (notFlaggedCount > 0) {
            validateFlagNotFlaggedTableSync(softAssert, filterName, notFlaggedCount);
            ensureNoOptionsSelected(filterName);
        } else {
            Logger.logMessage(filterName + " — Is not flagged count is 0; skipping not-flagged TC608 path");
        }

        String flaggedOption = resolveFirstNonZeroFlaggedFilterOption(filterName);
        if (flaggedOption != null) {
            validateFlagFlaggedTableSync(softAssert, filterName, flaggedOption);
        } else {
            Logger.logMessage(filterName + " — no Is flagged / child option with count > 0; skipping flagged TC608 path");
        }

        collapseFilter(filterName);
    }

    private void validateFlagNotFlaggedTableSync(SoftAssert softAssert, String filterName, int filterOptionCount)
            throws InterruptedException {
        Logger.logMessage(filterName + " TC608 — Is not flagged filterCount=" + filterOptionCount);
        selectFilterOption(filterName, FlagFilterConstants.IS_NOT_FLAGGED);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " Is not flagged — table record count matches filter count"
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");

        scrollOrdersGridUntilColumnVisible("Status");
        assertAllVisibleOrderRowsHaveStatusFlag(softAssert, filterName, false);
        Logger.logMessage(filterName + " Is not flagged — no Status flag icons on visible order rows");
    }

    private void validateFlagFlaggedTableSync(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " flagged option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage(filterName + " TC608 — flagged option='" + optionLabel + "' filterCount=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " flagged option '" + optionLabel + "' — table record count matches filter count"
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");

        scrollOrdersGridUntilColumnVisible("Status");
        assertAllVisibleOrderRowsHaveStatusFlag(softAssert, filterName, true);
        Logger.logMessage(filterName + " flagged option '" + optionLabel
                + "' — flag icon present in Status column on all visible order rows");
    }

    private String resolveFirstNonZeroFlaggedFilterOption(String filterName) throws InterruptedException {
        expandFilter(filterName);
        for (String child : FlagFilterConstants.FLAGGED_CHILD_OPTIONS) {
            if (getFilterOptionCount(filterName, child) > 0) {
                return LeftFilterOptionRotationUtil.pickNext(filterName + "#flagged-child",
                        Collections.singletonList(child), 1).getFirstOrNull();
            }
        }
        if (getFilterOptionCount(filterName, FlagFilterConstants.IS_FLAGGED) > 0) {
            return FlagFilterConstants.IS_FLAGGED;
        }
        return null;
    }

    private void assertAllVisibleOrderRowsHaveStatusFlag(SoftAssert softAssert, String filterName, boolean expectFlag) {
        int visibleRows = countVisibleOrderRows();
        Verify.softAssert(visibleRows > 0,
                filterName + " has visible order rows in Status column check (rows=" + visibleRows + ")");
        for (int row = 1; row <= visibleRows; row++) {
            boolean hasFlag = hasOrderRowStatusFlag(row);
            if (expectFlag) {
                Verify.softAssert(hasFlag,
                        filterName + " row " + row + " shows flag icon near Order Status");
            } else {
                Verify.softAssert(!hasFlag,
                        filterName + " row " + row + " has no flag icon near Order Status");
            }
        }
    }

    private boolean hasOrderRowStatusFlag(int rowIndexOneBased) {
        try {
            if (WaitUtil.isDisplayFast(ordersMainTablePage.orderRowStatusFlagIcon(rowIndexOneBased), 2)) {
                return true;
            }
            Object result = driver.get().browser().executeScript(
                    "var rows=document.querySelectorAll(\"table[id*='orderTable'] tbody tr\");"
                            + "var n=0;"
                            + "for(var i=0;i<rows.length;i++){"
                            + "  var tr=rows[i];"
                            + "  var cls=tr.className||'';"
                            + "  if(cls.indexOf('inner')>=0)continue;"
                            + "  if(cls.indexOf('row')<0 && cls.indexOf('clickable-row')<0)continue;"
                            + "  n++;"
                            + "  if(n===" + rowIndexOneBased + "){"
                            + "    var td=tr.querySelector('td.revised-status-col,td[class*=\"revised-status\"]');"
                            + "    if(!td)return false;"
                            + "    return !!td.querySelector('i.bi-flag,i.bi-flag-fill,[class*=\"flag\"],msc-flag-icon');"
                            + "  }"
                            + "}"
                            + "return false;");
            return toScriptBoolean(result);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read flag icon for order row " + rowIndexOneBased + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean brandCellMatchesSelectedOption(String cellValue, String optionLabel) {
        if (cellValue == null || optionLabel == null) {
            return false;
        }
        return cellValue.trim().equalsIgnoreCase(optionLabel.trim());
    }

    private static final int ACTIVITY_TYPE_EXPAND_WAIT_MS = 1500;
    private static final int TC620_TABLE_SETTLE_AFTER_MANAGE_COLUMNS_MS = 15_000;
    private static final int TC620_EXPANDED_LINE_ITEM_SETTLE_MS = 20_000;

    private int expandedGridWaitSeconds() {
        return Config.isLocalExecution() ? 30 : 45;
    }

    /**
     * TC620 on Orders: filter → Manage columns Activity Type → expand row and read line-item values.
     */
    public void validateActivityTypeTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureLeftFilterPanelOpen();
        if (!waitForFilterHeaderVisible(filterName, 30)) {
            Verify.softAssert(false, filterName + " filter header not found in left panel");
            return;
        }
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC620");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC620");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage("TC620 — select first non-zero " + filterName + " option='" + optionLabel
                + "' count=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        new ManageColumnsUtil().enableActivityTypeColumn(softAssert);

        Logger.logMessage("TC620 — waiting for Orders table after Manage columns (no page refresh)");
        Thread.sleep(TC620_TABLE_SETTLE_AFTER_MANAGE_COLUMNS_MS);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount > 0,
                filterName + " Orders table shows rows after Manage columns (rows=" + tableRecordCount + ")");

        boolean matchFound = expandOrdersUntilActivityTypeFound(softAssert, optionLabel);
        Verify.softAssert(matchFound,
                filterName + " expanded order line item shows " + ManageColumnOptions.ACTIVITY_TYPE + "='"
                        + optionLabel + "' (when column enabled in Manage columns)");

        collapseFilter(filterName);
    }

    /**
     * TC601 on Orders: Line Item Status filter → expand up to {@link #MAX_ORDERS_TO_EXPAND} order rows →
     * at least one expanded line item {@code revised-status-col} value matches the selected filter option.
     * Filter option count vs Orders table record count is not asserted (they count different grains).
     */
    public void validateLineItemStatusTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureLeftFilterPanelOpen();
        if (!waitForFilterHeaderVisible(filterName, 30)) {
            Verify.softAssert(false, filterName + " filter header not found in left panel");
            return;
        }
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC601");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC601");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage("TC601 — select first non-zero " + filterName + " option='" + optionLabel
                + "' count=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount > 0,
                filterName + " Orders table shows rows after selecting " + optionLabel
                        + " (rows=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " TC601 Orders view — filter count=" + filterOptionCount
                + ", table record count=" + tableRecordCount + " (count equality not checked on Orders view)");

        boolean matchFound = expandOrdersUntilLineItemStatusFound(softAssert, optionLabel);
        Verify.softAssert(matchFound,
                filterName + " expanded order line item shows Line Item Status matching '"
                        + optionLabel + "' (at least one line item; not all required)");

        collapseFilter(filterName);
    }

    /**
     * TC601 on Line Items tab: filter count sync → read {@code revised-status-col} on the main grid →
     * at least one visible Line Item Status matches the selected filter option.
     */
    public void validateLineItemStatusTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        ensureLeftFilterPanelOpen();
        if (!waitForFilterHeaderVisible(filterName, 30)) {
            Verify.softAssert(false, filterName + " filter header not found in left panel");
            return;
        }
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC601");
            return;
        }
        ensureNoOptionsSelected(filterName);

        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping TC601");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage("TC601 [Line Items] — select first non-zero " + filterName + " option='" + optionLabel
                + "' count=" + filterOptionCount);

        selectFilterOption(filterName, optionLabel);
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");

        List<String> statusValues = collectVisibleLineItemStatusLabels();
        Verify.softAssert(!statusValues.isEmpty(),
                filterName + " has visible Line Item Status values after selecting " + optionLabel);

        boolean matchFound = statusValues.stream()
                .anyMatch(value -> lineItemStatusMatchesFilterOption(value, optionLabel));
        if (!matchFound) {
            Logger.logMessage("TC601 [Line Items] status values checked (" + statusValues.size()
                    + "): " + statusValues);
        }
        Verify.softAssert(matchFound,
                filterName + " at least one Line Item Status matches '" + optionLabel
                        + "' (checked " + statusValues.size() + " visible row(s); not all required)");

        collapseFilter(filterName);
    }

    private String resolveFirstNonZeroFilterOption(String filterName) throws InterruptedException {
        List<String> allNonZero = resolveAllNonZeroFilterOptions(filterName);
        if (allNonZero.isEmpty()) {
            return null;
        }
        return LeftFilterOptionRotationUtil.pickNext(filterName, allNonZero, 1).getFirstOrNull();
    }

    /**
     * Brand filter on PROD exposes a literal option {@code All} (not Select All) — skip for dynamic
     * sample selection because it does not produce a reliable active-filter chip.
     */
    public static boolean isAmbiguousFilterOptionLabel(String label) {
        if (label == null) {
            return true;
        }
        return "All".equalsIgnoreCase(label.trim());
    }

    public String resolveFirstSelectableFilterOption(String filterName) throws InterruptedException {
        String nonZero = resolveFirstNonZeroFilterOption(filterName);
        if (nonZero != null) {
            return nonZero;
        }
        expandFilter(filterName);
        for (String label : getFilterOptionLabels(filterName)) {
            if (!isAmbiguousFilterOptionLabel(label)) {
                return label;
            }
        }
        return null;
    }

    /** All distinct non-zero options for a filter (skips ambiguous labels like {@code All}). */
    public List<String> resolveAllNonZeroFilterOptions(String filterName) throws InterruptedException {
        expandFilter(filterName);
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (FilterOption option : getFilterOptions(filterName)) {
            if (option.getCount() > 0 && !isAmbiguousFilterOptionLabel(option.getLabel())) {
                labels.add(option.getLabel());
            }
        }
        return new ArrayList<>(labels);
    }

    /** Clears active filter chips via the expanded Active filters panel Clear control. */
    public void clearAllActiveFiltersIfPresent() throws InterruptedException {
        if (!hasActiveFilterChips()) {
            return;
        }
        if (!WaitUtil.isDisplayFast(leftFilterPanel.activeFiltersToggleButton(), 2)) {
            return;
        }
        clickActiveFiltersButton();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        if (!WaitUtil.isDisplayFast(leftFilterPanel.activeFiltersClearButton(), 2)) {
            clickActiveFiltersButton();
            return;
        }
        DriverUtil.clickOnElement(leftFilterPanel.activeFiltersClearButton(), 5);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        WaitUtil.waitForJSToLoad(10);
        Logger.logReportMessage("Cleared all active filters");
    }

    /** True when at least one active filter chip/badge is visible. */
    public boolean hasActiveFilterChips() {
        if (WaitUtil.isDisplayFast(leftFilterPanel.activeFiltersBadge(), 2)) {
            return true;
        }
        return WaitUtil.isDisplayFast(
                By.XPath("//*[@id='collapsePanel']//msc-ui-chip-tag[not(@label='Clear')]"
                        + "[.//span[contains(@class,'chip-tag-label') and normalize-space()!='Clear']]"
                        + " | //*[@id='collapsePanel']//msc-ui-chip-tag-themed[not(@label='Clear')]"
                        + "[.//span[contains(@class,'chip-tag-label') and normalize-space()!='Clear']]"),
                2);
    }

    /** Second distinct non-zero option (skips {@code All} and {@code excludeLabel}). */
    public String resolveSecondSelectableFilterOption(String filterName, String excludeLabel)
            throws InterruptedException {
        List<String> allNonZero = resolveAllNonZeroFilterOptions(filterName);
        for (String label : allNonZero) {
            if (!label.equalsIgnoreCase(excludeLabel)) {
                return label;
            }
        }
        return null;
    }

    private void scrollOrdersGridToCollapseColumn() {
        scrollOrdersGridUntilColumnVisible(null);
    }

    /**
     * Horizontally scroll the Orders main table until {@code columnName} body cells are visible.
     * When {@code columnName} is null, scrolls to the far right (legacy collapse-column behavior).
     */
    private void scrollOrdersGridUntilColumnVisible(String columnName) {
        try {
            By cells = columnName != null ? leftFilterPanel.tableColumnCells(columnName) : null;
            if (cells != null && WaitUtil.isDisplayFast(cells, 2)) {
                return;
            }
            if (columnName != null && new ManageColumnsUtil().isOrderColumnVisibleOnGrid(columnName)) {
                scrollOrdersGridToRevealColumnCells(columnName);
                if (WaitUtil.isDisplayFast(cells, 2)) {
                    return;
                }
            }
            String[] scrollSelectors = {
                    "app-fulfillment-main-table-container .custom-table-wrapper",
                    "app-fulfillment-main-table-container .table-container",
                    "msc-custom-table .custom-table-wrapper"
            };
            for (String selector : scrollSelectors) {
                for (int step = 0; step <= 5; step++) {
                    driver.get().browser().executeScript(
                            "var el=document.querySelector('" + selector + "');"
                                    + "if(!el){return;}"
                                    + "var max=Math.max(el.scrollWidth-el.clientWidth,0);"
                                    + "el.scrollLeft=Math.round(max*" + step + "/5);");
                    Thread.sleep(80);
                    if (cells != null && WaitUtil.isDisplayFast(cells, 1)) {
                        return;
                    }
                }
            }
            if (columnName == null) {
                driver.get().browser().executeScript(
                        "var wrappers=document.querySelectorAll("
                                + "\"app-fulfillment-main-table-container .custom-table-wrapper,"
                                + " app-fulfillment-main-table-container .table-container,"
                                + " msc-custom-table .custom-table-wrapper\");"
                                + "for(var i=0;i<wrappers.length;i++){"
                                + "  wrappers[i].scrollLeft=wrappers[i].scrollWidth;"
                                + "}");
            }
            Thread.sleep(200);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Orders grid for column " + columnName + ": " + e.getMessage());
        }
    }

    /** Scrolls the grid just enough to bring {@code columnName} body cells into the viewport. */
    private void scrollOrdersGridToRevealColumnCells(String columnName) {
        try {
            String cssClass = columnName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + "-col";
            if ("Brand".equalsIgnoreCase(columnName)) {
                cssClass = "brand-col";
            } else if ("Status".equalsIgnoreCase(columnName)) {
                cssClass = "revised-status-col";
            } else if ("Assigned to".equalsIgnoreCase(columnName)) {
                cssClass = "assigned-to-col";
            }
            String escaped = cssClass.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    "var td=document.querySelector('app-fulfillment-main-table-container td." + escaped
                            + ",app-fulfillment-orders-main-table td." + escaped + "');"
                            + "if(!td){return;}"
                            + "td.scrollIntoView({block:'nearest',inline:'center'});"
                            + "var wrap=td.closest('.custom-table-wrapper,.table-container');"
                            + "if(wrap&&td.getBoundingClientRect){"
                            + "  var r=td.getBoundingClientRect();"
                            + "  var w=wrap.getBoundingClientRect();"
                            + "  if(r.right>w.right){wrap.scrollLeft+=r.right-w.right+24;}"
                            + "  else if(r.left<w.left){wrap.scrollLeft-=w.left-r.left+24;}"
                            + "}");
            Thread.sleep(200);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll Brand cells into view: " + e.getMessage());
        }
    }

    private static final int MAX_ORDERS_TO_EXPAND = 3;

    /**
     * Expands orders, waits for line-item grid, reads all Activity Type cells via PROD xpath,
     * and passes when at least one line item matches the selected filter option.
     */
    private boolean expandOrdersUntilActivityTypeFound(SoftAssert softAssert, String optionLabel)
            throws InterruptedException {
        String optionLower = optionLabel.toLowerCase(Locale.ROOT);
        int visibleRows = countVisibleOrderRows();
        int rowsToTry = Math.min(MAX_ORDERS_TO_EXPAND, Math.max(visibleRows, 1));
        Logger.logMessage("TC620 scanning up to " + rowsToTry + " order row(s) (visible=" + visibleRows + ")");
        boolean activityTypesRead = false;

        for (int row = 1; row <= rowsToTry; row++) {
            collapseExpandedOrders();
            if (!expandOrderRow(softAssert, row)) {
                Logger.logMessage("Could not expand order row " + row + " — stopping scan");
                break;
            }
            if (!waitForOrderRowExpanded(row, expandedGridWaitSeconds())) {
                Logger.logMessage("Order row " + row + " did not show expanded line-item grid");
                continue;
            }

            Logger.logMessage("TC620 — waiting " + (TC620_EXPANDED_LINE_ITEM_SETTLE_MS / 1000)
                    + "s after expand before reading Activity Type cells (order row " + row + ")");
            Thread.sleep(TC620_EXPANDED_LINE_ITEM_SETTLE_MS);

            List<String> activityTypes = collectExpandedLineItemActivityTypes();
            Logger.logMessage("TC620 order row " + row + " Activity Type values ("
                    + activityTypes.size() + " line item(s)): " + activityTypes);

            if (!activityTypes.isEmpty()) {
                activityTypesRead = true;
                Verify.softAssert1(true,
                        "TC620 expanded order row " + row + " has " + activityTypes.size()
                                + " line-item Activity Type cell(s)",
                        softAssert);
            }

            boolean match = activityTypes.stream()
                    .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(optionLower));
            if (match) {
                Logger.logMessage("TC620 order row " + row + " — at least one line item Activity Type matches '"
                        + optionLabel + "'");
                return true;
            }
            Logger.logMessage("TC620 order row " + row + " — no line item Activity Type matches '"
                    + optionLabel + "' (not all line items must match; checked " + activityTypes.size() + " value(s))");
        }
        if (!activityTypesRead) {
            Verify.softAssert1(false,
                    "TC620 could not read any line-item Activity Type values (//td[@class='col line-item-activity-type']) after expand",
                    softAssert);
        }
        return false;
    }

    /**
     * Expands orders, waits for line-item grid, reads Line Item Status labels, and passes when at least one
     * line item matches the selected filter option.
     */
    private boolean expandOrdersUntilLineItemStatusFound(SoftAssert softAssert, String optionLabel)
            throws InterruptedException {
        int visibleRows = countVisibleOrderRows();
        int rowsToTry = Math.min(MAX_ORDERS_TO_EXPAND, Math.max(visibleRows, 1));
        Logger.logMessage("TC601 scanning up to " + rowsToTry + " order row(s) (visible=" + visibleRows + ")");
        boolean statusesRead = false;

        for (int row = 1; row <= rowsToTry; row++) {
            collapseExpandedOrders();
            if (!expandOrderRow(softAssert, row)) {
                Logger.logMessage("Could not expand order row " + row + " — stopping scan");
                break;
            }
            if (!waitForOrderRowExpanded(row, expandedGridWaitSeconds())) {
                Logger.logMessage("Order row " + row + " did not show expanded line-item grid");
                continue;
            }

            Logger.logMessage("TC601 — waiting " + (TC620_EXPANDED_LINE_ITEM_SETTLE_MS / 1000)
                    + "s after expand before reading Line Item Status (order row " + row + ")");
            Thread.sleep(TC620_EXPANDED_LINE_ITEM_SETTLE_MS);

            List<String> statusValues = collectExpandedLineItemStatusLabels();
            Logger.logMessage("TC601 order row " + row + " Line Item Status values ("
                    + statusValues.size() + " line item(s)): " + statusValues);

            if (!statusValues.isEmpty()) {
                statusesRead = true;
                Verify.softAssert1(true,
                        "TC601 expanded order row " + row + " has " + statusValues.size()
                                + " line-item Status cell(s)",
                        softAssert);
            }

            boolean match = statusValues.stream()
                    .anyMatch(value -> lineItemStatusMatchesFilterOption(value, optionLabel));
            if (match) {
                Logger.logMessage("TC601 order row " + row + " — at least one line item Status matches '"
                        + optionLabel + "'");
                return true;
            }
            Logger.logMessage("TC601 order row " + row + " — no line item Status matches '"
                    + optionLabel + "' (not all line items must match; checked " + statusValues.size() + " value(s))");
        }
        if (!statusesRead) {
            Verify.softAssert1(false,
                    "TC601 could not read any expanded line-item Status values (revised-status-col) after expand",
                    softAssert);
        }
        return false;
    }

    private List<String> collectExpandedLineItemStatusLabels() {
        List<String> statuses = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder()
                    .findElements(ordersMainTablePage.expandedLineItemStatusLabels());
            for (DesktopBrowserElement cell : cells) {
                String text = normalizeCellText(cell.getText());
                if (!text.isEmpty()) {
                    statuses.add(text);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read expanded Line Item Status cells: " + e.getMessage());
        }
        if (statuses.isEmpty()) {
            statuses.addAll(collectExpandedLineItemStatusLabelsViaJs());
        }
        return statuses;
    }

    private List<String> collectExpandedLineItemStatusLabelsViaJs() {
        List<String> statuses = new ArrayList<>();
        try {
            Object result = driver.get().browser().executeScript(
                    "var out=[];"
                            + "document.querySelectorAll("
                            + "\"table[id*='orderTable'] td.revised-status-col span.status-label,"
                            + "tr.inner td.revised-status-col span.status-label,"
                            + "td[class*='revised-status'] span.status-label\").forEach(function(el){"
                            + "  var t=(el.textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(t){out.push(t);}"
                            + "});"
                            + "return out;");
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        String text = normalizeCellText(String.valueOf(item));
                        if (!text.isEmpty()) {
                            statuses.add(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("JS Line Item Status collect failed: " + e.getMessage());
        }
        return statuses;
    }

    private List<String> collectVisibleLineItemStatusLabels() {
        List<String> statuses = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder()
                    .findElements(lineItemsMainTablePage.visibleLineItemStatusLabels());
            for (DesktopBrowserElement cell : cells) {
                String text = normalizeCellText(cell.getText());
                if (!text.isEmpty()) {
                    statuses.add(text);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read Line Items tab status labels: " + e.getMessage());
        }
        if (statuses.isEmpty()) {
            statuses.addAll(getVisibleTableColumnValues("Status"));
        }
        return statuses;
    }

    private static boolean lineItemStatusMatchesFilterOption(String cellValue, String optionLabel) {
        if (cellValue == null || optionLabel == null) {
            return false;
        }
        String cellLower = cellValue.trim().toLowerCase(Locale.ROOT);
        String optionLower = optionLabel.trim().toLowerCase(Locale.ROOT);
        return cellLower.contains(optionLower) || optionLower.contains(cellLower);
    }

    /**
     * All Activity Type values from expanded line-item rows — PROD xpath
     * {@code //td[@class='col line-item-activity-type']}. Not every line item must match the filter.
     */
    private List<String> collectExpandedLineItemActivityTypes() {
        List<String> activityTypes = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder()
                    .findElements(ordersMainTablePage.expandedLineItemActivityTypeCellsExact());
            for (DesktopBrowserElement cell : cells) {
                String text = normalizeCellText(cell.getText());
                if (!text.isEmpty()) {
                    activityTypes.add(text);
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read Activity Type cells: " + e.getMessage());
        }
        if (activityTypes.isEmpty()) {
            activityTypes.addAll(collectExpandedLineItemActivityTypesViaJs());
        }
        return activityTypes;
    }

    private List<String> collectExpandedLineItemActivityTypesViaJs() {
        List<String> activityTypes = new ArrayList<>();
        try {
            Object result = driver.get().browser().executeScript(
                    "var out=[];"
                            + "document.querySelectorAll(\"td.col.line-item-activity-type,"
                            + "td[class='col line-item-activity-type']\").forEach(function(td){"
                            + "  var inner=td.querySelector('.default-cell span.label,.default-cell,.label');"
                            + "  var t=((inner||td).textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(t){out.push(t);}"
                            + "});"
                            + "return out;");
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        String text = normalizeCellText(String.valueOf(item));
                        if (!text.isEmpty()) {
                            activityTypes.add(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("JS Activity Type collect failed: " + e.getMessage());
        }
        return activityTypes;
    }

    private static String normalizeCellText(String text) {
        if (text == null || "null".equalsIgnoreCase(text.trim())) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private int countVisibleOrderRows() {
        try {
            driver.get().options().setElementTimeout(QUICK_ELEMENT_TIMEOUT_MS);
            return driver.get().finder().findElements(
                    By.XPath("//table[contains(@id,'orderTable')]//tbody//tr"
                            + "[(contains(@class,'row') or contains(@class,'clickable-row'))"
                            + " and not(contains(@class,'inner'))]")).size();
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not count visible order rows: " + e.getMessage());
            return MAX_ORDERS_TO_EXPAND;
        } finally {
            try {
                driver.get().options().setElementTimeout(
                        com.paramount.test.ff.common.driver.LocalCapabilityFactory.DEFAULT_ELEMENT_TIMEOUT);
            } catch (Exception ignored) {
                // session may be closing
            }
        }
    }

    private boolean expandOrderRow(SoftAssert softAssert, int rowIndexOneBased) throws InterruptedException {
        By expandControl = ordersMainTablePage.orderRowExpandChevron(rowIndexOneBased);
        scrollOrdersGridToCollapseColumn();
        if (!WaitUtil.isDisplayFast(expandControl, 5)) {
            if (!clickExpandViaScript(rowIndexOneBased)) {
                return false;
            }
        } else {
            DriverUtil.scrollToElement(expandControl);
            if (!DriverUtil.clickOnElement(expandControl, DEFAULT_WAIT_SECONDS)) {
                if (!clickExpandViaScript(rowIndexOneBased)) {
                    return false;
                }
            }
        }
        Thread.sleep(Config.isLocalExecution() ? ACTIVITY_TYPE_EXPAND_WAIT_MS : ACTIVITY_TYPE_EXPAND_WAIT_MS * 2);
        return waitForOrderRowExpanded(rowIndexOneBased, 8);
    }

    private boolean waitForOrderRowExpanded(int rowIndexOneBased, int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        By expandedChevron = ordersMainTablePage.orderRowExpandedChevronDown(rowIndexOneBased);
        while (System.currentTimeMillis() < deadline) {
            if (WaitUtil.isDisplayFast(expandedChevron, 1)
                    || WaitUtil.isDisplayFast(ordersMainTablePage.expandedInnerTableMarker(), 1)) {
                return true;
            }
            Thread.sleep(400);
        }
        return WaitUtil.isDisplayFast(expandedChevron, 1)
                || WaitUtil.isDisplayFast(ordersMainTablePage.expandedInnerTableMarker(), 1);
    }

    private boolean clickExpandViaScript(int rowIndexOneBased) {
        try {
            Object result = driver.get().browser().executeScript(
                    "var idx=" + (rowIndexOneBased - 1) + ";"
                            + "var rows=[].slice.call(document.querySelectorAll("
                            + "\"table[id*='orderTable'] tbody tr\")).filter(function(tr){"
                            + "  return !tr.className.match(/\\binner\\b/)"
                            + "    && (tr.className.match(/\\brow\\b/) || tr.className.match(/clickable-row/));"
                            + "});"
                            + "var row=rows[idx];"
                            + "if(!row){return false;}"
                            + "row.scrollIntoView({block:'center'});"
                            + "var chevron=row.querySelector("
                            + "\"td[class*='collapse'] i.bi-chevron-right, td[class*='collapse'] i.bi-chevron-down\");"
                            + "if(chevron){chevron.click();return true;}"
                            + "var cell=row.querySelector(\"td[class*='collapse'], td[class*='collapse-all']\");"
                            + "if(cell){cell.click();return true;}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("JS expand failed for row " + rowIndexOneBased + ": " + e.getMessage());
            return false;
        }
    }

    private void collapseExpandedOrders() {
        try {
            driver.get().browser().executeScript(
                    "document.querySelectorAll("
                            + "\"table[id*='orderTable'] tbody tr.row i.bi-chevron-down\""
                            + ").forEach(function(el){el.click();});");
            Thread.sleep(300);
        } catch (Exception ignored) {
            // best effort
        }
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

    /**
     * TC10xx Active filters: open filter → select 1st option → select 2nd (keep 1st) → click Active filters once → verify chips.
     */
    public void validateActiveFiltersTwoOptions(SoftAssert softAssert, String filterName) throws InterruptedException {
        List<String> selected = selectTwoOptionsAndOpenActiveFiltersPanel(softAssert, filterName);
        verifyActiveFilterChips(softAssert, filterName, selected);
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

    /**
     * TC11xx Clear filters: same as active filters (two options + open panel + verify chips), then click Clear.
     */
    public void validateClearFilters(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        List<String> selected = selectTwoOptionsAndOpenActiveFiltersPanel(softAssert, filterName);
        verifyActiveFilterChips(softAssert, filterName, selected);

        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFiltersClearButton(), DEFAULT_WAIT_SECONDS),
                "Active filters Clear control visible beside chips");
        DriverUtil.clickOnElement(leftFilterPanel.activeFiltersClearButton(), DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        for (String label : selected) {
            Verify.softAssert(!WaitUtil.isDisplayFast(leftFilterPanel.activeFilterChip(filterName, label), 2),
                    label + " cleared via Active filters Clear (not chip X)");
        }
    }

    /**
     * Steps 1–4: expand filter, select first + second option (keep first checked), click Active filters button once.
     */
    private List<String> selectTwoOptionsAndOpenActiveFiltersPanel(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        expandFilter(filterName);
        List<String> allNonZero = resolveAllNonZeroFilterOptions(filterName);
        if (allNonZero.isEmpty()) {
            String fallback = resolveFirstSelectableFilterOption(filterName);
            Verify.softAssert(fallback != null, filterName + " has at least one selectable option");
            if (fallback == null) {
                return Collections.emptyList();
            }
            allNonZero = Collections.singletonList(fallback);
        }

        LeftFilterOptionRotationUtil.RotationResult rotation =
                LeftFilterOptionRotationUtil.pickNext(filterName + "#active-filters", allNonZero, 2);
        List<String> selected = new ArrayList<>(rotation.getSelected());
        if (selected.isEmpty()) {
            Verify.softAssert(false, filterName + " has at least one selectable option");
            return Collections.emptyList();
        }

        String first = selected.get(0);
        selectFilterOptionIfNotSelected(filterName, first);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        if (selected.size() > 1) {
            String second = selected.get(1);
            selectFilterOptionIfNotSelected(filterName, second);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            Verify.softAssert(isFilterOptionSelected(filterName, first),
                    first + " still selected after selecting " + second + " on " + filterName);
        }

        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFiltersToggleButton(), DEFAULT_WAIT_SECONDS),
                "Active filters button visible for " + filterName);
        clickActiveFiltersButton();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        return selected;
    }

    private void verifyActiveFilterChips(SoftAssert softAssert, String filterName, List<String> optionLabels) {
        for (String label : optionLabels) {
            Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFilterChip(filterName, label),
                            DEFAULT_WAIT_SECONDS),
                    label + " appears in active filters for " + filterName);
        }
        if (optionLabels.size() > 1) {
            Verify.softAssert(WaitUtil.isDisplay(
                            leftFilterPanel.activeFiltersToggleBadgeCount(optionLabels.size()), DEFAULT_WAIT_SECONDS),
                    "Active filters badge shows count " + optionLabels.size() + " for " + filterName);
        }
    }

    /** Clicks {@code button.btn-active-filters} via JS — not the nested chevron icon. */
    public void clickActiveFiltersButton() {
        DriverUtil.clickOnElementJs(leftFilterPanel.activeFiltersLeftToggleButton(), DEFAULT_WAIT_SECONDS);
        Logger.logReportMessage("Clicked Active filters button (btn-active-filters toggle)");
    }

    public void selectFilterOptionIfNotSelected(String filterName, String optionLabel) {
        By checkbox = leftFilterPanel.filterOptionCheckbox(filterName, optionLabel);
        if (isFilterOptionSelected(filterName, optionLabel)) {
            Logger.logMessage(filterName + " option '" + optionLabel + "' already selected — skip click");
            return;
        }
        DriverUtil.scrollToElement(checkbox);
        DriverUtil.clickOnElement(checkbox, DEFAULT_WAIT_SECONDS);
    }

    public boolean isFilterOptionSelected(String filterName, String optionLabel) {
        try {
            return DriverUtil.isSelectedCheckbox(leftFilterPanel.filterOptionCheckbox(filterName, optionLabel));
        } catch (Exception e) {
            return false;
        }
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
        return getVisibleTableColumnValues(columnName, false);
    }

    /**
     * Reads visible body cells for {@code columnName}. When {@code includeBlankCells} is true, empty cells
     * are included (needed for Assigned To → Unassigned, where the column is present but has no value).
     */
    private List<String> getVisibleTableColumnValues(String columnName, boolean includeBlankCells) {
        List<String> values = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder()
                    .findElements(leftFilterPanel.tableColumnCells(columnName));
            for (DesktopBrowserElement cell : cells) {
                String text = cell.getText() == null ? "" : cell.getText().trim();
                if (!includeBlankCells && text.isEmpty()) {
                    continue;
                }
                values.add(text);
            }
            if (values.isEmpty() && !includeBlankCells) {
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
