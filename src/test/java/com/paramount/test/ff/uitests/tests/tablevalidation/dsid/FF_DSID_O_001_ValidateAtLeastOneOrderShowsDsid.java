package com.paramount.test.ff.uitests.tests.tablevalidation.dsid;

import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.DsidTestRailCaseIds.O_001;

/**
 * FF_DSID_O_001 — Enable DSID column on Orders tab; scan rows until at least one populated DSID is found.
 * Passes when any row (starting from first) shows DSID; blank rows are skipped.
 */
public class FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid extends DsidOrdersBaseTest {

    public static final String DSID_ORDERS_SETUP_GROUP = "dsidOrdersSetup";

    @Test(groups = DSID_ORDERS_SETUP_GROUP)
    @TmsLink(O_001)
    @Description("FF_DSID_O_001 — Orders: enable DSID column and verify at least one order shows DSID")
    public void validateAtLeastOneOrderShowsDsid() throws InterruptedException {
        enableDsidOnOrders(softAssert);
        dsidUtil.validateAtLeastOneRowShowsDsid(ColumnSection.ORDER, softAssert);
        softAssert.assertAll();
    }
}
