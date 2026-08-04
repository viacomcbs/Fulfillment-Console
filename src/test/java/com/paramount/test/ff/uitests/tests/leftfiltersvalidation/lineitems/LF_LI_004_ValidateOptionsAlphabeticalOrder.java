package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_004_ValidateOptionsAlphabeticalOrder extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate filter options are in alphabetical order on Line items tab")
    public void validateOptionsAlphabeticalOrder() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateOptionsAlphabeticalOrder(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}