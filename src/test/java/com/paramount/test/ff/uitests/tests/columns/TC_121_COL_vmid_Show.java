package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 121 | FF-MC-COL-vmid-SHOW: Enable "VMID" (lineitem). */
public class TC_121_COL_vmid_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 121;
    private static final String COLUMN_ID = "vmid";
    private static final String COLUMN_NAME = "VMID";
    private static final String SECTION = "lineitem";

    @Test(priority = 121)
    @Description("TC-121 FF-MC-COL-vmid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-121_FF-MC-COL-vmid-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
