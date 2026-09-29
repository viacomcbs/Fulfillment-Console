package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-079 | FF-LI-MC-COL-uuid-SHOW: Enable "UUID" on Line Items tab. */
public class TC_LI_079_COL_uuid_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "UUID";

    @Test(priority = 79)
    @Description("TC-LI-079 FF-LI-MC-COL-uuid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-079_FF-LI-MC-COL-uuid-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
