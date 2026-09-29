package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 075 | FF-MC-COL-lastUpdatedPackage-SHOW: Enable "Last updated" (package). */
public class TC_075_COL_lastUpdatedPackage_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 75;
    private static final String COLUMN_ID = "lastUpdatedPackage";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "package";

    @Test(priority = 75)
    @Description("TC-075 FF-MC-COL-lastUpdatedPackage-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-075_FF-MC-COL-lastUpdatedPackage-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
