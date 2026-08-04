package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_007_ValidateSelectAllSelectsAll extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate Select all selects all options on Orders tab")
    public void validateSelectAllSelectsAll() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSelectAllSelectsAll(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}