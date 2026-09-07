package com.paramount.test.ff.uitests.helpers.tablerefresh;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.TableRefreshHeaderPage;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;

import static com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions.Section;

/** Ensures Order ID / LineItem ID columns are visible in the grid via Manage columns (#tableViewButton). */
public class TableRefreshColumnUtil extends BaseTest {

    public static final String ORDER_ID = ManageColumnOptions.ORDER_ID;
    public static final String LINE_ITEM_ID = ManageColumnOptions.LINE_ITEM_ID;

    private static final int PANEL_WAIT_S = 10;
    private static final int PANEL_SETTLE_MS = 800;

    private final TableRefreshHeaderPage headerPage = new TableRefreshHeaderPage();
    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();

    public void ensureOrderIdColumnVisible(SoftAssert softAssert) throws InterruptedException {
        if (WaitUtil.isDisplayFast(headerPage.visibleOrderIdCells(), 3)) {
            Logger.logReportMessage("Order ID column already visible in Orders grid");
            return;
        }
        enableColumnInManagePanel(ORDER_ID, Section.ORDER, softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.visibleOrderIdCells(), PANEL_WAIT_S),
                "Order ID column visible in Orders grid after Manage columns", softAssert);
    }

    public void ensureLineItemIdColumnVisible(SoftAssert softAssert) throws InterruptedException {
        if (WaitUtil.isDisplayFast(headerPage.visibleLineItemIdCells(), 3)) {
            Logger.logReportMessage("LineItem ID column already visible in Line Items grid");
            return;
        }
        enableColumnInManagePanel(LINE_ITEM_ID, Section.LINE_ITEM, softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.visibleLineItemIdCells(), PANEL_WAIT_S),
                "LineItem ID column visible in Line Items grid after Manage columns", softAssert);
    }

    public void openManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        manageColumns.openPanel(softAssert);
    }

    private void enableColumnInManagePanel(String columnLabel, Section section, SoftAssert softAssert)
            throws InterruptedException {
        manageColumns.openPanel(softAssert);
        if (!manageColumns.waitForCheckbox(section, columnLabel, 10)) {
            Verify.softAssert1(false, columnLabel + " checkbox found in Manage columns panel", softAssert);
            return;
        }
        manageColumns.enableColumnIfNeeded(section, columnLabel, softAssert);
        manageColumns.closePanel(softAssert);
        Thread.sleep(PANEL_SETTLE_MS);
    }
}
