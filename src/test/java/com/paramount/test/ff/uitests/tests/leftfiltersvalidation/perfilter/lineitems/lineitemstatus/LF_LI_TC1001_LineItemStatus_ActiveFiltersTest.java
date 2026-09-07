package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus.LeftFilterLineItemStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1001 — LineItemStatus Active filters. */
public class LF_LI_TC1001_LineItemStatus_ActiveFiltersTest extends LeftFilterLineItemStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.LINE_ITEM_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC1001: LineItemStatus — Active filters")
    public void tc1001_lineItemStatusActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1001_lineItemStatusActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
