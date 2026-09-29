package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 250 | FF-SEARCH-COL-NEG-partnerGoLive: Negative column search for "Partner go live" (order). */
public class TC_250_SEARCH_NEG_partnerGoLive extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 250;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 250)
    @Description("TC-250 FF-SEARCH-COL-NEG-partnerGoLive: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-250_FF-SEARCH-COL-NEG-partnerGoLive");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
