package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;

import java.util.Arrays;
import java.util.List;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Runs one {@link LeftFilterTestCategory} for one filter name (data-driven per-filter suite).
 */
public final class LeftFilterPerFilterTestRunner {

    private LeftFilterPerFilterTestRunner() {
    }

    public static void run(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, ConsoleTab tab, String filterName,
                           LeftFilterTestCategory category, int filterIndexOneBased) throws InterruptedException {
        LeftFilterPerFilterConfig.FilterSpec spec = LeftFilterPerFilterConfig.specFor(tab, filterName);
        int tcId = category.testCaseId(filterIndexOneBased);
        Logger.logReportMessage("TC" + tcId + " [" + tab.getTabLabel() + "] " + filterName + " — " + category.getDescription());

        if (spec.isSkipped(category)) {
            Logger.logMessage("Skipping TC" + tcId + " for " + filterName + " (" + spec.getKind() + ")");
            return;
        }

        panelUtil.navigateToTab(tab);

        switch (category) {
            case BASIC:
                runBasic(softAssert, panelUtil, filterName, spec);
                break;
            case SEARCH:
                runSearch(softAssert, panelUtil, filterName, spec);
                break;
            case SELECT_ALL:
                runSelectAll(softAssert, panelUtil, filterName, spec);
                break;
            case TABLE_SYNC:
                runTableSync(softAssert, panelUtil, filterName, spec);
                break;
            case SCROLL:
                panelUtil.validateScrollFilterOptions(softAssert, filterName);
                break;
            case ACTIVE_FILTERS:
                runActiveFilters(softAssert, panelUtil, filterName, spec);
                break;
            case CLEAR_FILTERS:
                panelUtil.validateClearFilters(softAssert, filterName, null);
                break;
            default:
                Verify.softAssert(false, "Unhandled category: " + category);
        }

        panelUtil.resetFilterState(filterName);
    }

    private static void runBasic(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                 LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        Verify.softAssert(WaitUtil.isDisplay(panelUtil.getLeftFilterPanel().leftFilterByName(filterName), DEFAULT_WAIT_SECONDS),
                filterName + " is visible in left filter panel");
        panelUtil.validateExpandCollapseWorks(softAssert, filterName);

        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.RANGE) {
            panelUtil.validateRangeInputsPresent(softAssert, filterName);
            return;
        }

        panelUtil.validateOptionsAvailableOnExpand(softAssert, filterName);
        panelUtil.validateSelectAllAtTop(softAssert, filterName);
    }

    private static void runSearch(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                  LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.RANGE) {
            Logger.logMessage(filterName + " is a range filter — search category N/A");
            return;
        }
        panelUtil.expandFilter(filterName);
        List<String> labels = panelUtil.getFilterOptionLabels(filterName);
        Verify.softAssert(!labels.isEmpty(), filterName + " has at least one option for search test");
        String searchText = labels.get(0);
        Logger.logMessage(filterName + " search using first option: " + searchText);
        panelUtil.validateSearchInsideFilter(softAssert, filterName, searchText);
        panelUtil.clearFilterSearchInput(filterName);
    }

    private static void runSelectAll(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                     LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        if (spec.getKind() == LeftFilterPerFilterConfig.FilterKind.LARGE_LIST) {
            panelUtil.validateSelectAllDisabledWhenLargeOptionSet(softAssert, filterName);
            return;
        }
        panelUtil.validateSelectAllAtTop(softAssert, filterName);
        panelUtil.validateSelectAllSelectsAll(softAssert, filterName);

        final int optionsToPartiallySelect = 2;
        panelUtil.validateSelectAllIndeterminateAndDeselect(softAssert, filterName, optionsToPartiallySelect);

        String firstOption = resolveSampleOption(panelUtil, filterName, spec);
        panelUtil.validateFilterCountInsideAndOutside(softAssert, filterName, firstOption);
    }

    private static void runTableSync(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                     LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        String option = resolveSampleOption(panelUtil, filterName, spec);
        panelUtil.validateTableRecordCountReflectsFilter(softAssert, filterName, option);
        panelUtil.validateTableRecordsMatchFilter(softAssert, filterName, option, spec.getTableColumn());
    }

    private static void runActiveFilters(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                         LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        String first = resolveSampleOption(panelUtil, filterName, spec);
        panelUtil.validateSelectedOptionsInActiveFilters(softAssert, filterName, first);

        if (spec.getSecondSampleOption() != null) {
            panelUtil.selectFilterOption(filterName, spec.getSecondSampleOption());
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            panelUtil.validateMultipleActiveFilterChips(softAssert, filterName,
                    Arrays.asList(first, spec.getSecondSampleOption()));
        }
    }

    private static String resolveSampleOption(LeftFilterPanelUtil panelUtil, String filterName,
                                              LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        if (spec.getSampleOption() != null) {
            return spec.getSampleOption();
        }
        panelUtil.expandFilter(filterName);
        List<String> labels = panelUtil.getFilterOptionLabels(filterName);
        Verify.softAssert(!labels.isEmpty(), filterName + " has at least one option for dynamic sample selection");
        return labels.get(0);
    }
}
