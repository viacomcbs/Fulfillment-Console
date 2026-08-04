package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_010_ValidateSelectAllDisabledWhenMoreThan1000Options extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate Select all disabled when options exceed 1000 on Line items tab")
    public void validateSelectAllDisabledWhenMoreThan1000Options() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectAllDisabledWhenLargeOptionSet(softAssert, LARGE_OPTIONS_FILTER);
        softAssert.assertAll();
    }
}