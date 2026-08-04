package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil.ExportTarget;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_017;

/**
 * FF_PTS_O_017 — Deselect PTS Packaging ID: column hidden in grid and excluded from Excel export.
 */
public class FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes extends PtsPackagingOrdersBaseTest {

    @Test
    @TmsLink(O_017)
    @Description("FF_PTS_O_017 — PTS Packaging ID not in grid or Excel when deselected in Manage columns")
    public void validateColumnHiddenWhenUnselectedAndExportExcludes() throws Exception {
        ptsPackagingUtil.ensureColumnEnabledAndSaved(softAssert);
        ptsPackagingUtil.validateColumnVisibleInGrid(softAssert);

        ptsPackagingUtil.ensureColumnDisabledAndSaved(softAssert);
        ptsPackagingUtil.validateColumnNotVisibleInGrid(softAssert);
        ptsExportUtil.exportAndValidateColumnInExcel(ExportTarget.ORDERS, false, softAssert);
        softAssert.assertAll();
    }
}
