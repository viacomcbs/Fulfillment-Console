package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterDataProvider;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * Data-driven per-filter left panel validation for Orders tab (~120 tests).
 * Each filter runs BASIC, SEARCH, SELECT_ALL, TABLE_SYNC, SCROLL, ACTIVE_FILTERS, CLEAR_FILTERS where applicable.
 */
public class LF_O_PerFilterValidationTest extends LeftFilterOrdersTabBaseTest {

    @Test(dataProvider = "ordersPerFilterMatrix", dataProviderClass = LeftFilterPerFilterDataProvider.class)
    @Description("Orders tab per-filter validation by category")
    public void validateOrdersLeftFilter(int filterIndex, String filterName, LeftFilterTestCategory category)
            throws InterruptedException {
        softAssert = new SoftAssert(
                "validateOrdersLeftFilter_" + filterName.replace(' ', '_') + "_" + category.name(),
                getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                filterName, category, filterIndex);
        softAssert.assertAll();
    }
}
