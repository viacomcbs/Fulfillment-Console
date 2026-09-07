package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.jobtype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC105 — JobType Option list order. */
public class LF_LI_TC105_JobType_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.JOB_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 4;

    @Test(priority = 1)
    @Description("TC105: JobType — Option list order")
    public void tc105_jobTypeOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc105_jobTypeOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
