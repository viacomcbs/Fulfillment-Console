package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 196 | FF-SEARCH-COL-partnerProfile: Column search for "Partner Profile" (order). */
public class TC_196_SEARCH_partnerProfile extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 196;
    private static final String COLUMN_ID = "partnerProfile";
    private static final String COLUMN_NAME = "Partner Profile";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 196)
    @Description("TC-196 FF-SEARCH-COL-partnerProfile: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-196_FF-SEARCH-COL-partnerProfile");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
