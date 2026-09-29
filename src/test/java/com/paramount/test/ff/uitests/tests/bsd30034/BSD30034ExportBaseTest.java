package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.GridExport_util;
import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Shared setup for BSD-30034 order export tests (TC024 Scriptless flow).
 * Yesterday calendar -> Done filter -> Export > Orders -> CloudFront download.
 */
public abstract class BSD30034ExportBaseTest extends BSD30034BaseTest {

    private static boolean exportGridPrepared;
    private static boolean suiteExportAttempted;
    private static FirstRowExportResult suiteExportResult;

    protected GridExport_util gridExportUtil;

    @BeforeClass(alwaysRun = true)
    @Override
    public void bsd30034SuiteLaunch() throws InterruptedException {
        prepareBsd30034ExportSuiteOnce(DELIVERY_DATE_NAME, OFFSET_DELIVERY_DATE_NAME, SECTION);
        exportGridPrepared = FulfillmentJsUtil.isOrdersGridRendered();
        FulfillmentJsUtil.logOrdersGridHeaderLabels();
        Logger.logReportMessage("BSD-30034 export suite: TC024 grid prepared (rows="
                + FulfillmentJsUtil.countOrdersGridTopRows() + ", rendered=" + exportGridPrepared + ")");
    }

    @BeforeMethod(alwaysRun = true)
    public void initExportUtil() {
        if (gridExportUtil == null) {
            gridExportUtil = new GridExport_util();
        }
    }

    protected FirstRowExportResult prepareDoneFilterAndExportFirstRow() throws InterruptedException {
        if (suiteExportAttempted) {
            Logger.logReportMessage("Reusing suite export result (single export per run)");
            return suiteExportResult;
        }
        suiteExportAttempted = true;
        suiteExportResult = gridExportUtil.prepareAndExportFirstRow(
                OFFSET_DELIVERY_DATE_ID, DELIVERY_DATE_ID, exportGridPrepared);
        return suiteExportResult;
    }
}