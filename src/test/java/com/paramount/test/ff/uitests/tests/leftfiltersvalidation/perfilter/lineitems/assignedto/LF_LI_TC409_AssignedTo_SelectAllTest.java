package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.assignedto;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC409 — AssignedTo Select all. */
public class LF_LI_TC409_AssignedTo_SelectAllTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ASSIGNED_TO.getDisplayName();
    private static final int FILTER_INDEX = 9;

    @Test(priority = 1)
    @Description("TC409: AssignedTo — Select all")
    public void tc409_assignedToSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc409_assignedToSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
