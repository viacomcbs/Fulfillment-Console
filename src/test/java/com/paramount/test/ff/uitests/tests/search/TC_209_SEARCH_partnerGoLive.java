package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 209 | FF-SEARCH-COL-partnerGoLive: Column search for "Partner go live" (order). */
public class TC_209_SEARCH_partnerGoLive extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 209;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 209)
    @Description("TC-209 FF-SEARCH-COL-partnerGoLive: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-209_FF-SEARCH-COL-partnerGoLive");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
