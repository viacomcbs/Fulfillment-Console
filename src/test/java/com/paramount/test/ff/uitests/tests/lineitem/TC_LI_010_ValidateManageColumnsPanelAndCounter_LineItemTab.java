package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-010 | FF-LI-MC-007-A: Verify column selection counter and all columns listed on Line Items tab. */
public class TC_LI_010_ValidateManageColumnsPanelAndCounter_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 10)
    @Description("TC-LI-010 FF-LI-MC-007-A: Verify column selection counter and all columns listed on Line Items tab")
    public void validateManageColumnsPanelAndCounter() throws InterruptedException {
        initSoftAssert("TC-LI-010_FF-LI-MC-007-A");
        launchAndOpenManageColumnsLineItemTab();
        Logger.log("Verify column selection counter and full column list on Line Items tab");
        lineItemTabUtil.verifyColumnSelectionCounterVisible();
        lineItemTabUtil.verifyAllLineItemTabColumnsListed();
        softAssert.assertAll();
    }
}
