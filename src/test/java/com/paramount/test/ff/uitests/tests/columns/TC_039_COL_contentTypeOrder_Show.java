package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 039 | FF-MC-COL-contentTypeOrder-SHOW: Enable "Content type" (order). */
public class TC_039_COL_contentTypeOrder_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 39;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";

    @Test(priority = 39)
    @Description("TC-039 FF-MC-COL-contentTypeOrder-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-039_FF-MC-COL-contentTypeOrder-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
