package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-048 | FF-LI-MC-COL-episodeName-HIDE: Disable "Episode name" on Line Items tab. */
public class TC_LI_048_COL_episodeName_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Episode name";

    @Test(priority = 48)
    @Description("TC-LI-048 FF-LI-MC-COL-episodeName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-048_FF-LI-MC-COL-episodeName-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
