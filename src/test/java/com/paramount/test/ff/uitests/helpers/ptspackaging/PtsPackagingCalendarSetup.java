package com.paramount.test.ff.uitests.helpers.ptspackaging;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;

/**
 * One-time Yesterday + default bookmark for the BSD-29441 shared session.
 * After the first setup, Yesterday stays applied for all later tests — do not reopen the calendar.
 */
public final class PtsPackagingCalendarSetup {

    private PtsPackagingCalendarSetup() {
    }

    public static void setYesterdayAsDefaultOnce(SoftAssert softAssert) throws InterruptedException {
        if (PtsPackagingSessionHelper.isCalendarSetToYesterday()) {
            Logger.logReportMessage("Yesterday is default — skipping calendar (not reopened after refresh)");
            return;
        }
        CalendarSetupUtil calendar = new CalendarSetupUtil();
        if (calendar.isYesterdaySelected()) {
            Logger.logReportMessage("Yesterday already on toolbar — session marked; calendar not opened");
            PtsPackagingSessionHelper.markCalendarSetToYesterday();
            return;
        }
        Logger.logReportMessage("One-time setup — select Yesterday and mark as default");
        calendar.setDateRangeToYesterday(softAssert);
        PtsPackagingSessionHelper.markCalendarSetToYesterday();
    }
}
