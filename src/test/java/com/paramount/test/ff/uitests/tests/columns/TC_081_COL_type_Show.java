package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 081 | FF-MC-COL-type-SHOW: Enable "Type" (lineitem). */
public class TC_081_COL_type_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 81;
    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final String SECTION = "lineitem";

    @Test(priority = 81)
    @Description("TC-081 FF-MC-COL-type-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-081_FF-MC-COL-type-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
