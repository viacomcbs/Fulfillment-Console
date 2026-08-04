package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_015_ValidateFilterNamesMatchTableColumns extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate left filter names match table column names on Orders tab")
    public void validateFilterNamesMatchTableColumns() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateFilterNamesMatchTableColumns(softAssert, OrdersLeftFilter.allDisplayNames(), OrdersLeftFilter.allDisplayNames());
        softAssert.assertAll();
    }
}