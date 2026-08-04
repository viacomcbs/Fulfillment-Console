package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.PARTNER_DUPLICATE_CHECK_BATCH_SIZE;

public class DFOC_O_Partner_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Partner filter on Orders tab returns exactly one result per option (no duplicates) — first 100 partners only")
    public void validatePartnerFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(
                softAssert, FILTER_NAME, 0, PARTNER_DUPLICATE_CHECK_BATCH_SIZE);
        softAssert.assertAll();
    }
}
