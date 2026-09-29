package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.ErrorMessageBsd30019Util;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

@LeftFilterEmailScenario(manualId = "BSD-30019-O-002", scenario = "Orders — Error messages column shows selected error code")
public class FF_BSD30019_O_002_ValidateErrorCodeInTableColumn extends Bsd30019OrdersStoryBaseTest {

    @Test(priority = 1)
    @Description("BSD-30019 Orders: grid Error messages column contains selected error code")
    public void validateErrorCodeInTableColumnOrders() throws InterruptedException {
        softAssert = new SoftAssert("validateErrorCodeInTableColumnOrders", getClass().getSimpleName());
        ErrorMessageBsd30019Util.validateErrorCodeInTableColumn(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS);
        softAssert.assertAll();
    }
}
