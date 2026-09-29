package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-064 | FF-LI-MC-COL-endpoint-HIDE: Disable "Endpoint" on Line Items tab. */
public class TC_LI_064_COL_endpoint_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Endpoint";

    @Test(priority = 64)
    @Description("TC-LI-064 FF-LI-MC-COL-endpoint-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-064_FF-LI-MC-COL-endpoint-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
