package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class DFOC_LI_SubmittedBy_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsLineItemsTabBaseTest {

    private static final String FILTER_NAME = LineItemsLeftFilter.SUBMITTED_BY.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Submitted by filter on Line items tab returns exactly one result per option (no duplicates)")
    public void validateSubmittedByFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(softAssert, FILTER_NAME);
        softAssert.assertAll();
    }
}
