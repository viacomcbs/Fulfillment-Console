package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.common.loginUtil.Verify;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_002_ValidateOffsetDeliveryDateListed extends BSD30034BaseTest {

    @Test(priority = 2)
    @Description("BSD-30034-002: Offset Delivery Date column listed under ORDER COLUMNS in Manage Columns")
    public void validateOffsetDeliveryDateListedInManageColumns() throws InterruptedException {
        initSoftAssert("BSD-30034-002_ValidateOffsetDeliveryDateListed");
        openManageColumnsOnce();
        boolean listed = isColumnListedInManageColumns(OFFSET_DELIVERY_DATE_NAME);
        Verify.softAssert(listed, OFFSET_DELIVERY_DATE_NAME + " listed under ORDER COLUMNS");
        softAssert.assertAll();
    }
}