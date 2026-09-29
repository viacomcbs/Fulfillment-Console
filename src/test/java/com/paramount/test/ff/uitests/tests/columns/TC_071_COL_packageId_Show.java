package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 071 | FF-MC-COL-packageId-SHOW: Enable "Package ID" (package). */
public class TC_071_COL_packageId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 71;
    private static final String COLUMN_ID = "packageId";
    private static final String COLUMN_NAME = "Package ID";
    private static final String SECTION = "package";

    @Test(priority = 71)
    @Description("TC-071 FF-MC-COL-packageId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-071_FF-MC-COL-packageId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
