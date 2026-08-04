package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_005_ValidateNonZeroCountFirstThenDividerThenZeroCount extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate non-zero count options first, divider, then zero count on Orders tab")
    public void validateNonZeroCountFirstThenDividerThenZeroCount() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNonZeroThenDividerThenZero(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}