package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_002_ValidateExpandCollapseIcon extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate expand and collapse icon works on Line items tab")
    public void validateExpandCollapseIcon() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateExpandCollapseWorks(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}