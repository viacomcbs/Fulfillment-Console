package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.DateRangeCalendarPage;
import com.synergy.core.driver.By;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Calendar quick options for left-filter suites.
 * <p>
 * Yesterday default flow:
 * <ol>
 *   <li>Open calendar</li>
 *   <li>Click bookmark icon beside Yesterday label</li>
 *   <li>Hard-fail if icon does not turn blue ({@code default-option})</li>
 *   <li>Click Yesterday label to apply date and close calendar</li>
 * </ol>
 */
public final class CalendarSetupUtil extends BaseTest {

    private static final int SHORT_WAIT = 5;
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy")
    };

    private final DateRangeCalendarPage calendarPage = new DateRangeCalendarPage();

    public void setYesterdayDefaultBookmarkOnce(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("One-time setup — click Yesterday bookmark icon, then Yesterday label");

        waitForCalendarToolbar();
        Verify.hardAssert(WaitUtil.isDisplay(calendarPage.calendarRangeIcon(), 15),
                "Cannot set calendar — toolbar date picker not visible");

        if (WaitUtil.isDisplayFast(calendarPage.yesterdayDefaultBookmarkActive(), 2) && isYesterdaySelected()) {
            Logger.logReportMessage("Yesterday default bookmark already blue and date applied — skipping");
            return;
        }

        applyYesterdayDefaultBookmarkFlow(softAssert);
    }

    public void setDateRangeToYesterday(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Setting date range to Yesterday");

        waitForCalendarToolbar();
        Verify.hardAssert(WaitUtil.isDisplay(calendarPage.calendarRangeIcon(), 15),
                "Cannot set calendar — toolbar date picker not visible");

        String toolbarRange = readToolbarDateRange();
        if (isYesterdayRange(toolbarRange) && !isCalendarOpen()) {
            Logger.logReportMessage("Yesterday already set — skipping calendar setup: " + toolbarRange);
            return;
        }

        applyYesterdayDefaultBookmarkFlow(softAssert);
    }

    private void applyYesterdayDefaultBookmarkFlow(SoftAssert softAssert) throws InterruptedException {
        openCalendarIfClosed();
        scrollCalendarOptionIntoView("Yesterday");

        Verify.hardAssert(clickYesterdayBookmarkIcon(),
                "Clicked Yesterday default bookmark icon beside Yesterday label");

        if (!waitForYesterdayDefaultBookmarkActive(6)) {
            Logger.logReportMessage("Yesterday bookmark not blue after first click — clicking bookmark again");
            Verify.hardAssert(clickYesterdayBookmarkIcon(),
                    "Re-clicked Yesterday default bookmark icon");
            waitForYesterdayDefaultBookmarkActive(4);
        }

        Verify.hardAssert(isYesterdayDefaultBookmarkBlue(),
                "Yesterday default bookmark icon did not turn blue (default-option) beside Yesterday row — aborting test");

        clickYesterdayTextLabel();
        WaitUtil.waitForJSToLoad(SHORT_WAIT);
        if (isCalendarOpen()) {
            clickYesterdayTextLabel();
            WaitUtil.waitForJSToLoad(SHORT_WAIT);
        }
        Logger.logReportMessage("Yesterday applied and calendar closed via Yesterday label");
    }

    public String getToolbarDateRange() {
        return readToolbarDateRange();
    }

    public boolean isDateRangeSelected() {
        String range = readToolbarDateRange();
        return range != null && !range.trim().isEmpty() && range.contains("->");
    }

    public boolean isYesterdaySelected() {
        return isYesterdayRange(readToolbarDateRange());
    }

    public void setDateRangeToToday(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Setting date range to Today");

        waitForCalendarToolbar();
        Verify.hardAssert(WaitUtil.isDisplay(calendarPage.calendarRangeIcon(), 15),
                "Cannot set calendar — toolbar date picker not visible");

        String toolbarRange = readToolbarDateRange();
        if (isTodayRange(toolbarRange) && !isCalendarOpen()) {
            Logger.logReportMessage("Today already set — skipping calendar setup: " + toolbarRange);
            return;
        }

        openCalendarIfClosed();
        scrollCalendarOptionIntoView("Today");
        clickTodayBookmarkIcon();
        click(calendarPage.todayTextLabel());
        WaitUtil.waitForJSToLoad(SHORT_WAIT);
    }

    public boolean isTodaySelected() {
        return isTodayRange(readToolbarDateRange());
    }

    public void hardRefreshAndSetYesterday(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Hard refreshing Fulfillment Console to reload Orders data");
        driver.get().browser().refresh();
        WaitUtil.waitForJSToLoad(30);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        waitForCalendarToolbar();
        LeftFilterSessionHelper.resetCalendarState();
        setYesterdayDefaultBookmarkOnce(softAssert);
        WaitUtil.waitForJSToLoad(15);
        LeftFilterSessionHelper.markCalendarSetToYesterday();
    }

    public void dismissCalendarIfOpen() throws InterruptedException {
        if (!isCalendarOpen()) {
            return;
        }
        clickYesterdayTextLabel();
        WaitUtil.waitForJSToLoad(SHORT_WAIT);
    }

    private void openCalendarIfClosed() {
        if (isCalendarOpen()) {
            return;
        }
        By trigger = calendarPage.calendarRangeIcon();
        if (WaitUtil.isDisplay(trigger, SHORT_WAIT)) {
            DriverUtil.clickOnElement(trigger, DEFAULT_WAIT_SECONDS);
            WaitUtil.isDisplay(calendarPage.calendarPopup(), SHORT_WAIT);
        }
    }

    private boolean isCalendarOpen() {
        return WaitUtil.isDisplay(calendarPage.calendarPopup(), 1);
    }

    private boolean clickYesterdayBookmarkIcon() throws InterruptedException {
        By bookmarkIcon = calendarPage.yesterdayBookmarkIcon();
        boolean clicked = WaitUtil.isDisplay(bookmarkIcon, SHORT_WAIT)
                && DriverUtil.clickOnElement(bookmarkIcon, DEFAULT_WAIT_SECONDS);
        if (!clicked) {
            clicked = clickCalendarBookmarkViaJs("Yesterday");
            Logger.logReportMessage("Clicked Yesterday bookmark icon via JS: " + clicked);
        } else {
            Logger.logReportMessage("Clicked Yesterday bookmark icon beside Yesterday label");
        }
        Thread.sleep(400);
        return clicked;
    }

    private void clickTodayBookmarkIcon() throws InterruptedException {
        By bookmarkIcon = calendarPage.todayBookmarkIcon();
        if (!WaitUtil.isDisplay(bookmarkIcon, SHORT_WAIT)
                || !DriverUtil.clickOnElement(bookmarkIcon, DEFAULT_WAIT_SECONDS)) {
            clickCalendarBookmarkViaJs("Today");
        }
        Thread.sleep(400);
    }

    private boolean isYesterdayDefaultBookmarkBlue() {
        return WaitUtil.isDisplayFast(calendarPage.yesterdayDefaultBookmarkActive(), 1);
    }

    private boolean waitForYesterdayDefaultBookmarkActive(int attempts) throws InterruptedException {
        for (int i = 0; i < attempts; i++) {
            if (WaitUtil.isDisplayFast(calendarPage.yesterdayDefaultBookmarkActive(), 1)) {
                Logger.logReportMessage("Yesterday default bookmark is active (blue icon beside Yesterday)");
                return true;
            }
            Thread.sleep(400);
        }
        return WaitUtil.isDisplayFast(calendarPage.yesterdayDefaultBookmarkActive(), 1);
    }

    private void clickYesterdayTextLabel() {
        Logger.logReportMessage("Clicking Yesterday label to apply date and close calendar");
        click(calendarPage.yesterdayTextLabel());
    }

    private void scrollCalendarOptionIntoView(String optionLabel) {
        try {
            String escaped = optionLabel.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    "var name='" + escaped + "';"
                            + "var rows=[].slice.call(document.querySelectorAll('app-calendar-quick-options div.option'));"
                            + "for(var i=0;i<rows.length;i++){"
                            + "  var label=rows[i].querySelector('.option-label');"
                            + "  if(!label||(label.textContent||'').trim()!==name){continue;}"
                            + "  rows[i].scrollIntoView({block:'center',inline:'nearest'});"
                            + "  return true;"
                            + "}"
                            + "return false;");
            Thread.sleep(200);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll calendar option '" + optionLabel + "': " + e.getMessage());
        }
    }

    private boolean clickCalendarBookmarkViaJs(String optionLabel) {
        try {
            String escaped = optionLabel.replace("\\", "\\\\").replace("'", "\\'");
            Object clicked = driver.get().browser().executeScript(
                    "var name='" + escaped + "';"
                            + "var rows=[].slice.call(document.querySelectorAll('app-calendar-quick-options div.option'));"
                            + "for(var i=0;i<rows.length;i++){"
                            + "  var label=rows[i].querySelector('.option-label');"
                            + "  if(!label||(label.textContent||'').trim()!==name){continue;}"
                            + "  var icon=rows[i].querySelector('.option-icon i.bi-bookmark');"
                            + "  if(!icon){return false;}"
                            + "  (icon.closest('.option-icon')||icon).click();"
                            + "  return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(clicked) || "true".equals(String.valueOf(clicked));
        } catch (Exception e) {
            Logger.logConsoleMessage("Calendar bookmark JS click failed for '" + optionLabel + "': " + e.getMessage());
            return false;
        }
    }

    private void click(By locator) {
        if (WaitUtil.isDisplay(locator, SHORT_WAIT)) {
            DriverUtil.clickOnElement(locator, DEFAULT_WAIT_SECONDS);
        }
    }

    private void waitForCalendarToolbar() throws InterruptedException {
        Logger.logReportMessage("Waiting for calendar toolbar (skipping full filter load wait)...");
        long deadline = System.currentTimeMillis() + 30_000L;
        while (System.currentTimeMillis() < deadline) {
            if (WaitUtil.isDisplayFast(calendarPage.calendarRangeIcon(), 2)) {
                Logger.logReportMessage("Calendar toolbar visible");
                return;
            }
            WaitUtil.waitForJSToLoad(3);
            Thread.sleep(500);
        }
    }

    private String readToolbarDateRange() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var p=document.querySelector('app-custom-date-range-picker,#calendar-dropdown');"
                            + "if(!p)return '';"
                            + "var i=p.querySelectorAll('input[placeholder*=\"Start\"],input[placeholder*=\"End\"]');"
                            + "if(i.length<2)return '';"
                            + "var s=(i[0].value||'').trim(),e=(i[1].value||'').trim();"
                            + "return s&&e?s+' -> '+e:'';");
            return result == null ? "" : result.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isYesterdayRange(String text) {
        if (text == null || !text.contains("->")) {
            return false;
        }
        String[] parts = text.split("->");
        if (parts.length < 2 || !parts[0].trim().equals(parts[1].trim())) {
            return false;
        }
        LocalDate toolbarDate = parseToolbarDate(parts[0].trim());
        if (toolbarDate == null) {
            return false;
        }
        return toolbarDate.equals(LocalDate.now().minusDays(1));
    }

    private boolean isTodayRange(String text) {
        if (text == null || !text.contains("->")) {
            return false;
        }
        String[] parts = text.split("->");
        if (parts.length < 2 || !parts[0].trim().equals(parts[1].trim())) {
            return false;
        }
        LocalDate toolbarDate = parseToolbarDate(parts[0].trim());
        return toolbarDate != null && toolbarDate.equals(LocalDate.now());
    }

    private LocalDate parseToolbarDate(String dateText) {
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(dateText, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }
}
