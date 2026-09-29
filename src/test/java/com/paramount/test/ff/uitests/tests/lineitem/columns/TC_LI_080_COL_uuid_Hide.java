package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-080 | FF-LI-MC-COL-uuid-HIDE: Disable "UUID" on Line Items tab. */
public class TC_LI_080_COL_uuid_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "UUID";

    @Test(priority = 80)
    @Description("TC-LI-080 FF-LI-MC-COL-uuid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-080_FF-LI-MC-COL-uuid-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
