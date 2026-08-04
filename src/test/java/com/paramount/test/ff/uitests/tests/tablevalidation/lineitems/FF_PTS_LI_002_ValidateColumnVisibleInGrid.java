package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_001_ValidateColumnAvailableInTableView.PTS_LINE_ITEMS_SETUP_GROUP;

/** FF_PTS_LI_002 — PTS Packaging ID column visible in Line Items grid (no Manage columns). */
public class FF_PTS_LI_002_ValidateColumnVisibleInGrid extends PtsPackagingLineItemsBaseTest {

    @Test(dependsOnGroups = PTS_LINE_ITEMS_SETUP_GROUP)
    @Description("FF_PTS_LI_002 — PTS Packaging ID column visible in Line Items grid (PTS demand system)")
    public void validateColumnVisibleInGrid() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.validateColumnVisibleInGrid(softAssert);
        softAssert.assertAll();
    }
}
