package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil.ExportTarget;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_001_ValidateColumnAvailableInTableView.PTS_LINE_ITEMS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_009_ValidateColumnSearch.PTS_LINE_ITEMS_SEARCH_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_011_ValidateColumnSort.PTS_LINE_ITEMS_SORT_GROUP;

/** FF_PTS_LI_013 — Excel export includes PTS Packaging ID when column is selected (Line Items). */
public class FF_PTS_LI_013_ValidateColumnInExcelExport extends PtsPackagingLineItemsBaseTest {

    private static final int MAX_GRID_ROWS_BEFORE_EXPORT = 10;

    @Test(dependsOnGroups = {PTS_LINE_ITEMS_SETUP_GROUP, PTS_LINE_ITEMS_SORT_GROUP, PTS_LINE_ITEMS_SEARCH_GROUP})
    @Description("FF_PTS_LI_013 — Export Line Items Excel after column search; FulfillmentExport matches filtered first row")
    public void validateColumnInExcelExport() throws Exception {
        ptsPackagingUtil.requirePtsColumnOnGrid(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.prepareGridForExport(MAX_GRID_ROWS_BEFORE_EXPORT, softAssert);
        String gridPtsValue = ptsPackagingUtil.readFirstRowPtsCellValue(softAssert);
        ptsExportUtil.exportAndValidateFirstPtsIdInExcel(ExportTarget.LINE_ITEMS, gridPtsValue, softAssert);
        softAssert.assertAll();
    }
}
