package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1005 — SubmittedBy Active filters. */
public class LF_LI_TC1005_SubmittedBy_ActiveFiltersTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.SUBMITTED_BY.getDisplayName();
    private static final int FILTER_INDEX = 5;

    @Test(priority = 1)
    @Description("TC1005: SubmittedBy — Active filters")
    public void tc1005_submittedByActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1005_submittedByActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
