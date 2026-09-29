package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 092 | FF-MC-COL-language-HIDE: Disable "Language" (lineitem). */
public class TC_092_COL_language_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 92;
    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final String SECTION = "lineitem";

    @Test(priority = 92)
    @Description("TC-092 FF-MC-COL-language-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-092_FF-MC-COL-language-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
