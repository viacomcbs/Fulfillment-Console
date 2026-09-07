package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.jobtype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC204 — JobType Search. */
public class LF_LI_TC204_JobType_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.JOB_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 4;

    @Test(priority = 1)
    @Description("TC204: JobType — Search")
    public void tc204_jobTypeSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc204_jobTypeSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
