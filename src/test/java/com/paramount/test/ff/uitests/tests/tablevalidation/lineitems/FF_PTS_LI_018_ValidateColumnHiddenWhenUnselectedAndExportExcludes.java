package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil.ExportTarget;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * FF_PTS_LI_018 — Deselect PTS Packaging ID: column hidden in grid and excluded from Excel export.
 */
public class FF_PTS_LI_018_ValidateColumnHiddenWhenUnselectedAndExportExcludes
        extends PtsPackagingLineItemsBaseTest {

    @Test
    @Description("FF_PTS_LI_018 — PTS Packaging ID not in Line Items grid or Excel when deselected")
    public void validateColumnHiddenWhenUnselectedAndExportExcludes() throws Exception {
        ptsPackagingUtil.ensureColumnEnabledAndSaved(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.validateColumnVisibleInGrid(softAssert);

        ptsPackagingUtil.ensureColumnDisabledAndSaved(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.validateColumnNotVisibleInGrid(softAssert);
        ptsExportUtil.exportAndValidateColumnInExcel(ExportTarget.LINE_ITEMS, false, softAssert);
        softAssert.assertAll();
    }
}
