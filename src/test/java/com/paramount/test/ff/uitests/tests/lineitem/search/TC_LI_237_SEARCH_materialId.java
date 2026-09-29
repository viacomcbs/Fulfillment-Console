package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-237 | FF-LI-SEARCH-COL-materialId: Column search for "Material ID" on Line Items tab. */
public class TC_LI_237_SEARCH_materialId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "materialId";
    private static final String COLUMN_NAME = "Material ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 237)
    @Description("TC-LI-237 FF-LI-SEARCH-COL-materialId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-237_FF-LI-SEARCH-COL-materialId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
