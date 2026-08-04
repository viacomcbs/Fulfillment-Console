package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class DFOC_O_EpisodeNumber_ValidateNoDuplicateFilterOptionsOnSearch extends DuplicateFilterOptionsOrdersTabBaseTest {

    private static final String FILTER_NAME = OrdersLeftFilter.EPISODE_NUMBER.getDisplayName();

    @Test(priority = 0)
    @Description("Validate narrow search inside Episode number filter on Orders tab returns exactly one result per option (no duplicates)")
    public void validateEpisodeNumberFilterNoDuplicateOptionsOnSearch() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateNoDuplicateFilterOptionsOnSearch(softAssert, FILTER_NAME);
        softAssert.assertAll();
    }
}
