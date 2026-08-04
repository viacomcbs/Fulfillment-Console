package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_O_002_ValidateExpandCollapseIcon extends LeftFilterOrdersTabBaseTest {

    @Test(priority = 0)
    @Description("Validate expand and collapse icon works on Orders tab")
    public void validateExpandCollapseIcon() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateExpandCollapseWorks(softAssert, DEFAULT_FILTER);
        softAssert.assertAll();
    }
}