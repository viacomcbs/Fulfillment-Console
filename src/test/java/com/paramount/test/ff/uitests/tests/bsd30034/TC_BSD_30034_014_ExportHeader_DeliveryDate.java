package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_014_ExportHeader_DeliveryDate extends BSD30034ExportBaseTest {

    @Test(priority = 14)
    @Description("BSD-30034-014: Export Excel contains Delivery date column header")
    public void validateExportDeliveryDateColumnHeader() throws InterruptedException {
        initSoftAssert("BSD-30034-014_ExportHeader_DeliveryDate");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportDeliveryDateHeader(result);
        softAssert.assertAll();
    }
}