package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 110 | FF-MC-COL-materialId-HIDE: Disable "Material ID" (lineitem). */
public class TC_110_COL_materialId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 110;
    private static final String COLUMN_ID = "materialId";
    private static final String COLUMN_NAME = "Material ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 110)
    @Description("TC-110 FF-MC-COL-materialId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-110_FF-MC-COL-materialId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
