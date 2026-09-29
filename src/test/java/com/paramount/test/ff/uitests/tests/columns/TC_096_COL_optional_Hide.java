package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 096 | FF-MC-COL-optional-HIDE: Disable "Optional" (lineitem). */
public class TC_096_COL_optional_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 96;
    private static final String COLUMN_ID = "optional";
    private static final String COLUMN_NAME = "Optional";
    private static final String SECTION = "lineitem";

    @Test(priority = 96)
    @Description("TC-096 FF-MC-COL-optional-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-096_FF-MC-COL-optional-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
