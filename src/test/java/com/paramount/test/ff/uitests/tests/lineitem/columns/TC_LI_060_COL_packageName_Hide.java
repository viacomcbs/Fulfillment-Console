package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-060 | FF-LI-MC-COL-packageName-HIDE: Disable "Package name" on Line Items tab. */
public class TC_LI_060_COL_packageName_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Package name";

    @Test(priority = 60)
    @Description("TC-LI-060 FF-LI-MC-COL-packageName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-060_FF-LI-MC-COL-packageName-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
