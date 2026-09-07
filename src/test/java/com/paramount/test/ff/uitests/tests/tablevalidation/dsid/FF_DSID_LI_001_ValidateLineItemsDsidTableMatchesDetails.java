package com.paramount.test.ff.uitests.tests.tablevalidation.dsid;

import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.DsidTestRailCaseIds.LI_001;
import static com.paramount.test.ff.uitests.tests.tablevalidation.dsid.FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid.DSID_ORDERS_SETUP_GROUP;

/**
 * FF_DSID_LI_001 — Line Items tab: enable DSID, find row with DSID, Details panel last DSID matches table.
 */
public class FF_DSID_LI_001_ValidateLineItemsDsidTableMatchesDetails extends DsidOrdersBaseTest {

    @Test(dependsOnGroups = DSID_ORDERS_SETUP_GROUP)
    @TmsLink(LI_001)
    @Description("FF_DSID_LI_001 — Line Items: DSID in grid matches last DSID in Details panel")
    public void validateLineItemsDsidTableMatchesDetails() throws InterruptedException {
        ensureLineItemsTabWithFilters();
        enableDsidOnLineItems(softAssert);
        dsidUtil.validateAtLeastOneRowShowsDsid(ColumnSection.LINE_ITEM, softAssert);
        dsidUtil.openDetailsForStoredRow(ColumnSection.LINE_ITEM, softAssert);
        dsidUtil.validateTableDsidMatchesDetailsPanelLastDsid(ColumnSection.LINE_ITEM, softAssert);
        softAssert.assertAll();
    }
}
