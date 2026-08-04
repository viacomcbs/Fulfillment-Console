package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_012_ValidatePaginationWorks extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate pagination inside filter on Orders tab")
    public void validatePaginationWorks() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validatePaginationWorks(softAssert, LARGE_OPTIONS_FILTER);
        softAssert.assertAll();
    }
}