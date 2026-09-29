package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 078 | FF-MC-COL-endpoint-HIDE: Disable "Endpoint" (package). */
public class TC_078_COL_endpoint_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 78;
    private static final String COLUMN_ID = "endpoint";
    private static final String COLUMN_NAME = "Endpoint";
    private static final String SECTION = "package";

    @Test(priority = 78)
    @Description("TC-078 FF-MC-COL-endpoint-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-078_FF-MC-COL-endpoint-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
