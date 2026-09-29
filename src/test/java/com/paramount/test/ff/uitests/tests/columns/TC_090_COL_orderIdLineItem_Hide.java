package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 090 | FF-MC-COL-orderIdLineItem-HIDE: Disable "Order ID" (lineitem). */
public class TC_090_COL_orderIdLineItem_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 90;
    private static final String COLUMN_ID = "orderIdLineItem";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 90)
    @Description("TC-090 FF-MC-COL-orderIdLineItem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-090_FF-MC-COL-orderIdLineItem-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
