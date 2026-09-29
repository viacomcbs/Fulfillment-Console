package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-047 | FF-LI-MC-COL-episodeName-SHOW: Enable "Episode name" on Line Items tab. */
public class TC_LI_047_COL_episodeName_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Episode name";

    @Test(priority = 47)
    @Description("TC-LI-047 FF-LI-MC-COL-episodeName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-047_FF-LI-MC-COL-episodeName-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
