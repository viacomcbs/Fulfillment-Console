package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingSessionHelper;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_009;

import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_001_ValidateColumnAvailableInTableView.PTS_ORDERS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_011_ValidateColumnSort.PTS_ORDERS_SORT_GROUP;

/** FF_PTS_O_009 — Column search on PTS Packaging ID (Orders, PTS demand system). */
public class FF_PTS_O_009_ValidateColumnSearch extends PtsPackagingOrdersBaseTest {

    /** Run before O_013 export — column search narrows grid to a small result set. */
    public static final String PTS_ORDERS_SEARCH_GROUP = "ptsOrdersSearch";

    @Test(groups = PTS_ORDERS_SEARCH_GROUP, dependsOnGroups = {PTS_ORDERS_SETUP_GROUP, PTS_ORDERS_SORT_GROUP})
    @TmsLink(O_009)
    @Description("FF_PTS_O_009 — PTS Packaging ID column search on PTS orders")
    public void validateColumnSearch() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(softAssert);
        String sampleValue = ptsPackagingUtil.readFirstRowPtsCellValue(softAssert);
        ptsPackagingUtil.filterColumnByValue(sampleValue, softAssert);
        PtsPackagingSessionHelper.setLastOrdersColumnSearchTerm(sampleValue);
        ptsPackagingUtil.validateColumnSearchReturnsValue(sampleValue, softAssert);
        softAssert.assertAll();
    }
}
