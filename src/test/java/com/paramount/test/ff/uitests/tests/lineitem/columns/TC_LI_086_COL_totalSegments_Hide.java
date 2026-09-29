package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-086 | FF-LI-MC-COL-totalSegments-HIDE: Disable "Total segments" on Line Items tab. */
public class TC_LI_086_COL_totalSegments_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Total segments";

    @Test(priority = 86)
    @Description("TC-LI-086 FF-LI-MC-COL-totalSegments-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-086_FF-LI-MC-COL-totalSegments-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
