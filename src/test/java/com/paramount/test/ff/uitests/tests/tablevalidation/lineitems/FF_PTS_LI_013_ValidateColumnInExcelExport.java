package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil.ExportTarget;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_001_ValidateColumnAvailableInTableView.PTS_LINE_ITEMS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_011_ValidateColumnSort.PTS_LINE_ITEMS_SORT_GROUP;

/** FF_PTS_LI_013 — Excel export includes PTS Packaging ID when column is selected (Line Items). */
public class FF_PTS_LI_013_ValidateColumnInExcelExport extends PtsPackagingLineItemsBaseTest {

    @Test(dependsOnGroups = {PTS_LINE_ITEMS_SETUP_GROUP, PTS_LINE_ITEMS_SORT_GROUP})
    @Description("FF_PTS_LI_013 — PTS Packaging ID column included in Line Items Excel export (after LI_011 sort)")
    public void validateColumnInExcelExport() throws Exception {
        ptsPackagingUtil.requirePtsColumnOnGrid(ColumnSection.LINE_ITEM, softAssert);
        String gridPtsValue = ptsPackagingUtil.readFirstRowPtsCellValue(softAssert);
        ptsExportUtil.exportAndValidateFirstPtsIdInExcel(ExportTarget.LINE_ITEMS, gridPtsValue, softAssert);
        softAssert.assertAll();
    }
}
