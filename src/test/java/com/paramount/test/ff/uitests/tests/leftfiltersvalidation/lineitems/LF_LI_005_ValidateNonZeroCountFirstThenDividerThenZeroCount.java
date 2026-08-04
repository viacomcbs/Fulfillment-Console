package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_005_ValidateNonZeroCountFirstThenDividerThenZeroCount extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate non-zero count options first, divider, then zero count on Line items tab")
    public void validateNonZeroCountFirstThenDividerThenZeroCount() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNonZeroThenDividerThenZero(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}