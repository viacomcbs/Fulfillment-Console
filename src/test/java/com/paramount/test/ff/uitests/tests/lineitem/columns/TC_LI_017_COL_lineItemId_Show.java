package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-017 | FF-LI-MC-COL-lineItemId-SHOW: Enable "LineItem ID" on Line Items tab. */
public class TC_LI_017_COL_lineItemId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "LineItem ID";

    @Test(priority = 17)
    @Description("TC-LI-017 FF-LI-MC-COL-lineItemId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-017_FF-LI-MC-COL-lineItemId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
