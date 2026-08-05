package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC110 — SeasonNumber Basic. */
public class LF_LI_TC110_SeasonNumber_BasicTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.SEASON_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 10;

    @Test(priority = 1)
    @Description("TC110: SeasonNumber — Basic")
    public void tc110_seasonNumberBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc110_seasonNumberBasic", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.BASIC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
