package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_002;

import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_001_ValidateColumnAvailableInTableView.PTS_ORDERS_SETUP_GROUP;

/** FF_PTS_O_002 — PTS Packaging ID column visible in Orders grid (no Manage columns). */
public class FF_PTS_O_002_ValidateColumnVisibleInGrid extends PtsPackagingOrdersBaseTest {

    @Test(dependsOnGroups = PTS_ORDERS_SETUP_GROUP)
    @TmsLink(O_002)
    @Description("FF_PTS_O_002 — PTS Packaging ID column visible in Orders grid (PTS demand system)")
    public void validateColumnVisibleInGrid() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(softAssert);
        ptsPackagingUtil.validateColumnVisibleInGrid(softAssert);
        softAssert.assertAll();
    }
}
