package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_016_ValidateSelectedOptionsInActiveFilters extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate selected options appear in active filters on Line items tab")
    public void validateSelectedOptionsInActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectedOptionsInActiveFilters(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION);
        softAssert.assertAll();
    }
}