package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 032 | FF-MC-COL-brand-HIDE: Disable "Brand" (order). */
public class TC_032_COL_brand_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 32;
    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String SECTION = "order";

    @Test(priority = 32)
    @Description("TC-032 FF-MC-COL-brand-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-032_FF-MC-COL-brand-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
