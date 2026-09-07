package com.paramount.test.ff.uitests.helpers.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

/**
 * Recovers Orders tab when no date is selected or grid/filter data has not loaded —
 * hard refresh + Yesterday calendar + expand left filter panel.
 */
public final class OrdersDataRecoveryHelper extends BaseTest {

    private static final ThreadLocal<Boolean> RECOVERY_ATTEMPTED = ThreadLocal.withInitial(() -> false);

    private final OrdersTabTestSetupHelper ordersSetup = new OrdersTabTestSetupHelper();
    private final CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    public static void resetRecoveryAttempt() {
        RECOVERY_ATTEMPTED.remove();
    }

    public boolean needsOrdersDataRecovery() throws InterruptedException {
        if (!calendarSetup.isDateRangeSelected()) {
            Logger.logMessage("Orders recovery needed — no date range selected on toolbar");
            return true;
        }

        LeftFilterPanelUtil panelUtil = new LeftFilterPanelUtil();
        panelUtil.ensureLeftFilterPanelOpen();

        if (!ordersSetup.isFilterPanelAnchored(8)) {
            Logger.logMessage("Orders recovery needed — left filter panel not anchored");
            return true;
        }

        String orderStatusFilter = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
        if (!WaitUtil.isDisplay(leftFilterPanel.leftFilterByName(orderStatusFilter), 8)) {
            Logger.logMessage("Orders recovery needed — '" + orderStatusFilter + "' filter header not visible");
            return true;
        }

        if (isOrdersGridEmpty(panelUtil)) {
            Logger.logMessage("Orders recovery needed — no records / filter options loaded");
            return true;
        }

        return false;
    }

    public void recoverOrdersPageIfNeeded(SoftAssert softAssert) throws InterruptedException {
        if (RECOVERY_ATTEMPTED.get() || !needsOrdersDataRecovery()) {
            return;
        }
        RECOVERY_ATTEMPTED.set(true);
        calendarSetup.hardRefreshAndSetYesterday(softAssert);
        LeftFilterSessionHelper.markCalendarSetToYesterday();
        LeftFilterPanelUtil panelUtil = new LeftFilterPanelUtil();
        panelUtil.ensureLeftFilterPanelOpen();
        panelUtil.waitForFilterHeaderVisible(OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName(), 30);
        Logger.logReportMessage("Orders page recovery complete after hard refresh");
    }

    private boolean isOrdersGridEmpty(LeftFilterPanelUtil panelUtil) {
        int tableCount = panelUtil.getTableRecordCount();
        if (tableCount < 0) {
            Logger.logMessage("Orders recovery — table record count label not available");
            return true;
        }
        // Do not expand Order Status (or any filter) during recovery — TC620 and others only need
        // their target filter expanded. Zero rows with a valid count label is acceptable.
        return false;
    }
}