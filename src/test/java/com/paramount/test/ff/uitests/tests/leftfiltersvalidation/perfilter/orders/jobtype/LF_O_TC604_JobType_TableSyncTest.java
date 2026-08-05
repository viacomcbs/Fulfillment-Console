package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.jobtype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC604 — JobType Table sync. */
public class LF_O_TC604_JobType_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 4;

    @Test(priority = 1)
    @Description("TC604: JobType — Table sync")
    public void tc604_jobTypeTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc604_jobTypeTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
