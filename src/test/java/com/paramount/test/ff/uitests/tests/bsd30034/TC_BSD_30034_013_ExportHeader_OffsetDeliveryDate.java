package com.paramount.test.ff.uitests.tests.bsd30034;

import com.paramount.test.ff.uitests.helpers.GridExport_util.FirstRowExportResult;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

public class TC_BSD_30034_013_ExportHeader_OffsetDeliveryDate extends BSD30034ExportBaseTest {

    @Test(priority = 13)
    @Description("BSD-30034-013: Export Excel contains Offset Delivery date column header")
    public void validateExportOffsetDeliveryDateColumnHeader() throws InterruptedException {
        initSoftAssert("BSD-30034-013_ExportHeader_OffsetDeliveryDate");
        FirstRowExportResult result = prepareDoneFilterAndExportFirstRow();
        gridExportUtil.verifyExportOffsetDeliveryDateHeader(result);
        softAssert.assertAll();
    }
}