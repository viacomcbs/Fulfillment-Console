package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-061 | FF-LI-MC-COL-packageId-SHOW: Enable "Package ID" on Line Items tab. */
public class TC_LI_061_COL_packageId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Package ID";

    @Test(priority = 61)
    @Description("TC-LI-061 FF-LI-MC-COL-packageId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-061_FF-LI-MC-COL-packageId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
