package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1116 — DemandSystem Clear filters. */
public class LF_LI_TC1116_DemandSystem_ClearFiltersTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.DEMAND_SYSTEM.getDisplayName();
    private static final int FILTER_INDEX = 16;

    @Test(priority = 1)
    @Description("TC1116: DemandSystem — Clear filters")
    public void tc1116_demandSystemClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1116_demandSystemClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
