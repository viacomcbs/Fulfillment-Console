package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-032 | FF-LI-MC-COL-assetId-HIDE: Disable "Asset ID" on Line Items tab. */
public class TC_LI_032_COL_assetId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Asset ID";

    @Test(priority = 32)
    @Description("TC-LI-032 FF-LI-MC-COL-assetId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-032_FF-LI-MC-COL-assetId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
