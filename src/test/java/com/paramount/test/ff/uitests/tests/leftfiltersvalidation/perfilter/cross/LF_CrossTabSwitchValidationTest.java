package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.cross;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Cross-tab behaviour when switching between Orders and Line items with filters applied.
 */
public class LF_CrossTabSwitchValidationTest extends LeftFilterOrdersTabBaseTest {

    private static final String SAMPLE_FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    private static final String SAMPLE_OPTION = "Done: Delivered";

    @Test(priority = 1)
    @Description("Switch Orders -> Line items -> Orders preserves filter panel")
    public void validateTabSwitchPreservesFilterPanel() throws InterruptedException {
        softAssert = new SoftAssert("validateTabSwitchPreservesFilterPanel", getClass().getSimpleName());

        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanelUtil.getLeftFilterPanel().filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Filters panel visible on Orders");

        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanelUtil.getLeftFilterPanel().filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Filters panel visible on Line items");

        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanelUtil.getLeftFilterPanel().filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Filters panel visible after returning to Orders");

        softAssert.assertAll();
    }

    @Test(priority = 2)
    @Description("Active filter on Orders tab does not leak incorrect state to Line items tab")
    public void validateFilterSelectionDoesNotBreakTabSwitch() throws InterruptedException {
        softAssert = new SoftAssert("validateFilterSelectionDoesNotBreakTabSwitch", getClass().getSimpleName());

        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.expandFilter(SAMPLE_FILTER);
        leftFilterPanelUtil.selectFilterOption(SAMPLE_FILTER, SAMPLE_OPTION);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanelUtil.getLeftFilterPanel().filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Line items tab loads after Orders filter selection");

        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        Verify.softAssert(WaitUtil.isDisplay(leftFilterPanelUtil.getLeftFilterPanel().filterPanelHeader(), DEFAULT_WAIT_SECONDS),
                "Orders tab loads after Line items visit");

        softAssert.assertAll();
    }
}
