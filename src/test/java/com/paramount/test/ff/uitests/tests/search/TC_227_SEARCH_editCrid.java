package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 227 | FF-SEARCH-COL-editCrid: Column search for "Edit CRID" (lineitem). */
public class TC_227_SEARCH_editCrid extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 227;
    private static final String COLUMN_ID = "editCrid";
    private static final String COLUMN_NAME = "Edit CRID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 227)
    @Description("TC-227 FF-SEARCH-COL-editCrid: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-227_FF-SEARCH-COL-editCrid");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
