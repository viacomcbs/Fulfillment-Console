package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds.O_015;

import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_001_ValidateColumnAvailableInTableView.PTS_ORDERS_SETUP_GROUP;
import static com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_011_ValidateColumnSort.PTS_ORDERS_SORT_GROUP;

/** FF_PTS_O_015 — Orders only: table vs Details panel parity (not applicable on Line Items tab). */
public class FF_PTS_O_015_ValidateTableMatchesDetails extends PtsPackagingOrdersBaseTest {

    @Test(dependsOnGroups = {PTS_ORDERS_SETUP_GROUP, PTS_ORDERS_SORT_GROUP})
    @TmsLink(O_015)
    @Description("FF_PTS_O_015 — PTS Packaging ID in grid matches Details panel (first row after sort)")
    public void validateTableMatchesDetails() throws InterruptedException {
        ptsPackagingUtil.requirePtsColumnOnGrid(softAssert);
        ptsPackagingUtil.openDetailsForFirstGridRow(softAssert);
        ptsPackagingUtil.validateTableMatchesDetailsPanel(softAssert);
        softAssert.assertAll();
    }
}
