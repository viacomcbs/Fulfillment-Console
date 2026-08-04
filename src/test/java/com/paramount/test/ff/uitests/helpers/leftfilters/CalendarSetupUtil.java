package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.DateRangeCalendarPage;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.synergy.core.driver.By;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Sets date range to Yesterday: default bookmark on Yesterday row → Yesterday text to close.
 */
public final class CalendarSetupUtil extends BaseTest {

    private static final int SHORT_WAIT = 5;
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy")
    };

    private final DateRangeCalendarPage calendarPage = new DateRangeCalendarPage();

    public void setDateRangeToYesterday(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Setting date range to Yesterday");

        new OrdersTabTestSetupHelper().ensureFulfillmentConsoleReady();
        Verify.hardAssert(WaitUtil.isDisplay(calendarPage.calendarRangeIcon(), 15),
                "Cannot set calendar — toolbar date picker not visible");

        String toolbarRange = readToolbarDateRange();
        if (isYesterdayRange(toolbarRange) && !isCalendarOpen()) {
            Logger.logReportMessage("Yesterday already set — skipping calendar setup: " + toolbarRange);
            return;
        }

        openCalendarIfClosed();
        click(calendarPage.yesterdaySetDefaultBookmark());
        click(calendarPage.yesterdayTextLabel());
        WaitUtil.waitForJSToLoad(SHORT_WAIT);
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

    /** Hard refresh then re-apply Yesterday — used when the grid/filters did not load. */
    public void hardRefreshAndSetYesterday(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Hard refreshing Fulfillment Console to reload Orders data");
        driver.get().browser().refresh();
        WaitUtil.waitForJSToLoad(45);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        OrdersTabTestSetupHelper ordersSetup = new OrdersTabTestSetupHelper();
        ordersSetup.ensureFulfillmentConsoleReady();
        LeftFilterSessionHelper.resetCalendarState();
        setDateRangeToYesterday(softAssert);
        LeftFilterSessionHelper.markCalendarSetToYesterday();
    }

    public void dismissCalendarIfOpen() throws InterruptedException {
        if (!isCalendarOpen()) {
            return;
        }
        click(calendarPage.yesterdayTextLabel());
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

    private void click(By locator) {
        if (WaitUtil.isDisplay(locator, SHORT_WAIT)) {
            DriverUtil.clickOnElement(locator, DEFAULT_WAIT_SECONDS);
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
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return toolbarDate.equals(yesterday);
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
