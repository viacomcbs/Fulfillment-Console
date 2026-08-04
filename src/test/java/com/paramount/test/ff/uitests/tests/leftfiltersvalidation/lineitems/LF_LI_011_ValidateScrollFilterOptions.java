package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_011_ValidateScrollFilterOptions extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate scrolling filter options on Line items tab")
    public void validateScrollFilterOptions() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateScrollFilterOptions(softAssert, LARGE_OPTIONS_FILTER);
        softAssert.assertAll();
    }
}