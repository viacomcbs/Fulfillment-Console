package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_013_ValidateTableRecordCountReflectsFilter extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate table record count reflects selected filter on Line items tab")
    public void validateTableRecordCountReflectsFilter() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateTableRecordCountReflectsFilter(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION);
        softAssert.assertAll();
    }
}