package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-082 | FF-LI-MC-COL-editCrid-HIDE: Disable "Edit CRID" on Line Items tab. */
public class TC_LI_082_COL_editCrid_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Edit CRID";

    @Test(priority = 82)
    @Description("TC-LI-082 FF-LI-MC-COL-editCrid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-082_FF-LI-MC-COL-editCrid-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
