package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-031 | FF-LI-MC-COL-assetId-SHOW: Enable "Asset ID" on Line Items tab. */
public class TC_LI_031_COL_assetId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Asset ID";

    @Test(priority = 31)
    @Description("TC-LI-031 FF-LI-MC-COL-assetId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-031_FF-LI-MC-COL-assetId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
