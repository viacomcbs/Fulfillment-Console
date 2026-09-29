package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 108 | FF-MC-COL-contentTypeLineItem-HIDE: Disable "Content type" (lineitem). */
public class TC_108_COL_contentTypeLineItem_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 108;
    private static final String COLUMN_ID = "contentTypeLineItem";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "lineitem";

    @Test(priority = 108)
    @Description("TC-108 FF-MC-COL-contentTypeLineItem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-108_FF-MC-COL-contentTypeLineItem-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
