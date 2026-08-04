package com.paramount.test.ff.uitests.helpers.duplicatefilteroptions;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsDuplicateAnalyzer;
import com.paramount.test.ff.uitests.helpers.partneroptions.PartnerOptionsGraphqlResponseParser;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.COLLECTION_MAX_STALE_SCROLL_ATTEMPTS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.MAX_STALE_SCROLL_ATTEMPTS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SCROLL_SETTLE_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SCROLL_STEP_PX;

/**
 * Collects all filter option labels by scrolling the CDK virtual-scroll viewport.
 * Partner and other large filters render options inside {@code cdk-virtual-scroll-viewport};
 * pagination buttons are not used.
 */
public class DuplicateFilterOptionsCollectionHelper extends BaseTest {

    private static final int MIN_VIRTUAL_SCROLL_STEP_PX = 32;
    private static final Pattern COUNT_IN_PARENS = Pattern.compile("\\((\\d+)\\)");

    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();
    private final LeftFilterPanelUtil leftFilterPanelUtil;

    public DuplicateFilterOptionsCollectionHelper(LeftFilterPanelUtil leftFilterPanelUtil) {
        this.leftFilterPanelUtil = leftFilterPanelUtil;
    }

    public List<String> collectAllFilterOptionLabels(String filterName) throws InterruptedException {
        leftFilterPanelUtil.expandFilter(filterName);
        DriverUtil.scrollToElement(leftFilterPanel.leftFilterByName(filterName));

        if (!leftFilterPanelUtil.hasFilterSearchInput(filterName)) {
            Logger.logMessage(filterName + " has no search input; skipping option collection");
            return new ArrayList<>();
        }

        leftFilterPanelUtil.clearFilterSearchInput(filterName);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        List<String> allLabels = new ArrayList<>();
        int expectedTotal = getExpectedTotalOptionCount(filterName);

        Logger.logMessage(filterName + ": expected total option count from UI = "
                + (expectedTotal > 0 ? expectedTotal : "unknown"));

        collectAllOptionsViaVirtualScroll(filterName, allLabels, expectedTotal);

        Logger.logMessage(filterName + ": collected " + allLabels.size() + " raw filter option row(s), "
                + PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels) + " unique from UI");
        return allLabels;
    }

    private static void appendCapturedLabels(List<String> allLabels, List<String> visibleLabels) {
        List<String> merged = PartnerOptionsGraphqlResponseParser.mergeBatches(allLabels, visibleLabels);
        allLabels.clear();
        allLabels.addAll(merged);
    }

    /**
     * Scrolls the CDK viewport, validates each newly discovered label, then scrolls again.
     * After each search validation the filter search is cleared and scroll position is restored
     * so the virtual list can continue loading the next partners.
     *
     * @param skipCount       number of discovered labels to scroll past without validating
     * @param maxValidations  max labels to validate in this run ({@link Integer#MAX_VALUE} for all)
     */
    public ScrollValidationSummary validateWhileScrolling(String filterName, int skipCount, int maxValidations,
                                                          FilterOptionSearchValidator validator)
            throws InterruptedException {
        leftFilterPanelUtil.expandFilter(filterName);
        DriverUtil.scrollToElement(leftFilterPanel.leftFilterByName(filterName));

        if (!leftFilterPanelUtil.hasFilterSearchInput(filterName)) {
            Logger.logMessage(filterName + " has no search input; skipping scroll validation");
            return new ScrollValidationSummary(0, 0, -1);
        }

        leftFilterPanelUtil.clearFilterSearchInput(filterName);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        int expectedTotal = getExpectedTotalOptionCount(filterName);
        Logger.logMessage(filterName + ": incremental scroll-validate, expected total="
                + (expectedTotal > 0 ? expectedTotal : "unknown")
                + ", skip=" + skipCount + ", maxValidate=" + maxValidations);

        By container = leftFilterPanel.filterOptionsContainer(filterName);
        if (!WaitUtil.isDisplay(container, 3)) {
            return validateWithoutVirtualScroll(filterName, skipCount, maxValidations, validator, expectedTotal);
        }

        DesktopBrowserElement viewport = driver.get().finder().findElement(container);
        Set<String> discovered = new LinkedHashSet<>();
        int discoveredIndex = 0;
        int validatedCount = 0;
        int validationLimit = maxValidations <= 0 ? Integer.MAX_VALUE : maxValidations;

        scrollVirtualViewport(viewport, 0);
        Thread.sleep(SCROLL_SETTLE_MS);

        int scrollStep = getVirtualScrollStep(viewport);
        long maxScrollTop = getVirtualScrollMaxTop(viewport, expectedTotal, scrollStep);
        long scrollTop = 0;
        int staleScrollAttempts = 0;
        int previousDiscoveredSize = 0;
        boolean batchComplete = false;

        Logger.logMessage(filterName + ": virtual scroll maxTop=" + maxScrollTop + ", step=" + scrollStep);

        while (!batchComplete && scrollTop <= maxScrollTop && staleScrollAttempts < MAX_STALE_SCROLL_ATTEMPTS) {
            long currentScrollTop = scrollTop;
            List<String> visibleLabels = leftFilterPanelUtil.getFilterOptionLabels(filterName);

            for (String label : visibleLabels) {
                if (!discovered.add(label)) {
                    continue;
                }

                if (discoveredIndex < skipCount) {
                    discoveredIndex++;
                    continue;
                }

                if (validatedCount >= validationLimit) {
                    batchComplete = true;
                    break;
                }

                validator.validate(label, currentScrollTop);
                validatedCount++;
                discoveredIndex++;

                leftFilterPanelUtil.clearFilterSearchInput(filterName);
                scrollVirtualViewport(viewport, currentScrollTop);
                Thread.sleep(SCROLL_SETTLE_MS);

                Logger.logMessage(filterName + ": validated '" + label + "' at scrollTop=" + currentScrollTop
                        + " (" + validatedCount + " in batch, " + discovered.size() + " discovered)");
            }

            if (batchComplete) {
                break;
            }

            if (!visibleLabels.isEmpty()) {
                scrollLastVisibleOptionIntoView(filterName, viewport, visibleLabels.get(visibleLabels.size() - 1));
                Thread.sleep(SCROLL_SETTLE_MS);
            }

            if (expectedTotal > 0 && discovered.size() >= expectedTotal
                    && discoveredIndex >= skipCount + validatedCount) {
                break;
            }

            if (discovered.size() == previousDiscoveredSize) {
                staleScrollAttempts++;
            } else {
                staleScrollAttempts = 0;
                previousDiscoveredSize = discovered.size();
            }

            if (scrollTop >= maxScrollTop) {
                if (expectedTotal > 0 && discovered.size() < expectedTotal
                        && staleScrollAttempts < MAX_STALE_SCROLL_ATTEMPTS) {
                    scrollToRevealMoreOptions(viewport);
                    Thread.sleep(SCROLL_SETTLE_MS);
                    long remeasuredMax = measureVirtualScrollMaxTop(viewport);
                    if (remeasuredMax > maxScrollTop) {
                        maxScrollTop = remeasuredMax;
                    } else {
                        scrollTop += scrollStep;
                        maxScrollTop = getVirtualScrollMaxTop(viewport, expectedTotal, scrollStep);
                        continue;
                    }
                } else {
                    break;
                }
            }

            scrollTop = Math.min(scrollTop + scrollStep, maxScrollTop);
            scrollVirtualViewport(viewport, scrollTop);
            Thread.sleep(SCROLL_SETTLE_MS);
            maxScrollTop = getVirtualScrollMaxTop(viewport, expectedTotal, scrollStep);
        }

        scrollVirtualViewport(viewport, 0);
        Logger.logMessage(filterName + ": incremental validation finished, discovered="
                + discovered.size() + ", validated=" + validatedCount);
        return new ScrollValidationSummary(discovered.size(), validatedCount, expectedTotal);
    }

    private ScrollValidationSummary validateWithoutVirtualScroll(String filterName, int skipCount, int maxValidations,
                                                                 FilterOptionSearchValidator validator,
                                                                 int expectedTotal) throws InterruptedException {
        List<String> labels = leftFilterPanelUtil.getFilterOptionLabels(filterName);
        int discoveredIndex = 0;
        int validatedCount = 0;
        int validationLimit = maxValidations <= 0 ? Integer.MAX_VALUE : maxValidations;

        for (String label : labels) {
            if (discoveredIndex < skipCount) {
                discoveredIndex++;
                continue;
            }
            if (validatedCount >= validationLimit) {
                break;
            }
            validator.validate(label, 0);
            validatedCount++;
            discoveredIndex++;
            leftFilterPanelUtil.clearFilterSearchInput(filterName);
        }
        return new ScrollValidationSummary(labels.size(), validatedCount, expectedTotal);
    }

    @FunctionalInterface
    public interface FilterOptionSearchValidator {
        void validate(String optionLabel, long scrollTopBeforeSearch) throws InterruptedException;
    }

    public static final class ScrollValidationSummary {
        private final int discoveredCount;
        private final int validatedCount;
        private final int expectedTotal;

        public ScrollValidationSummary(int discoveredCount, int validatedCount, int expectedTotal) {
            this.discoveredCount = discoveredCount;
            this.validatedCount = validatedCount;
            this.expectedTotal = expectedTotal;
        }

        public int getDiscoveredCount() {
            return discoveredCount;
        }

        public int getValidatedCount() {
            return validatedCount;
        }

        public int getExpectedTotal() {
            return expectedTotal;
        }
    }

    private void collectAllOptionsViaVirtualScroll(String filterName, List<String> allLabels,
                                                   int expectedTotal) throws InterruptedException {
        By container = leftFilterPanel.filterOptionsContainer(filterName);
        if (!WaitUtil.isDisplay(container, 3)) {
            appendCapturedLabels(allLabels, leftFilterPanelUtil.getFilterOptionLabels(filterName));
            return;
        }

        DesktopBrowserElement viewport = driver.get().finder().findElement(container);
        scrollToTopOfVirtualList(viewport);
        Thread.sleep(SCROLL_SETTLE_MS);
        appendCapturedLabels(allLabels, leftFilterPanelUtil.getFilterOptionLabels(filterName));

        int scrollStep = getVirtualScrollStep(viewport);
        long measuredMaxTop = measureVirtualScrollMaxTop(viewport);
        Logger.logMessage(filterName + ": virtual scroll measured maxTop=" + measuredMaxTop + ", step=" + scrollStep);

        if (measuredMaxTop > 0) {
            collectViaScrollTop(filterName, allLabels, expectedTotal, viewport, scrollStep, measuredMaxTop);
        } else {
            collectViaScrollIntoView(filterName, allLabels, expectedTotal, viewport);
        }

        appendCapturedLabels(allLabels, leftFilterPanelUtil.getFilterOptionLabels(filterName));
        scrollToTopOfVirtualList(viewport);
    }

    /**
     * Scrolls the partner/options list down one step (CDK virtual scroll via scrollIntoView).
     */
    public void scrollPartnerListDown(String filterName, DesktopBrowserElement viewport) throws InterruptedException {
        scrollToRevealMoreOptions(viewport);
        Thread.sleep(SCROLL_SETTLE_MS);
    }

    public DesktopBrowserElement getFilterOptionsViewport(String filterName) {
        By container = leftFilterPanel.filterOptionsContainer(filterName);
        if (!WaitUtil.isDisplay(container, 3)) {
            return null;
        }
        return driver.get().finder().findElement(container);
    }

    private void collectViaScrollTop(String filterName, List<String> allLabels, int expectedTotal,
                                     DesktopBrowserElement viewport, int scrollStep, long maxScrollTop)
            throws InterruptedException {
        long scrollTop = 0;
        int previousUniqueCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels);
        int staleScrollAttempts = 0;

        while (scrollTop < maxScrollTop && staleScrollAttempts < COLLECTION_MAX_STALE_SCROLL_ATTEMPTS) {
            scrollTop = Math.min(scrollTop + scrollStep, maxScrollTop);
            scrollVirtualViewport(viewport, scrollTop);
            Thread.sleep(SCROLL_SETTLE_MS);
            appendCapturedLabels(allLabels, leftFilterPanelUtil.getFilterOptionLabels(filterName));

            if (expectedTotal > 0
                    && PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels) >= expectedTotal) {
                Logger.logMessage(filterName + ": reached expected total " + expectedTotal + " while scrolling");
                break;
            }

            int uniqueCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels);
            if (uniqueCount == previousUniqueCount) {
                staleScrollAttempts++;
            } else {
                staleScrollAttempts = 0;
                previousUniqueCount = uniqueCount;
            }
        }
    }

    private void collectViaScrollIntoView(String filterName, List<String> allLabels, int expectedTotal,
                                          DesktopBrowserElement viewport) throws InterruptedException {
        String lastAnchorLabel = null;
        int staleScrollAttempts = 0;

        Logger.logMessage(filterName + ": using scrollIntoView collection (CDK translateY viewport)");

        while (staleScrollAttempts < COLLECTION_MAX_STALE_SCROLL_ATTEMPTS) {
            List<String> visible = leftFilterPanelUtil.getFilterOptionLabels(filterName);
            int beforeUnique = PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels);
            appendCapturedLabels(allLabels, visible);
            int afterUnique = PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels);
            if (afterUnique > beforeUnique && afterUnique % 100 == 0) {
                Logger.logMessage(filterName + ": scroll progress " + afterUnique + " unique / "
                        + allLabels.size() + " raw rows");
            }

            if (expectedTotal > 0 && afterUnique >= expectedTotal) {
                Logger.logMessage(filterName + ": reached expected total " + expectedTotal + " while scrolling");
                break;
            }

            if (visible.isEmpty()) {
                scrollToRevealMoreOptions(viewport);
                Thread.sleep(SCROLL_SETTLE_MS);
                staleScrollAttempts++;
                continue;
            }

            String lastVisible = visible.get(visible.size() - 1);
            if (lastVisible.equals(lastAnchorLabel)) {
                staleScrollAttempts++;
            } else {
                staleScrollAttempts = 0;
                lastAnchorLabel = lastVisible;
            }

            scrollToRevealMoreOptions(viewport);
            Thread.sleep(SCROLL_SETTLE_MS);
        }

        Logger.logMessage(filterName + ": scrollIntoView finished with " + allLabels.size()
                + " raw row(s), " + PartnerOptionsDuplicateAnalyzer.uniqueCount(allLabels)
                + " unique label(s), staleAttempts=" + staleScrollAttempts);
    }

    private void scrollToTopOfVirtualList(DesktopBrowserElement viewport) {
        viewport.executeScript(
                "var viewport = arguments[0];"
                        + "viewport.scrollTop = 0;"
                        + "var items = viewport.querySelectorAll('.draggable-item, a.option-item');"
                        + "if (items.length > 0) { items[0].scrollIntoView({ block: 'start' }); }"
                        + "viewport.dispatchEvent(new Event('scroll', { bubbles: true }));");
    }

    private void scrollVirtualViewport(DesktopBrowserElement viewport, long scrollTop) {
        viewport.executeScript(
                "arguments[0].scrollTop = " + scrollTop + ";"
                        + "arguments[0].dispatchEvent(new Event('scroll', { bubbles: true }));");
    }

    private void scrollToRevealMoreOptions(DesktopBrowserElement viewport) {
        viewport.executeScript(
                "var viewport = arguments[0];"
                        + "var items = viewport.querySelectorAll('.draggable-item, a.option-item, .option-container');"
                        + "if (items.length > 0) {"
                        + "  items[items.length - 1].scrollIntoView({ block: 'end' });"
                        + "}"
                        + "viewport.dispatchEvent(new Event('scroll', { bubbles: true }));");
    }

    private void scrollLastVisibleOptionIntoView(String filterName, DesktopBrowserElement viewport, String label) {
        try {
            By optionRow = leftFilterPanel.filterOptionRow(filterName, label);
            driver.get().options().setElementTimeout(2000);
            List<DesktopBrowserElement> rows = driver.get().finder().findElements(optionRow);
            if (!rows.isEmpty()) {
                rows.get(rows.size() - 1).executeScript(
                        "arguments[0].scrollIntoView({ block: 'end' });");
                viewport.executeScript(
                        "arguments[0].dispatchEvent(new Event('scroll', { bubbles: true }));");
                return;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll option row into view for '" + label + "': " + e.getMessage());
        } finally {
            driver.get().options().setElementTimeout(60000);
        }
        scrollToRevealMoreOptions(viewport);
    }

    private long getVirtualScrollMaxTop(DesktopBrowserElement viewport, int expectedTotal, int scrollStep) {
        long measured = measureVirtualScrollMaxTop(viewport);
        if (measured > 0) {
            return measured;
        }
        if (expectedTotal > 0 && scrollStep > 0) {
            long estimated = Math.min((long) expectedTotal * scrollStep, 50_000L);
            Logger.logMessage("Virtual scroll maxTop=0; using estimated maxTop=" + estimated
                    + " (expectedTotal=" + expectedTotal + ", step=" + scrollStep + ")");
            return estimated;
        }
        return 0L;
    }

    private long measureVirtualScrollMaxTop(DesktopBrowserElement viewport) {
        Object result = viewport.executeScript(
                "var viewport = arguments[0];"
                        + "var spacer = viewport.querySelector('.cdk-virtual-scroll-spacer');"
                        + "var wrapper = viewport.querySelector('.cdk-virtual-scroll-content-wrapper');"
                        + "var totalHeight = 0;"
                        + "if (spacer) {"
                        + "  var styleHeight = spacer.style.height || '';"
                        + "  if (styleHeight) { totalHeight = parseFloat(styleHeight) || 0; }"
                        + "  if (!totalHeight) { totalHeight = spacer.offsetHeight || 0; }"
                        + "}"
                        + "if (!totalHeight && wrapper) { totalHeight = wrapper.offsetHeight || 0; }"
                        + "if (!totalHeight) { totalHeight = viewport.scrollHeight || 0; }"
                        + "return Math.max(0, totalHeight - viewport.clientHeight);");
        return toLong(result);
    }

    private int getVirtualScrollStep(DesktopBrowserElement viewport) {
        long clientHeight = toLong(viewport.executeScript("return arguments[0].clientHeight;"));
        Object itemHeight = viewport.executeScript(
                "var item = arguments[0].querySelector('.draggable-item, .option-item, .option-container');"
                        + "return item ? item.offsetHeight : 0;");
        long measuredItemHeight = toLong(itemHeight);
        if (measuredItemHeight > 0) {
            return (int) Math.max(MIN_VIRTUAL_SCROLL_STEP_PX, measuredItemHeight);
        }
        if (clientHeight > 0) {
            return (int) Math.max(MIN_VIRTUAL_SCROLL_STEP_PX, clientHeight / 3);
        }
        return SCROLL_STEP_PX;
    }

    private int getExpectedTotalOptionCount(String filterName) {
        int fromSelectAll = getSelectAllTotalCount(filterName);
        if (fromSelectAll > 0) {
            return fromSelectAll;
        }

        int fromSelectionCount = leftFilterPanelUtil.getTotalOptionCount(filterName);
        if (fromSelectionCount > 0) {
            return fromSelectionCount;
        }
        return -1;
    }

    private int getSelectAllTotalCount(String filterName) {
        try {
            if (!WaitUtil.isDisplay(leftFilterPanel.selectAllLabel(filterName), 3)) {
                return -1;
            }
            String text = driver.get().finder().findElement(leftFilterPanel.selectAllLabel(filterName)).getText().trim();
            Matcher matcher = COUNT_IN_PARENS.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read Select All total for " + filterName + ": " + e.getMessage());
        }
        return -1;
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
