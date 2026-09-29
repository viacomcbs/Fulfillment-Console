package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 211 | FF-SEARCH-COL-deliveryProtocol: Column search for "Delivery Protocol" (order). */
public class TC_211_SEARCH_deliveryProtocol extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 211;
    private static final String COLUMN_ID = "deliveryProtocol";
    private static final String COLUMN_NAME = "Delivery Protocol";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 211)
    @Description("TC-211 FF-SEARCH-COL-deliveryProtocol: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-211_FF-SEARCH-COL-deliveryProtocol");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
