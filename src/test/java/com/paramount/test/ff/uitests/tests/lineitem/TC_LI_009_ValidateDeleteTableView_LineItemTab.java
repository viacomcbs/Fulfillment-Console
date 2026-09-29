package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-009 | FF-LI-MC-008-B: Delete saved table view on Line Items tab. */
public class TC_LI_009_ValidateDeleteTableView_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 9)
    @Description("TC-LI-009 FF-LI-MC-008-B: Delete saved table view on Line Items tab")
    public void validateDeleteTableView() throws InterruptedException {
        initSoftAssert("TC-LI-009_FF-LI-MC-008-A");
        String viewName = "AutoLIDelete_" + System.currentTimeMillis();
        launchAndOpenManageColumnsLineItemTab();
        lineItemTabUtil.saveTableView(viewName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.deleteTableView(viewName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.assertTableViewAbsent(viewName);
        softAssert.assertAll();
    }
}
