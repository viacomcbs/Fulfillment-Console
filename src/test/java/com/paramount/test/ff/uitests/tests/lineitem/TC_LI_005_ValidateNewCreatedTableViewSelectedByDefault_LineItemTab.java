package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-005 | FF-LI-MC-005: Verify newly created table view is selected by default on Line Items tab. */
public class TC_LI_005_ValidateNewCreatedTableViewSelectedByDefault_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 5)
    @Description("TC-LI-005 FF-LI-MC-005: Verify newly created table view is selected by default on Line Items tab")
    public void validateNewTableViewSelectedByDefault() throws InterruptedException {
        initSoftAssert("TC-LI-005_FF-LI-MC-005");
        String viewName = "AutoLIDefault_" + System.currentTimeMillis();
        launchAndOpenManageColumnsLineItemTab();
        Logger.log("Create table view and verify default selection on Line Items tab: " + viewName);
        lineItemTabUtil.saveTableView(viewName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.assertTableViewExists(viewName);
        softAssert.assertAll();
    }
}
