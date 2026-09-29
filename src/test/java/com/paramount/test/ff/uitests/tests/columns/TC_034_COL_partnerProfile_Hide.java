package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 034 | FF-MC-COL-partnerProfile-HIDE: Disable "Partner Profile" (order). */
public class TC_034_COL_partnerProfile_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 34;
    private static final String COLUMN_ID = "partnerProfile";
    private static final String COLUMN_NAME = "Partner Profile";
    private static final String SECTION = "order";

    @Test(priority = 34)
    @Description("TC-034 FF-MC-COL-partnerProfile-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-034_FF-MC-COL-partnerProfile-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
