package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_004_ValidateOptionsAlphabeticalOrder extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate filter options are in alphabetical order on Orders tab")
    public void validateOptionsAlphabeticalOrder() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateOptionsAlphabeticalOrder(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}