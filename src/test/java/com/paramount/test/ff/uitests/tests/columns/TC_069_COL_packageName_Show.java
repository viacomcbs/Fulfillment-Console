package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 069 | FF-MC-COL-packageName-SHOW: Enable "Package name" (package). */
public class TC_069_COL_packageName_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 69;
    private static final String COLUMN_ID = "packageName";
    private static final String COLUMN_NAME = "Package name";
    private static final String SECTION = "package";

    @Test(priority = 69)
    @Description("TC-069 FF-MC-COL-packageName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-069_FF-MC-COL-packageName-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
