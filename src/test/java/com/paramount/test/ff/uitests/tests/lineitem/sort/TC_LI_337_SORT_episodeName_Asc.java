package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-337 | FF-LI-SORT-COL-episodeName-ASC: Sort "Episode name" ascending on Line Items tab. */
public class TC_LI_337_SORT_episodeName_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 337)
    @Description("TC-LI-337 FF-LI-SORT-COL-episodeName-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-337_FF-LI-SORT-COL-episodeName-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
