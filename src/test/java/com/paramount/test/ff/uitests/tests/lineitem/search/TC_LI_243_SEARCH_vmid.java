package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-243 | FF-LI-SEARCH-COL-vmid: Column search for "VMID" on Line Items tab. */
public class TC_LI_243_SEARCH_vmid extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "vmid";
    private static final String COLUMN_NAME = "VMID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 243)
    @Description("TC-LI-243 FF-LI-SEARCH-COL-vmid: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-243_FF-LI-SEARCH-COL-vmid");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
