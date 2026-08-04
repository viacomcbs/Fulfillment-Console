package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_009_ValidateSearchInsideFilter extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate search inside filter on Orders tab")
    public void validateSearchInsideFilter() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateSearchInsideFilter(softAssert, DEFAULT_FILTER, DEFAULT_SEARCH_TEXT);
        softAssert.assertAll();
    }
}