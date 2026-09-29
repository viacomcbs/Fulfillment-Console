package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 076 | FF-MC-COL-lastUpdatedPackage-HIDE: Disable "Last updated" (package). */
public class TC_076_COL_lastUpdatedPackage_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 76;
    private static final String COLUMN_ID = "lastUpdatedPackage";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "package";

    @Test(priority = 76)
    @Description("TC-076 FF-MC-COL-lastUpdatedPackage-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-076_FF-MC-COL-lastUpdatedPackage-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
