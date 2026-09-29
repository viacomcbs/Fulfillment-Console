package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 038 | FF-MC-COL-episodeName-HIDE: Disable "Episode name" (order). */
public class TC_038_COL_episodeName_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 38;
    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String SECTION = "order";

    @Test(priority = 38)
    @Description("TC-038 FF-MC-COL-episodeName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-038_FF-MC-COL-episodeName-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
