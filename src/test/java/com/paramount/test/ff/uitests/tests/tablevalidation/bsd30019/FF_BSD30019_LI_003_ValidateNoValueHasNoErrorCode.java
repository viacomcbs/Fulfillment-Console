package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.ErrorMessageBsd30019Util;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

@LeftFilterEmailScenario(manualId = "BSD-30019-LI-003", scenario = "Line Items — [No Value] rows have no error code prefix")
public class FF_BSD30019_LI_003_ValidateNoValueHasNoErrorCode extends Bsd30019LineItemsStoryBaseTest {

    @Test(priority = 1)
    @Description("BSD-30019 Line Items: [No Value] filter — no error code in Error message column")
    public void validateNoValueHasNoErrorCodeLineItems() throws InterruptedException {
        softAssert = new SoftAssert("validateNoValueHasNoErrorCodeLineItems", getClass().getSimpleName());
        ErrorMessageBsd30019Util.validateNoValueHasNoErrorCode(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS);
        softAssert.assertAll();
    }
}
