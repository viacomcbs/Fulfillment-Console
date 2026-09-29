package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 062 | FF-MC-COL-partnerEndDate-HIDE: Disable "Partner end date" (order). */
public class TC_062_COL_partnerEndDate_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 62;
    private static final String COLUMN_ID = "partnerEndDate";
    private static final String COLUMN_NAME = "Partner end date";
    private static final String SECTION = "order";

    @Test(priority = 62)
    @Description("TC-062 FF-MC-COL-partnerEndDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-062_FF-MC-COL-partnerEndDate-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
