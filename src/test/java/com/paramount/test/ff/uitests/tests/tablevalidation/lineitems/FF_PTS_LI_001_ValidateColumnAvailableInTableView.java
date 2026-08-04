package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * FF_PTS_LI_001 — one-time Line Items setup: Manage columns, Automation view, PTS column enabled,
 * moved to first, Save changes.
 */
public class FF_PTS_LI_001_ValidateColumnAvailableInTableView extends PtsPackagingLineItemsBaseTest {

    public static final String PTS_LINE_ITEMS_SETUP_GROUP = "ptsLineItemsSetup";

    @Test(groups = PTS_LINE_ITEMS_SETUP_GROUP)
    @Description("FF_PTS_LI_001 — Enable PTS Packaging ID on Automation view (Line Items); reorder to first and save")
    public void validateColumnAvailableInTableView() throws InterruptedException {
        ptsPackagingUtil.openManageColumnsPanel(softAssert);
        ptsPackagingUtil.validateColumnListedInManagePanel(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.configureAutomationViewWithPtsColumn(ColumnSection.LINE_ITEM, softAssert);
        softAssert.assertAll();
    }
}
