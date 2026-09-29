package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 229 | FF-SEARCH-COL-materialId: Column search for "Material ID" (lineitem). */
public class TC_229_SEARCH_materialId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 229;
    private static final String COLUMN_ID = "materialId";
    private static final String COLUMN_NAME = "Material ID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 229)
    @Description("TC-229 FF-SEARCH-COL-materialId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-229_FF-SEARCH-COL-materialId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
