package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 068 | FF-MC-COL-ptsPackagingId-HIDE: Disable "PTS Packaging ID" (order). */
public class TC_068_COL_ptsPackagingId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 68;
    private static final String COLUMN_ID = "ptsPackagingId";
    private static final String COLUMN_NAME = "PTS Packaging ID";
    private static final String SECTION = "order";

    @Test(priority = 68)
    @Description("TC-068 FF-MC-COL-ptsPackagingId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-068_FF-MC-COL-ptsPackagingId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
