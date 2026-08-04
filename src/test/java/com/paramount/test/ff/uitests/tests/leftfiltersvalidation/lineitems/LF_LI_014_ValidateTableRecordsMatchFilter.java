package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_014_ValidateTableRecordsMatchFilter extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate table records match selected filter on Line items tab")
    public void validateTableRecordsMatchFilter() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateTableRecordsMatchFilter(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}