package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-201 | FF-LI-SEARCH-COL-titleSeasonEpisode: Column search for "Title, Season, Episode" on Line Items tab. */
public class TC_LI_201_SEARCH_titleSeasonEpisode extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 201)
    @Description("TC-LI-201 FF-LI-SEARCH-COL-titleSeasonEpisode: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-201_FF-LI-SEARCH-COL-titleSeasonEpisode");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
