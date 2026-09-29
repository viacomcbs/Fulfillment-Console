package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 235 | FF-SEARCH-COL-vmid: Column search for "VMID" (lineitem). */
public class TC_235_SEARCH_vmid extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 235;
    private static final String COLUMN_ID = "vmid";
    private static final String COLUMN_NAME = "VMID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 235)
    @Description("TC-235 FF-SEARCH-COL-vmid: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-235_FF-SEARCH-COL-vmid");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
