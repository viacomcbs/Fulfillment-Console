package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-002 | FF-LI-MC-002: Verify Manage Columns panel opens on Line Items tab. */
public class TC_LI_002_ValidateOpenManageColumn_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 2)
    @Description("TC-LI-002 FF-LI-MC-002: Verify Manage Columns panel opens on Line Items tab")
    public void validateOpenManageColumn() throws InterruptedException {
        initSoftAssert("TC-LI-002_FF-LI-MC-002");
        launchFulfillmentConsoleAndOpenLineItemsTab();
        Logger.log("Open Manage Columns (Line item table) panel");
        lineItemTabUtil.openManageColumns();
        softAssert.assertAll();
    }
}
