package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-058 | FF-LI-MC-COL-fastTrack-HIDE: Disable "Fast Track" on Line Items tab. */
public class TC_LI_058_COL_fastTrack_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Fast Track";

    @Test(priority = 58)
    @Description("TC-LI-058 FF-LI-MC-COL-fastTrack-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-058_FF-LI-MC-COL-fastTrack-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
