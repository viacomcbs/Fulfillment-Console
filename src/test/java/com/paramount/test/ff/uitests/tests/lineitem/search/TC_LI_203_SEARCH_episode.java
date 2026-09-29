package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-203 | FF-LI-SEARCH-COL-episode: Column search for "Episode" on Line Items tab. */
public class TC_LI_203_SEARCH_episode extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 203)
    @Description("TC-LI-203 FF-LI-SEARCH-COL-episode: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-203_FF-LI-SEARCH-COL-episode");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
