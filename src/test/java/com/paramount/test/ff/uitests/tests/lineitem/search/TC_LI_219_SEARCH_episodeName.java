package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-219 | FF-LI-SEARCH-COL-episodeName: Column search for "Episode name" on Line Items tab. */
public class TC_LI_219_SEARCH_episodeName extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 219)
    @Description("TC-LI-219 FF-LI-SEARCH-COL-episodeName: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-219_FF-LI-SEARCH-COL-episodeName");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
