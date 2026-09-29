package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 109 | FF-MC-COL-materialId-SHOW: Enable "Material ID" (lineitem). */
public class TC_109_COL_materialId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 109;
    private static final String COLUMN_ID = "materialId";
    private static final String COLUMN_NAME = "Material ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 109)
    @Description("TC-109 FF-MC-COL-materialId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-109_FF-MC-COL-materialId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
