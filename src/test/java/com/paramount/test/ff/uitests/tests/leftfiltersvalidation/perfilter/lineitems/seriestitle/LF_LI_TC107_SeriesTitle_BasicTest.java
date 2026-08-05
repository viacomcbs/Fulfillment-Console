package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.seriestitle;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC107 — SeriesTitle Basic. */
public class LF_LI_TC107_SeriesTitle_BasicTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.SERIES_TITLE.getDisplayName();
    private static final int FILTER_INDEX = 7;

    @Test(priority = 1)
    @Description("TC107: SeriesTitle — Basic")
    public void tc107_seriesTitleBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc107_seriesTitleBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
