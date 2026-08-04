package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class DFOC_O_Franchise_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.FRANCHISE.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Franchise filter on Orders tab returns exactly one result per option (no duplicates)")
    public void validateFranchiseFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(softAssert, FILTER_NAME);
        softAssert.assertAll();
    }
}
