package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 214 | FF-SEARCH-COL-ptsPackagingId: Column search for "PTS Packaging ID" (order). */
public class TC_214_SEARCH_ptsPackagingId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 214;
    private static final String COLUMN_ID = "ptsPackagingId";
    private static final String COLUMN_NAME = "PTS Packaging ID";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 214)
    @Description("TC-214 FF-SEARCH-COL-ptsPackagingId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-214_FF-SEARCH-COL-ptsPackagingId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
