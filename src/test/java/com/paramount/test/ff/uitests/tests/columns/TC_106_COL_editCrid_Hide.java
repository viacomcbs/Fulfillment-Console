package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 106 | FF-MC-COL-editCrid-HIDE: Disable "Edit CRID" (lineitem). */
public class TC_106_COL_editCrid_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 106;
    private static final String COLUMN_ID = "editCrid";
    private static final String COLUMN_NAME = "Edit CRID";
    private static final String SECTION = "lineitem";

    @Test(priority = 106)
    @Description("TC-106 FF-MC-COL-editCrid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-106_FF-MC-COL-editCrid-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
