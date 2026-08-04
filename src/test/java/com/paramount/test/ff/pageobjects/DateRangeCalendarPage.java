package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Calendar quick options: {@code app-calendar-quick-options} with {@code div.option} rows.
 * Bookmark in {@code div.option-icon}; Yesterday text in {@code div.option-label} closes the popup.
 */
public class DateRangeCalendarPage {

    private static final String QUICK_OPTIONS = "//app-calendar-quick-options";
    private static final String YESTERDAY_LABEL =
            "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='yesterday'";
    private static final String YESTERDAY_ROW = QUICK_OPTIONS
            + "//div[contains(@class,'option')][.//div[contains(@class,'option-label')]//*[" + YESTERDAY_LABEL + "]]";

    public By calendarRangeIcon() {
        return By.XPath("//app-custom-date-range-picker//i[contains(@class,'bi-calendar-range')]"
                + " | //div[@id='calendar-dropdown']//i[contains(@class,'calendar-icon')]");
    }

    public By calendarPopup() {
        return By.XPath(QUICK_OPTIONS + " | //div[contains(@class,'cdk-overlay-pane')]" + YESTERDAY_ROW);
    }

    public By yesterdaySelectedOptionRow() {
        return By.XPath(YESTERDAY_ROW + "[contains(@class,'selected')]");
    }

    /** Click to apply (unselected row) or close (any row). */
    public By yesterdayTextLabel() {
        return By.XPath(YESTERDAY_ROW + "//div[contains(@class,'option-label')]//*[" + YESTERDAY_LABEL + "]");
    }

    public By yesterdayQuickOption() {
        return By.XPath(YESTERDAY_ROW + "[not(contains(@class,'selected'))]"
                + "//div[contains(@class,'option-label')]//*[" + YESTERDAY_LABEL + "]");
    }

    public By yesterdaySetDefaultBookmark() {
        return By.XPath(YESTERDAY_ROW + "//div[contains(@class,'option-icon')]"
                + "//i[contains(@class,'bi-bookmark')]");
    }
}
