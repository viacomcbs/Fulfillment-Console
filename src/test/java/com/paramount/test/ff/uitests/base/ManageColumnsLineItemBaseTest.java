package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.LineItemTabColumnData;
import com.paramount.test.ff.uitests.helpers.LineItemTab_util;
import com.paramount.test.ff.uitests.helpers.TableViewGraphqlClient;
import com.paramount.test.ff.uitests.helpers.TableView_utill;
import org.testng.annotations.BeforeClass;

/**
 * Base class for Manage Columns / Table View tests on the Line Items tab.
 * Reuses one browser session across TC_LI_001–TC_LI_010 for faster suite runs.
 */
public abstract class ManageColumnsLineItemBaseTest extends BaseTest {

    protected ManageColumnsLineItemBaseTest() {
        keepDriverAliveAfterTestMethod = true;
    }

    private static boolean lineItemSessionReady;
    private static boolean lineItemGridPrepared;

    protected LineItemTab_util lineItemTabUtil;
    protected TableView_utill tableViewUtill;
    protected FilterPanel_Util filterPanelUtil;
    protected Login login;

    @BeforeClass(alwaysRun = true)
    public void lineItemTabSetup() {
        ensureHelpersInitialized();
    }

    protected void ensureHelpersInitialized() {
        if (lineItemTabUtil == null) {
            lineItemTabUtil = new LineItemTab_util();
        }
        if (tableViewUtill == null) {
            tableViewUtill = new TableView_utill();
        }
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
        if (login == null) {
            login = new Login();
        }
    }

    protected void initSoftAssert(String methodName) {
        softAssert = new SoftAssert(methodName, getClass().getSimpleName());
    }

    protected void launchFulfillmentConsoleAndOpenLineItemsTab() throws InterruptedException {
        ensureHelpersInitialized();
        if (lineItemSessionReady && FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            Logger.logReportMessage("Reusing Fulfillment Console session (Line Items tab fast path)");
            login.ensureMaximizedWithPageZoom();
            lineItemTabUtil.navigateToLineItemsTab();
            return;
        }
        if (!lineItemSessionReady || !FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            Logger.logReportMessage("Launching Fulfillment Console for Line Items tab");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            FulfillmentJsUtil.installNetworkCaptureHook();
            TableViewGraphqlClient.installAuthCaptureHook();
            lineItemSessionReady = true;
        } else {
            Logger.logReportMessage("Reusing Fulfillment Console session for Line Items tab");
            login.loginToFF();
            FulfillmentJsUtil.installNetworkCaptureHook();
            TableViewGraphqlClient.installAuthCaptureHook();
        }
        lineItemTabUtil.navigateToLineItemsTab();
    }

    protected void prepareLineItemsGridForColumnTest() throws InterruptedException {
        launchFulfillmentConsoleAndOpenLineItemsTab();
        if (lineItemGridPrepared && FulfillmentJsUtil.isLineItemsGridReady()) {
            return;
        }
        FulfillmentJsUtil.useFastElementTimeout();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        lineItemTabUtil.navigateToLineItemsTab();
        lineItemTabUtil.waitForLineItemsGridReadyFast(15);
        lineItemGridPrepared = true;
    }

    protected void launchAndOpenManageColumnsLineItemTab() throws InterruptedException {
        prepareLineItemsGridForColumnTest();
        if (lineItemTabUtil.isManageColumnsPanelOpen() && lineItemTabUtil.isStandardViewSelected()) {
            Logger.logReportMessage("Manage Columns (Line item table) already open with Standard View");
            return;
        }
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.selectStandardView();
    }

    protected void runLineItemColumnShowTest(String columnName) throws InterruptedException {
        prepareLineItemsGridForColumnTest();
        lineItemTabUtil.closeManageColumnsIfOpen();
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.selectStandardView();
        lineItemTabUtil.scrollToColumn(columnName);

        if (!lineItemTabUtil.isColumnListed(columnName)) {
            Verify.softAssert(false, columnName + " is not listed in Manage Columns (Line item table)");
            lineItemTabUtil.closeManageColumns();
            return;
        }

        if (!lineItemTabUtil.isColumnCheckboxSelected(columnName)) {
            lineItemTabUtil.enableColumn(columnName);
        }
        Verify.softAssert(
                lineItemTabUtil.isColumnCheckboxSelected(columnName),
                columnName + " checkbox is selected in Manage Columns (Line item table)");

        lineItemTabUtil.closeManageColumns();
        lineItemTabUtil.closeManageColumnsIfOpen();
        lineItemTabUtil.refreshLineItemsGridAfterColumnChange();

        LineItemTabColumnData.ColumnDef column = LineItemTabColumnData.findByLabel(columnName);
        String columnId = column == null ? columnName.replaceAll("[^A-Za-z0-9]+", "") : column.id;
        boolean visible = FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId);
        Verify.softAssert(visible, columnName + " is visible on Line Items grid");
    }

    protected void runLineItemColumnHideTest(String columnName) throws InterruptedException {
        prepareLineItemsGridForColumnTest();
        lineItemTabUtil.closeManageColumnsIfOpen();
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.selectStandardView();
        lineItemTabUtil.scrollToColumn(columnName);

        if (!lineItemTabUtil.isColumnListed(columnName)) {
            Verify.softAssert(false, columnName + " is not listed in Manage Columns (Line item table)");
            lineItemTabUtil.closeManageColumns();
            return;
        }

        lineItemTabUtil.disableColumn(columnName);
        boolean columnHidden = !lineItemTabUtil.isColumnCheckboxSelected(columnName);
        lineItemTabUtil.closeManageColumns();
        lineItemTabUtil.waitForLineItemsGridReadyFast(10);
        Verify.softAssert(columnHidden, columnName + " is hidden on Line Items grid");
    }

    public static void resetLineItemTabSession() {
        lineItemSessionReady = false;
        lineItemGridPrepared = false;
        FilterPanel_Util.resetYesterdayDateFilterState();
        Login.resetSessionState();
        ManageColumnsBaseTest.resetFulfillmentSession();
    }
}
