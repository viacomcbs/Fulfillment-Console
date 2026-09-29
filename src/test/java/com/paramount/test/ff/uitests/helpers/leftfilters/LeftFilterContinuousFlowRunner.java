package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;

/**
 * Runs all eight left-filter scenarios for one filter in a single browser session without refresh.
 * <p>
 * Fixed order (must not change):
 * Basic → Option order → Scroll → Search → Table sync → Active filters → Clear filters → Select all.
 * Season/Episode only: Enter Range (TC310/312) runs after Clear filters.
 * <p>
 * Search selects the first matching option; table sync, active filters, and clear filters reuse that selection.
 * Each step is recorded in {@link LeftFilterEmailReport} as a separate scenario row (8 per filter).
 */
public final class LeftFilterContinuousFlowRunner {

    @FunctionalInterface
    private interface FlowStep {
        void execute(SoftAssert stepAssert) throws InterruptedException;
    }

    private LeftFilterContinuousFlowRunner() {
    }

    public static void run(SoftAssert flowSoftAssert, LeftFilterPanelUtil panelUtil, ConsoleTab tab,
                           String filterName, int filterIndexOneBased) throws InterruptedException {
        LeftFilterPerFilterConfig.FilterSpec spec = LeftFilterPerFilterConfig.specFor(tab, filterName);
        panelUtil.navigateToTab(tab);

        Logger.logReportMessage("=== Continuous flow start: " + filterName + " (8 scenarios, no browser refresh) ===");

        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.BASIC.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.BASIC, (step) -> runBasic(step, panelUtil, filterName, spec, filterIndexOneBased));
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.BASIC.testCaseId(filterIndexOneBased) + 1,
                "OptionOrder", (step) -> runOptionOrder(step, panelUtil, filterName, spec, filterIndexOneBased));
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.SCROLL.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.SCROLL, (step) -> runScroll(step, panelUtil, filterName, spec, filterIndexOneBased));

        final String[] selectedOption = { null };
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.SEARCH.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.SEARCH, (step) -> {
                    selectedOption[0] = runSearchAndSelect(step, panelUtil, filterName, spec, filterIndexOneBased);
                });
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.TABLE_SYNC.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.TABLE_SYNC,
                (step) -> runTableSync(step, panelUtil, tab, filterName, spec, filterIndexOneBased, selectedOption[0]));
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.ACTIVE_FILTERS.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.ACTIVE_FILTERS,
                (step) -> runActiveFilters(step, panelUtil, filterName, spec, filterIndexOneBased, selectedOption[0]));
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.CLEAR_FILTERS.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.CLEAR_FILTERS,
                (step) -> runClearFilters(step, panelUtil, filterName, spec, filterIndexOneBased, selectedOption[0]));
        runStep(flowSoftAssert, filterName, LeftFilterTestCategory.SELECT_ALL.testCaseId(filterIndexOneBased),
                LeftFilterTestCategory.SELECT_ALL,
                (step) -> runSelectAll(step, panelUtil, filterName, spec, filterIndexOneBased));
        if (OrdersLeftFilter.SEASON_NUMBER.getDisplayName().equals(filterName)
                || OrdersLeftFilter.EPISODE_NUMBER.getDisplayName().equals(filterName)) {
            runStep(flowSoftAssert, filterName, 300 + filterIndexOneBased, "EnterRange",
                    (step) -> runEnterRange(step, panelUtil, filterName, filterIndexOneBased));
        }

        panelUtil.resetFilterState(filterName);
        Logger.logReportMessage("=== Continuous flow complete: " + filterName + " ===");
    }

    private static void runStep(SoftAssert flowSoftAssert, String filterName, int tcId,
                                LeftFilterTestCategory category, FlowStep step) throws InterruptedException {
        runStep(flowSoftAssert, filterName, tcId, LeftFilterEmailReport.stepSuffixFor(category), step);
    }

    private static void runStep(SoftAssert flowSoftAssert, String filterName, int tcId, String stepSuffix,
                                FlowStep step) throws InterruptedException {
        String reportClass = LeftFilterEmailReport.flowStepClassName(tcId, filterName, stepSuffix);
        SoftAssert stepAssert = new SoftAssert("tc" + tcId, reportClass);
        SoftAssert previous = BaseTest.softAssert;
        BaseTest.softAssert = stepAssert;
        long startMs = System.currentTimeMillis();
        try {
            step.execute(stepAssert);
            stepAssert.assertAll();
            LeftFilterEmailReport.recordFlowStepResult(reportClass, "PASS", null, System.currentTimeMillis() - startMs);
        } catch (AssertionError e) {
            String message = e.getMessage() != null ? e.getMessage() : "Step failed";
            LeftFilterEmailReport.recordFlowStepResult(reportClass, "FAIL", message, System.currentTimeMillis() - startMs);
            Verify.softAssert1(false, reportClass + ": " + firstLine(message), flowSoftAssert);
        } finally {
            BaseTest.softAssert = previous;
        }
    }

    private static String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "Assertion failed";
        }
        int idx = message.indexOf('\n');
        return idx > 0 ? message.substring(0, idx).trim() : message.trim();
    }

    private static void runBasic(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                 LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex) throws InterruptedException {
        int tcId = LeftFilterTestCategory.BASIC.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Basic — continuous flow step 1/8");
        if (spec.isSkipped(LeftFilterTestCategory.BASIC)) {
            Logger.logMessage("Skipping Basic for " + filterName);
            return;
        }
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.RANGE) {
            Verify.softAssert(WaitUtil.isDisplay(panelUtil.getLeftFilterPanel().leftFilterByName(filterName),
                            LeftFilterConstants.DEFAULT_WAIT_SECONDS),
                    filterName + " is visible in left filter panel");
            panelUtil.validateExpandCollapseWorks(softAssert, filterName);
            panelUtil.validateRangeInputsPresent(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        panelUtil.validateBasicSmoke(softAssert, filterName);
    }

    private static void runOptionOrder(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                       LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex)
            throws InterruptedException {
        int tcId = LeftFilterTestCategory.BASIC.testCaseId(filterIndex) + 1;
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Option order — continuous flow step 2/8");
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.RANGE) {
            Logger.logMessage(filterName + " is a range filter — option order N/A");
            return;
        }
        if (FlagFilterConstants.FILTER_DISPLAY_NAME.equalsIgnoreCase(filterName)) {
            panelUtil.validateFlagOptionListOrderSmoke(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        panelUtil.validateOptionListOrderSmoke(softAssert, filterName);
    }

    private static void runScroll(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                  LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex)
            throws InterruptedException {
        int tcId = LeftFilterTestCategory.SCROLL.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Scroll — continuous flow step 3/8");
        if (spec.isSkipped(LeftFilterTestCategory.SCROLL)) {
            Logger.logMessage("Skipping Scroll for " + filterName);
            return;
        }
        panelUtil.validateScrollFilterOptions(softAssert, filterName);
    }

    private static void runSelectAll(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                     LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex)
            throws InterruptedException {
        int tcId = LeftFilterTestCategory.SELECT_ALL.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Select all — continuous flow step 8/8");
        if (spec.isSkipped(LeftFilterTestCategory.SELECT_ALL)) {
            Logger.logMessage("Skipping Select all for " + filterName);
            return;
        }
        if (OrdersLeftFilter.PARTNER.getDisplayName().equals(filterName)) {
            panelUtil.validatePartnerSelectAllSmoke(softAssert, filterName,
                    LeftFilterConstants.PARTNER_SELECT_ALL_SEARCH_TEXT);
            panelUtil.resetFilterState(filterName);
            return;
        }
        if (OrdersLeftFilter.SERIES_TITLE.getDisplayName().equals(filterName)) {
            panelUtil.validateSeriesTitleSelectAllSmoke(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        if (OrdersLeftFilter.SEASON_NUMBER.getDisplayName().equals(filterName)) {
            panelUtil.validateSeasonNumberSelectAllSmoke(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        if (OrdersLeftFilter.EPISODE_NUMBER.getDisplayName().equals(filterName)) {
            panelUtil.validateEpisodeNumberSelectAllSmoke(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.LARGE_LIST) {
            panelUtil.validateSelectAllDisabledWhenLargeOptionSet(softAssert, filterName);
            panelUtil.resetFilterState(filterName);
            return;
        }
        panelUtil.validateSelectAllAtTop(softAssert, filterName);
        panelUtil.validateSelectAllSelectsAll(softAssert, filterName);
        int partialCount = FlagFilterConstants.FILTER_DISPLAY_NAME.equalsIgnoreCase(filterName) ? 1 : 2;
        panelUtil.validateSelectAllIndeterminateAndDeselect(softAssert, filterName, partialCount);
        panelUtil.expandFilter(filterName);
        String firstOption = panelUtil.resolveFirstSelectableFilterOption(filterName);
        if (firstOption != null) {
            panelUtil.validateFilterCountInsideAndOutside(softAssert, filterName, firstOption);
        }
        panelUtil.resetFilterState(filterName);
    }

    private static String runSearchAndSelect(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                             LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex)
            throws InterruptedException {
        int tcId = LeftFilterTestCategory.SEARCH.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Search — continuous flow step 5/8");
        if (spec.isSkipped(LeftFilterTestCategory.SEARCH)) {
            Logger.logMessage("Skipping Search for " + filterName);
            return null;
        }
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.RANGE) {
            Logger.logMessage(filterName + " is a range filter — search N/A");
            return null;
        }
        return panelUtil.validateSearchAndSelectFirstOptionForFlow(softAssert, filterName);
    }

    private static void runTableSync(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, ConsoleTab tab,
                                     String filterName, LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex,
                                     String selectedOption) throws InterruptedException {
        int tcId = LeftFilterTestCategory.TABLE_SYNC.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Table sync — continuous flow step 6/8");
        if (spec.isSkipped(LeftFilterTestCategory.TABLE_SYNC)) {
            Logger.logMessage("Skipping Table sync for " + filterName);
            return;
        }
        if (selectedOption == null || selectedOption.isBlank()) {
            LeftFilterTableSyncFlowHelper.runStandalone(softAssert, panelUtil, filterName);
            return;
        }
        LeftFilterTableSyncFlowHelper.runForSelectedOption(softAssert, panelUtil, tab, filterName, selectedOption, spec);
    }

    private static void runEnterRange(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                      int filterIndex) throws InterruptedException {
        if (OrdersLeftFilter.SEASON_NUMBER.getDisplayName().equals(filterName)) {
            int tcId = 300 + filterIndex;
            Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Enter Range — continuous flow step 9");
            panelUtil.validateSeasonNumberEnterRangeSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.EPISODE_NUMBER.getDisplayName().equals(filterName)) {
            int tcId = 300 + filterIndex;
            Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Enter Range — continuous flow step 9");
            panelUtil.validateEpisodeNumberEnterRangeSmoke(softAssert, filterName);
        }
    }

    private static void runActiveFilters(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                         LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex,
                                         String selectedOption) throws InterruptedException {
        int tcId = LeftFilterTestCategory.ACTIVE_FILTERS.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Active filters — continuous flow step 7/8");
        if (spec.isSkipped(LeftFilterTestCategory.ACTIVE_FILTERS)) {
            Logger.logMessage("Skipping Active filters for " + filterName);
            return;
        }
        if (selectedOption == null || selectedOption.isBlank()) {
            panelUtil.validateActiveFiltersForPreservedSelection(softAssert, filterName);
            return;
        }
        panelUtil.validateActiveFiltersForSelectedOption(softAssert, filterName, selectedOption);
    }

    private static void runClearFilters(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                        LeftFilterPerFilterConfig.FilterSpec spec, int filterIndex,
                                        String selectedOption) throws InterruptedException {
        int tcId = LeftFilterTestCategory.CLEAR_FILTERS.testCaseId(filterIndex);
        Logger.logReportMessage("TC" + tcId + " [" + filterName + "] Clear filters — continuous flow step 8/8");
        if (spec.isSkipped(LeftFilterTestCategory.CLEAR_FILTERS)) {
            Logger.logMessage("Skipping Clear filters for " + filterName);
            return;
        }
        panelUtil.validateClearFiltersControlAvailable(softAssert, filterName, selectedOption);
    }
}
