package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-001 | FF-LI-MC-001: Verify Manage Columns icon visible on Line Items tab. */
public class TC_LI_001_ValidateManageColumnIconLineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 1)
    @Description("TC-LI-001 FF-LI-MC-001: Verify Manage Columns icon is visible on Line Items tab")
    public void validateManageColumnIcon() throws InterruptedException {
        initSoftAssert("TC-LI-001_FF-LI-MC-001");
        launchFulfillmentConsoleAndOpenLineItemsTab();
        Logger.log("Validating Manage Columns / Table View icon on Line Items tab");
        lineItemTabUtil.validateManageColumnIconLineItemTab(softAssert);
        softAssert.assertAll();
    }
}
