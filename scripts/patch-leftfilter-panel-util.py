#!/usr/bin/env python3
"""Apply missing-method recovery patches to LeftFilterPanelUtil.java."""
from pathlib import Path

TARGET = Path(r"c:\FulfillmentConsole\Fulfillment-Console\src\test\java\com\paramount\test\ff\uitests\helpers\leftfilters\LeftFilterPanelUtil.java")

INSERTION_BLOCK = r'''
    // --- table sync helpers (keep-expanded / continuous flow) ---

    public String resolveFirstSelectedNonZeroFilterOption(String filterName) throws InterruptedException {
        expandFilter(filterName);
        for (FilterOption option : getFilterOptions(filterName)) {
            if (option.getCount() > 0
                    && !isAmbiguousFilterOptionLabel(option.getLabel())
                    && isFilterOptionSelected(filterName, option.getLabel())) {
                return option.getLabel();
            }
        }
        return null;
    }

    public List<String> resolveAllSelectedNonZeroFilterOptions(String filterName) throws InterruptedException {
        expandFilter(filterName);
        List<String> selected = new ArrayList<>();
        for (FilterOption option : getFilterOptions(filterName)) {
            if (option.getCount() > 0
                    && !isAmbiguousFilterOptionLabel(option.getLabel())
                    && isFilterOptionSelected(filterName, option.getLabel())) {
                selected.add(option.getLabel());
            }
        }
        return selected;
    }

    /**
     * Table sync setup: reuse preserved selection in keep-expanded suites; otherwise clear and pick first non-zero.
     */
    private String resolveTableSyncOptionLabel(String filterName) throws InterruptedException {
        if (LeftFilterSessionHelper.shouldKeepFilterExpanded(filterName)) {
            String preserved = resolveFirstSelectedNonZeroFilterOption(filterName);
            if (preserved != null) {
                Logger.logReportMessage("Table sync — reusing preserved " + filterName + " selection: " + preserved);
                return preserved;
            }
        }
        ensureNoOptionsSelected(filterName);
        String optionLabel = resolveFirstNonZeroFilterOption(filterName);
        if (optionLabel != null) {
            selectFilterOption(filterName, optionLabel);
        }
        return optionLabel;
    }

    private static boolean orderLevelCellMatchesSelectedOption(String cellValue, String optionLabel) {
        if (cellValue == null || optionLabel == null) {
            return false;
        }
        return cellValue.trim().equalsIgnoreCase(optionLabel.trim());
    }

    private static boolean orderLevelCellMatchesAnySelectedOption(String cellValue, List<String> optionLabels) {
        if (cellValue == null || optionLabels == null) {
            return false;
        }
        for (String label : optionLabels) {
            if (orderLevelCellMatchesSelectedOption(cellValue, label)) {
                return true;
            }
        }
        return false;
    }

    private void assertFirstVisibleOrderRowColumnSync(SoftAssert softAssert, String filterName,
                                                      String tableColumnName, List<String> expectedOptions) {
        scrollOrdersGridUntilColumnVisible(tableColumnName);
        List<String> columnValues = getVisibleTableColumnValues(tableColumnName);
        Verify.softAssert(!columnValues.isEmpty(),
                filterName + " has visible " + tableColumnName + " column values (first-row sync)");
        String cellText = columnValues.get(0);
        if (expectedOptions.size() > 1) {
            Verify.softAssert(orderLevelCellMatchesAnySelectedOption(cellText, expectedOptions),
                    filterName + " first visible " + tableColumnName + " row matches one of "
                            + expectedOptions + " (actual='" + cellText + "')");
        } else {
            Verify.softAssert(orderLevelCellMatchesSelectedOption(cellText, expectedOptions.get(0)),
                    filterName + " first visible " + tableColumnName + " row matches '"
                            + expectedOptions.get(0) + "' (actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " first-row " + tableColumnName + " sync OK (checked 1 of "
                + columnValues.size() + " visible cell(s))");
    }

    private void assertCountSync(SoftAssert softAssert, String filterName, String optionLabel, int filterOptionCount)
            throws InterruptedException {
        waitUtils.waitForVisibilityOfElement(leftFilterPanel.tableRecordCountLabel(), DEFAULT_WAIT_SECONDS);
        waitForTableRecordCount(filterOptionCount, DEFAULT_WAIT_SECONDS);
        int tableRecordCount = getTableRecordCount();
        Verify.softAssert(tableRecordCount == filterOptionCount,
                filterName + " table record count matches filter option count for " + optionLabel
                        + " (filter=" + filterOptionCount + ", table=" + tableRecordCount + ")");
        Logger.logMessage(filterName + " count sync OK for " + optionLabel + ": filter=" + filterOptionCount
                + ", table=" + tableRecordCount);
    }

    private void runCountOnlyTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for count-only table sync");
            return;
        }
        String optionLabel = resolveTableSyncOptionLabel(filterName);
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping count-only table sync");
            collapseFilter(filterName);
            return;
        }
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
        if (!LeftFilterSessionHelper.shouldKeepFilterExpanded(filterName)) {
            collapseFilter(filterName);
        }
    }

    private void runCountOnlyTableSyncForSelectedOption(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        selectFilterOptionIfNotSelected(filterName, optionLabel);
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
    }

    public void validateCountOnlyTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateCountOnlyTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                            String optionLabel) throws InterruptedException {
        runCountOnlyTableSyncForSelectedOption(softAssert, filterName, optionLabel);
    }

    public void validateOrderLevelColumnTableSyncSmoke(SoftAssert softAssert, String filterName,
                                                        String tableColumnName) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for table sync");
            return;
        }

        List<String> preservedSelections = LeftFilterSessionHelper.shouldKeepFilterExpanded(filterName)
                ? resolveAllSelectedNonZeroFilterOptions(filterName) : Collections.emptyList();
        boolean multiSelectPreserved = preservedSelections.size() > 1;
        String optionLabel;

        if (multiSelectPreserved) {
            optionLabel = String.join(" + ", preservedSelections);
            Logger.logReportMessage("Table sync — preserved multi-select for " + filterName + ": "
                    + preservedSelections);
        } else {
            optionLabel = resolveTableSyncOptionLabel(filterName);
        }
        if (optionLabel == null) {
            Logger.logMessage(filterName + " has no options with count > 0 — skipping table sync");
            collapseFilter(filterName);
            return;
        }

        int filterOptionCount;
        if (multiSelectPreserved) {
            filterOptionCount = 0;
            for (String sel : preservedSelections) {
                filterOptionCount += getFilterOptionCount(filterName, sel);
            }
        } else {
            filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        }
        Verify.softAssert(filterOptionCount > 0,
                filterName + " option '" + optionLabel + "' has count > 0 (actual=" + filterOptionCount + ")");
        Logger.logMessage(filterName + " table sync option='" + optionLabel + "' filterCount=" + filterOptionCount
                + " column='" + tableColumnName + "'");

        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
        List<String> expected = multiSelectPreserved ? preservedSelections : Collections.singletonList(optionLabel);
        assertFirstVisibleOrderRowColumnSync(softAssert, filterName, tableColumnName, expected);

        if (!LeftFilterSessionHelper.shouldKeepFilterExpanded(filterName)) {
            collapseFilter(filterName);
        }
    }

    public void validateOrderLevelColumnTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                                   String optionLabel, String tableColumnName)
            throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        selectFilterOptionIfNotSelected(filterName, optionLabel);
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
        assertFirstVisibleOrderRowColumnSync(softAssert, filterName, tableColumnName,
                Collections.singletonList(optionLabel));
    }

    public void validatePartnerSelectAllSmoke(SoftAssert softAssert, String filterName, String searchText)
            throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        if (!isFilterExpanded(filterName)) {
            Verify.softAssert(false, filterName + " filter could not be expanded for TC406");
            return;
        }
        ensureNoOptionsSelected(filterName);

        int totalOptions = getTotalOptionCount(filterName);
        Verify.softAssert(totalOptions > SELECT_ALL_DISABLED_OPTION_THRESHOLD,
                filterName + " total option count exceeds Select all threshold (count=" + totalOptions + ")");
        Verify.softAssert(isSelectAllDisabled(filterName),
                filterName + " Select all is disabled when total options > "
                        + SELECT_ALL_DISABLED_OPTION_THRESHOLD);

        String tooltip = hoverSelectAllAndReadTooltip(filterName);
        Verify.softAssert(tooltip != null && !tooltip.isEmpty(),
                filterName + " Select all tooltip visible on hover when disabled");

        List<String> searchResults = searchFilterOptions(filterName, searchText);
        Verify.softAssert(!searchResults.isEmpty(),
                filterName + " search returned results for: " + searchText);
        Verify.softAssert(!isSelectAllDisabled(filterName),
                filterName + " Select all is enabled after search narrows options (search='" + searchText + "')");

        clearFilterSearchInput(filterName);
        collapseFilter(filterName);
    }

    public void validatePartnerTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.PARTNER);
    }

    public void validatePartnerTableSyncForSelectedOption(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        validateOrderLevelColumnTableSyncForSelectedOption(softAssert, filterName, optionLabel,
                ManageColumnOptions.PARTNER);
    }

    public void validateSeriesTitleSelectAllSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        String searchText = resolveFirstSelectableFilterOption(filterName);
        if (searchText == null || searchText.length() < 3) {
            searchText = "the";
        } else {
            searchText = searchText.substring(0, Math.min(3, searchText.length()));
        }
        validatePartnerSelectAllSmoke(softAssert, filterName, searchText);
    }

    public void validateSeasonNumberSelectAllSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateSeriesTitleSelectAllSmoke(softAssert, filterName);
    }

    public void validateEpisodeNumberSelectAllSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateSeriesTitleSelectAllSmoke(softAssert, filterName);
    }

    private void runEnterRangeSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        expandFilter(filterName);
        validateRangeInputsPresent(softAssert, filterName);
        List<String> numericOptions = new ArrayList<>();
        for (FilterOption option : getFilterOptions(filterName)) {
            if (option.getLabel().matches("\\d+")) {
                numericOptions.add(option.getLabel());
            }
        }
        Verify.softAssert(numericOptions.size() >= 2,
                filterName + " has at least two numeric options for range test (found=" + numericOptions + ")");
        if (numericOptions.size() < 2) {
            collapseFilter(filterName);
            return;
        }
        String fromValue = numericOptions.get(0);
        String toValue = numericOptions.get(1);
        DriverUtil.sendKeyToElement(leftFilterPanel.filterRangeFromInput(filterName), DEFAULT_WAIT_SECONDS, fromValue);
        DriverUtil.sendKeyToElement(leftFilterPanel.filterRangeToInput(filterName), DEFAULT_WAIT_SECONDS, toValue);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        Verify.softAssert(getTableRecordCount() >= 0,
                filterName + " table record count available after Enter Range " + fromValue + "-" + toValue);
        collapseFilter(filterName);
    }

    public void validateSeasonNumberEnterRangeSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runEnterRangeSmoke(softAssert, filterName);
    }

    public void validateEpisodeNumberEnterRangeSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runEnterRangeSmoke(softAssert, filterName);
    }

    public void validateDemandSystemTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateDemandSystemTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                               String optionLabel) throws InterruptedException {
        runCountOnlyTableSyncForSelectedOption(softAssert, filterName, optionLabel);
    }

    public void validateDemandSystemTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateDeliveryProtocolTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateDeliveryProtocolTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                                   String optionLabel) throws InterruptedException {
        runCountOnlyTableSyncForSelectedOption(softAssert, filterName, optionLabel);
    }

    public void validateLanguageTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateLanguageTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                             String optionLabel) throws InterruptedException {
        runCountOnlyTableSyncForSelectedOption(softAssert, filterName, optionLabel);
    }

    public void validateRegionTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateRegionTableSyncForSelectedOption(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        runCountOnlyTableSyncForSelectedOption(softAssert, filterName, optionLabel);
    }

    public void validateContentTypeTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.CONTENT_TYPE);
    }

    public void validateJobTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.JOB);
    }

    public void validateJobTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateSeriesTitleTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.TITLE_SEASON_EPISODE);
    }

    public void validateSeasonNumberTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.TITLE_SEASON_EPISODE);
    }

    public void validateEpisodeNumberTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.TITLE_SEASON_EPISODE);
    }

    public void validateFranchiseTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateFranchiseTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateSystemNameTableSyncSmoke(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateSystemNameTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateOrderStatusTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateEnvironmentTableSyncOnLineItemsTab(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        runCountOnlyTableSyncSmoke(softAssert, filterName);
    }

    public void validateSubmittedByTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                              String optionLabel) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        selectFilterOptionIfNotSelected(filterName, optionLabel);
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.SUBMITTED_BY);
        List<String> values = getVisibleTableColumnValues(ManageColumnOptions.SUBMITTED_BY);
        Verify.softAssert(!values.isEmpty(),
                filterName + " has visible Submitted By values after selecting " + optionLabel);
        Verify.softAssert(submittedByCellMatchesSelectedOption(values.get(0), optionLabel),
                filterName + " first visible Submitted By row matches '" + optionLabel + "' (actual='"
                        + values.get(0) + "')");
    }

    public void validateAssignedToTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                             String optionLabel) throws InterruptedException {
        ensureOrdersDataLoaded(softAssert);
        expandFilter(filterName);
        selectFilterOptionIfNotSelected(filterName, optionLabel);
        int filterOptionCount = getFilterOptionCount(filterName, optionLabel);
        assertCountSync(softAssert, filterName, optionLabel, filterOptionCount);
        scrollOrdersGridUntilColumnVisible(ManageColumnOptions.ASSIGNED_TO);
        List<String> values = getVisibleTableColumnValues(ManageColumnOptions.ASSIGNED_TO, true);
        Verify.softAssert(!values.isEmpty(),
                filterName + " has visible Assigned to values after selecting " + optionLabel);
        if (isUnassignedFilterOption(optionLabel)) {
            Verify.softAssert(assignedToCellIsBlank(values.get(0)),
                    filterName + " first visible Assigned to row is blank for Unassigned");
        } else {
            Verify.softAssert(assignedToCellMatchesSelectedPerson(values.get(0), optionLabel),
                    filterName + " first visible Assigned to row matches '" + optionLabel + "'");
        }
    }

    public void validateActivityTypeTableSyncForSelectedOption(SoftAssert softAssert, String filterName,
                                                               String optionLabel) throws InterruptedException {
        validateActivityTypeTableSyncSmoke(softAssert, filterName);
    }

    public void validateSelectAllSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateSelectAllAtTop(softAssert, filterName);
        validateSelectAllSelectsAll(softAssert, filterName);
        int partialCount = FlagFilterConstants.FILTER_DISPLAY_NAME.equalsIgnoreCase(filterName) ? 1 : 2;
        validateSelectAllIndeterminateAndDeselect(softAssert, filterName, partialCount);
    }

    public void validateScrollSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateScrollFilterOptions(softAssert, filterName);
    }

    public void validateActiveFiltersSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateActiveFiltersTwoOptions(softAssert, filterName);
    }

    public void validateClearFiltersSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateClearFilters(softAssert, filterName, null);
    }

    public void validateClearFiltersControlAvailable(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        expandFilter(filterName);
        String option = resolveTableSyncOptionLabel(filterName);
        if (option == null) {
            option = resolveFirstSelectableFilterOption(filterName);
        }
        Verify.softAssert(option != null, filterName + " has a selectable option for Clear filters check");
        if (option != null) {
            selectFilterOptionIfNotSelected(filterName, option);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }
        clickActiveFiltersButton();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanel.activeFiltersClearButton(), DEFAULT_WAIT_SECONDS),
                "Active filters Clear control visible for " + filterName);
    }

    public void validateActiveFiltersForPreservedSelection(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        expandFilter(filterName);
        List<String> preserved = resolveAllSelectedNonZeroFilterOptions(filterName);
        if (preserved.isEmpty()) {
            String option = resolveTableSyncOptionLabel(filterName);
            if (option != null) {
                preserved = Collections.singletonList(option);
            }
        }
        Verify.softAssert(!preserved.isEmpty(), filterName + " has selection for active filters check");
        clickActiveFiltersButton();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        verifyActiveFilterChips(softAssert, filterName, preserved);
    }

    public void validateActiveFiltersForSelectedOption(SoftAssert softAssert, String filterName, String optionLabel)
            throws InterruptedException {
        expandFilter(filterName);
        selectFilterOptionIfNotSelected(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        clickActiveFiltersButton();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        verifyActiveFilterChips(softAssert, filterName, Collections.singletonList(optionLabel));
    }

    public String validateSearchAndSelectFirstOptionForFlow(SoftAssert softAssert, String filterName)
            throws InterruptedException {
        expandFilter(filterName);
        String searchText = resolveFirstSelectableFilterOption(filterName);
        Verify.softAssert(searchText != null, filterName + " has at least one option for search flow");
        if (searchText == null) {
            return null;
        }
        validateSearchInsideFilter(softAssert, filterName, searchText);
        selectFilterOptionIfNotSelected(filterName, searchText);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        clearFilterSearchInput(filterName);
        Logger.logReportMessage(filterName + " flow search selected: " + searchText);
        return searchText;
    }

    public String hoverSelectAllAndReadTooltip(String filterName) throws InterruptedException {
        scrollFilterListTowardFilter(filterName);
        By selectAllText = leftFilterPanel.selectAllLabel(filterName);
        DriverUtil.scrollToElement(selectAllText);
        Thread.sleep(300);
        try {
            DesktopBrowserElement element = driver.get().finder().findElement(selectAllText);
            element.mouseOver();
            Thread.sleep(900);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not hover Select all for " + filterName + ": " + e.getMessage());
        }
        for (int i = 0; i < 8; i++) {
            String tooltip = readVisibleOverlayTooltipText();
            if (tooltip != null && !tooltip.isEmpty()) {
                return tooltip;
            }
            Thread.sleep(250);
        }
        return readVisibleOverlayTooltipText();
    }

    private String readVisibleOverlayTooltipText() {
        try {
            Object text = driver.get().browser().executeScript(
                    "var nodes=document.querySelectorAll('float-ui-content,.mat-mdc-tooltip,[role=tooltip]');"
                            + "for(var i=0;i<nodes.length;i++){"
                            + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                            + "  if(t.length>0)return t;"
                            + "}"
                            + "return null;");
            if (text != null) {
                String tooltip = String.valueOf(text).trim();
                if (!tooltip.isEmpty() && !"null".equalsIgnoreCase(tooltip)) {
                    return tooltip;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

'''

