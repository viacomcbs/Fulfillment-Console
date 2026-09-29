package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 093 | FF-MC-COL-lastUpdatedLineItem-SHOW: Enable "Last updated" (lineitem). */
public class TC_093_COL_lastUpdatedLineItem_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 93;
    private static final String COLUMN_ID = "lastUpdatedLineItem";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "lineitem";

    @Test(priority = 93)
    @Description("TC-093 FF-MC-COL-lastUpdatedLineItem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-093_FF-MC-COL-lastUpdatedLineItem-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
