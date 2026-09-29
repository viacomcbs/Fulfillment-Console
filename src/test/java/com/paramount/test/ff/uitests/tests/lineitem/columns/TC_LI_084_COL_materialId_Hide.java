package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-084 | FF-LI-MC-COL-materialId-HIDE: Disable "Material ID" on Line Items tab. */
public class TC_LI_084_COL_materialId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Material ID";

    @Test(priority = 84)
    @Description("TC-LI-084 FF-LI-MC-COL-materialId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-084_FF-LI-MC-COL-materialId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
