package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.contenttype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1021 — ContentType Active filters. */
public class LF_LI_TC1021_ContentType_ActiveFiltersTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.CONTENT_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 21;

    @Test(priority = 1)
    @Description("TC1021: ContentType — Active filters")
    public void tc1021_contentType_activeFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1021_contentType_activeFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
