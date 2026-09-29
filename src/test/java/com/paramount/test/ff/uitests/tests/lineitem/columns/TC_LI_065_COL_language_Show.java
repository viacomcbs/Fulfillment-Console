package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-065 | FF-LI-MC-COL-language-SHOW: Enable "Language" on Line Items tab. */
public class TC_LI_065_COL_language_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Language";

    @Test(priority = 65)
    @Description("TC-LI-065 FF-LI-MC-COL-language-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-065_FF-LI-MC-COL-language-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
