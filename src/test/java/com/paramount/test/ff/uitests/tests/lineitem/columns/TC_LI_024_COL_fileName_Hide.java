package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-024 | FF-LI-MC-COL-fileName-HIDE: Disable "File name" on Line Items tab. */
public class TC_LI_024_COL_fileName_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "File name";

    @Test(priority = 24)
    @Description("TC-LI-024 FF-LI-MC-COL-fileName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-024_FF-LI-MC-COL-fileName-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
