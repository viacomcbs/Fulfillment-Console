package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.errormessage;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1017 — ErrorMessage Active filters. */
public class LF_LI_TC1017_ErrorMessage_ActiveFiltersTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ERROR_MESSAGE.getDisplayName();
    private static final int FILTER_INDEX = 17;

    @Test(priority = 1)
    @Description("TC1017: ErrorMessage — Active filters")
    public void tc1017_errorMessageActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1017_errorMessageActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
