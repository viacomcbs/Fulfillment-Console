package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-043 | FF-LI-MC-COL-brand-SHOW: Enable "Brand" on Line Items tab. */
public class TC_LI_043_COL_brand_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Brand";

    @Test(priority = 43)
    @Description("TC-LI-043 FF-LI-MC-COL-brand-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-043_FF-LI-MC-COL-brand-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
