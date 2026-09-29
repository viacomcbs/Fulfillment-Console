package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-003 | FF-LI-MC-003: Verify Standard View mode can be selected on Line Items tab. */
public class TC_LI_003_ValidateStandardViewModeManageColumn_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 3)
    @Description("TC-LI-003 FF-LI-MC-003: Verify Standard View mode can be selected on Line Items tab")
    public void validateStandardViewMode() throws InterruptedException {
        initSoftAssert("TC-LI-003_FF-LI-MC-003");
        launchAndOpenManageColumnsLineItemTab();
        Logger.log("Verify Standard View is selected in Manage Columns (Line item table) panel");
        Verify.softAssert(lineItemTabUtil.isStandardViewSelected(), "Standard View mode selected on Line Items tab");
        softAssert.assertAll();
    }
}
