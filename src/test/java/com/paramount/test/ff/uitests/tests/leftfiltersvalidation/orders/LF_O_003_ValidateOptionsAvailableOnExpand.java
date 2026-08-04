package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_003_ValidateOptionsAvailableOnExpand extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate filter options are available when expanded on Orders tab")
    public void validateOptionsAvailableOnExpand() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateOptionsAvailableOnExpand(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}