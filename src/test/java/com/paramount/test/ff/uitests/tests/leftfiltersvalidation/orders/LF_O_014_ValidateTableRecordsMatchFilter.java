package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_014_ValidateTableRecordsMatchFilter extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate table records match selected filter on Orders tab")
    public void validateTableRecordsMatchFilter() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateTableRecordsMatchFilter(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}