package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Episode number (collapse after search, select all last). */
public class LF_O_BatchFlow_EpisodeNumberTest extends LeftFilterEpisodeNumberOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.EPISODE_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 12;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Episode number batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_episodeNumberAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_episodeNumberAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
