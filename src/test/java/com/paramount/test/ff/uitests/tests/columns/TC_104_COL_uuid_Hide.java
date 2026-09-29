package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 104 | FF-MC-COL-uuid-HIDE: Disable "UUID" (lineitem). */
public class TC_104_COL_uuid_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 104;
    private static final String COLUMN_ID = "uuid";
    private static final String COLUMN_NAME = "UUID";
    private static final String SECTION = "lineitem";

    @Test(priority = 104)
    @Description("TC-104 FF-MC-COL-uuid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-104_FF-MC-COL-uuid-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
