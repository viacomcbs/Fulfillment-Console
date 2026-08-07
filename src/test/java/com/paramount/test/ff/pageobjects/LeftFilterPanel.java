package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

import java.util.Locale;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.SELECT_ALL_LABEL;

/**
 * Left filter panel locators for {@code msc-left-filter-panel} / {@code msc-multi-select-accordion}.
 */
public class LeftFilterPanel {

    private static final String PANEL = "//msc-left-filter-panel[contains(@class,'filter-panel')]";
    private static final String FILTER_LIST_SECTION = PANEL + "//div[contains(@class,'filter-list-section')]";
    private static final String OPTION_LABEL = "label[contains(@class,'form-check-label') and contains(@class,'option-label')]";
    private static final String SELECT_ALL_LABEL_XPATH =
            "normalize-space()='" + SELECT_ALL_LABEL + "'"
                    + " or starts-with(normalize-space(),concat('" + SELECT_ALL_LABEL + "',' ('))";

    private String filterAccordionXPath(String filterName) {
        return FILTER_LIST_SECTION
                + "//span[contains(@class,'accordion-label') and normalize-space()='" + filterName + "']"
                + "/ancestor::div[contains(@class,'accordion-item')][1]";
    }

    private String filterAccordionBodyXPath(String filterName) {
        return filterAccordionXPath(filterName) + "//div[contains(@class,'accordion-body')]";
    }

    public By leftFilterPanel() {
        return By.XPath(PANEL);
    }

    public By filterListSection() {
        return By.XPath(FILTER_LIST_SECTION);
    }

    public By filterPanelHeader() {
        return By.XPath(PANEL + "//span[@class='filter-label' and contains(normalize-space(),'Filters')]");
    }

    public By filterPanelClosed() {
        return By.XPath("//msc-left-filter-panel[contains(@class,'filter-panel-closed')]");
    }

    public By filterPanelOpen() {
        return By.XPath("//msc-left-filter-panel[contains(@class,'filter-panel-open')]");
    }

    /** Click target for expanding collapsed side nav (container + icon). */
    public By filterPanelExpandContainer() {
        return By.XPath("//msc-left-filter-panel//div[contains(@class,'filterIconDivClosed') and contains(@class,'show-block')]"
                + " | //msc-left-filter-panel[contains(@class,'filter-panel-closed')]"
                + "//div[contains(@class,'filterIcon')]");
    }

    /** Funnel icon shown when the side nav is collapsed ({@code filterIconDivClosed show-block}). */
    public By filterPanelExpandIcon() {
        return By.XPath("//msc-left-filter-panel//div[contains(@class,'filterIconDivClosed') and contains(@class,'show-block')]"
                + "//i[contains(@class,'bi-filter')]"
                + " | //msc-left-filter-panel[contains(@class,'filter-panel-closed')]//i[contains(@class,'bi-filter')]");
    }

    /** Collapse icon when filter side nav is open (BSD-29441 / Jul 2026 DEV). */
    public By filterPanelCollapseButton() {
        return By.XPath("//msc-left-filter-panel[contains(@class,'filter-panel-open')]"
                + "//div[contains(@class,'filter-header')]"
                + "//button[.//i[contains(@class,'bi-layout-sidebar') or contains(@class,'bi-chevron-bar')]]");
    }

    public By filterPanelSideNavOpen() {
        return By.XPath("//msc-left-filter-panel//div[contains(@class,'sideNav') and contains(@class,'show-block')]");
    }

    public By filterPanelSafeClickArea() {
        return By.XPath("//msc-left-filter-panel[contains(@class,'filter-panel-open')]"
                + " | " + PANEL);
    }

    public By leftFilterByName(String filterName) {
        return By.XPath(FILTER_LIST_SECTION
                + "//span[contains(@class,'accordion-label') and normalize-space()='" + filterName + "']");
    }

    /** Accordion header button (contains label, count, chevron). */
    public By filterAccordionButton(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button')]");
    }

