package com.paramount.test.ff.uitests.tests.tablevalidation.dsid;

import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.DsidTestRailCaseIds.O_002;
import static com.paramount.test.ff.uitests.tests.tablevalidation.dsid.FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid.DSID_ORDERS_SETUP_GROUP;

/**
 * FF_DSID_O_002 — Open Details panel for the order with DSID; last DSID in panel must match table value.
 */
public class FF_DSID_O_002_ValidateTableDsidMatchesDetailsPanelLastDsid extends DsidOrdersBaseTest {

    @Test(dependsOnGroups = DSID_ORDERS_SETUP_GROUP)
    @TmsLink(O_002)
    @Description("FF_DSID_O_002 — Orders: table DSID matches last DSID in Details panel")
    public void validateTableDsidMatchesDetailsPanelLastDsid() throws InterruptedException {
        enableDsidOnOrders(softAssert);
        dsidUtil.openDetailsForStoredRow(ColumnSection.ORDER, softAssert);
        dsidUtil.validateTableDsidMatchesDetailsPanelLastDsid(ColumnSection.ORDER, softAssert);
        softAssert.assertAll();
    }
}
