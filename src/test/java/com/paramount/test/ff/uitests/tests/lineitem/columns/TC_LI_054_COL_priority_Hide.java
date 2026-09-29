package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-054 | FF-LI-MC-COL-priority-HIDE: Disable "Priority" on Line Items tab. */
public class TC_LI_054_COL_priority_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Priority";

    @Test(priority = 54)
    @Description("TC-LI-054 FF-LI-MC-COL-priority-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-054_FF-LI-MC-COL-priority-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
