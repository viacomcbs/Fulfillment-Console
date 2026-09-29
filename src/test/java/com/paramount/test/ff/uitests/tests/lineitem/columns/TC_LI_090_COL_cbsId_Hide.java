package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-090 | FF-LI-MC-COL-cbsId-HIDE: Disable "CBS ID" on Line Items tab. */
public class TC_LI_090_COL_cbsId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "CBS ID";

    @Test(priority = 90)
    @Description("TC-LI-090 FF-LI-MC-COL-cbsId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-090_FF-LI-MC-COL-cbsId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
