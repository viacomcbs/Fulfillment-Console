package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 036 | FF-MC-COL-submittedBy-HIDE: Disable "Submitted by" (order). */
public class TC_036_COL_submittedBy_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 36;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";

    @Test(priority = 36)
    @Description("TC-036 FF-MC-COL-submittedBy-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-036_FF-MC-COL-submittedBy-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
