package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 217 | FF-SEARCH-COL-fileName: Column search for "File name" (lineitem). */
public class TC_217_SEARCH_fileName extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 217;
    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 217)
    @Description("TC-217 FF-SEARCH-COL-fileName: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-217_FF-SEARCH-COL-fileName");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
