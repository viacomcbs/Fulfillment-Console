package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.common.loginUtil.Verify;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_003_ValidateDeliveryOffsetRenamed extends BSD30034BaseTest {

    @Test(priority = 3)
    @Description("BSD-30034-003: Legacy Delivery Offset label no longer listed - renamed to Offset Delivery Date")
    public void validateLegacyDeliveryOffsetNotListed() throws InterruptedException {
        initSoftAssert("BSD-30034-003_ValidateDeliveryOffsetRenamed");
        openManageColumnsOnce();
        boolean legacyListed = tableViewUtill.isExactColumnLabelListedInUi(
                LEGACY_DELIVERY_OFFSET_NAME, SECTION, false);
        boolean renamedListed = isColumnListedInManageColumns(OFFSET_DELIVERY_DATE_NAME);
        Verify.softAssert(renamedListed, OFFSET_DELIVERY_DATE_NAME + " is listed under ORDER COLUMNS");
        Verify.softAssert(!legacyListed,
                LEGACY_DELIVERY_OFFSET_NAME + " is not listed (renamed to " + OFFSET_DELIVERY_DATE_NAME + ")");
        tableViewUtill.closeManageColumns();
        softAssert.assertAll();
    }
}