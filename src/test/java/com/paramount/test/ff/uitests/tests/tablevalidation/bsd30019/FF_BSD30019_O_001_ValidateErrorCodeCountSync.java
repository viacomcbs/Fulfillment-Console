package com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.ErrorMessageBsd30019Util;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailScenario;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

@LeftFilterEmailScenario(manualId = "BSD-30019-O-001", scenario = "Orders — Error code filter count matches table results")
public class FF_BSD30019_O_001_ValidateErrorCodeCountSync extends Bsd30019OrdersStoryBaseTest {

    @Test(priority = 1)
    @Description("BSD-30019 Orders: single error code filter count = table result count")
    public void validateErrorCodeCountSyncOrders() throws InterruptedException {
        softAssert = new SoftAssert("validateErrorCodeCountSyncOrders", getClass().getSimpleName());
        ErrorMessageBsd30019Util.validateErrorCodeCountSync(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS);
        softAssert.assertAll();
    }
}
