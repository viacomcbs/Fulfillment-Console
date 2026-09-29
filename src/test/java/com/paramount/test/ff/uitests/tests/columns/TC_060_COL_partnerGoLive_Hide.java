package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 060 | FF-MC-COL-partnerGoLive-HIDE: Disable "Partner go live" (order). */
public class TC_060_COL_partnerGoLive_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 60;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";

    @Test(priority = 60)
    @Description("TC-060 FF-MC-COL-partnerGoLive-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-060_FF-MC-COL-partnerGoLive-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
