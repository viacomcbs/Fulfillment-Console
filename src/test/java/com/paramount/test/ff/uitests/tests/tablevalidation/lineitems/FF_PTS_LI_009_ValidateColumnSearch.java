package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil.ColumnSection;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_001_ValidateColumnAvailableInTableView.PTS_LINE_ITEMS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.lineitems.FF_PTS_LI_011_ValidateColumnSort.PTS_LINE_ITEMS_SORT_GROUP;

/** FF_PTS_LI_009 — Column search on PTS Packaging ID (Line Items, PTS demand system). */
public class FF_PTS_LI_009_ValidateColumnSearch extends PtsPackagingLineItemsBaseTest {

    /** Run before LI_013 export — column search narrows grid to a small result set. */
    public static final String PTS_LINE_ITEMS_SEARCH_GROUP = "ptsLineItemsSearch";

    @Test(groups = PTS_LINE_ITEMS_SEARCH_GROUP,
            dependsOnGroups = {PTS_LINE_ITEMS_SETUP_GROUP, PTS_LINE_ITEMS_SORT_GROUP})
    @Description("FF_PTS_LI_009 — PTS Packaging ID column search on Line Items (after LI_011 sort)")
    public void validateColumnSearch() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(ColumnSection.LINE_ITEM, softAssert);
        String sampleValue = ptsPackagingUtil.readFirstRowPtsCellValue(softAssert);
        ptsPackagingUtil.filterColumnByValue(sampleValue, softAssert);
        ptsPackagingUtil.validateColumnSearchReturnsValue(sampleValue, softAssert);
        softAssert.assertAll();
    }
}
