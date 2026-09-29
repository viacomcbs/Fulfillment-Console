package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-049 | FF-LI-MC-COL-contentType-SHOW: Enable "Content type" on Line Items tab. */
public class TC_LI_049_COL_contentType_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Content type";

    @Test(priority = 49)
    @Description("TC-LI-049 FF-LI-MC-COL-contentType-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-049_FF-LI-MC-COL-contentType-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