    /** Chevron icon — bi-chevron-down (collapsed) or bi-chevron-up (expanded). */
    public By filterExpandCollapseChevron(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button')]"
                + "//i[contains(@class,'bi-chevron-down') or contains(@class,'bi-chevron-up')]");
    }

    public By expandedAccordionButton(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button') and (@aria-expanded='true' or contains(@class,'is-expanded'))]");
    }

    public By collapsedAccordionButton(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button') and (@aria-expanded='false' or not(@aria-expanded='true'))]");
    }

    public By expandCollapseIcon(String filterName) {
        return filterExpandCollapseChevron(filterName);
    }

    public By expandChevronIcon(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button')]//i[contains(@class,'bi-chevron-up')]");
    }

    public By collapseChevronIcon(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//button[contains(@class,'accordion-button')]//i[contains(@class,'bi-chevron-down')]");
    }

    public By expandedFilterPanel(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//div[contains(@class,'accordion-collapse') and contains(@class,'show')]");
    }

    public By filterOptionsContainer(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//cdk-virtual-scroll-viewport[contains(@class,'cdk-options-viewport')]"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'options-list-scrollbar')]");
    }

    public By selectAllCheckbox(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and contains(@class,'all')]"
                + "//input[contains(@class,'form-check-input') and @type='checkbox']");
    }

    public By selectAllLabel(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and contains(@class,'all')]"
                + "//" + OPTION_LABEL + "[" + SELECT_ALL_LABEL_XPATH + "]");
    }

    public By filterOptionByLabel(String filterName, String optionLabel) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and not(contains(@class,'all'))]"
                + "//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item')]"
                + "//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']");
    }

    public By filterOptionCheckbox(String filterName, String optionLabel) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and not(contains(@class,'all'))]"
                + "//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']"
                + "/preceding-sibling::input[contains(@class,'form-check-input') and @type='checkbox']"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item')]"
                + "//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']"
                + "/preceding-sibling::input[contains(@class,'form-check-input') and @type='checkbox']");
    }

    public By filterOptionRow(String filterName, String optionLabel) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and not(contains(@class,'all'))]"
                + "[.//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']]"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item')]"
                + "[.//" + OPTION_LABEL + "[normalize-space()='" + optionLabel + "']]");
    }

    public By allFilterOptionContainers(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//a[contains(@class,'option-item') and not(contains(@class,'all'))]"
                + "//div[contains(@class,'option-container')]"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item')]"
                + "//div[contains(@class,'option-container')]");
    }

    public By allFilterOptionLabels(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//cdk-virtual-scroll-viewport//div[contains(@class,'cdk-virtual-scroll-content-wrapper')]//"
                + OPTION_LABEL
                + " | " + filterAccordionBodyXPath(filterName)
                + "//cdk-virtual-scroll-viewport//" + OPTION_LABEL);
    }

    /**
     * Divider between non-zero and zero-count options is CSS-only ({@code ::before} on the first
     * zero-count row) — not an {@code <hr>} in the DOM. Prefer {@link #firstZeroCountOptionRow}.
     */
    @Deprecated
    public By zeroCountDivider(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'options-list-scrollbar')]//hr"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//hr"
                + " | " + filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'divider')]");
    }

    /** First zero-count row ({@code dim-zero-count}). */
    public By firstZeroCountOptionRow(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item') and contains(@class,'dim-zero-count')][1]");
    }

    /** First zero-count option link — CSS divider {@code ::before} is on this element. */
    public By firstZeroCountOptionLink(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'dim-zero-count')][1]"
                + "//a[contains(@class,'option-item')]");
    }

    /** Last non-zero option row — divider may appear as {@code ::after} on this row. */
    public By lastNonZeroOptionRow(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item') and not(contains(@class,'dim-zero-count'))][last()]");
    }

    public By zeroCountOptionRowByLabel(String filterName, String optionLabel) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'draggable-item')]"
                + "[.//a[contains(@class,'option-item') and @data-option-count='0']]"
                + "[.//label[contains(@class,'option-label') and normalize-space()=\"" + optionLabel + "\"]]");
    }

    public By zeroCountOptions(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//div[contains(@class,'draggable-item') and contains(@class,'dim-zero-count')]");
    }

    public By filterSearchInput(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'input-box')]//input[@type='search' and @placeholder='Search...']");
    }

    /** Magnifying glass — {@code i.bi-search.search-icon} (may gain {@code textEntered} when input has text). */
    public By filterSearchIcon(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'input-box')]//i[contains(@class,'bi-search') and contains(@class,'search-icon')]");
    }

    /** Clear (X) — visible only when input has text ({@code hide} class removed). */
    public By filterSearchClearIcon(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'input-box')]"
                + "//i[contains(@class,'bi-x-circle-fill') and contains(@class,'clear-icon') and not(contains(@class,'hide'))]");
    }

    public By filterRangeFromInput(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'range')]//input[contains(@class,'range-input') and @placeholder='From']");
    }

    public By filterRangeToInput(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//div[contains(@class,'range')]//input[contains(@class,'range-input') and @placeholder='To']");
    }

    public By filterPaginationNext(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//button[contains(@class,'next') or contains(@aria-label,'Next')]");
    }

    public By filterPaginationPrev(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//button[contains(@class,'prev') or contains(@aria-label,'Previous')]");
    }

    public By filterPaginationInfo(String filterName) {
        return By.XPath(filterAccordionBodyXPath(filterName)
                + "//span[contains(@class,'pagination') or contains(text(),'of')]");
    }

    public By filterSelectionCount(String filterName) {
        return By.XPath(filterAccordionXPath(filterName)
                + "//span[contains(@class,'selection-count')]");
    }

    public By activeFiltersSection() {
        return By.XPath("//div[contains(@class,'active-filter') or contains(@class,'applied-filter')]");
    }

    public By activeFilterChip(String optionLabel) {
        return By.XPath("//div[contains(@class,'active-filter') or contains(@class,'applied-filter')]"
                + "//span[contains(normalize-space(),'" + optionLabel + "')]");
    }

    public By activeFilterChipRemoveButton(String optionLabel) {
        return By.XPath("//div[contains(@class,'active-filter') or contains(@class,'applied-filter')]"
                + "//span[contains(normalize-space(),'" + optionLabel + "')]"
                + "/following-sibling::button | "
                + "//div[contains(@class,'active-filter') or contains(@class,'applied-filter')]"
                + "//span[contains(normalize-space(),'" + optionLabel + "')]/ancestor::*[contains(@class,'chip') or contains(@class,'tag')]"
                + "//button[contains(@class,'close') or contains(@class,'remove') or contains(@class,'clear')]");
    }

    public By activeFiltersClearButton() {
        return By.XPath("//span[contains(@class,'chip-tag-label') and normalize-space()='Clear']");
    }

    public By leftFilterPanelClearButton() {
        return By.XPath("//span[contains(@class,'overlay-bypass-class') and normalize-space()='Clear']");
    }

    public By clearAllFiltersButton() {
        return leftFilterPanelClearButton();
    }

    public By saveFilterButton() {
        return By.XPath("//button[contains(normalize-space(),'Save filter') or contains(normalize-space(),'Save Filter')]");
    }

    /** Orders/Line items grid total above the table, e.g. {@code <div class="total-count">3258 results</div>}. */
    public By tableRecordCountLabel() {
        return By.XPath("//app-fulfillment-main-table-container//div[contains(@class,'total-count')]"
                + " | //div[contains(@class,'table-top')]//div[contains(@class,'total-count')]");
    }

    /** Status summary chips above the Orders table (In progress / Failed / Ready for delivery / Done). */
    public By statusSummaryChip(String chipText) {
        return By.XPath("//*[contains(@class,'chip') or contains(@class,'badge') or self::button or self::div]"
                + "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),"
                + " '" + chipText.toLowerCase() + "')]");
    }

    public By activeFiltersBadge() {
        return By.XPath("//*[contains(normalize-space(),'Active filters')]"
                + "//*[contains(@class,'badge') or contains(@class,'count')]");
    }

    public By tableRows() {
        return By.XPath("//table//tbody/tr | //div[contains(@class,'table-row') or contains(@class,'ag-row')]");
    }

    public By tableColumnHeader(String columnName) {
        return By.XPath("//th[contains(normalize-space(),'" + columnName + "')]"
                + " | //div[contains(@class,'header')]//span[normalize-space()='" + columnName + "']");
    }

    /** Body cells for a table column (Orders grid uses {@code td.col.<name>-col}). */
    public By tableColumnCells(String columnName) {
        String cssClass = toTableColumnCssClass(columnName);
        return By.XPath("//tbody//tr[contains(@class,'row')]//td[contains(@class,'" + cssClass + "')]"
                + " | //tr[contains(@class,'row')]//td[contains(@class,'" + cssClass + "')]"
                + " | //div[contains(@class,'table-row')]//div[contains(@class,'" + cssClass + "')]");
    }

    private static String toTableColumnCssClass(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return "";
        }
        switch (columnName.trim().toLowerCase(Locale.ROOT)) {
            case "status":
                return "revised-status-col";
            case "brand":
                return "brand-col";
            case "assigned to":
                return "assigned-to-col";
            default:
                return columnName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + "-col";
        }
    }
}
