package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

public class FilterPanel {

    public static final String ORDER_STATUS_FILTER = "Order Status";
    public static final String DELIVERED_STATUS = "Done";
    public static final String DELIVERED_ROW_STATUS = "Delivered";
    public static final String YESTERDAY_DATE_PRESET = "Yesterday";
    public static final String LAST_UPDATED_FILTER_BY = "Last Updated";
    public static final String LINE_ITEM_STATUS_FILTER = "Line Item Status";
    public static final String ENVIRONMENT_FILTER = "Environment";

    public By environmentFilter() {
        return getFilter(ENVIRONMENT_FILTER);
    }

    public By getFilter(String filterName) {
        return By.XPath("//div[@class='filter-list-section']/descendant::span[contains(text(),'" + filterName + "')]"
                + " | //div[contains(@class,'filter-list-section')]//span[contains(normalize-space(),'" + filterName + "')]"
                + " | //*[contains(@class,'filter')]//span[contains(normalize-space(),'" + filterName + "')]");
    }

    public By orderStatusFilter() {
        return getFilter(ORDER_STATUS_FILTER);
    }

    public By lineItemStatusFilter() {
        return getFilter(LINE_ITEM_STATUS_FILTER);
    }

    public By lineItemStatusSearchInput() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]//input[contains(@placeholder,'Search')]"
                + " | //div[contains(@class,'filter-list-section')][.//*[contains(normalize-space(),'"
                + LINE_ITEM_STATUS_FILTER + "')]]//input[@type='text' or @type='search' or not(@type)]"
                + " | //*[contains(@class,'filter-list')]//input[contains(@placeholder,'Search')]"
                + " | //div[contains(@class,'filter-list-section')]//input[contains(@placeholder,'Search')]");
    }

    public By lineItemStatusCheckbox(String statusLabel) {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]//label[contains(normalize-space(),'"
                + statusLabel + "')]/preceding-sibling::input[@type='checkbox']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//label[starts-with(normalize-space(),'"
                + statusLabel + "')]/preceding-sibling::input[@type='checkbox']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//label[contains(normalize-space(),'"
                + statusLabel + "')]/..//input[@type='checkbox']"
                + " | //div[contains(@class,'filter-list-section')]//label[contains(normalize-space(),'"
                + statusLabel + "')]/preceding-sibling::input[@type='checkbox']"
                + " | //div[contains(@class,'filter-list-section')]//label[starts-with(normalize-space(),'"
                + statusLabel + "')]/preceding-sibling::input[@type='checkbox']"
                + " | //label[contains(normalize-space(),'" + statusLabel
                + "')]/..//input[@type='checkbox']");
    }

    public By statusSummaryChip(String statusLabel) {
        return By.XPath("//button[contains(normalize-space(),'" + statusLabel + "')]"
                + " | //span[contains(normalize-space(),'" + statusLabel + "')]"
                + " | //div[contains(@class,'status')][contains(normalize-space(),'" + statusLabel + "')]");
    }

    public By orderStatusOption(String statusLabel) {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane') or contains(@class,'filter')]"
                + "//label[normalize-space()='" + statusLabel + "']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//span[normalize-space()='" + statusLabel + "']"
                + " | //label[normalize-space()='" + statusLabel + "']"
                + " | //span[normalize-space()='" + statusLabel + "']");
    }

    public By orderStatusCheckbox(String statusLabel) {
        return By.XPath("//label[normalize-space()='" + statusLabel + "']/preceding-sibling::input[@type='checkbox']"
                + " | //label[normalize-space()='" + statusLabel + "']/..//input[@type='checkbox']"
                + " | //span[normalize-space()='" + statusLabel + "']/preceding-sibling::input[@type='checkbox']");
    }

    public By getFilterOpenedModal(String filterName) {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')][.//*[contains(normalize-space(),'" + filterName + "')]]"
                + " | //div[contains(@class,'filter-panel') or contains(@class,'filter-dropdown')]");
    }

    public By lastUpdatedFilterDropdown() {
        return By.XPath("//button[contains(@class,'dropdown-toggle')][normalize-space()='Last updated']"
                + " | //button[contains(@class,'dropdown-toggle')][contains(normalize-space(),'Last updated')]"
                + " | //button[contains(@class,'dropdown-toggle')][normalize-space()='Last Updated']");
    }

    public By dateRangeTrigger() {
        return By.XPath("//*[(self::button or self::span or self::div or self::a)]"
                + "[contains(normalize-space(.), '20') "
                + "and (contains(translate(normalize-space(.), '\u2013\u2014', '--'), '-')"
                + " or contains(normalize-space(.), '->'))]"
                + "[not(ancestor::*[contains(@class,'ag-root') or contains(@class,'ag-cell') or contains(@class,'ag-row')])]"
                + " | //button[contains(@class,'date')]"
                + "[not(ancestor::*[contains(@class,'ag-root')])]"
                + " | //*[contains(@class,'date-range')]"
                + "[not(ancestor::*[contains(@class,'ag-root')])]");
    }

    public By datePresetOption(String presetLabel) {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]"
                + "[.//*[contains(@class,'date') or contains(@class,'calendar') or contains(@class,'picker')"
                + " or contains(@class,'preset') or contains(@class,'range')]]"
                + "//*[normalize-space()='" + presetLabel + "']"
                + " | //div[contains(@class,'dropdown-menu') and contains(@class,'show')]"
                + "[.//*[contains(@class,'date') or contains(@class,'calendar') or contains(@class,'preset')]]"
                + "//*[normalize-space()='" + presetLabel + "']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//button[normalize-space()='" + presetLabel + "']"
                + " | //div[contains(@class,'cdk-overlay-pane')]//span[normalize-space()='" + presetLabel + "']");
    }

    public By datePickerCloseButton() {
        return By.XPath("//div[contains(@class,'cdk-overlay-pane')]//button[normalize-space()='Close']"
                + " | //button[normalize-space()='Close']");
    }

    /** TC024 Scriptless: calendar dropdown icon to open quick date presets. */
    public By calendarDropdownIcon() {
        return By.XPath("//*[@id='calendar-dropdown']//i[2]"
                + " | //*[@id='calendar-dropdown']//div/i");
    }

    /** TC024 Scriptless: Yesterday preset in calendar quick options. */
    public By calendarYesterdayOption() {
        return By.XPath("//app-calendar-quick-options//span[normalize-space()='Yesterday']"
                + " | //app-calendar-quick-options//*[normalize-space()='Yesterday']");
    }

    /** TC024 Scriptless: Done status filter button in left button group. */
    public By doneStatusFilterButton() {
        return By.XPath("//*[@id='leftButtongroupContainer']//msc-filter-button-themed//button"
                + "[contains(normalize-space(),'Done')]"
                + " | //*[@id='leftButtongroupContainer']/div[4]//msc-filter-button-themed//button");
    }
}