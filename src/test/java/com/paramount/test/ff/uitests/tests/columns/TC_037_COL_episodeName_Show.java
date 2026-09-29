package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 037 | FF-MC-COL-episodeName-SHOW: Enable "Episode name" (order). */
public class TC_037_COL_episodeName_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 37;
    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String SECTION = "order";

    @Test(priority = 37)
    @Description("TC-037 FF-MC-COL-episodeName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-037_FF-MC-COL-episodeName-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
