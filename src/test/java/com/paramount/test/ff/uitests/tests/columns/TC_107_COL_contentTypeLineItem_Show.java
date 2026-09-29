package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 107 | FF-MC-COL-contentTypeLineItem-SHOW: Enable "Content type" (lineitem). */
public class TC_107_COL_contentTypeLineItem_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 107;
    private static final String COLUMN_ID = "contentTypeLineItem";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "lineitem";

    @Test(priority = 107)
    @Description("TC-107 FF-MC-COL-contentTypeLineItem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-107_FF-MC-COL-contentTypeLineItem-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
