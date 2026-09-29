package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 095 | FF-MC-COL-optional-SHOW: Enable "Optional" (lineitem). */
public class TC_095_COL_optional_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 95;
    private static final String COLUMN_ID = "optional";
    private static final String COLUMN_NAME = "Optional";
    private static final String SECTION = "lineitem";

    @Test(priority = 95)
    @Description("TC-095 FF-MC-COL-optional-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-095_FF-MC-COL-optional-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
