package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 103 | FF-MC-COL-uuid-SHOW: Enable "UUID" (lineitem). */
public class TC_103_COL_uuid_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 103;
    private static final String COLUMN_ID = "uuid";
    private static final String COLUMN_NAME = "UUID";
    private static final String SECTION = "lineitem";

    @Test(priority = 103)
    @Description("TC-103 FF-MC-COL-uuid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-103_FF-MC-COL-uuid-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
