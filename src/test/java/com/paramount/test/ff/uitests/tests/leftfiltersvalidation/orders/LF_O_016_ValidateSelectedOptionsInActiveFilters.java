package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_016_ValidateSelectedOptionsInActiveFilters extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate selected options appear in active filters on Orders tab")
    public void validateSelectedOptionsInActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectedOptionsInActiveFilters(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION);
        softAssert.assertAll();
    }
}