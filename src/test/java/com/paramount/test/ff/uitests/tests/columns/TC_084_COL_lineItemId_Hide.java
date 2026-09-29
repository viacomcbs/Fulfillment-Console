package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 084 | FF-MC-COL-lineItemId-HIDE: Disable "LineItem ID" (lineitem). */
public class TC_084_COL_lineItemId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 84;
    private static final String COLUMN_ID = "lineItemId";
    private static final String COLUMN_NAME = "LineItem ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 84)
    @Description("TC-084 FF-MC-COL-lineItemId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-084_FF-MC-COL-lineItemId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
