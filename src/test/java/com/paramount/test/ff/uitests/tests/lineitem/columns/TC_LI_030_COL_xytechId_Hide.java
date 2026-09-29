package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-030 | FF-LI-MC-COL-xytechId-HIDE: Disable "Xytech ID" on Line Items tab. */
public class TC_LI_030_COL_xytechId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Xytech ID";

    @Test(priority = 30)
    @Description("TC-LI-030 FF-LI-MC-COL-xytechId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-030_FF-LI-MC-COL-xytechId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
