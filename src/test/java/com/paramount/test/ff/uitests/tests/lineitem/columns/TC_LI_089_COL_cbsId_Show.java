package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-089 | FF-LI-MC-COL-cbsId-SHOW: Enable "CBS ID" on Line Items tab. */
public class TC_LI_089_COL_cbsId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "CBS ID";

    @Test(priority = 89)
    @Description("TC-LI-089 FF-LI-MC-COL-cbsId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-089_FF-LI-MC-COL-cbsId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
