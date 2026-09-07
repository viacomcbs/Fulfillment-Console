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
        String searchText = panelUtil.resolveFirstSelectableFilterOption(filterName);
        if (searchText == null) {
            Verify.softAssert(false, filterName + " has at least one option for search test");
            return;
        }
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

        // Flag: Is flagged cascades to all children — use 1 partial option (see FlagFilterConstants).
        final int optionsToPartiallySelect = FlagFilterConstants.FILTER_DISPLAY_NAME.equalsIgnoreCase(filterName)
                ? 1 : 2;
        panelUtil.validateSelectAllIndeterminateAndDeselect(softAssert, filterName, optionsToPartiallySelect);

        String firstOption = resolveSampleOption(panelUtil, filterName, spec);
        panelUtil.validateFilterCountInsideAndOutside(softAssert, filterName, firstOption);
    }

    private static void runTableSync(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                     LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        String option = resolveSampleOption(panelUtil, filterName, spec);
        panelUtil.validateTableRecordCountReflectsFilter(softAssert, filterName, option);
        String tableColumn = spec.getTableColumn();
        if (tableColumn != null && !tableColumn.isBlank()) {
            panelUtil.validateTableRecordsMatchFilter(softAssert, filterName, option, tableColumn);
        } else {
            Logger.logMessage(filterName + " has no table column — skipping row/cell value checks (count-only sync)");
        }
    }

    private static void runActiveFilters(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName,
                                         LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        panelUtil.validateActiveFiltersTwoOptions(softAssert, filterName);
    }

    private static String resolveSampleOption(LeftFilterPanelUtil panelUtil, String filterName,
                                              LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        if (spec.getSampleOption() != null) {
            return spec.getSampleOption();
        }
        panelUtil.expandFilter(filterName);
        String option = panelUtil.resolveFirstSelectableFilterOption(filterName);
        Verify.softAssert(option != null, filterName + " has at least one selectable option for dynamic sample");
        return option;
    }
}
