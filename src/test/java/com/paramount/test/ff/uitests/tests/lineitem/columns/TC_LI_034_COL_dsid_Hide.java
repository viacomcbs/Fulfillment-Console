package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-034 | FF-LI-MC-COL-dsid-HIDE: Disable "DSID" on Line Items tab. */
public class TC_LI_034_COL_dsid_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "DSID";

    @Test(priority = 34)
    @Description("TC-LI-034 FF-LI-MC-COL-dsid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-034_FF-LI-MC-COL-dsid-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
