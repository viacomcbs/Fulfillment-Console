package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-004 | FF-LI-MC-004: Save Standard View column set as a new table view on Line Items tab. */
public class TC_LI_004_ValidateCreateNewTableView_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 4)
    @Description("TC-LI-004 FF-LI-MC-004: Create new table view from Standard View on Line Items tab")
    public void validateCreateNewTableView() throws InterruptedException {
        initSoftAssert("TC-LI-004_FF-LI-MC-004");
        String viewName = "AutoLITableView_" + System.currentTimeMillis();
        launchAndOpenManageColumnsLineItemTab();
        Logger.log("Save Standard View as new table view on Line Items tab: " + viewName);
        lineItemTabUtil.saveTableView(viewName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.assertTableViewExists(viewName);
        softAssert.assertAll();
    }
}
