package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_001_ValidateColumnAvailableInTableView.PTS_LINE_ITEMS_SETUP_GROUP;

/** FF_PTS_LI_011 — Column sort on PTS Packaging ID (Line Items). */
public class FF_PTS_LI_011_ValidateColumnSort extends PtsPackagingLineItemsBaseTest {

    /** Run before LI_009 / LI_013 so populated PTS row is first. */
    public static final String PTS_LINE_ITEMS_SORT_GROUP = "ptsLineItemsSort";

    @Test(groups = PTS_LINE_ITEMS_SORT_GROUP, dependsOnGroups = PTS_LINE_ITEMS_SETUP_GROUP)
    @Description("FF_PTS_LI_011 — PTS Packaging ID sort asc/desc on Line Items")
    public void validateColumnSort() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(ColumnSection.LINE_ITEM, softAssert);
        ptsPackagingUtil.validateSortChangesRowOrder(softAssert);
        softAssert.assertAll();
    }
}
