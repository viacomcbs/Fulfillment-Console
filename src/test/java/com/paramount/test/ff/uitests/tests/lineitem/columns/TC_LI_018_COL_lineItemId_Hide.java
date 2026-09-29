package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-018 | FF-LI-MC-COL-lineItemId-HIDE: Disable "LineItem ID" on Line Items tab. */
public class TC_LI_018_COL_lineItemId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "LineItem ID";

    @Test(priority = 18)
    @Description("TC-LI-018 FF-LI-MC-COL-lineItemId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-018_FF-LI-MC-COL-lineItemId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
