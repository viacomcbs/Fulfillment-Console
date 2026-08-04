package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterDataProvider;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * Data-driven per-filter left panel validation for Line items tab (~120 tests).
 */
public class LF_LI_PerFilterValidationTest extends LeftFilterLineItemsTabBaseTest {

    @Test(dataProvider = "lineItemsPerFilterMatrix", dataProviderClass = LeftFilterPerFilterDataProvider.class)
    @Description("Line items tab per-filter validation by category")
    public void validateLineItemsLeftFilter(int filterIndex, String filterName, LeftFilterTestCategory category)
            throws InterruptedException {
        softAssert = new SoftAssert(
                "validateLineItemsLeftFilter_" + filterName.replace(' ', '_') + "_" + category.name(),
                getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                filterName, category, filterIndex);
        softAssert.assertAll();
    }
}
