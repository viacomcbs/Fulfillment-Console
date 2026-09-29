package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.common.loginUtil.Verify;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_001_ValidateDeliveryDateListed extends BSD30034BaseTest {

    @Test(priority = 1)
    @Description("BSD-30034-001: Delivery Date column listed under ORDER COLUMNS in Manage Columns")
    public void validateDeliveryDateListedInManageColumns() throws InterruptedException {
        initSoftAssert("BSD-30034-001_ValidateDeliveryDateListed");
        openManageColumnsOnce();
        boolean listed = isColumnListedInManageColumns(DELIVERY_DATE_NAME);
        Verify.softAssert(listed, DELIVERY_DATE_NAME + " listed under ORDER COLUMNS");
        softAssert.assertAll();
    }
}