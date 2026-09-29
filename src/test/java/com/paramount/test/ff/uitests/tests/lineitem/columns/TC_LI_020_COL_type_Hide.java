package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-020 | FF-LI-MC-COL-type-HIDE: Disable "Type" on Line Items tab. */
public class TC_LI_020_COL_type_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Type";

    @Test(priority = 20)
    @Description("TC-LI-020 FF-LI-MC-COL-type-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-020_FF-LI-MC-COL-type-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
