package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 094 | FF-MC-COL-lastUpdatedLineItem-HIDE: Disable "Last updated" (lineitem). */
public class TC_094_COL_lastUpdatedLineItem_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 94;
    private static final String COLUMN_ID = "lastUpdatedLineItem";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "lineitem";

    @Test(priority = 94)
    @Description("TC-094 FF-MC-COL-lastUpdatedLineItem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-094_FF-MC-COL-lastUpdatedLineItem-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
