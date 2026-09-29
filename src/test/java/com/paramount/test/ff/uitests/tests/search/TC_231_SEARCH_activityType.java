package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 231 | FF-SEARCH-COL-activityType: Column search for "Activity Type" (lineitem). */
public class TC_231_SEARCH_activityType extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 231;
    private static final String COLUMN_ID = "activityType";
    private static final String COLUMN_NAME = "Activity Type";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 231)
    @Description("TC-231 FF-SEARCH-COL-activityType: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-231_FF-SEARCH-COL-activityType");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
