package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class LF_LI_015_ValidateFilterNamesMatchTableColumns extends LeftFilterLineItemsTabBaseTest {

    @Test(priority = 0)
    @Description("Validate left filter names match table column names on Line items tab")
    public void validateFilterNamesMatchTableColumns() throws InterruptedException {
        softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());
        leftFilterPanelUtil.validateFilterNamesMatchTableColumns(softAssert, LineItemsLeftFilter.allDisplayNames(), LineItemsLeftFilter.allDisplayNames());
        softAssert.assertAll();
    }
}