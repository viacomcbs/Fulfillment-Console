package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 202 | FF-SEARCH-COL-currentSystem: Column search for "Current system" (order). */
public class TC_202_SEARCH_currentSystem extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 202;
    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 202)
    @Description("TC-202 FF-SEARCH-COL-currentSystem: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-202_FF-SEARCH-COL-currentSystem");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
