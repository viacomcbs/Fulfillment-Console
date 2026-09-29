package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.language;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC615 — Language table sync on Orders view.
 *
 * <ol>
 *   <li>Language left filter — first option with count &gt; 0 (e.g. bg-BG)</li>
 *   <li>Manage columns — enable Language line-item column if needed</li>
 *   <li>Expand order → wait for line-item grid → verify at least one Language matches filter</li>
 * </ol>
 */
public class LF_O_TC615_Language_TableSyncTest extends LeftFilterLanguageOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LANGUAGE.getDisplayName();

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("TC615: Language — filter, Manage columns Language, expanded row line item matches filter")
    public void tc615_languageTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc615_languageTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateLanguageTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
