package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-078 | FF-LI-MC-COL-errorMessage-HIDE: Disable "Error message" on Line Items tab. */
public class TC_LI_078_COL_errorMessage_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Error message";

    @Test(priority = 78)
    @Description("TC-LI-078 FF-LI-MC-COL-errorMessage-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-078_FF-LI-MC-COL-errorMessage-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
