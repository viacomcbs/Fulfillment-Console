package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 022 | FF-MC-COL-assetId-HIDE: Disable "Asset ID" (order). */
public class TC_022_COL_assetId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 22;
    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String SECTION = "order";

    @Test(priority = 22)
    @Description("TC-022 FF-MC-COL-assetId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-022_FF-MC-COL-assetId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
