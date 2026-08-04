package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_001_ValidateAllLeftFiltersAvailable extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate all left filters are available on Line items tab")
    public void validateAllLeftFiltersAvailable() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateAllFiltersAvailable(softAssert, LineItemsLeftFilter.allDisplayNames());
        softAssert.assertAll();
    }
}