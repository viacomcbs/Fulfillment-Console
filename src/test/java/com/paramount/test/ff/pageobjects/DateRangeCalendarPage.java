package com.paramount.test.ff.pageobjects;

import com.synergy.core.driver.By;

/**
 * Calendar quick options: {@code app-calendar-quick-options} with {@code div.option} rows.
 * Bookmark: {@code div.option-icon > i.bi-bookmark} beside each quick-option label (click to set default preset).
 * <p>
 * Row XPath uses class-token {@code option} only — not {@code contains(@class,'option')} which also
 * matches {@code option-popper}, {@code option-label}, and returns the first bookmark (This Month).
 */
public class DateRangeCalendarPage {

    private static final String QUICK_OPTIONS = "//app-calendar-quick-options";

    /** Matches {@code div.option} and {@code div.option selected} — not option-label / option-popper. */
    private static final String QUICK_OPTION_ROW =
            "div[contains(concat(' ', normalize-space(@class), ' '), ' option ')]";

    private static final String YESTERDAY_ROW = QUICK_OPTIONS
            + "//" + QUICK_OPTION_ROW
            + "[.//div[contains(@class,'option-label')]//span[normalize-space()='Yesterday']]";
    private static final String TODAY_ROW = QUICK_OPTIONS
            + "//" + QUICK_OPTION_ROW
            + "[.//div[contains(@class,'option-label')]//span[normalize-space()='Today']]";

    public By calendarRangeIcon() {
        return By.XPath("//app-custom-date-range-picker//i[contains(@class,'bi-calendar-range')]"
                + " | //div[@id='calendar-dropdown']//i[contains(@class,'calendar-icon')]");
    }

    public By calendarPopup() {
        return By.XPath(QUICK_OPTIONS
                + " | //div[contains(@class,'cdk-overlay-pane')]//app-calendar-quick-options");
    }

    public By yesterdaySelectedOptionRow() {
        return By.XPath(YESTERDAY_ROW + "[contains(@class,'selected')]");
    }

    /** Click to apply (unselected row) or close (any row). */
    public By yesterdayTextLabel() {
        return By.XPath(YESTERDAY_ROW + "//div[contains(@class,'option-label')]//span[normalize-space()='Yesterday']");
    }

    public By yesterdayQuickOption() {
        return By.XPath(YESTERDAY_ROW + "[not(contains(@class,'selected'))]"
                + "//div[contains(@class,'option-label')]//span[normalize-space()='Yesterday']");
    }

    /** Bookmark icon beside Yesterday label. */
    public By yesterdayBookmarkIcon() {
        return By.XPath("(" + YESTERDAY_ROW + "//div[contains(@class,'option-icon')]"
                + "//i[contains(@class,'bi-bookmark')])[1]");
    }

    /** Blue/active default bookmark on Yesterday row. */
    public By yesterdayDefaultBookmarkActive() {
        return By.XPath("(" + YESTERDAY_ROW + "//div[contains(@class,'option-icon')]"
                + "//i[contains(@class,'default-option') and contains(@class,'bi-bookmark')])[1]");
    }

    /** @deprecated use {@link #yesterdayBookmarkIcon()} for clicking; {@link #yesterdayDefaultBookmarkActive()} for verify */
    public By yesterdaySetDefaultBookmark() {
        return yesterdayBookmarkIcon();
    }

    public By todaySelectedOptionRow() {
        return By.XPath(TODAY_ROW + "[contains(@class,'selected')]");
    }

    public By todayTextLabel() {
        return By.XPath(TODAY_ROW + "//div[contains(@class,'option-label')]//span[normalize-space()='Today']");
    }

    public By todayQuickOption() {
        return By.XPath(TODAY_ROW + "[not(contains(@class,'selected'))]"
                + "//div[contains(@class,'option-label')]//span[normalize-space()='Today']");
    }

    public By todayBookmarkIcon() {
        return By.XPath("(" + TODAY_ROW + "//div[contains(@class,'option-icon')]"
                + "//i[contains(@class,'bi-bookmark')])[1]");
    }

    public By todayDefaultBookmarkActive() {
        return By.XPath("(" + TODAY_ROW + "//div[contains(@class,'option-icon')]"
                + "//i[contains(@class,'default-option') and contains(@class,'bi-bookmark')])[1]");
    }

    public By todaySetDefaultBookmark() {
        return todayBookmarkIcon();
    }
}
