package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_001_ValidateAllLeftFiltersAvailable extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate all left filters are available on Orders tab")
    public void validateAllLeftFiltersAvailable() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateAllFiltersAvailable(softAssert, OrdersLeftFilter.allDisplayNames());
        softAssert.assertAll();
    }
}