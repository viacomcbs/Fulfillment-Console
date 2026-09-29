package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 072 | FF-MC-COL-packageId-HIDE: Disable "Package ID" (package). */
public class TC_072_COL_packageId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 72;
    private static final String COLUMN_ID = "packageId";
    private static final String COLUMN_NAME = "Package ID";
    private static final String SECTION = "package";

    @Test(priority = 72)
    @Description("TC-072 FF-MC-COL-packageId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-072_FF-MC-COL-packageId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
