package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 061 | FF-MC-COL-partnerEndDate-SHOW: Enable "Partner end date" (order). */
public class TC_061_COL_partnerEndDate_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 61;
    private static final String COLUMN_ID = "partnerEndDate";
    private static final String COLUMN_NAME = "Partner end date";
    private static final String SECTION = "order";

    @Test(priority = 61)
    @Description("TC-061 FF-MC-COL-partnerEndDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-061_FF-MC-COL-partnerEndDate-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
