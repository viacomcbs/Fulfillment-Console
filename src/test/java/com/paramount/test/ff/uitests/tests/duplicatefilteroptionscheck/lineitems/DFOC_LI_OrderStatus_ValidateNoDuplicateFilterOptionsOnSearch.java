package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class DFOC_LI_OrderStatus_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsLineItemsTabBaseTest {

    private static final String FILTER_NAME = LineItemsLeftFilter.ORDER_STATUS.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Order Status filter on Line items tab returns exactly one result per option (no duplicates)")
    public void validateOrderStatusFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(softAssert, FILTER_NAME);
        softAssert.assertAll();
    }
}
