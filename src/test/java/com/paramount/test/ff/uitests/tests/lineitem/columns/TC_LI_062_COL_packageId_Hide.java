package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-062 | FF-LI-MC-COL-packageId-HIDE: Disable "Package ID" on Line Items tab. */
public class TC_LI_062_COL_packageId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Package ID";

    @Test(priority = 62)
    @Description("TC-LI-062 FF-LI-MC-COL-packageId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-062_FF-LI-MC-COL-packageId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
