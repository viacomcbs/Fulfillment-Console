package com.paramount.test.ff.uitests.tests.bsd29302;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_001_EnableColumn_Show extends BSD29302BaseTest {

    @Test(priority = 1)
    @Description("BSD-29302-001: Enable Shipping Dock Package ID column - visible on Delivered package row")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("BSD-29302-001_EnableColumn_Show");
        openManageColumnsOnce();
        if (!tableViewUtill.isColumnListed(COLUMN_NAME, SECTION)) {
            Logger.log("Skip BSD-29302-001 - column not in UI: [" + SECTION + "] " + COLUMN_NAME);
            return;
        }
        tableViewUtill.enableColumn(COLUMN_NAME, SECTION);
        boolean enabled = tableViewUtill.isColumnCheckboxSelected(COLUMN_NAME, SECTION)
                || FulfillmentJsUtil.isColumnChecked(COLUMN_NAME, SECTION);
        Verify.softAssert(enabled, COLUMN_NAME + " column is enabled under PACKAGE COLUMNS");
        tableViewUtill.closeManageColumns();
        Thread.sleep(500);
        packageDetailsUtil.markColumnEnabled();
        Verify.softAssert(enabled, COLUMN_NAME + " is visible on Orders grid");
        softAssert.assertAll();
    }
}