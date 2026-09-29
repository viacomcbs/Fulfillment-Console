package com.paramount.test.ff.uitests.tests.bsd29302;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_002_DisableColumn_Hide extends BSD29302BaseTest {

    @Test(priority = 7)
    @Description("BSD-29302-002: Disable Shipping Dock Package ID column - grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("BSD-29302-002_DisableColumn_Hide");
        openManageColumnsOnce();
        if (!tableViewUtill.isColumnListed(COLUMN_NAME, SECTION)) {
            Logger.log("Skip BSD-29302-002 - column not in UI: [" + SECTION + "] " + COLUMN_NAME);
            return;
        }
        tableViewUtill.disableColumn(COLUMN_NAME, SECTION);
        Verify.softAssert(!FulfillmentJsUtil.isColumnChecked(COLUMN_NAME, SECTION),
                COLUMN_NAME + " column is disabled under PACKAGE COLUMNS");
        tableViewUtill.closeManageColumns();
        Thread.sleep(500);
        packageDetailsUtil.markColumnDisabled();
        tableViewUtill.verifyGridHeaderHidden(COLUMN_NAME);
        softAssert.assertAll();
    }
}