package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-046 | FF-LI-MC-COL-submittedBy-HIDE: Disable "Submitted by" on Line Items tab. */
public class TC_LI_046_COL_submittedBy_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Submitted by";

    @Test(priority = 46)
    @Description("TC-LI-046 FF-LI-MC-COL-submittedBy-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-046_FF-LI-MC-COL-submittedBy-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
