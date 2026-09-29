package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 083 | FF-MC-COL-lineItemId-SHOW: Enable "LineItem ID" (lineitem). */
public class TC_083_COL_lineItemId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 83;
    private static final String COLUMN_ID = "lineItemId";
    private static final String COLUMN_NAME = "LineItem ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 83)
    @Description("TC-083 FF-MC-COL-lineItemId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-083_FF-MC-COL-lineItemId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
