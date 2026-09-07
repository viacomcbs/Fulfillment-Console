package com.paramount.test.ff.uitests.helpers.dsid;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;

/** One-time Today + default bookmark for BSD-29870 shared session. */
public final class DsidCalendarSetup {

    private DsidCalendarSetup() {
    }

    public static void setTodayAsDefaultOnce(SoftAssert softAssert) throws InterruptedException {
        if (DsidSessionHelper.isCalendarSetToToday()) {
            Logger.logReportMessage("Today is default — skipping calendar");
            return;
        }
        CalendarSetupUtil calendar = new CalendarSetupUtil();
        if (calendar.isTodaySelected()) {
            Logger.logReportMessage("Today already on toolbar — session marked");
            DsidSessionHelper.markCalendarSetToToday();
            return;
        }
        Logger.logReportMessage("One-time setup — select Today and mark as default");
        calendar.setDateRangeToToday(softAssert);
        DsidSessionHelper.markCalendarSetToToday();
    }
}
