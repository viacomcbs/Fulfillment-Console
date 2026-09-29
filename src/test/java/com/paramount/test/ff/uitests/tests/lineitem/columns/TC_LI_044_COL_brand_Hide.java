package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-044 | FF-LI-MC-COL-brand-HIDE: Disable "Brand" on Line Items tab. */
public class TC_LI_044_COL_brand_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Brand";

    @Test(priority = 44)
    @Description("TC-LI-044 FF-LI-MC-COL-brand-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-044_FF-LI-MC-COL-brand-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
