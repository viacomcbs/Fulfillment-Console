package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 082 | FF-MC-COL-type-HIDE: Disable "Type" (lineitem). */
public class TC_082_COL_type_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 82;
    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final String SECTION = "lineitem";

    @Test(priority = 82)
    @Description("TC-082 FF-MC-COL-type-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-082_FF-MC-COL-type-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
