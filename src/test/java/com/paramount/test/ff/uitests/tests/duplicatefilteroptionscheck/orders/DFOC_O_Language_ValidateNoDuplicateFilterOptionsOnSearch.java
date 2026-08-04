package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class DFOC_O_Language_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.LANGUAGE.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Language filter on Orders tab returns exactly one result per option (no duplicates)")
    public void validateLanguageFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(softAssert, FILTER_NAME);
        softAssert.assertAll();
    }
}
