package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 040 | FF-MC-COL-contentTypeOrder-HIDE: Disable "Content type" (order). */
public class TC_040_COL_contentTypeOrder_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 40;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";

    @Test(priority = 40)
    @Description("TC-040 FF-MC-COL-contentTypeOrder-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-040_FF-MC-COL-contentTypeOrder-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
