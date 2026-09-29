package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.ErrorMessageBsd30019Util;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

@LeftFilterEmailScenario(manualId = "BSD-30019-O-003", scenario = "Orders — [No Value] rows have no error code prefix")
public class FF_BSD30019_O_003_ValidateNoValueHasNoErrorCode extends Bsd30019OrdersStoryBaseTest {

    @Test(priority = 1)
    @Description("BSD-30019 Orders: [No Value] filter — no error code in Error messages column")
    public void validateNoValueHasNoErrorCodeOrders() throws InterruptedException {
        softAssert = new SoftAssert("validateNoValueHasNoErrorCodeOrders", getClass().getSimpleName());
        ErrorMessageBsd30019Util.validateNoValueHasNoErrorCode(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS);
        softAssert.assertAll();
    }
}
