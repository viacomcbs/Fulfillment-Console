package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_006_ValidateSelectAllOptionAtTop extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate Select all option is at the top on Line items tab")
    public void validateSelectAllOptionAtTop() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectAllAtTop(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}