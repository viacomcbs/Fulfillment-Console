package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 077 | FF-MC-COL-endpoint-SHOW: Enable "Endpoint" (package). */
public class TC_077_COL_endpoint_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 77;
    private static final String COLUMN_ID = "endpoint";
    private static final String COLUMN_NAME = "Endpoint";
    private static final String SECTION = "package";

    @Test(priority = 77)
    @Description("TC-077 FF-MC-COL-endpoint-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-077_FF-MC-COL-endpoint-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
