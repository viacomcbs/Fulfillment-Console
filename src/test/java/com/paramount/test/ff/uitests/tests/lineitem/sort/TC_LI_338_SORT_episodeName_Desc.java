package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-338 | FF-LI-SORT-COL-episodeName-DESC: Sort "Episode name" descending on Line Items tab. */
public class TC_LI_338_SORT_episodeName_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 338)
    @Description("TC-LI-338 FF-LI-SORT-COL-episodeName-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-338_FF-LI-SORT-COL-episodeName-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
