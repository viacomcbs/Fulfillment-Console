package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-092 | FF-LI-MC-COL-downstreamSystemName-HIDE: Disable "Downstream System Name" on Line Items tab. */
public class TC_LI_092_COL_downstreamSystemName_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Downstream System Name";

    @Test(priority = 92)
    @Description("TC-LI-092 FF-LI-MC-COL-downstreamSystemName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-092_FF-LI-MC-COL-downstreamSystemName-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
