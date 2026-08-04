package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_013_ValidateTableRecordCountReflectsFilter extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate table record count reflects selected filter on Orders tab")
    public void validateTableRecordCountReflectsFilter() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateTableRecordCountReflectsFilter(softAssert, DEFAULT_FILTER, DEFAULT_FILTER_OPTION);
        softAssert.assertAll();
    }
}