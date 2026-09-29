package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-008 | FF-LI-MC-008-A: Rename saved table view on Line Items tab. */
public class TC_LI_008_ValidateRenameTableView_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 8)
    @Description("TC-LI-008 FF-LI-MC-008-A: Rename saved table view on Line Items tab")
    public void validateRenameTableView() throws InterruptedException {
        initSoftAssert("TC-LI-008_FF-LI-MC-008-A");
        String originalName = "AutoLIRename_" + System.currentTimeMillis();
        String renamedName = originalName + "_updated";
        launchAndOpenManageColumnsLineItemTab();
        lineItemTabUtil.saveTableView(originalName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.renameTableView(originalName, renamedName);
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.assertTableViewExists(renamedName);
        lineItemTabUtil.assertTableViewAbsent(originalName);
        softAssert.assertAll();
    }
}
