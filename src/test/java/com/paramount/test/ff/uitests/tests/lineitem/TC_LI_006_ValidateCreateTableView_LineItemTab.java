package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-006 | FF-LI-MC-007-B: Create table view with LineItem ID, Type, File name on Line Items tab. */
public class TC_LI_006_ValidateCreateTableView_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 6)
    @Description("TC-LI-006 FF-LI-MC-007-B: Create table view with LineItem ID, Type, File name on Line Items tab")
    public void validateCreateTableView() throws InterruptedException {
        initSoftAssert("TC-LI-006_FF-LI-MC-007-B");
        String viewName = "AutoLITableView_" + System.currentTimeMillis();
        launchAndOpenManageColumnsLineItemTab();
        lineItemTabUtil.enableColumn("LineItem ID");
        lineItemTabUtil.enableColumn("Type");
        lineItemTabUtil.enableColumn("File name");
        lineItemTabUtil.applyColumnChanges();
        Logger.log("Save table view with custom columns on Line Items tab: " + viewName);
        lineItemTabUtil.saveTableView(viewName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.assertTableViewExists(viewName);
        lineItemTabUtil.closeManageColumns();
        lineItemTabUtil.verifyGridHeaderVisible("LineItem ID");
        lineItemTabUtil.verifyGridHeaderVisible("Type");
        lineItemTabUtil.verifyGridHeaderVisible("File name");
        softAssert.assertAll();
    }
}
