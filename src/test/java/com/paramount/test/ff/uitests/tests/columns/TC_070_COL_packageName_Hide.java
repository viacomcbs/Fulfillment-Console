package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 070 | FF-MC-COL-packageName-HIDE: Disable "Package name" (package). */
public class TC_070_COL_packageName_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 70;
    private static final String COLUMN_ID = "packageName";
    private static final String COLUMN_NAME = "Package name";
    private static final String SECTION = "package";

    @Test(priority = 70)
    @Description("TC-070 FF-MC-COL-packageName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-070_FF-MC-COL-packageName-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
