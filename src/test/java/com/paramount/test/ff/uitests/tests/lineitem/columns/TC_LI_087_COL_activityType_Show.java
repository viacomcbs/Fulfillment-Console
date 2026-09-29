package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-087 | FF-LI-MC-COL-activityType-SHOW: Enable "Activity Type" on Line Items tab. */
public class TC_LI_087_COL_activityType_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Activity Type";

    @Test(priority = 87)
    @Description("TC-LI-087 FF-LI-MC-COL-activityType-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-087_FF-LI-MC-COL-activityType-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
