package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 228 | FF-SEARCH-COL-contentTypeLineItem: Column search for "Content type" (lineitem). */
public class TC_228_SEARCH_contentTypeLineItem extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 228;
    private static final String COLUMN_ID = "contentTypeLineItem";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 228)
    @Description("TC-228 FF-SEARCH-COL-contentTypeLineItem: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-228_FF-SEARCH-COL-contentTypeLineItem");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
