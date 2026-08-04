package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil.ExportTarget;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_013;

import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_001_ValidateColumnAvailableInTableView.PTS_ORDERS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_009_ValidateColumnSearch.PTS_ORDERS_SEARCH_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_011_ValidateColumnSort.PTS_ORDERS_SORT_GROUP;

/** FF_PTS_O_013 — Excel export includes PTS Packaging ID and first row matches grid (Orders). */
public class FF_PTS_O_013_ValidateColumnInExcelExport extends PtsPackagingOrdersBaseTest {

    /** Export runs faster on PROD when O_009 column search has narrowed the grid. */
    private static final int MAX_GRID_ROWS_BEFORE_EXPORT = 10;

    @Test(dependsOnGroups = {PTS_ORDERS_SETUP_GROUP, PTS_ORDERS_SORT_GROUP, PTS_ORDERS_SEARCH_GROUP})
    @TmsLink(O_013)
    @Description("FF_PTS_O_013 — Export Orders Excel after column search; FulfillmentExport matches filtered first row")
    public void validateColumnInExcelExport() throws Exception {
        ptsPackagingUtil.requirePtsColumnOnGrid(softAssert);
        ptsPackagingUtil.prepareGridForExport(MAX_GRID_ROWS_BEFORE_EXPORT, softAssert);
        String gridPtsValue = ptsPackagingUtil.readFirstRowPtsCellValue(softAssert);
        ptsExportUtil.exportAndValidateFirstPtsIdInExcel(ExportTarget.ORDERS, gridPtsValue, softAssert);
        softAssert.assertAll();
    }
}
