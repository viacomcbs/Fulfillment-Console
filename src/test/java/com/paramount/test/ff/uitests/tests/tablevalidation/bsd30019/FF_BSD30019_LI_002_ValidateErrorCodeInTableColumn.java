package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.ErrorMessageBsd30019Util;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

@LeftFilterEmailScenario(manualId = "BSD-30019-LI-002", scenario = "Line Items — Error message column shows selected error code")
public class FF_BSD30019_LI_002_ValidateErrorCodeInTableColumn extends Bsd30019LineItemsStoryBaseTest {

    @Test(priority = 1)
    @Description("BSD-30019 Line Items: grid Error message column contains selected error code")
    public void validateErrorCodeInTableColumnLineItems() throws InterruptedException {
        softAssert = new SoftAssert("validateErrorCodeInTableColumnLineItems", getClass().getSimpleName());
        ErrorMessageBsd30019Util.validateErrorCodeInTableColumn(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS);
        softAssert.assertAll();
    }
}
