package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-076 | FF-LI-MC-COL-assignedTo-HIDE: Disable "Assigned to" on Line Items tab. */
public class TC_LI_076_COL_assignedTo_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Assigned to";

    @Test(priority = 76)
    @Description("TC-LI-076 FF-LI-MC-COL-assignedTo-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-076_FF-LI-MC-COL-assignedTo-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
