package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 186 | FF-SEARCH-COL-season: Column search for "Season" (order). */
public class TC_186_SEARCH_season extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 186;
    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 186)
    @Description("TC-186 FF-SEARCH-COL-season: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-186_FF-SEARCH-COL-season");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
