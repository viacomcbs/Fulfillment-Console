package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-066 | FF-LI-MC-COL-language-HIDE: Disable "Language" on Line Items tab. */
public class TC_LI_066_COL_language_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Language";

    @Test(priority = 66)
    @Description("TC-LI-066 FF-LI-MC-COL-language-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-066_FF-LI-MC-COL-language-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
