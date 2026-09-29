package com.paramount.test.ff.uitests.tests.bsd29302;

import com.paramount.test.ff.common.util.Logger;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_29302_003_ValidateColumnListed extends BSD29302BaseTest {

    @Test(priority = 1)
    @Description("BSD-29302-003: Shipping Dock Package ID listed under PACKAGE COLUMNS")
    public void validateColumnListedInManageColumns() throws InterruptedException {
        initSoftAssert("BSD-29302-003_ValidateColumnListed");
        openManageColumnsOnce();
        boolean listed = tableViewUtill.isColumnListed(COLUMN_NAME, SECTION);
        if (!listed) {
            Logger.log("Skip BSD-29302-003 - column not in UI: [" + SECTION + "] " + COLUMN_NAME);
            return;
        }
        com.paramount.test.ff.common.loginUtil.Verify.softAssert(listed,
                COLUMN_NAME + " listed under PACKAGE COLUMNS");
        tableViewUtill.closeManageColumns();
        softAssert.assertAll();
    }
}