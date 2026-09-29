package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 031 | FF-MC-COL-brand-SHOW: Enable "Brand" (order). */
public class TC_031_COL_brand_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 31;
    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String SECTION = "order";

    @Test(priority = 31)
    @Description("TC-031 FF-MC-COL-brand-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-031_FF-MC-COL-brand-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
