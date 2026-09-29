package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * BSD-30019 story validation: error code count sync, code in grid column, and [No Value] behaviour.
 */
public final class ErrorMessageBsd30019Util {

    public static final String FILTER = "Error message";
    public static final String NO_VALUE_LABEL = "[No Value]";

    private static final Pattern ERROR_CODE_PREFIX = Pattern.compile(
            "(UWF[A-Z0-9_]+|UWFSYS\\d+|UWFUSR\\d+|UMTSVAL\\d+|ASL\\d+|SR\\d+|AS\\d+)\\s*:",
            Pattern.CASE_INSENSITIVE);

    private ErrorMessageBsd30019Util() {
    }

    public static void validateErrorCodeCountSync(SoftAssert softAssert, LeftFilterPanelUtil panel,
            ConsoleTab tab) throws InterruptedException {
        panel.navigateToTab(tab);
        panel.clearAllActiveFiltersIfPresent();
        panel.expandFilter(FILTER);
        String option = resolveFirstErrorCodeOption(panel);
        if (option == null) {
            Logger.logMessage("BSD-30019: no non-zero error code options on " + tab.getTabLabel() + " — skip count sync");
            panel.collapseFilter(FILTER);
            return;
        }
        int filterCount = panel.getFilterOptionCount(FILTER, option);
        panel.selectFilterOption(FILTER, option);
        panel.waitForTableRecordCount(filterCount, DEFAULT_WAIT_SECONDS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        int tableCount = panel.getTableRecordCount();
        Verify.softAssert(tableCount == filterCount,
                tab.getTabLabel() + " Error message count sync for " + option
                        + " (filter=" + filterCount + ", table=" + tableCount + ")");
        panel.resetFilterState(FILTER);
    }

    public static void validateErrorCodeInTableColumn(SoftAssert softAssert, LeftFilterPanelUtil panel,
            ConsoleTab tab) throws InterruptedException {
        panel.navigateToTab(tab);
        panel.clearAllActiveFiltersIfPresent();
        panel.expandFilter(FILTER);
        String option = resolveFirstErrorCodeOption(panel);
        if (option == null) {
            Logger.logMessage("BSD-30019: no non-zero error code options on " + tab.getTabLabel() + " — skip code match");
            panel.collapseFilter(FILTER);
            return;
        }
        panel.selectFilterOption(FILTER, option);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        String column = tab == ConsoleTab.ORDERS ? ManageColumnOptions.ERROR_MESSAGES : ManageColumnOptions.ERROR_MESSAGE;
        List<String> values = panel.readVisibleColumnValues(column);
        Verify.softAssert(!values.isEmpty(),
                tab.getTabLabel() + " has visible rows in " + column + " after selecting " + option);
        for (String cell : values) {
            Verify.softAssert(cell.toUpperCase(Locale.ROOT).contains(option.toUpperCase(Locale.ROOT)),
                    column + " contains error code " + option + " (cell=" + cell + ")");
        }
        panel.resetFilterState(FILTER);
    }

    public static void validateNoValueHasNoErrorCode(SoftAssert softAssert, LeftFilterPanelUtil panel,
            ConsoleTab tab) throws InterruptedException {
        panel.navigateToTab(tab);
        panel.clearAllActiveFiltersIfPresent();
        panel.expandFilter(FILTER);
        String noValue = resolveNoValueOption(panel);
        if (noValue == null) {
            Logger.logMessage("BSD-30019: [No Value] option not found on " + tab.getTabLabel() + " — skip");
            panel.collapseFilter(FILTER);
            return;
        }
        panel.selectFilterOption(FILTER, noValue);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        String column = tab == ConsoleTab.ORDERS ? ManageColumnOptions.ERROR_MESSAGES : ManageColumnOptions.ERROR_MESSAGE;
        List<String> values = panel.readVisibleColumnValuesIncludingBlanks(column);
        for (String cell : values) {
            if (cell == null || cell.isBlank()) {
                continue;
            }
            Verify.softAssert(!ERROR_CODE_PREFIX.matcher(cell).find(),
                    tab.getTabLabel() + " [No Value] row must not show error code prefix (cell=" + cell + ")");
        }
        panel.resetFilterState(FILTER);
    }

    private static String resolveFirstErrorCodeOption(LeftFilterPanelUtil panel) throws InterruptedException {
        for (String label : panel.resolveAllNonZeroFilterOptions(FILTER)) {
            if (!isNoValueLabel(label)) {
                return label;
            }
        }
        return null;
    }

    private static String resolveNoValueOption(LeftFilterPanelUtil panel) throws InterruptedException {
        panel.expandFilter(FILTER);
        for (String label : panel.getFilterOptionLabels(FILTER)) {
            if (isNoValueLabel(label)) {
                return label;
            }
        }
        return null;
    }

    static boolean isNoValueLabel(String label) {
        if (label == null) {
            return false;
        }
        return label.replaceAll("\\s+", " ").equalsIgnoreCase(NO_VALUE_LABEL);
    }
}
