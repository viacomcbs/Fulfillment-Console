package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 122 | FF-MC-COL-vmid-HIDE: Disable "VMID" (lineitem). */
public class TC_122_COL_vmid_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 122;
    private static final String COLUMN_ID = "vmid";
    private static final String COLUMN_NAME = "VMID";
    private static final String SECTION = "lineitem";

    @Test(priority = 122)
    @Description("TC-122 FF-MC-COL-vmid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-122_FF-MC-COL-vmid-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
