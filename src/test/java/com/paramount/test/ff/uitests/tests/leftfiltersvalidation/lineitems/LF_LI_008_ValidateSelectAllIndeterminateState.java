package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_008_ValidateSelectAllIndeterminateState extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate Select all indeterminate state and deselect all on Line items tab")
    public void validateSelectAllIndeterminateState() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectAllIndeterminateAndDeselect(softAssert, DEFAULT_FILTER, 2);
        softAssert.assertAll();
    }
}