package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_012_ExportHeader_DeliveryOffset extends BSD30034ExportBaseTest {

    @Test(priority = 12)
    @Description("BSD-30034-012: Export Excel contains Delivery Offset column header")
    public void validateExportDeliveryOffsetColumnHeader() throws InterruptedException {
        initSoftAssert("BSD-30034-012_ExportHeader_DeliveryOffset");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportDeliveryOffsetHeader(result);
        softAssert.assertAll();
    }
}