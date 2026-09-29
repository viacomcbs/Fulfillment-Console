package com.paramount.test.ff.uitests.tests.lineitem;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC LI-007 | FF-LI-MC-007-C: Switch between Standard View and saved table view on Line Items tab. */
public class TC_LI_007_ValidateSwitchTableView_LineItemTab extends ManageColumnsLineItemBaseTest {

    @Test(priority = 7)
    @Description("TC-LI-007 FF-LI-MC-007-C: Switch between Standard View and saved table view on Line Items tab")
    public void validateSwitchTableView() throws InterruptedException {
        initSoftAssert("TC-LI-007_FF-LI-MC-007-C");
        String viewName = "AutoLISwitch_" + System.currentTimeMillis();
        launchAndOpenManageColumnsLineItemTab();
        lineItemTabUtil.enableColumn("Title, Season, Episode");
        lineItemTabUtil.applyColumnChanges();
        lineItemTabUtil.saveTableView(viewName);
        lineItemTabUtil.selectStandardView();
        lineItemTabUtil.switchTableView(viewName);
        lineItemTabUtil.closeManageColumns();
        Logger.log("Verify Title column visible after switching view on Line Items tab");
        lineItemTabUtil.verifyGridHeaderVisible("Title");
        softAssert.assertAll();
    }
}
