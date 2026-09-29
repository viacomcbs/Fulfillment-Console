package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_015_ExportData_DeliveryOffset extends BSD30034ExportBaseTest {

    @Test(priority = 15)
    @Description("BSD-30034-015: Export Excel Delivery Offset data matches UI first row")
    public void validateExportDeliveryOffsetDataMatchesUi() throws InterruptedException {
        initSoftAssert("BSD-30034-015_ExportData_DeliveryOffset");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportDeliveryOffsetData(result);
        softAssert.assertAll();
    }
}