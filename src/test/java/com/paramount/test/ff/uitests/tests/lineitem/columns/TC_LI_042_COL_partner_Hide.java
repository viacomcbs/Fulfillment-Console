package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-042 | FF-LI-MC-COL-partner-HIDE: Disable "Partner" on Line Items tab. */
public class TC_LI_042_COL_partner_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Partner";

    @Test(priority = 42)
    @Description("TC-LI-042 FF-LI-MC-COL-partner-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-042_FF-LI-MC-COL-partner-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
