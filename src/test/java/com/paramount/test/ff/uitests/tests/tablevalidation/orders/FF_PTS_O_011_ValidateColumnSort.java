package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_011;

import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_001_ValidateColumnAvailableInTableView.PTS_ORDERS_SETUP_GROUP;

/** FF_PTS_O_011 — Column sort on PTS Packaging ID (Orders, PTS demand system). */
public class FF_PTS_O_011_ValidateColumnSort extends PtsPackagingOrdersBaseTest {

    /** Run before O_009 / O_015 / O_013 — sort so populated PTS Packaging ID is on first row. */
    public static final String PTS_ORDERS_SORT_GROUP = "ptsOrdersSort";

    @Test(groups = PTS_ORDERS_SORT_GROUP, dependsOnGroups = PTS_ORDERS_SETUP_GROUP)
    @TmsLink(O_011)
    @Description("FF_PTS_O_011 — PTS Packaging ID sort asc/desc; blank rows last, value on first row")
    public void validateColumnSort() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(softAssert);
        ptsPackagingUtil.validateSortChangesRowOrder(softAssert);
        softAssert.assertAll();
    }
}
