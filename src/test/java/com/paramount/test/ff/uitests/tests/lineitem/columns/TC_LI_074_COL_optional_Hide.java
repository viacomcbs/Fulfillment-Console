package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-074 | FF-LI-MC-COL-optional-HIDE: Disable "Optional" on Line Items tab. */
public class TC_LI_074_COL_optional_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Optional";

    @Test(priority = 74)
    @Description("TC-LI-074 FF-LI-MC-COL-optional-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-074_FF-LI-MC-COL-optional-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