def main():
    content = TARGET.read_text(encoding="utf-8")

    if "validateOrderLevelColumnTableSyncSmoke" in content:
        print("Already patched")
        return

    # Constants
    content = content.replace(
        "    private static final Pattern COUNT_PATTERN = Pattern.compile(\"\\\\((\\\\d+)\\\\)\");\n",
        "    private static final Pattern COUNT_PATTERN = Pattern.compile(\"\\\\((\\\\d+)\\\\)\");\n"
        "    private static final int MAX_FLAG_TABLE_SYNC_FILTER_COUNT = 500;\n"
        "    private static final int MAX_TABLE_SYNC_VISIBLE_ROWS = 10;\n",
        1,
    )

    # forceCollapseFilter
    old_collapse = """    public void collapseFilter(String filterName) throws InterruptedException {
        if (isFilterExpanded(filterName)) {
            DriverUtil.clickOnElement(leftFilterPanel.filterAccordionButton(filterName), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }
    }

    public boolean isFilterExpanded(String filterName) {"""
    new_collapse = """    public void collapseFilter(String filterName) throws InterruptedException {
        if (isFilterExpanded(filterName)) {
            DriverUtil.clickOnElement(leftFilterPanel.filterAccordionButton(filterName), DEFAULT_WAIT_SECONDS);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }
    }

    /** Collapses filter even when keep-expanded mode is active (suite teardown). */
    public void forceCollapseFilter(String filterName) throws InterruptedException {
        if (isFilterExpanded(filterName)) {
            collapseFilter(filterName);
            if (isFilterExpanded(filterName)) {
                DriverUtil.clickOnElementJs(leftFilterPanel.filterAccordionButton(filterName), DEFAULT_WAIT_SECONDS);
                Thread.sleep(FILTER_EXPAND_WAIT_MS);
            }
        }
        clearFilterSearchInput(filterName);
    }

    public boolean isFilterExpanded(String filterName) {"""
    if old_collapse not in content:
        raise SystemExit("collapseFilter anchor not found")
    content = content.replace(old_collapse, new_collapse, 1)

    # Replace validateBrandTableSyncSmoke body with delegation
    old_brand_start = "    public void validateBrandTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {"
    idx = content.find(old_brand_start)
    if idx < 0:
        raise SystemExit("validateBrandTableSyncSmoke not found")
    end_marker = "    /**\n     * TC609 smoke for Assigned To:"
    end_idx = content.find(end_marker, idx)
    if end_idx < 0:
        raise SystemExit("brand method end not found")
    new_brand = """    public void validateBrandTableSyncSmoke(SoftAssert softAssert, String filterName) throws InterruptedException {
        validateOrderLevelColumnTableSyncSmoke(softAssert, filterName, ManageColumnOptions.BRAND);
    }

"""
    content = content[:idx] + new_brand + content[end_idx:]

    # First-row-only: Order Status column loop
    old_status_loop = """        String keywordLower = statusKeyword.toLowerCase(Locale.ROOT);
        for (int i = 0; i < statusValues.size(); i++) {
            String statusText = statusValues.get(i);
            Verify.softAssert(statusText.toLowerCase(Locale.ROOT).contains(keywordLower),
                    filterName + " row " + (i + 1) + " Status is '" + statusKeyword
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + statusText + "')");
        }
        Logger.logMessage(filterName + " Status column OK — all " + statusValues.size()
                + " visible row(s) match keyword '" + statusKeyword + "' for '" + optionLabel + "'");"""
    new_status_loop = """        String keywordLower = statusKeyword.toLowerCase(Locale.ROOT);
        String statusText = statusValues.get(0);
        Verify.softAssert(statusText.toLowerCase(Locale.ROOT).contains(keywordLower),
                filterName + " first visible Status row is '" + statusKeyword
                        + "' after filter selection (filter='" + optionLabel + "', actual='" + statusText + "')");
        Logger.logMessage(filterName + " Status column OK — first of " + statusValues.size()
                + " visible row(s) matches keyword '" + statusKeyword + "' for '" + optionLabel + "'");"""
    content = content.replace(old_status_loop, new_status_loop, 1)

    # First-row-only: Submitted By
    old_submitted = """        for (int i = 0; i < submittedByValues.size(); i++) {
            String cellText = submittedByValues.get(i);
            Verify.softAssert(submittedByCellMatchesSelectedOption(cellText, optionLabel),
                    filterName + " row " + (i + 1) + " Submitted By initials are '" + expectedInitials
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Submitted By column OK — all " + submittedByValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");"""
    new_submitted = """        String cellText = submittedByValues.get(0);
        Verify.softAssert(submittedByCellMatchesSelectedOption(cellText, optionLabel),
                filterName + " first visible Submitted By initials are '" + expectedInitials
                        + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        Logger.logMessage(filterName + " Submitted By column OK — first of " + submittedByValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");"""
    content = content.replace(old_submitted, new_submitted, 1)

    # First-row-only: Assigned To person
    old_assigned = """        for (int i = 0; i < assignedToValues.size(); i++) {
            String cellText = assignedToValues.get(i);
            Verify.softAssert(assignedToCellMatchesSelectedPerson(cellText, optionLabel),
                    filterName + " row " + (i + 1) + " Assigned to initials are '" + expectedInitials
                            + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Assigned to column OK — all " + assignedToValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");"""
    new_assigned = """        String cellText = assignedToValues.get(0);
        Verify.softAssert(assignedToCellMatchesSelectedPerson(cellText, optionLabel),
                filterName + " first visible Assigned to initials are '" + expectedInitials
                        + "' after filter selection (filter='" + optionLabel + "', actual='" + cellText + "')");
        Logger.logMessage(filterName + " Assigned to column OK — first of " + assignedToValues.size()
                + " visible row(s) match initials '" + expectedInitials + "' for '" + optionLabel + "'");"""
    content = content.replace(old_assigned, new_assigned, 1)

    # First-row-only: Assigned To unassigned
    old_unassigned = """        for (int i = 0; i < assignedToValues.size(); i++) {
            String cellText = assignedToValues.get(i);
            Verify.softAssert(assignedToCellIsBlank(cellText),
                    filterName + " row " + (i + 1) + " Assigned to is blank for Unassigned filter"
                            + " (actual='" + cellText + "')");
        }
        Logger.logMessage(filterName + " Assigned to column OK — all " + assignedToValues.size()
                + " visible row(s) are blank for Unassigned filter");"""
    new_unassigned = """        String cellText = assignedToValues.get(0);
        Verify.softAssert(assignedToCellIsBlank(cellText),
                filterName + " first visible Assigned to is blank for Unassigned filter"
                        + " (actual='" + cellText + "')");
        Logger.logMessage(filterName + " Assigned to column OK — first of " + assignedToValues.size()
                + " visible row(s) blank for Unassigned filter");"""
    content = content.replace(old_unassigned, new_unassigned, 1)

    # brandCellMatchesSelectedOption -> delegate to orderLevel (will be added in block)
    content = content.replace(
        "    private static boolean brandCellMatchesSelectedOption(String cellValue, String optionLabel) {\n"
        "        if (cellValue == null || optionLabel == null) {\n"
        "            return false;\n"
        "        }\n"
        "        return cellValue.trim().equalsIgnoreCase(optionLabel.trim());\n"
        "    }\n",
        "    private static boolean brandCellMatchesSelectedOption(String cellValue, String optionLabel) {\n"
        "        return orderLevelCellMatchesSelectedOption(cellValue, optionLabel);\n"
        "    }\n",
        1,
    )

    # Insert big block before validateFilterNamesMatchTableColumns
    anchor = "    public void validateFilterNamesMatchTableColumns(SoftAssert softAssert, List<String> filterNames,"
    if anchor not in content:
        raise SystemExit("insertion anchor not found")
    content = content.replace(anchor, INSERTION_BLOCK + "\n" + anchor, 1)

    # public waitForTableRecordCount
    content = content.replace(
        "    private void waitForTableRecordCount(int expectedCount, int timeoutSeconds)",
        "    public void waitForTableRecordCount(int expectedCount, int timeoutSeconds)",
        1,
    )

    # readVisibleColumnValues public wrappers
    old_get_visible = """    private List<String> getVisibleTableColumnValues(String columnName) {
        return getVisibleTableColumnValues(columnName, false);
    }

    /**
     * Reads visible body cells for {@code columnName}. When {@code includeBlankCells} is true, empty cells"""
    new_get_visible = """    private List<String> getVisibleTableColumnValues(String columnName) {
        return getVisibleTableColumnValues(columnName, false);
    }

    /** Reads visible main-grid cell text for a column header (used by BSD-30019 and table sync). */
    public List<String> readVisibleColumnValues(String columnName) {
        return getVisibleTableColumnValues(columnName);
    }

    /** Reads visible main-grid cell text including blank cells. */
    public List<String> readVisibleColumnValuesIncludingBlanks(String columnName) {
        return getVisibleTableColumnValues(columnName, true);
    }

    /**
     * Reads visible body cells for {@code columnName}. When {@code includeBlankCells} is true, empty cells"""
    content = content.replace(old_get_visible, new_get_visible, 1)

    TARGET.write_text(content, encoding="utf-8", newline="\n")
    lines = content.count("\n") + 1
    print(f"Patched OK — {lines} lines")


if __name__ == "__main__":
    main()
