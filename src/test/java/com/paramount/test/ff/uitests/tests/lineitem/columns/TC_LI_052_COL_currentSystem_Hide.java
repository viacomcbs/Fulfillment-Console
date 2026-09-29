package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-052 | FF-LI-MC-COL-currentSystem-HIDE: Disable "Current system" on Line Items tab. */
public class TC_LI_052_COL_currentSystem_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Current system";

    @Test(priority = 52)
    @Description("TC-LI-052 FF-LI-MC-COL-currentSystem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-052_FF-LI-MC-COL-currentSystem-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
