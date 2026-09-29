package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 218 | FF-SEARCH-COL-submission: Column search for "Submission" (lineitem). */
public class TC_218_SEARCH_submission extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 218;
    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 218)
    @Description("TC-218 FF-SEARCH-COL-submission: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-218_FF-SEARCH-COL-submission");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
