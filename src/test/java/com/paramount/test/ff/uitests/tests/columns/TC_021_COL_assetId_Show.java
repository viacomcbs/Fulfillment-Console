package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 021 | FF-MC-COL-assetId-SHOW: Enable "Asset ID" (order). */
public class TC_021_COL_assetId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 21;
    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String SECTION = "order";

    @Test(priority = 21)
    @Description("TC-021 FF-MC-COL-assetId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-021_FF-MC-COL-assetId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
